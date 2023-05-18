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

    private final Integer chatId;
    private final TelegramBot telegramBot;
    private final long notificationThreshold;

    private long lastNotificationTime = 0;

    public TelegramService(@Value("${telegram.token}") String telegramToken,
                           @Value("${telegram.chatId}") Integer chatId,
                           @Value("${telegram.notification.threshold}") Integer notificationThreshold) {
        this.telegramBot = new TelegramBot(telegramToken);
        this.chatId = chatId;
        this.notificationThreshold = notificationThreshold * 1000; // convert to millis
    }

    public synchronized void sendPhoto(byte[] photo, String caption) {
        if (minutePassedSinceLastNotification()) {
            logger.info("Notifying user via Telegram");
            var sendPhotoRequest = new SendPhoto(chatId, photo).caption(caption);
            telegramBot.execute(sendPhotoRequest);
            lastNotificationTime = System.currentTimeMillis();
        }
    }

    // Don't send notification more than once a minute.
    private boolean minutePassedSinceLastNotification() {
        return System.currentTimeMillis() - lastNotificationTime > notificationThreshold;
    }

}
