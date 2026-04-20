package io.cockroachdb.bootcamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import io.cockroachdb.bootcamp.locking.LockAspect;
import io.cockroachdb.bootcamp.locking.LockService;

@Configuration
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
public class DemoConfig implements SchedulingConfigurer {
    @Bean
    public LockAspect lockAspect(@Autowired LockService lockService) {
        return new LockAspect(lockService);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        taskScheduler.setThreadNamePrefix("scheduled-task-pool-");
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }

    @Bean
    @Primary
    public AsyncTaskExecutor backgroundTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setThreadNamePrefix("background-task-");
        executor.setCancelRemainingTasksOnClose(true);
        executor.setConcurrencyLimit(-1);
        return executor;
    }
}
