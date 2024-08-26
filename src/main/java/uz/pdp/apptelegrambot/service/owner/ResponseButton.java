package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ResponseButton {
    private final ButtonService buttonService;
    private final LangService langService;

    @Cacheable(value = "responseButton", key = "#userId")
    public ReplyKeyboard start(long userId) {
        List<String> list = new ArrayList<>();

        list.add(langService.getMessage(LangFields.ADD_BOT, userId));

        list.add(langService.getMessage(LangFields.MY_BOTS, userId));

        list.add(langService.getMessage(LangFields.LANG_SETTINGS, userId));

        list.add(langService.getMessage(LangFields.CONTACT_US, userId));

        return buttonService.withString(list);
    }
}
