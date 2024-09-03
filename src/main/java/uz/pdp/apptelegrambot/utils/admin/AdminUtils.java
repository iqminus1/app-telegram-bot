package uz.pdp.apptelegrambot.utils.admin;

import lombok.RequiredArgsConstructor;
import uz.pdp.apptelegrambot.entity.UserLang;
import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.repository.UserLangRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
public class AdminUtils {
    private final UserLangRepository userLangRepository;
    private final ConcurrentMap<Long, UserLang> usersLang = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, StateEnum> userState = new ConcurrentHashMap<>();
    private final ConcurrentMap<Byte, Set<Long>> sendingMessage = new ConcurrentHashMap<>();

    public void setUserState(Long userId, StateEnum stateEnum) {
        userState.put(userId, stateEnum);
    }

    public Set<Long> getSendingMessage(byte attempt) {
        return sendingMessage.getOrDefault(attempt, new HashSet<>());
    }

    public void putSendingMessage(byte attempt, Long userId) {
        if (sendingMessage.containsKey(attempt)) {
            Set<Long> longs = sendingMessage.get(attempt);
            longs.add(userId);
            sendingMessage.put(attempt, longs);
            return;
        }
        sendingMessage.put(attempt, new HashSet<>(Set.of(userId)));
    }

    public void updateSendingMessage(byte attempt, byte where, Set<Long> longs) {
        if (sendingMessage.containsKey(where)) {
            sendingMessage.remove(where, longs);
            sendingMessage.putIfAbsent(attempt, longs);
        }
    }

    public void remove(byte attempt) {
        sendingMessage.remove(attempt);
    }

    public StateEnum getUserState(Long userId) {
        if (userState.containsKey(userId)) {
            return userState.get(userId);
        }
        setUserState(userId, StateEnum.START);
        return getUserState(userId);
    }

    public boolean hasUserLang(Long userId) {
        return usersLang.containsKey(userId);
    }

    public void setUserLang(Long userId, String lang) {
        Optional<UserLang> optionalUserLang = userLangRepository.findById(userId);
        if (optionalUserLang.isPresent()) {
            UserLang userLang = optionalUserLang.get();
            userLang.setLang(lang);
            usersLang.put(userId, userLang);
            return;
        }
        UserLang userLang = new UserLang(userId, lang);
        userLangRepository.save(userLang);
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
