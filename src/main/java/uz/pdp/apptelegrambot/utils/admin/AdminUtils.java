package uz.pdp.apptelegrambot.utils.admin;

import lombok.RequiredArgsConstructor;
import uz.pdp.apptelegrambot.entity.UserLang;
import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.repository.UserLangRepository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
public class AdminUtils {
    private final UserLangRepository userLangRepository;
    private final ConcurrentMap<Long, UserLang> usersLang = new ConcurrentHashMap<>();

    public void setUserLang(Long userId, String lang) {
        Optional<UserLang> optionalUserLang = userLangRepository.findById(userId);
        if (optionalUserLang.isPresent()) {
            UserLang userLang = optionalUserLang.get();
            userLang.setLang(lang);
            usersLang.put(userId, userLang);
            return;
        }
        UserLang userLang = new UserLang(userId, lang);
        userLangRepository.saveOptional(userLang);
        usersLang.put(userId, userLang);
    }

    public String getUserLang(Long userId) {
        if (usersLang.containsKey(userId)) {
            return usersLang.get(userId).getLang();
        }
        setUserLang(userId, LangEnum.RU.name());
        return getUserLang(userId);
    }
}
