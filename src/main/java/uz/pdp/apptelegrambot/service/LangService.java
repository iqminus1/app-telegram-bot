package uz.pdp.apptelegrambot.service;

import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.enums.LangFields;

public interface LangService {

    String getMessage(LangFields keyword, String lang);

    LangEnum getLanguageEnum(String text);

    LangEnum getLang(String languageCode);
}
