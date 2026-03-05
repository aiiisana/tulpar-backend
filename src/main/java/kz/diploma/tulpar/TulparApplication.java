package kz.diploma.tulpar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class TulparApplication {

    public static void main(String[] args) {
        SpringApplication.run(TulparApplication.class, args);
    }
}
