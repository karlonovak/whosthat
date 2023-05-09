package hr.kn.whosthat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DumpController {

    private final Logger logger = LoggerFactory.getLogger(DumpController.class);

    @PostMapping(value = "/")
    @ResponseBody
    public void processEvent(HttpEntity<String> httpEntity) {
        if (httpEntity.getBody() != null && httpEntity.getBody().contains("Motion alarm")) {
            logger.info("process motion");

        } else {
            logger.info("UNKNOWN event");
        }
//        if (httpEntity.getBody() != null && httpEntity.getBody().contains("fielddetection")) {
//            ftpClient.listFiles("")
//                .stream()
//                .filter(f -> !f.getName().equals("processed")) // ignore 'processed' folder
//                .peek(f -> logger.info("File on FTP server: {}", f.getName()))
//                .max(Comparator.comparing(v -> v.getTimestamp().getTimeInMillis()))
//                .ifPresent(f -> {
//                    logger.info("Person detected in file {}", f.getName());
//                    var file = ftpClient.retrieveFile(f);
//                    telegramService.sendPhoto(file, f.getName());
//                    ftpClient.rename(f.getName(), "processed/" + f.getName());
//                });
//        }
    }
}
