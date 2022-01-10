package org.jetlinks.pro.datasource.rule;

import lombok.SneakyThrows;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers;
import org.hswebframework.ezorm.rdb.operator.dml.query.Selects;
import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.pro.configuration.CommonConfiguration;
import org.jetlinks.pro.datasource.DataSourceManager;
import org.jetlinks.pro.datasource.rdb.RDBDataSource;
import org.jetlinks.pro.datasource.rdb.RDBDataSourceProperties;
import org.jetlinks.pro.datasource.rdb.RDBDataSourceProvider;
import org.jetlinks.pro.datasource.rdb.RDBDataSourceType;
import org.jetlinks.pro.datasource.rdb.command.Column;
import org.jetlinks.pro.datasource.rdb.command.CreateOrAlterTable;
import org.jetlinks.pro.datasource.rdb.command.Table;
import org.jetlinks.reactor.ql.utils.CastUtils;
import org.jetlinks.rule.engine.api.RuleData;
import org.jetlinks.rule.engine.api.scheduler.ScheduleJob;
import org.jetlinks.rule.engine.api.task.ExecutionContext;
import org.jetlinks.rule.engine.api.task.Input;
import org.jetlinks.rule.engine.api.task.TaskExecutor;
import org.jetlinks.rule.engine.defaults.DefaultExecutionContext;
import org.jetlinks.rule.engine.defaults.scope.InMemoryGlobalScope;
import org.jetlinks.supports.event.BrokerEventBus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class RDBDatasourceTaskExecutorProviderTest {

    @Test
    @SneakyThrows
    void test() {
        CommonConfiguration.load();
        DataSourceManager manager = Mockito.mock(DataSourceManager.class);

        RDBDataSourceProperties properties = new RDBDataSourceProperties();
        properties.setUrl("r2dbc:h2:mem:///./data");
        properties.setSchema("PUBLIC");

        RDBDataSource dataSource = RDBDataSourceProvider.create("test", properties);

        Mockito.when(manager.getDataSource(RDBDataSourceType.rdb, "test"))
               .thenReturn(Mono.just(dataSource));

        dataSource.execute(new CreateOrAlterTable(
                      new Table("test_table", Arrays.asList(new Column("t", "BIGINT", false, 32, 0, 32, true, "test")))
                  ))
                  .as(StepVerifier::create)
                  .expectComplete()
                  .verify();

        RDBDatasourceTaskExecutorProvider.RDBDatasourceTaskProperties taskProperties = new RDBDatasourceTaskExecutorProvider.RDBDatasourceTaskProperties();
        taskProperties.setDatasourceId("test");
        taskProperties.setBuffer(1);
        taskProperties.setBufferTimeout(Duration.ofMillis(100));
        taskProperties.setTable("test_table");

        RDBDatasourceTaskExecutorProvider provider = new RDBDatasourceTaskExecutorProvider(manager);
        ScheduleJob job = new ScheduleJob();
        job.setExecutor("rdb-operation");
        job.setConfiguration(FastBeanCopier.copy(taskProperties, new HashMap<>()));

        ExecutionContext context = Mockito.mock(ExecutionContext.class);
        Mockito.when(context.getJob()).thenReturn(job);
        Mockito.when(context.getInput()).thenReturn(() -> Flux
            .interval(Duration.ofMillis(50))
            .map(l -> RuleData.create(Collections.singletonMap("t", l))));

        Mockito.when(context.onError(Mockito.any(Throwable.class), Mockito.any()))
               .thenReturn(Mono.empty());

        TaskExecutor taskExecutor = provider.createTask(context).block();

        assertNotNull(taskExecutor);
        taskExecutor.start();

        Thread.sleep(1000);

        dataSource.operator()
                  .dml()
                  .query("test_table")
                  .select(Selects.count1().as("total"))
                  .fetch(ResultWrappers.map())
                  .reactive()
                  .map(m -> CastUtils.castNumber(m.get("total")).longValue())
                  .as(StepVerifier::create)
                  .expectNextMatches(i -> i > 0)
                  .verifyComplete();

    }
}