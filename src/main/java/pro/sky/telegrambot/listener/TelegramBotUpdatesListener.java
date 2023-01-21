package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final NotificationTaskRepository repository;

    @Autowired
    private TelegramBot telegramBot;

    public TelegramBotUpdatesListener(NotificationTaskRepository repository) {
        this.repository = repository;
    }

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
                    SendMessage message = new SendMessage(update.message().chat().id(), "Привет меня зовут Gary");
                    SendResponse response = this.telegramBot.execute(message);
                    response.isOk();
                    break;
                default:
                    Long chatId = update.message().chat().id();
                    if (saveMessage(chatId, update.message().text())) {
                        SendMessage message1 = new SendMessage(chatId, "Записано");
                        SendResponse response2 = this.telegramBot.execute(message1);
                        response2.isOk();
                    }
                    else {
                        SendMessage message1 = new SendMessage(chatId, "Ошибка");
                        SendResponse response2 = this.telegramBot.execute(message1);
                        response2.isOk();
                    }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    public boolean saveMessage (Long chatId, String newTask) {
        Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
        Matcher matcher = pattern.matcher(newTask);
        if (matcher.matches()) {
            String date = matcher.group(1);
            String textTask = matcher.group(3);
            System.out.println(date);
            System.out.println(textTask);
            try{
                LocalDateTime notificationDate = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                NotificationTask notificationTask = new NotificationTask();
                notificationTask.setText(textTask);
                notificationTask.setData(notificationDate);
                notificationTask.setIdChat(chatId);
                repository.save(notificationTask);
                return true;
            }
            catch (DateTimeParseException e) {
                return false;
            }
        }
        else return false;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotifications() {
        List<NotificationTask> tasksToNotify = this.repository
                .findByDataEquals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        tasksToNotify.forEach(task -> {
            this.telegramBot.execute(new SendMessage(task.getIdChat(), task.getText()));
        });
        this.repository.deleteAll(tasksToNotify);
    }

}
