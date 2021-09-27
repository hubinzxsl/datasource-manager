package org.jetlinks.pro.datasource.web;

import com.alibaba.fastjson.JSON;
import org.jetlinks.pro.assets.DefaultAsset;
import org.jetlinks.pro.datasource.DataSourceConfig;
import org.jetlinks.pro.datasource.MockDataSourceProvider;
import org.jetlinks.pro.datasource.entity.ClusterDataSourceConfig;
import org.jetlinks.pro.datasource.entity.DataSourceConfigEntity;
import org.jetlinks.pro.datasource.enums.DataSourceConfigState;
import org.jetlinks.pro.datasource.service.DataSourceConfigService;
import org.jetlinks.pro.datasource.tenant.DatasourceAssetType;
import org.jetlinks.pro.datasource.tenant.DatasourceAssetsSupplier;
import org.jetlinks.pro.test.spring.TestJetLinksController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@WebFluxTest(DataSourceConfigController.class)
class DataSourceConfigControllerTest extends TestJetLinksController {
    @Autowired
    MockDataSourceProvider   mockDataSourceProvider;
    @Autowired
    DatasourceAssetsSupplier datasourceAssetsSupplier;
    @Autowired
    DataSourceConfigService dataSourceConfigService;

    public static final String BASE_URL = "/datasource/config";
    public static final String ID_1     = "test001";
    public static final String ID_2     = "test002";
    public static final String ID_3     = "test003";

    @Test
    void types() {
        List<DataSourceConfigController.DataSourceTypeView> list = client.get()
            .uri(BASE_URL + "/types")
            .exchange()
            .expectBodyList(DataSourceConfigController.DataSourceTypeView.class)
            .returnResult()
            .getResponseBody();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertNotNull(list.get(0));
        assertEquals(mockDataSourceProvider.getType().getId(), list.get(0).getId());
        assertEquals(mockDataSourceProvider.getType().getName(), list.get(0).getName());
    }

    @Test
    void test() {
        add();
        delete();
        // 不能删除enabled状态的数据
        DataSourceConfigEntity entity = findById(ID_1);
        assertEquals(ID_1, entity.getId());

        getAssets();
        getConfig();
        edit();
        enable();
        disable();
        delete();

        DataSourceConfigEntity entity2 = findById(ID_1);
        assertNull(entity2.getId());
    }

    void add() {
        DataSourceConfigEntity entity = new DataSourceConfigEntity();
        entity.setTypeId(mockDataSourceProvider.getType().getId());
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

        client.patch()
            .uri(BASE_URL)
            .bodyValue(entity)
            .exchange()
            .expectStatus()
            .is2xxSuccessful();

        DataSourceConfigEntity entity2 = new DataSourceConfigEntity();
        entity2.setTypeId(mockDataSourceProvider.getType().getId());
        entity2.setId(ID_2);
        entity2.setName("test2");
        entity2.setDescription("单元测试");
        entity2.setShareCluster(true);
        Map<String, Object> shareConfigMap = new HashMap<>();
        shareConfigMap.put("username", "admin");
        shareConfigMap.put("password", "password");
        entity2.setShareConfig(shareConfigMap);

        client.patch()
            .uri(BASE_URL)
            .bodyValue(entity2)
            .exchange()
            .expectStatus()
            .is2xxSuccessful();

        DataSourceConfigEntity entity3 = new DataSourceConfigEntity();
        entity3.setTypeId(mockDataSourceProvider.getType().getId());
        entity3.setId(ID_3);
        entity3.setName("test3");
        entity3.setDescription("单元测试");
        entity3.setShareCluster(false);
        entity3.setClusterConfigs(new ArrayList<>());

        client.patch()
            .uri(BASE_URL)
            .bodyValue(entity3)
            .exchange()
            .expectStatus()
            .is2xxSuccessful();
    }

    void edit() {
        String newName = "test-2";
        DataSourceConfigEntity entity = new DataSourceConfigEntity();
        entity.setTypeId(mockDataSourceProvider.getType().getId());
        entity.setId(ID_1);
        entity.setName(newName);

        client.patch()
            .uri(BASE_URL)
            .bodyValue(entity)
            .exchange()
            .expectStatus()
            .is2xxSuccessful();

        DataSourceConfigEntity entity2 = findById(ID_1);
        System.out.println(JSON.toJSONString(entity2));
        assertEquals(newName, entity2.getName());
    }

    void disable() {
        client.put()
            .uri(BASE_URL + "/test001/_disable")
            .exchange()
            .expectStatus()
            .is2xxSuccessful();

        DataSourceConfigEntity entity = findById(ID_1);

        assertNotNull(entity);
        assertNotNull(entity.getState());
        assertEquals(DataSourceConfigState.disabled, entity.getState());
    }

    void enable() {
        client.put()
            .uri(BASE_URL + "/test001/_enable")
            .exchange()
            .expectStatus()
            .is2xxSuccessful();

        DataSourceConfigEntity entity = findById(ID_1);

        assertNotNull(entity);
        assertNotNull(entity.getState());
        assertEquals(DataSourceConfigState.enabled, entity.getState());
    }

    void delete() {
        client.delete()
            .uri(BASE_URL + "/" + ID_1)
            .exchange()
            .expectStatus()
            .is2xxSuccessful();
    }

    void getAssets() {
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

    void getConfig() {
        dataSourceConfigService.getConfig(mockDataSourceProvider.getType().getId(), ID_1)
            .map(DataSourceConfig::getConfiguration)
            .map(map -> map.get("hostname"))
            .as(StepVerifier::create)
            .expectNext("test")
            .verifyComplete();
    }

    DataSourceConfigEntity findById(String id) {
        return client.get()
            .uri(BASE_URL + "/" + id)
            .exchange()
            .expectBody(DataSourceConfigEntity.class)
            .returnResult()
            .getResponseBody();
    }
}