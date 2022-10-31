package org.jetlinks.pro.datasource;

import org.hswebframework.web.starter.jackson.CustomCodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportAutoConfiguration({
    CodecsAutoConfiguration.class,
    JacksonAutoConfiguration.class,
    CustomCodecsAutoConfiguration.class
})
public class DatasourceTestConfiguration {





}
