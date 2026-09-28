package com.leets.mission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// TODO: DB 연동 시 exclude 제거 및 application.properties에 datasource 설정 추가하기
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MissionApplication {

	public static void main(String[] args) {
		SpringApplication.run(MissionApplication.class, args);
	}

}
