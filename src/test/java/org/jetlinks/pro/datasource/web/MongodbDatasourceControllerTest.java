package org.jetlinks.pro.datasource.web;

import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.pro.datasource.entity.DataSourceConfigEntity;
import org.jetlinks.pro.datasource.enums.DataSourceConfigState;
import org.jetlinks.pro.datasource.mongodb.MongodbDataSource;
import org.jetlinks.pro.datasource.mongodb.MongodbProperties;
import org.jetlinks.pro.datasource.mongodb.command.CreateCollectionConfig;
import org.jetlinks.pro.datasource.service.DataSourceConfigService;
import org.jetlinks.pro.test.spring.TestJetLinksController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(MongodbDatasourceController.class)
class MongodbDatasourceControllerTest extends TestJetLinksController {

    @Autowired
    @SuppressWarnings("all")
    private DataSourceConfigService configService;

    @BeforeEach
    void setup() {
        DataSourceConfigEntity entity = new DataSourceConfigEntity();
        entity.setId("test_mongodb");
        entity.setName("test_mongodb");
        entity.setCreatorId("test");
        entity.setCreateTimeNow();
        entity.setTypeId(MongodbDataSource.type.getId());

        MongodbProperties properties = new MongodbProperties();
        properties.setUrl("mongodb://localhost:27017");
        properties.setUsername("admin");
        properties.setPassword("admin");
        properties.setDatabaseName("test");
        entity.setShareConfig(FastBeanCopier.copy(properties, new HashMap<>()));

        configService.save(entity)
                     .as(StepVerifier::create)
                     .expectNextCount(1)
                     .verifyComplete();
        configService
            .changeState(entity.getId(), DataSourceConfigState.enabled)
            .block();
    }

    @Test
    void test() {

        String collectionName = "collection_test";
        CreateCollectionConfig config = CreateCollectionConfig.of(collectionName, false, 0L, 0L);
        String name = client
            .post()
            .uri("/datasource/mongodb/test_mongodb/collection")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(config)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(name);
        assertEquals(("test." + collectionName), name);


        List<String> collections = client
            .get()
            .uri("/datasource/mongodb/test_mongodb/collections")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBodyList(String.class)
            .returnResult()
            .getResponseBody();


        assertNotNull(collections);
        assertTrue(collections.size() > 0);

        client
            .delete()
            .uri("/datasource/mongodb/test_mongodb/collection/" + collectionName)
            .exchange()
            .expectStatus()
            .is2xxSuccessful();
    }
}