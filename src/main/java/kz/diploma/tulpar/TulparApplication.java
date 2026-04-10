package kz.diploma.tulpar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling   // for daily reminder cron job
@EnableAsync        // for @Async push dispatch in NotificationService
public class TulparApplication {

    public static void main(String[] args) {
        SpringApplication.run(TulparApplication.class, args);
    }
}
