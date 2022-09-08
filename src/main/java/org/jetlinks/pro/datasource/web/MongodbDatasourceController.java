package org.jetlinks.pro.datasource.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.ResourceAction;
import org.jetlinks.pro.assets.annotation.AssetsController;
import org.jetlinks.pro.datasource.DataSourceManager;
import org.jetlinks.pro.datasource.mongodb.MongodbDataSource;
import org.jetlinks.pro.datasource.mongodb.command.CreateCollection;
import org.jetlinks.pro.datasource.mongodb.command.CreateCollectionConfig;
import org.jetlinks.pro.datasource.mongodb.command.DelCollection;
import org.jetlinks.pro.datasource.mongodb.command.GetCollectionNames;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@RestController
@Resource(id = "datasource-config", name = "数据源配置")
@AssetsController(type = "datasource")
@Tag(name = "mongodb数据库管理")
@RequestMapping("/datasource/mongodb")
@AllArgsConstructor
public class MongodbDatasourceController {

    private final DataSourceManager dataSourceManager;

    @GetMapping("/{datasourceId}/collections")
    @Operation(summary = "获取全部集合名称")
    public Flux<String> collections(@PathVariable String datasourceId) {
        return dataSourceManager
            .getDataSourceOrError(MongodbDataSource.type, datasourceId)
            .flatMapMany(dataSource -> dataSource.execute(new GetCollectionNames()));
    }

    @PostMapping("/{datasourceId}/collection")
    @Operation(summary = "创建mongodb集合")
    @ResourceAction(id = "create-mongodb-collection", name = "创建mongodb集合")
    public Mono<String> create(@PathVariable String datasourceId,
                               @RequestBody @Parameter(description = "创建配置") Mono<CreateCollectionConfig> configs) {
        return Mono
            .zip(
                dataSourceManager.getDataSourceOrError(MongodbDataSource.type, datasourceId),
                configs,
                (dataSource, config) -> dataSource.execute(new CreateCollection(config))
            )
            .flatMap(Function.identity());
    }

    @DeleteMapping("/{datasourceId}/collection/{collectionName}")
    @Operation(summary = "删除mongodb集合")
    @ResourceAction(id = "del-mongodb-collection", name = "删除mongodb集合")
    public Mono<Void> del(@PathVariable String datasourceId,
                          @PathVariable @Parameter(description = "集合名称") String collectionName) {
        return dataSourceManager
            .getDataSourceOrError(MongodbDataSource.type, datasourceId)
            .flatMap(dataSource -> dataSource.execute(new DelCollection(collectionName)));

    }

}
