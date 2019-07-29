package org.xxjr.job.listener;

import org.ddq.common.core.SpringAppContext;
import org.llw.job.core.ScheduleManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.xxjr.job.listener.mq.JobMqReceiver;

import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@ComponentScan(basePackages = {"org.xxjr.*","org.ddq.*","org.llw.*"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@Slf4j
public class JobListenerApplication {
	public static void main(String[] args) {
		log.info("=============xxjr-job-listener is run start===============");
		SpringApplication.run(JobListenerApplication.class, args);
		
		log.info("=============initSchedule is run start===============");
		ScheduleManager.getInstance().initSchedule();
		log.info("=============initSchedule is run success===============");
		
		log.info("=============initSchedule is run start===============");
		JobMqReceiver jobm = SpringAppContext.getBean(JobMqReceiver.class);
		jobm.init();
		log.info("=============initSchedule is run success===============");
		
		log.info("=============xxjr-job-listener is run success===============");
	}
}

