package org.jetlinks.pro.datasource.web;

import com.alibaba.fastjson.JSON;
import org.jetlinks.pro.datasource.entity.ClusterDataSourceConfig;
import org.jetlinks.pro.datasource.entity.DataSourceConfigEntity;
import org.jetlinks.pro.datasource.enums.DataSourceConfigState;
import org.jetlinks.pro.test.spring.TestJetLinksController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@WebFluxTest(DataSourceConfigController.class)
class DataSourceConfigControllerTest extends TestJetLinksController {
    public static final String TYPE_ID   = "mock";
    public static final String TYPE_NAME = "Mock";
    public static final String BASE_URL  = "/datasource/config";
    public static final String ID_1      = "test001";
    public static final String ID_2      = "test002";
    public static final String ID_3      = "test003";

    @Test
    void types() {
        List<DataSourceConfigController.DataSourceTypeView> list = client.get()
            .uri(BASE_URL + "/types")
            .exchange()
            .expectBodyList(DataSourceConfigController.DataSourceTypeView.class)
            .returnResult()
            .getResponseBody();
        assertFalse(CollectionUtils.isEmpty(list));
        assertNotNull(list.stream().filter(type -> TYPE_ID.equals(type.getId())).findAny().orElse(null));
    }

    @Test
    void test() {
        add();
        delete();
        // 不能删除enabled状态的数据
        DataSourceConfigEntity entity = findById(ID_1);
        assertEquals(ID_1, entity.getId());

        edit();
        enable();
        disable();
        delete();

        DataSourceConfigEntity entity2 = findById(ID_1);
        assertNull(entity2.getId());
    }

    void add() {
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

        client.patch()
            .uri(BASE_URL)
            .bodyValue(entity)
            .exchange()
            .expectStatus()
            .is2xxSuccessful();

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

        client.patch()
            .uri(BASE_URL)
            .bodyValue(entity2)
            .exchange()
            .expectStatus()
            .is2xxSuccessful();

        DataSourceConfigEntity entity3 = new DataSourceConfigEntity();
        entity3.setTypeId(TYPE_ID);
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
        entity.setTypeId(TYPE_ID);
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

    DataSourceConfigEntity findById(String id) {
        return client.get()
            .uri(BASE_URL + "/" + id)
            .exchange()
            .expectBody(DataSourceConfigEntity.class)
            .returnResult()
            .getResponseBody();
    }
}