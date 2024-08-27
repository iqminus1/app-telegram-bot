package uz.pdp.apptelegrambot.service.admin;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface AdminMessageService {
    void process(Message message);
}
