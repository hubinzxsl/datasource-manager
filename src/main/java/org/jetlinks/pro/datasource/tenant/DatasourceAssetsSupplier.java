package org.jetlinks.pro.datasource.tenant;

import lombok.AllArgsConstructor;
import org.jetlinks.pro.assets.Asset;
import org.jetlinks.pro.assets.AssetSupplier;
import org.jetlinks.pro.assets.AssetType;
import org.jetlinks.pro.assets.DefaultAsset;
import org.jetlinks.pro.datasource.entity.DataSourceConfigEntity;
import org.jetlinks.pro.datasource.service.DataSourceConfigService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author bestfeng
 */
@Component
@AllArgsConstructor
public class DatasourceAssetsSupplier implements AssetSupplier {

    private final DataSourceConfigService dataSourceConfigService;

    @Override
    public List<? extends AssetType> getTypes() {
        return Collections.singletonList(DatasourceAssetType.datasource);
    }

    @Override
    public Flux<? extends Asset> getAssets(AssetType type, Collection<?> assetId) {
        return dataSourceConfigService
            .createQuery()
            .where()
            .in(DataSourceConfigEntity::getId, assetId)
            .fetch()
            .map(ps -> new DefaultAsset(ps.getId(), ps.getName(), DatasourceAssetType.datasource));
    }
}
