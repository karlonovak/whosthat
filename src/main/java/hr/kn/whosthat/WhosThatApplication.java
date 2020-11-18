package hr.kn.whosthat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import origami.Origami;

@SpringBootApplication
@EnableScheduling
public class WhosThatApplication {

    public static void main(String[] args) {
        Origami.init();
        SpringApplication.run(WhosThatApplication.class);
    }

}
