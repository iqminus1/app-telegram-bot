package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.apptelegrambot.entity.Tariff;
import uz.pdp.apptelegrambot.enums.ExpireType;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.repository.TariffRepository;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.utils.AppConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ResponseButton {
    private final ButtonService buttonService;
    private final LangService langService;
    private final TariffRepository tariffRepository;
    private final Temp temp;

    @Cacheable(value = "responseButtonStart", key = "#userId")
    public ReplyKeyboard start(long userId) {
        List<String> list = new ArrayList<>();

        list.add(langService.getMessage(LangFields.BUTTON_ADD_BOT, userId));

        list.add(langService.getMessage(LangFields.BUTTON_MY_BOTS, userId));

        list.add(langService.getMessage(LangFields.BUTTON_LANG_SETTINGS, userId));

        list.add(langService.getMessage(LangFields.BUTTON_CONTACT_US, userId));

        return buttonService.withString(list);
    }

    @Cacheable(value = "responseButtonContactNumber", key = "commonUtils.getUserLang(#userId)")
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

    public InlineKeyboardMarkup tariffList(Long botId, Long userId) {
        List<Tariff> tariffs = temp.getTempTariffs(userId);
        List<Integer> ordinals = tariffs.stream().map(Tariff::getType).map(ExpireType::ordinal).toList();
        List<Map<String, String>> list = new ArrayList<>();
        String week = langService.getMessage(LangFields.TARIFF_WEEK_TEXT, userId);
        if (!ordinals.contains(0)) {
            list.add(Map.of(week, AppConstant.TARIFF_SELECTING_DATA + botId + "+0:false"));
        } else {
            list.add(Map.of(week + AppConstant.GREEN_TEXT, AppConstant.TARIFF_SELECTING_DATA + botId + "+0:true"));
        }
        String day15 = langService.getMessage(LangFields.TARIFF_15_DAY_TEXT, userId);
        if (!ordinals.contains(1)) {
            list.add(Map.of(day15, AppConstant.TARIFF_SELECTING_DATA + botId + "+1:false"));
        } else {
            list.add(Map.of(day15 + AppConstant.GREEN_TEXT, AppConstant.TARIFF_SELECTING_DATA + botId + "+1:true"));
        }

        String month = langService.getMessage(LangFields.TARIFF_MONTH_TEXT, userId);
        if (!ordinals.contains(2)) {
            list.add(Map.of(month, AppConstant.TARIFF_SELECTING_DATA + botId + "+2:false"));
        } else {
            list.add(Map.of(month + AppConstant.GREEN_TEXT, AppConstant.TARIFF_SELECTING_DATA + botId + "+2:true"));
        }

        String year = langService.getMessage(LangFields.TARIFF_YEAR_TEXT, userId);
        if (!ordinals.contains(3)) {
            list.add(Map.of(year, AppConstant.TARIFF_SELECTING_DATA + botId + "+3:false"));
        } else {
            list.add(Map.of(year + AppConstant.GREEN_TEXT, AppConstant.TARIFF_SELECTING_DATA + botId + "+3:true"));
        }

        String unlimited = langService.getMessage(LangFields.TARIFF_UNLIMITED_TEXT, userId);
        if (!ordinals.contains(4)) {
            list.add(Map.of(unlimited, AppConstant.TARIFF_SELECTING_DATA + botId + "+4:false"));
        } else {
            list.add(Map.of(unlimited + AppConstant.GREEN_TEXT, AppConstant.TARIFF_SELECTING_DATA + botId + "+4:true"));
        }

        list.add(Map.of(langService.getMessage(LangFields.ACCEPT_TARIFFS_TEXT, userId), AppConstant.ACCEPT_TARIFFS_DATA));

        return buttonService.callbackKeyboard(list);
    }
}
