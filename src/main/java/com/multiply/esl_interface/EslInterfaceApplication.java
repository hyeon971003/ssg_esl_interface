package com.multiply.esl_interface;

import com.multiply.esl_interface.v1.global.common.converter.EncodingConverter;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@ComponentScan("com.multiply.esl_interface.v1")
@EnableMongoRepositories(basePackages = "com.multiply.esl_interface.v1.web.repository")
@EnableMongoAuditing
@EnableScheduling
public class EslInterfaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EslInterfaceApplication.class, args);
	}

}
