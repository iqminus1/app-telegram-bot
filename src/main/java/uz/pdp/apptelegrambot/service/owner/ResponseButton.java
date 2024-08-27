package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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

    @Cacheable(value = "responseButtonStart", key = "#userId")
    public ReplyKeyboard start(long userId) {
        List<String> list = new ArrayList<>();

        list.add(langService.getMessage(LangFields.BUTTON_ADD_BOT, userId));

        list.add(langService.getMessage(LangFields.BUTTON_MY_BOTS, userId));

        list.add(langService.getMessage(LangFields.BUTTON_LANG_SETTINGS, userId));

        list.add(langService.getMessage(LangFields.BUTTON_CONTACT_US, userId));

        return buttonService.withString(list);
    }

    @Cacheable(value = "responseButtonContactNumber",key = "commonUtils.getUserLang(#userId)")
    public ReplyKeyboard contactNumber(Long userId) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        KeyboardButton keyboardButton = new KeyboardButton(langService.getMessage(LangFields.BUTTON_SEND_CONTACT_NUMBER, userId));
        keyboardButton.setRequestContact(true);
        KeyboardRow row = new KeyboardRow();
        row.add(keyboardButton);
        markup.setKeyboard(List.of(row));
        return markup;
    }
}
