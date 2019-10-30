package com.botreminder.botreminder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
public class BotreminderApplication {


	public static void main(String[] args) {
		ApiContextInitializer.init();
		SpringApplication.run(BotreminderApplication.class, args);

	}

}
