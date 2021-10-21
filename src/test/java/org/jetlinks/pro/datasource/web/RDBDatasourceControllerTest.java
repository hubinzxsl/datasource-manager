package org.jetlinks.pro.datasource.web;

import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.pro.datasource.DataSourceConfig;
import org.jetlinks.pro.datasource.entity.DataSourceConfigEntity;
import org.jetlinks.pro.datasource.enums.DataSourceConfigState;
import org.jetlinks.pro.datasource.rdb.RDBDataSourceProperties;
import org.jetlinks.pro.datasource.rdb.RDBDataSourceProvider;
import org.jetlinks.pro.datasource.rdb.RDBDataSourceType;
import org.jetlinks.pro.datasource.rdb.command.Column;
import org.jetlinks.pro.datasource.rdb.command.Table;
import org.jetlinks.pro.datasource.service.DataSourceConfigService;
import org.jetlinks.pro.test.spring.TestJetLinksController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(RDBDatasourceController.class)
class RDBDatasourceControllerTest extends TestJetLinksController {

    @Autowired
    @SuppressWarnings("all")
    private DataSourceConfigService configService;

    @BeforeEach
    void setup() {
        DataSourceConfigEntity entity = new DataSourceConfigEntity();
        entity.setId("test-rdb");
        entity.setName("TestRdb");
        entity.setCreatorId("test");
        entity.setCreateTimeNow();
        entity.setTypeId(RDBDataSourceType.rdb.getId());

        RDBDataSourceProperties properties = new RDBDataSourceProperties();
        properties.setUrl("r2dbc:h2:mem:///./data");
        properties.setSchema("PUBLIC");
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
        Table table = new Table();
        table.setName("s_test");

        Column id = new Column();
        id.setName("id");
        id.setLength(64);
        id.setPrimaryKey(true);
        id.setType("VARCHAR");

        Column name = new Column();
        name.setName("name");
        name.setLength(64);
        name.setType("VARCHAR");
        table.setColumns(Arrays.asList(id, name));

        client.patch()
              .uri("/datasource/rdb/test-rdb/table")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(table)
              .exchange()
              .expectStatus()
              .is2xxSuccessful();

        List<Table> tables = client
            .get()
            .uri("/datasource/rdb/test-rdb/tables")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBodyList(Table.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(tables);
        assertFalse(tables.isEmpty());
        assertNotNull(tables.get(0).getColumns());

        client
            .post()
            .uri("/datasource/rdb/test-rdb/table/s_test/drop-column")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("[\"name\"]")
            .exchange()
            .expectStatus()
            .is2xxSuccessful();

        Table parsedTable = client
            .get()
            .uri("/datasource/rdb/test-rdb/table/s_test")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(Table.class)
            .returnResult()
            .getResponseBody();
        assertNotNull(parsedTable);
        assertNotNull(parsedTable.getColumns());
        assertEquals(1,parsedTable.getColumns().size());
    }
}