package org.jetlinks.pro.datasource.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ClusterDataSourceConfig {
    private String serverId;

    private Map<String,Object> configuration;
}
