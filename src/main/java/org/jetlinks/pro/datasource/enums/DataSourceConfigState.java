package org.jetlinks.pro.datasource.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.I18nEnumDict;
import org.jetlinks.pro.datasource.DataSourceConfigManager;

@Getter
@AllArgsConstructor
public enum DataSourceConfigState implements I18nEnumDict<String> {
    enabled("正常", DataSourceConfigManager.ConfigState.normal),
    disabled("禁用", DataSourceConfigManager.ConfigState.disabled);

    private final String text;
    private final DataSourceConfigManager.ConfigState configState;
    @Override
    public String getValue() {
        return name();
    }
}
