package hr.kn.whosthat.notification;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendPhoto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TelegramService {

    private final Logger logger = LoggerFactory.getLogger(TelegramService.class);

    @Value("${telegram.chatId}")
    private Integer chatId;

    private final TelegramBot telegramBot;

    private long lastNotificationTime = 0;

    public TelegramService(@Value("${telegram.token}") String telegramToken) {
        this.telegramBot = new TelegramBot(telegramToken);
    }

    public synchronized void sendPhoto(byte[] photo, String caption) {
        if (minutePassedSinceLastNotification()) {
            logger.info("Notifying user!");
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
