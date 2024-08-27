package uz.pdp.apptelegrambot.service.owner;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackService {
    void process(CallbackQuery callbackQuery);
}
