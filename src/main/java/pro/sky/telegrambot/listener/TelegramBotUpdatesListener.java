package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }


    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            // Process your updates here
            switch(update.message().text()){
                case "/start":
                    SendMessage message = new SendMessage("5280286983", "Привет меня зовут Gary");
                    SendResponse response = this.telegramBot.execute(message);
                    response.isOk();
                    break;
                default:
                    SendMessage message1 = new SendMessage("5280286983", "Некорректная команда");
                    SendResponse response2 = this.telegramBot.execute(message1);
                    response2.isOk();
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}
