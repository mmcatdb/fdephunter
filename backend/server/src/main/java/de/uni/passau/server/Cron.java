package de.uni.passau.server;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableScheduling
public class Cron {

    @Scheduled(cron = "*/30 * * * * *")
    // @Scheduled(cron = "0 * * * * *")
    public void scheduledJobEveryMinute() {
        // scheduledJobsService.executeJobs();
    }

    @Scheduled(cron = "0 0 * * * *")
    // @Scheduled(cron = "* * * * * *")
    public void scheduledJobEveryHour() {
        System.out.println("HOUR:" + System.currentTimeMillis());
    }

    @Bean
    @Qualifier("asyncExecutor")
    public TaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.initialize();
        return executor;
    }

}
