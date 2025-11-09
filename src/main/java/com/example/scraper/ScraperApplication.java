package com.example.scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.WebApplicationType;

@SpringBootApplication
public class ScraperApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ScraperApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE); // 🛑 No web server
		app.run(args);
	}
}
