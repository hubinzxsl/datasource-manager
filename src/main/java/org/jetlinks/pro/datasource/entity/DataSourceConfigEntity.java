package org.jetlinks.pro.datasource.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.ezorm.rdb.mapping.annotation.EnumCodec;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.crud.annotation.EnableEntityEvent;
import org.hswebframework.web.crud.generator.Generators;
import org.hswebframework.web.validator.CreateGroup;
import org.jetlinks.pro.datasource.DataSourceConfig;
import org.jetlinks.pro.datasource.enums.DataSourceConfigState;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.sql.JDBCType;
import java.util.*;

@Getter
@Setter
@Table(name = "data_source_config")
@Comment("数据源信息表")
@EnableEntityEvent
public class DataSourceConfigEntity extends GenericEntity<String> implements RecordCreationEntity {

    @Column(length = 32, nullable = false, updatable = false)
    @NotBlank(groups = CreateGroup.class)
    @Schema(description = "类型ID")
    private String typeId;

    @Column
    @Schema(description = "名称")
    private String name;

    @Column
    @Schema(description = "说明")
    private String description;

    @Column(updatable = false)
    @Schema(
        description = "创建者ID(只读)"
        , accessMode = Schema.AccessMode.READ_ONLY
    )
    private String creatorId;

    @Column(updatable = false)
    @Schema(
        description = "创建者名称(只读)"
        , accessMode = Schema.AccessMode.READ_ONLY
    )
    private String creatorName;

    @Column(updatable = false)
    @DefaultValue(generator = Generators.CURRENT_TIME)
    @Schema(
        description = "创建时间(只读)"
        , accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long createTime;

    @Column
    @JsonCodec
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR, javaType = String.class)
    @Schema(description = "集群共用的配置")
    private Map<String, Object> shareConfig;

    @Column(nullable = false)
    @Schema(description = "集群是否共用配置")
    @DefaultValue("true")
    private Boolean shareCluster;

    @Column
    @JsonCodec
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR, javaType = String.class)
    @Schema(description = "集群独立配置信息")
    private List<ClusterDataSourceConfig> clusterConfigs;

    @Column(nullable = false)
    @EnumCodec
    @ColumnType(javaType = String.class)
    @Schema(description = "配置状态")
    @DefaultValue("enabled")
    private DataSourceConfigState state;

    public Optional<DataSourceConfig> getShareConfig(String currentServerId) {
        if (Boolean.TRUE.equals(shareCluster)) {
            return Optional.of(convertConfig(shareConfig));
        }
        if (CollectionUtils.isEmpty(clusterConfigs)) {
            return Optional.empty();
        }

        return clusterConfigs
            .stream()
            .filter(conf -> Objects.equals(conf.getServerId(), currentServerId))
            .map(ClusterDataSourceConfig::getConfiguration)
            .map(this::convertConfig)
            .findAny();
    }

    private DataSourceConfig convertConfig(Map<String, Object> config) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setId(getId());
        dataSourceConfig.setTypeId(getTypeId());
        dataSourceConfig.setConfiguration(config);
        return dataSourceConfig;
    }
}
