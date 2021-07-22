package org.jetlinks.pro.datasource.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.jetlinks.pro.datasource.DataSourceConfig;
import org.jetlinks.pro.datasource.DataSourceManager;
import org.jetlinks.pro.datasource.DataSourceType;
import org.jetlinks.pro.datasource.entity.DataSourceConfigEntity;
import org.jetlinks.pro.datasource.enums.DataSourceConfigState;
import org.jetlinks.pro.datasource.service.DataSourceConfigService;
import org.jetlinks.pro.tenant.annotation.TenantAssets;
import org.jetlinks.pro.tenant.crud.TenantAccessCrudController;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/datasource/config")
@Resource(id = "datasource-config", name = "数据源配置")
@TenantAssets(type = "datasource")
@Tag(name = "数据源管理")
public class DataSourceConfigController implements TenantAccessCrudController<DataSourceConfigEntity, String> {

    @Getter
    private final DataSourceConfigService service;

    private final DataSourceManager dataSourceManager;

    @PutMapping("/{id}/_enable")
    @SaveAction
    @Operation(summary = "启用")
    public Mono<Void> enable(@PathVariable String id) {
        return service.changeState(id, DataSourceConfigState.enabled);
    }

    @PutMapping("/{id}/_disable")
    @SaveAction
    @Operation(summary = "禁用")
    public Mono<Void> disable(@PathVariable String id) {
        return service.changeState(id, DataSourceConfigState.disabled);
    }

    @GetMapping("/types")
    @QueryAction
    @Operation(summary = "获取支持的类型")
    public Flux<DataSourceTypeView> getTypes() {
        return Flux
            .fromIterable(
                dataSourceManager.getSupportedType()
            )
            .map(DataSourceTypeView::of);
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataSourceTypeView {
        @Schema(description = "ID")
        private String id;
        @Schema(description = "名称")
        private String name;

        public static DataSourceTypeView of(DataSourceType dataSourceType) {
            return new DataSourceTypeView(dataSourceType.getId(), dataSourceType.getName());
        }
    }

}
