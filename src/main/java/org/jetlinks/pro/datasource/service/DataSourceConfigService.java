package org.jetlinks.pro.datasource.service;

import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.jetlinks.core.cluster.ClusterManager;
import org.jetlinks.core.event.EventBus;
import org.jetlinks.core.event.Subscription;
import org.jetlinks.pro.datasource.DataSourceConfig;
import org.jetlinks.pro.datasource.DataSourceConfigManager;
import org.jetlinks.pro.datasource.entity.DataSourceConfigEntity;
import org.jetlinks.pro.datasource.enums.DataSourceConfigState;
import org.jetlinks.pro.gateway.annotation.Subscribe;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

@Service
public class DataSourceConfigService extends GenericReactiveCrudService<DataSourceConfigEntity, String> implements DataSourceConfigManager, CommandLineRunner {

    private final EventBus eventBus;

    private final String serverId;

    private final List<BiConsumer<ConfigState, DataSourceConfig>> callbacks = new CopyOnWriteArrayList<>();

    public DataSourceConfigService(EventBus eventBus, ClusterManager clusterManager) {
        this.eventBus = eventBus;
        this.serverId = clusterManager.getCurrentServerId();
    }


    public Mono<Void> changeState(String id, DataSourceConfigState state) {
        return createUpdate()
            .set(DataSourceConfigEntity::getState, state)
            .where(DataSourceConfigEntity::getId, id)
            .execute()
            .then(
                doReloadDataSource(id)
            );
    }

    @Override
    public Mono<DataSourceConfig> getConfig(String typeId, String datasourceId) {
        return createQuery()
            .where(DataSourceConfigEntity::getId, datasourceId)
            .and(DataSourceConfigEntity::getTypeId, typeId)
            .fetch()
            .filter(conf -> Objects.equals(conf.getState(), DataSourceConfigState.enabled))
            .flatMap(conf -> Mono.justOrEmpty(conf.getShareConfig(serverId)))
            .singleOrEmpty();
    }

    @Subscribe(value = "/_sys/datasource-changed", features = Subscription.Feature.broker)
    public Mono<Void> reloadDataSource(String id) {
        return findById(id)
            .doOnNext(this::fireChanged)
            .then();
    }

    private void fireChanged(DataSourceConfigEntity entity) {
        entity.getShareConfig(serverId)
              .ifPresent(conf -> {
                  for (BiConsumer<ConfigState, DataSourceConfig> callback : callbacks) {
                      callback.accept(entity.getState().getConfigState(), conf);
                  }
              });
    }

    private Mono<Void> doReloadDataSource(String id) {
        return Mono
            .defer(() -> reloadDataSource(id)
                //广播到集群中
                .then(eventBus.publish("/_sys/datasource-changed", id)))
            .then();
    }

    @Override
    public Disposable doOnConfigChanged(BiConsumer<ConfigState, DataSourceConfig> callback) {
        callbacks.add(callback);
        return () -> callbacks.remove(callback);
    }

    @Override
    public void run(String... args) {
        if (callbacks.size() > 0) {
            //启动时重新加载数据源
            createQuery()
                .where(DataSourceConfigEntity::getState, DataSourceConfigState.enabled)
                .fetch()
                .doOnNext(this::fireChanged)
                .subscribe();
        }
    }
}
