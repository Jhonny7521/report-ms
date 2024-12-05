package com.bm_nttdata.report_ms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ReportMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReportMsApplication.class, args);
	}

}
