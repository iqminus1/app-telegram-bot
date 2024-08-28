package uz.pdp.apptelegrambot.service.admin;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface AdminCallbackService {
    void process(CallbackQuery callbackQuery);
}
