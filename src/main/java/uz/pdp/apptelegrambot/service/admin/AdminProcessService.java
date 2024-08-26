package uz.pdp.apptelegrambot.service.admin;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface AdminProcessService {
    void process(Update update, Long adminId);
}
