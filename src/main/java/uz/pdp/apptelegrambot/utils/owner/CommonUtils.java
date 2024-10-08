package uz.pdp.apptelegrambot.utils.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrambot.entity.User;
import uz.pdp.apptelegrambot.entity.UserLang;
import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.repository.UserLangRepository;
import uz.pdp.apptelegrambot.repository.UserRepository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
@Component
public class CommonUtils {
    private final ConcurrentMap<Long, User> users = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, UserLang> userLang = new ConcurrentHashMap<>();
    private final UserLangRepository userLangRepository;
    private final UserRepository userRepository;

    public User getUser(Long userId) {
        if (users.containsKey(userId)) {
            return users.get(userId);
        }
        Optional<User> optional = userRepository.findById(userId);
        if (optional.isPresent()) {
            users.put(userId, optional.get());
            return optional.get();
        }
        User user = userRepository.save(new User(userId, StateEnum.START, null));
        users.put(userId, user);
        return user;
    }

    public void setState(Long userId, StateEnum state) {
        User user = getUser(userId);
        user.setState(state);
    }

    public String getUserLang(long userId) {
        if (userLang.containsKey(userId)) {
            return userLang.get(userId).getLang();
        }
        UserLang user = userLangRepository.findById(userId).orElseGet(() ->
                userLangRepository.save(new UserLang(userId, "ru"))
        );
        userLang.put(userId, user);
        return user.getLang();
    }

    public void setLang(long userId, LangEnum language) {
        if (userLang.containsKey(userId)) {
            UserLang lang = userLang.get(userId);
            lang.setLang(language.name());
            return;
        }
        UserLang user = userLangRepository.findById(userId).orElseGet(() ->
                userLangRepository.save(new UserLang(userId, language.name()))
        );
        userLang.put(userId, user);
    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void clearUserList() {
        saveUsers();
        saveLanguages();
    }

    @Async
    void saveUsers() {
        for (Long userId : users.keySet()) {
            User user = users.get(userId);
            userRepository.save(user);
        }
        users.clear();
    }

    @Async
    void saveLanguages() {
        for (Long userId : userLang.keySet()) {
            UserLang lang = userLang.get(userId);
            userLangRepository.save(lang);
        }
        userLang.clear();
    }

}
