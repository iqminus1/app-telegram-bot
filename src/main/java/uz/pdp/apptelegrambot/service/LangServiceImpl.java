package uz.pdp.apptelegrambot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import uz.pdp.apptelegrambot.entity.UserLang;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.repository.UserLangRepository;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LangServiceImpl implements LangService {
    private final ResourceBundleMessageSource messageSource;
    private final UserLangRepository userLangRepository;

    @Override
    public String getMessage(LangFields keyword, long userId) {
        try {
            return messageSource.getMessage(keyword.name(), null, new Locale(getUserLang(userId)));
        } catch (Exception e) {
            return messageSource.getMessage(keyword.name(), null, new Locale("ru"));
        }
    }

    @Cacheable(value = "userLang", key = "#userId")
    public String getUserLang(long userId) {
        return userLangRepository.findById(userId).orElseGet(() ->
                userLangRepository.saveOptional(new UserLang(userId, "ru")).get()
        ).getLang();
    }
}
