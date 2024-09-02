package uz.pdp.apptelegrambot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.apptelegrambot.enums.LangFields;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ButtonServiceImpl implements ButtonService {
    private final LangService langService;

    @Override
    public ReplyKeyboard withString(List<String> list, int rowSize) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        int i = 1;
        for (String text : list) {
            row.add(new KeyboardButton(text));
            if (i == rowSize) {
                rows.add(row);
                row = new KeyboardRow();
                i = 0;
            }
            i++;
        }
        markup.setKeyboard(rows);
        return markup;
    }

    @Override
    public InlineKeyboardMarkup callbackKeyboard(List<Map<String, String>> textData) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (Map<String, String> map : textData) {

            for (String text : map.keySet()) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setCallbackData(map.get(text));
                button.setText(text);
                row.add(button);
            }

            rows.add(row);
            row = new ArrayList<>();

        }
        markup.setKeyboard(rows);
        return markup;
    }

    @Override
    public ReplyKeyboard language(String lang) {
        List<String> list = new ArrayList<>();

        list.add(langService.getMessage(LangFields.BUTTON_LANGUAGE_UZBEK, lang));

        list.add(langService.getMessage(LangFields.BUTTON_LANGUAGE_RUSSIAN, lang));

        list.add(langService.getMessage(LangFields.BUTTON_LANGUAGE_ENGLISH, lang));

        return withString(list);
    }
}
