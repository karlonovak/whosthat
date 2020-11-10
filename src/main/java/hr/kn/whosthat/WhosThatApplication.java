package hr.kn.whosthat;

import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import origami.Origami;

@SpringBootApplication
public class WhosThatApplication {

    @Autowired
    private PeopleDetector peopleDetector;

    @EventListener(classes = {ContextRefreshedEvent.class})
    public void contextStartedListener() {
        var result = peopleDetector.detectPeople("/home/knovak/Pictures/p.jpg");
        if (result.arePeopleDetected()) {
            Imgcodecs.imwrite("/home/knovak/Pictures/p_.jpg", result.getImage());
        }
    }

    public static void main(String[] args) {
        Origami.init();
        SpringApplication.run(WhosThatApplication.class);
    }

}
