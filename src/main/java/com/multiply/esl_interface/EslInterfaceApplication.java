package com.multiply.esl_interface;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableJpaRepositories(basePackages = "com.multiply.esl_interface.v1.web.repository") // Repository 경로
@EnableTransactionManagement
@EnableScheduling // 스케줄러 사용 시 필요
@SpringBootApplication
public class EslInterfaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EslInterfaceApplication.class, args);
	}

}
