package org.jetlinks.pro.datasource;

import org.hswebframework.web.authorization.token.DefaultUserTokenManager;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.starter.jackson.CustomCodecsAutoConfiguration;
import org.jetlinks.core.event.EventBus;
import org.jetlinks.core.ipc.IpcService;
import org.jetlinks.core.rpc.RpcServiceFactory;
import org.jetlinks.rule.engine.api.task.TaskExecutorProvider;
import org.jetlinks.rule.engine.cluster.ClusterSchedulerRegistry;
import org.jetlinks.rule.engine.defaults.LocalScheduler;
import org.jetlinks.rule.engine.defaults.LocalWorker;
import org.jetlinks.supports.cluster.redis.RedisClusterManager;
import org.jetlinks.supports.event.BrokerEventBus;
import org.jetlinks.supports.ipc.EventBusIpcService;
import org.jetlinks.supports.rpc.IpcRpcServiceFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

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


    @Bean
    public IpcService rpcService(EventBus eventBus) {
        return new EventBusIpcService(ThreadLocalRandom.current().nextInt(100000, 1000000), eventBus);
    }

    @Bean
    public RpcServiceFactory rpcServiceFactory(IpcService ipcService) {
        return new IpcRpcServiceFactory(ipcService);
    }

    @Bean(initMethod = "setup", destroyMethod = "cleanup")
    public ClusterSchedulerRegistry schedulerRegistry(EventBus eventBus,
                                                      RpcServiceFactory rpcService,
                                                      ObjectProvider<TaskExecutorProvider> taskExecutorProviders) {
        ClusterSchedulerRegistry registry = new ClusterSchedulerRegistry(eventBus, rpcService);
        LocalScheduler scheduler = new LocalScheduler("test");
        LocalWorker worker = new LocalWorker("test", "test", eventBus, (condition, context) -> true);
        taskExecutorProviders.forEach(worker::addExecutor);
        scheduler.addWorker(worker);
        registry.register(scheduler);
        registry.setKeepaliveInterval(Duration.ofSeconds(30));
        return registry;
    }

}
