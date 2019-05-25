package org.xxjr.store.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@ComponentScan(basePackages = { "org.xxjr.*", "org.ddq.*" ,"org.llw.*"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@Slf4j
public class StoreWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoreWebApplication.class, args);
		log.info("=============store client web is run start===============");
//		ZkClientBean clientBean = SpringAppContext.getBean(ZkClientBean.class);
//		clientBean.contextInitialized();
		log.info("=============store client web is run success=============");
	}
}
