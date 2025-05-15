package ru.medgrand.DBKPProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DbcpProjectApplication {
	public static void main(String[] args) {
		SpringApplication.run(DbcpProjectApplication.class, args);
	}
}
