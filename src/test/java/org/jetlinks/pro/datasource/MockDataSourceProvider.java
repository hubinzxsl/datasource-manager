package org.jetlinks.pro.datasource;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

@Component
public class MockDataSourceProvider implements DataSourceProvider {

    public static DataSourceType type = new DataSourceType() {
        @Override
        public String getId() {
            return "mock";
        }

        @Override
        public String getName() {
            return "Mock";
        }
    };

    @Nonnull
    @Override
    public DataSourceType getType() {
        return type;
    }

    @Nonnull
    @Override
    public Mono<DataSource> createDataSource(@Nonnull DataSourceConfig properties) {
        return Mono.just(new MockDataSource(properties));
    }

    @Nonnull
    @Override
    public Mono<DataSource> reload(@Nonnull DataSource dataSource, @Nonnull DataSourceConfig properties) {
        ((MockDataSource) dataSource).setProperties(properties);
        return Mono.just(dataSource);
    }


}