package hr.kn.whosthat.notification;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendPhoto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TelegramService {

    @Value("${telegram.token}")
    private String telegramToken;

    @Value("${telegram.chatId}")
    private Integer chatId;

    private final TelegramBot telegramBot;

    private long lastNotificationTime = System.currentTimeMillis();

    public TelegramService() {
        this.telegramBot = new TelegramBot(telegramToken);
    }

    public void sendPhoto(byte[] photo, String caption) {
        if (minutePassedSinceLastNotification()) {
            var sendPhotoRequest = new SendPhoto(chatId, photo).caption(caption);
            telegramBot.execute(sendPhotoRequest);
            lastNotificationTime = System.currentTimeMillis();
        }
    }

    // Don't send notification more than once a minute.
    private boolean minutePassedSinceLastNotification() {
        return System.currentTimeMillis() - lastNotificationTime > 60_000;
    }

}
