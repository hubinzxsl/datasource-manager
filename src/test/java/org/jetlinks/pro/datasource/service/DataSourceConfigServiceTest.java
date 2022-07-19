package org.jetlinks.pro.datasource.service;

import org.hswebframework.ezorm.core.StaticMethodReferenceColumn;
import org.hswebframework.ezorm.rdb.mapping.ReactiveQuery;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.jetlinks.core.cluster.ClusterManager;
import org.jetlinks.pro.datasource.DataSourceConfig;
import org.jetlinks.pro.datasource.entity.ClusterDataSourceConfig;
import org.jetlinks.pro.datasource.entity.DataSourceConfigEntity;
import org.jetlinks.pro.datasource.enums.DataSourceConfigState;
import org.jetlinks.supports.event.BrokerEventBus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 输入描述.
 *
 * @author zhangji
 * @version 1.0 2021/9/28
 */
public class DataSourceConfigServiceTest {
    public static final String TYPE_ID = "mock";
    public static final String ID_1    = "test001";

    @Test
    void getConfig() {
        ReactiveRepository<DataSourceConfigEntity, String> repository = Mockito.mock(ReactiveRepository.class);
        ReactiveQuery<DataSourceConfigEntity> query = Mockito.mock(ReactiveQuery.class);

        Mockito.when(repository.createQuery())
            .thenReturn(query);
        Mockito.when(query.where(Mockito.any(StaticMethodReferenceColumn.class), Mockito.any()))
            .thenReturn(query);
        Mockito.when(query.and(Mockito.any(StaticMethodReferenceColumn.class), Mockito.any()))
            .thenReturn(query);
        Mockito.when(query.fetch())
            .thenReturn(Flux.just(entity()));

        ClusterManager clusterManager = Mockito.mock(ClusterManager.class);
        Mockito.when(clusterManager.getCurrentServerId())
            .thenReturn("test");

        DataSourceConfigService service = new DataSourceConfigService(new BrokerEventBus(), clusterManager) {
            @Override
            public ReactiveRepository<DataSourceConfigEntity, String> getRepository() {
                return repository;
            }
        };

        service.getConfig(TYPE_ID, ID_1)
            .map(DataSourceConfig::getConfiguration)
            .map(map -> map.get("hostname"))
            .as(StepVerifier::create)
            .expectNext("test")
            .verifyComplete();
    }

    DataSourceConfigEntity entity() {
        DataSourceConfigEntity entity = new DataSourceConfigEntity();
        entity.setTypeId(TYPE_ID);
        entity.setId(ID_1);
        entity.setName("test");
        entity.setDescription("单元测试");
        entity.setShareCluster(false);
        entity.setState(DataSourceConfigState.enabled);
        List<ClusterDataSourceConfig> configList = new ArrayList<>();
        ClusterDataSourceConfig config = new ClusterDataSourceConfig();
        config.setServerId("test");
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("hostname", "test");
        config.setConfiguration(configMap);
        configList.add(config);
        entity.setClusterConfigs(configList);
        return entity;
    }
}
