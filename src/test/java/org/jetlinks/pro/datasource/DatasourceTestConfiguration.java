package org.jetlinks.pro.datasource;

import org.hswebframework.web.authorization.token.DefaultUserTokenManager;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.starter.jackson.CustomCodecsAutoConfiguration;
import org.jetlinks.core.event.EventBus;
import org.jetlinks.supports.cluster.redis.RedisClusterManager;
import org.jetlinks.supports.event.BrokerEventBus;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
@ImportAutoConfiguration({
    CodecsAutoConfiguration.class,
    JacksonAutoConfiguration.class,
    CustomCodecsAutoConfiguration.class
})
public class DatasourceTestConfiguration {


    @Bean
    public EventBus eventBus(){
        return new BrokerEventBus();
    }

    @Bean
    public Scheduler scheduler(){
        return Schedulers.parallel();
    }

    @Bean
    public UserTokenManager userTokenManager(){
        return new DefaultUserTokenManager();
    }

    @Bean(initMethod = "startup",destroyMethod = "shutdown")
    public RedisClusterManager clusterManager(ReactiveRedisTemplate<Object,Object> template){
        return new RedisClusterManager("test","test",template);
    }
}
