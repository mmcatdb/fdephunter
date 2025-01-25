package de.uni.passau.server;

import de.uni.passau.server.service.ScheduledJobsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class Cron {

    @Autowired
    private ScheduledJobsService scheduledJobsService;

    @Scheduled(cron = "*/6 * * * * *")
    public void scheduledJobEverySecond() {
        scheduledJobsService.executeUpdateNegativeExampleJobs();
        scheduledJobsService.executeAssignJobs();
    }

    @Scheduled(cron = "*/30 * * * * *")
    // @Scheduled(cron = "0 * * * * *")
    public void scheduledJobEveryMinute() {
        scheduledJobsService.executeDiscoveryJobs();
    }

    @Scheduled(cron = "0 0 * * * *")
    public void scheduledJobEveryHour() {
        System.out.println("HOUR:" + System.currentTimeMillis());
    }

}
