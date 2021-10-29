package org.jetlinks.pro.datasource.rule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.pro.datasource.DataSourceManager;
import org.jetlinks.pro.datasource.rdb.RDBDataSourceType;
import org.jetlinks.pro.datasource.rdb.command.Upsert;
import org.jetlinks.pro.rule.engine.editor.annotation.EditorResource;
import org.jetlinks.rule.engine.api.RuleData;
import org.jetlinks.rule.engine.api.task.ExecutionContext;
import org.jetlinks.rule.engine.api.task.TaskExecutor;
import org.jetlinks.rule.engine.api.task.TaskExecutorProvider;
import org.jetlinks.rule.engine.defaults.AbstractTaskExecutor;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Set;

@AllArgsConstructor
@Component
@EditorResource(
    id = "rdb-operation",
    name = "数据库",
    editor = "rule-engine/editor/datasource/rdb-operation.html",
    helper = "rule-engine/i18n/{language}/datasource/rdb-operation.html",
    order = 200
)
public class RDBDatasourceTaskExecutorProvider implements TaskExecutorProvider {

    private final DataSourceManager dataSourceManager;

    @Override
    public String getExecutor() {
        return "rdb-operation";
    }

    @Override
    public Mono<TaskExecutor> createTask(ExecutionContext context) {
        return Mono.just(new RDBDatasourceTaskExecutor(context));
    }

    class RDBDatasourceTaskExecutor extends AbstractTaskExecutor {
        private RDBDatasourceTaskProperties properties;

        public RDBDatasourceTaskExecutor(ExecutionContext context) {
            super(context);
            init();
        }

        @Override
        public String getName() {
            return "RDB Operation";
        }

        @Override
        protected Disposable doStart() {
            return context
                .getInput()
                .accept()
                .flatMap(RuleData::dataToMap)
                .bufferTimeout(properties.buffer, properties.bufferTimeout)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(buffer -> dataSourceManager
                    .getDataSource(RDBDataSourceType.rdb, properties.datasourceId)
                    .flatMap(dataSource -> dataSource.execute(new Upsert(properties.table, buffer, properties.ignoreUpdateColumns)))
                    .onErrorResume(err -> context.onError(err, context.newRuleData(buffer))))
                .subscribe();
        }

        protected void init() {
            this.properties = FastBeanCopier
                .copy(context.getJob().getConfiguration(),
                      RDBDatasourceTaskProperties::new);
        }

        @Override
        public void reload() {
            init();
            if (this.disposable != null) {
                this.disposable.dispose();
            }
            this.disposable = doStart();
        }
    }

    @Getter
    @Setter
    public static class RDBDatasourceTaskProperties {
        //数据源ID
        private String datasourceId;
        //表名
        private String table;
        //忽略更新的列
        private Set<String> ignoreUpdateColumns;
        //缓存数量
        private int buffer = 100;
        //缓冲超时时间
        private Duration bufferTimeout = Duration.ofSeconds(2);
    }
}
