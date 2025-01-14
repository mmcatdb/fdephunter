/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow;

import de.uni.passau.server.workflow.service.ScheduledJobsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 *
 * @author pavel.koupil
 */
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
