package org.jetlinks.pro.datasource.service.terms;

import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.jetlinks.pro.assets.impl.terms.AssetsCreatorTermFragmentBuilder;
import org.jetlinks.pro.datasource.tenant.DatasourceAssetType;
import org.springframework.stereotype.Component;

/**
 * 输入描述.
 *
 * @author zhangji 2023/1/4
 */
@Component
public class DatasourceAssetsCreatorTerm extends AssetsCreatorTermFragmentBuilder {

    public DatasourceAssetsCreatorTerm() {
        super(DatasourceAssetType.datasource);
    }

    @Override
    public String getTable(RDBColumnMetadata column) {
        return "data_source_config";
    }
}
