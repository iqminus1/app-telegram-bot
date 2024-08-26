package uz.pdp.apptelegrambot.service;

import uz.pdp.apptelegrambot.entity.User;
import uz.pdp.apptelegrambot.enums.LangFields;

public interface LangService {
    String getMessage(LangFields keyword, long userId);

    String getMessage(LangFields keyword, String lang);
}
