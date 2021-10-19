package org.jetlinks.pro.datasource.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.ResourceAction;
import org.jetlinks.pro.datasource.DataSourceManager;
import org.jetlinks.pro.datasource.rdb.RDBDataSourceType;
import org.jetlinks.pro.datasource.rdb.command.CreateOrAlterTable;
import org.jetlinks.pro.datasource.rdb.command.GetTable;
import org.jetlinks.pro.datasource.rdb.command.GetTables;
import org.jetlinks.pro.datasource.rdb.command.Table;
import org.jetlinks.pro.tenant.annotation.TenantAssets;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@RestController
@Resource(id = "datasource-config", name = "数据源配置")
@TenantAssets(type = "datasource")
@Tag(name = "关系型数据库管理")
@RequestMapping("/datasource/rdb")
@AllArgsConstructor
public class RDBDatasourceController {

    private final DataSourceManager dataSourceManager;

    @GetMapping("/{datasourceId}/tables")
    @Operation(summary = "获取全部表")
    public Flux<Table> tables(@PathVariable String datasourceId,
                              @RequestParam(required = false, defaultValue = "true") boolean includeColumns) {
        return dataSourceManager
            .getDataSource(RDBDataSourceType.rdb, datasourceId)
            .flatMapMany(dataSource -> dataSource.execute(new GetTables(includeColumns)));
    }

    @GetMapping("/{datasourceId}/table/{table}")
    @Operation(summary = "获取单个表信息")
    public Mono<Table> table(@PathVariable String datasourceId,
                             @PathVariable String table) {
        return dataSourceManager
            .getDataSource(RDBDataSourceType.rdb, datasourceId)
            .flatMap(dataSource -> dataSource.execute(new GetTable(table)));
    }

    @PatchMapping("/{datasourceId}/table")
    @Operation(summary = "创建或者修改表结构")
    @ResourceAction(id = "rdb-ddl", name = "数据库DDL")
    public Mono<Void> ddl(@PathVariable String datasourceId,
                            @RequestBody Mono<Table> tableMono) {

        return Mono
            .zip(
                dataSourceManager.getDataSource(RDBDataSourceType.rdb, datasourceId),
                tableMono,
                (dataSource, table) -> dataSource.execute(new CreateOrAlterTable(table))
            )
            .flatMap(Function.identity());
    }

}
