package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.entity.Tariff;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.repository.TariffRepository;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.owner.ResponseText;
import uz.pdp.apptelegrambot.utils.AppConstant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdminResponseButton {
    private final ButtonService buttonService;
    private final GroupRepository groupRepository;
    private final LangService langService;
    private final TariffRepository tariffRepository;
    private final ResponseText responseText;

    public ReplyKeyboard start(Long botId, String lang) {
        Group group = groupRepository.getByIdDefault(botId);
        List<String> list = new ArrayList<>();
        if (group.isClick()) {
            list.add(langService.getMessage(LangFields.BUTTON_ADMIN_PAYMENT_CLICK_TEXT, lang));
        }
        if (group.isPayme())
            list.add(langService.getMessage(LangFields.BUTTON_ADMIN_PAYMENT_PAYME_TEXT, lang));
        if (group.isScreenShot()) {
            list.add(langService.getMessage(LangFields.BUTTON_ADMIN_PAYMENT_SCREENSHOT_TEXT, lang));
        }
        if (group.isCode()) {
            list.add(langService.getMessage(LangFields.BUTTON_ADMIN_PAYMENT_CODE_TEXT, lang));
        }
        list.add(langService.getMessage(LangFields.MY_ORDERS_TEXT, lang));
        list.add(langService.getMessage(LangFields.BUTTON_LANG_SETTINGS, lang));

        return buttonService.withString(list);
    }

    public InlineKeyboardMarkup tariffList(Long botId, String data, String userLang) {
        List<Tariff> tariffs = tariffRepository.findAllByBotIdDefault(botId);
        if (tariffs.isEmpty()) {
            return null;
        }
        tariffs.sort(Comparator.comparing(t -> t.getType().ordinal()));
        List<Map<String, String>> list = new ArrayList<>();
        for (Tariff tariff : tariffs) {
            list.add(Map.of(responseText.getTariffExpireText(tariff.getType().ordinal(), userLang), data + "+" + AppConstant.TARIFF_DATA + tariff.getId()));
        }
        return buttonService.callbackKeyboard(list);
    }

    public InlineKeyboardMarkup callbackWithLink(Map<String, String> map) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (String text : map.keySet()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(text);
            inlineKeyboardButton.setUrl(map.get(text));
            buttons.add(List.of(inlineKeyboardButton));
        }
        markup.setKeyboard(buttons);
        return markup;
    }


}
