package org.jetlinks.pro.datasource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

@Setter
@Getter
@AllArgsConstructor
public class MockDataSource implements DataSource {
    DataSourceConfig properties;

    @Override
    public String getId() {
        return "test";
    }

    @Override
    public DataSourceType getType() {
        return MockDataSourceProvider.type;
    }

    @Nonnull
    @Override
    public <R> R execute(@Nonnull Command<R> command) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose() {

    }
}
