package uz.pdp.apptelegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Random;

//@EnableCaching
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class AppTelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppTelegramBotApplication.class, args);
    }

    @Bean
    public Random random() {
        return new Random();
    }
}
