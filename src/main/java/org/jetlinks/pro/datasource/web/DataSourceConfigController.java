package org.jetlinks.pro.datasource.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.jetlinks.pro.datasource.DataSourceConfig;
import org.jetlinks.pro.datasource.entity.DataSourceConfigEntity;
import org.jetlinks.pro.datasource.enums.DataSourceConfigState;
import org.jetlinks.pro.datasource.service.DataSourceConfigService;
import org.jetlinks.pro.tenant.annotation.TenantAssets;
import org.jetlinks.pro.tenant.crud.TenantAccessCrudController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @PutMapping("/{id}/{state}")
    @SaveAction
    public Mono<Void> changeState(@PathVariable String id,
                                  @PathVariable DataSourceConfigState state) {
        return service.changeState(id, state);
    }
}
