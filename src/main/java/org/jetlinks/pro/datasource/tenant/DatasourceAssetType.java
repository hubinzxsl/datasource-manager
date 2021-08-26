package org.jetlinks.pro.datasource.tenant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetlinks.pro.assets.AssetPermission;
import org.jetlinks.pro.assets.EnumAssetType;
import org.jetlinks.pro.assets.crud.CrudAssetPermission;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
public enum DatasourceAssetType implements EnumAssetType {

    datasource("数据源配置", Arrays.asList(CrudAssetPermission.values())),
    ;

    private final String name;

    private final List<AssetPermission> permissions;

    @Override
    public String getId() {
        return name();
    }

}
