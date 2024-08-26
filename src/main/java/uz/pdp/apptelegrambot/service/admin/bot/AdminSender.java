package uz.pdp.apptelegrambot.service.admin.bot;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;

public class AdminSender extends DefaultAbsSender {

    public AdminSender(String token) {
        super(new DefaultBotOptions(), token);
    }

}
