package uz.pdp.apptelegrambot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.utils.owner.CommonUtils;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LangServiceImpl implements LangService {
    private final ResourceBundleMessageSource messageSource;
    private final CommonUtils commonUtils;

    @Cacheable(value = "languageMessage", key = "#commonUtils.getUserLang(#userId)")
    @Override
    public String getMessage(LangFields keyword, long userId) {
        try {
            return messageSource.getMessage(keyword.name(), null, new Locale(commonUtils.getUserLang(userId)));
        } catch (Exception e) {
            return messageSource.getMessage(keyword.name(), null, new Locale(LangEnum.RU.name()));
        }
    }

    @Cacheable(value = "languageMessage", key = "#lang")
    @Override
    public String getMessage(LangFields keyword, String lang) {
        try {
            return messageSource.getMessage(keyword.name(), null, new Locale(lang));
        } catch (Exception e) {
            return messageSource.getMessage(keyword.name(), null, new Locale(LangEnum.RU.name()));
        }
    }

    @Override
    public LangEnum getLanguageEnum(String text) {
        if (text.equals(getMessage(LangFields.BUTTON_LANGUAGE_UZBEK, "Uz"))) {
            return LangEnum.UZ;
        } else if (text.equals(getMessage(LangFields.BUTTON_LANGUAGE_RUSSIAN, "Uz"))) {
            return LangEnum.RU;
        } else if (text.equals(getMessage(LangFields.BUTTON_LANGUAGE_ENGLISH, "Uz"))) {
            return LangEnum.ENG;
        }
        return null;
    }

}
