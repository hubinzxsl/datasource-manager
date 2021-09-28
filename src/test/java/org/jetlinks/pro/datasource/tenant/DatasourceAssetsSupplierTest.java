package org.jetlinks.pro.datasource.tenant;

import org.hswebframework.ezorm.core.StaticMethodReferenceColumn;
import org.hswebframework.ezorm.rdb.mapping.ReactiveQuery;
import org.jetlinks.pro.assets.DefaultAsset;
import org.jetlinks.pro.datasource.entity.ClusterDataSourceConfig;
import org.jetlinks.pro.datasource.entity.DataSourceConfigEntity;
import org.jetlinks.pro.datasource.service.DataSourceConfigService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 输入描述.
 *
 * @author zhangji
 * @version 1.0 2021/9/28
 */
public class DatasourceAssetsSupplierTest {
    public static final String TYPE_ID = "mock";
    public static final String ID_1    = "test001";
    public static final String ID_2    = "test002";
    public static final String ID_3    = "test003";

    @Test
    void getAssets() {
        DataSourceConfigService service = Mockito.mock(DataSourceConfigService.class);
        ReactiveQuery<DataSourceConfigEntity> query = Mockito.mock(ReactiveQuery.class);

        Mockito.when(service.createQuery())
            .thenReturn(query);
        Mockito.when(query.where())
            .thenReturn(query);
        Mockito.when(query.in(Mockito.any(StaticMethodReferenceColumn.class), Mockito.anyCollection()))
            .thenReturn(query);
        Mockito.when(query.fetch())
            .thenReturn(Flux.fromIterable(list()));

        DatasourceAssetsSupplier datasourceAssetsSupplier = new DatasourceAssetsSupplier(service);


        datasourceAssetsSupplier.getAssets(DatasourceAssetType.datasource, Arrays.asList(ID_1, ID_2, ID_3))
            .cast(DefaultAsset.class)
            .filter(asset -> asset.getType().getName().equals(DatasourceAssetType.datasource.getName()))
            .map(DefaultAsset::getId)
            .as(StepVerifier::create)
            .expectNext(ID_1)
            .expectNext(ID_2)
            .expectNext(ID_3)
            .verifyComplete();
    }

    List<DataSourceConfigEntity> list() {
        DataSourceConfigEntity entity = new DataSourceConfigEntity();
        entity.setTypeId(TYPE_ID);
        entity.setId(ID_1);
        entity.setName("test");
        entity.setDescription("单元测试");
        entity.setShareCluster(false);
        List<ClusterDataSourceConfig> configList = new ArrayList<>();
        ClusterDataSourceConfig config = new ClusterDataSourceConfig();
        config.setServerId("test");
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("hostname", "test");
        config.setConfiguration(configMap);
        configList.add(config);
        entity.setClusterConfigs(configList);

        DataSourceConfigEntity entity2 = new DataSourceConfigEntity();
        entity2.setTypeId(TYPE_ID);
        entity2.setId(ID_2);
        entity2.setName("test2");
        entity2.setDescription("单元测试");
        entity2.setShareCluster(true);
        Map<String, Object> shareConfigMap = new HashMap<>();
        shareConfigMap.put("username", "admin");
        shareConfigMap.put("password", "password");
        entity2.setShareConfig(shareConfigMap);

        DataSourceConfigEntity entity3 = new DataSourceConfigEntity();
        entity3.setTypeId(TYPE_ID);
        entity3.setId(ID_3);
        entity3.setName("test3");
        entity3.setDescription("单元测试");
        entity3.setShareCluster(false);
        entity3.setClusterConfigs(new ArrayList<>());

        return Arrays.asList(entity, entity2, entity3);
    }
}
