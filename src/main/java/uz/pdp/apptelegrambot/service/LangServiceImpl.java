package uz.pdp.apptelegrambot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.repository.UserLangRepository;
import uz.pdp.apptelegrambot.utils.CommonUtils;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LangServiceImpl implements LangService {
    private final ResourceBundleMessageSource messageSource;
    private final UserLangRepository userLangRepository;
    private final CommonUtils commonUtils;

    @Override
    public String getMessage(LangFields keyword, long userId) {
        try {
            return messageSource.getMessage(keyword.name(), null, new Locale(commonUtils.getUserLang(userId)));
        } catch (Exception e) {
            return messageSource.getMessage(keyword.name(), null, new Locale(LangEnum.RU.name()));
        }
    }

    @Override
    public String getMessage(LangFields keyword, String lang) {
        try {
            return messageSource.getMessage(keyword.name(), null, new Locale(lang));
        } catch (Exception e) {
            return messageSource.getMessage(keyword.name(), null, new Locale(LangEnum.RU.name()));
        }
    }


}
