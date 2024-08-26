package uz.pdp.apptelegrambot.service.owner;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface ProcessService {
    void process(Update update);
}
