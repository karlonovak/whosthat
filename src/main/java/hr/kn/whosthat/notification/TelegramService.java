package hr.kn.whosthat.notification;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendPhoto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class TelegramService {

    @Value("${telegram.token}")
    private String telegramToken;

    private final TelegramBot telegramBot;

    public TelegramService() {
        this.telegramBot = new TelegramBot(telegramToken);
    }

    public void sendPhoto(String photoPath, String caption) {
        var file = new File(photoPath);
        var sendPhotoRequest = new SendPhoto(-321238055, file).caption(caption);
        telegramBot.execute(sendPhotoRequest);
    }

}
