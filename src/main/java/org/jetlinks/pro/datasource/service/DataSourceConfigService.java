package org.jetlinks.pro.datasource.service;

import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.jetlinks.core.cluster.ClusterManager;
import org.jetlinks.core.event.EventBus;
import org.jetlinks.core.event.Subscription;
import org.jetlinks.pro.datasource.DataSourceConfig;
import org.jetlinks.pro.datasource.DataSourceConfigManager;
import org.jetlinks.pro.datasource.entity.DataSourceConfigEntity;
import org.jetlinks.pro.datasource.enums.DataSourceConfigState;
import org.jetlinks.pro.gateway.annotation.Subscribe;
import org.reactivestreams.Publisher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Service
public class DataSourceConfigService extends GenericReactiveCrudService<DataSourceConfigEntity, String> implements DataSourceConfigManager, CommandLineRunner {

    private final EventBus eventBus;

    private final String serverId;

    private final List<BiFunction<ConfigState, DataSourceConfig, Mono<Void>>> callbacks = new CopyOnWriteArrayList<>();

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
            .flatMap(this::fireChanged);
    }

    @Override
    public Mono<Integer> deleteById(Publisher<String> idPublisher) {

        return Flux
            .from(idPublisher)
            .collectList()
            .filter(CollectionUtils::isNotEmpty)
            .flatMap(list -> this
                .createDelete()
                .where(DataSourceConfigEntity::getState, DataSourceConfigState.disabled)
                .in(DataSourceConfigEntity::getId, list)
                .execute()
            )
            .defaultIfEmpty(0);
    }

    private Mono<Void> fireChanged(DataSourceConfigEntity entity) {
        return entity
            .getShareConfig(serverId)
            .map(conf -> Flux
                .fromIterable(callbacks)
                .map(callback -> callback.apply(entity.getState().getConfigState(), conf))
                .as(Flux::concat))
            .orElse(Flux.empty())
            .then();
    }

    private Mono<Void> doReloadDataSource(String id) {
        return Mono
            .defer(() -> reloadDataSource(id)
                //广播到集群中
                .then(eventBus.publish("/_sys/datasource-changed", id)))
            .then();
    }

    @Override
    public Disposable doOnConfigChanged(BiFunction<ConfigState, DataSourceConfig, Mono<Void>> callback) {
        callbacks.add(callback);
        return () -> callbacks.remove(callback);
    }

    @Override
    public void run(String... args) {
//        if (callbacks.size() > 0) {
//            //启动时重新加载数据源
//            createQuery()
//                .where(DataSourceConfigEntity::getState, DataSourceConfigState.enabled)
//                .fetch()
//                .doOnNext(this::fireChanged)
//                .subscribe();
//        }
    }
}
