package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.entity.Tariff;
import uz.pdp.apptelegrambot.enums.ExpireType;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.repository.TariffRepository;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.utils.AppConstant;
import uz.pdp.apptelegrambot.utils.owner.CommonUtils;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ResponseButton {
    private final ButtonService buttonService;
    private final LangService langService;
    private final TariffRepository tariffRepository;
    private final Temp temp;
    private final GroupRepository groupRepository;
    private final ResponseText responseText;
    private final CommonUtils commonUtils;

    public ReplyKeyboard start(String lang) {
        List<String> list = new ArrayList<>();

        list.add(langService.getMessage(LangFields.BUTTON_ADD_BOT, lang));

        list.add(langService.getMessage(LangFields.BUTTON_MY_BOTS, lang));

        list.add(langService.getMessage(LangFields.BUTTON_LANG_SETTINGS, lang));

        list.add(langService.getMessage(LangFields.BUTTON_CONTACT_US, lang));

        return buttonService.withString(list);
    }

    public ReplyKeyboard contactNumber(String lang) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        KeyboardButton keyboardButton = new KeyboardButton(langService.getMessage(LangFields.BUTTON_SEND_CONTACT_NUMBER, lang));
        keyboardButton.setRequestContact(true);
        KeyboardRow row = new KeyboardRow();
        row.add(keyboardButton);
        markup.setKeyboard(List.of(row));
        return markup;
    }

    public InlineKeyboardMarkup tariffList(Long botId, Long userId) {
        List<Tariff> tariffs = temp.getTempTariffs(userId);
        String userLang = commonUtils.getUserLang(userId);
        List<Integer> ordinals = tariffs.stream().map(Tariff::getType).map(ExpireType::ordinal).toList();
        List<Map<String, String>> list = new ArrayList<>();
        String week = langService.getMessage(LangFields.TARIFF_WEEK_TEXT, userLang);
        if (!ordinals.contains(0)) {
            list.add(Map.of(week, AppConstant.TARIFF_SELECTING_DATA + botId + "+0:false"));
        } else {
            list.add(Map.of(week + AppConstant.GREEN_TEXT, AppConstant.TARIFF_SELECTING_DATA + botId + "+0:true"));
        }
        String day15 = langService.getMessage(LangFields.TARIFF_15_DAY_TEXT, userLang);
        if (!ordinals.contains(1)) {
            list.add(Map.of(day15, AppConstant.TARIFF_SELECTING_DATA + botId + "+1:false"));
        } else {
            list.add(Map.of(day15 + AppConstant.GREEN_TEXT, AppConstant.TARIFF_SELECTING_DATA + botId + "+1:true"));
        }

        String month = langService.getMessage(LangFields.TARIFF_MONTH_TEXT, userLang);
        if (!ordinals.contains(2)) {
            list.add(Map.of(month, AppConstant.TARIFF_SELECTING_DATA + botId + "+2:false"));
        } else {
            list.add(Map.of(month + AppConstant.GREEN_TEXT, AppConstant.TARIFF_SELECTING_DATA + botId + "+2:true"));
        }

        String year = langService.getMessage(LangFields.TARIFF_YEAR_TEXT, userLang);
        if (!ordinals.contains(3)) {
            list.add(Map.of(year, AppConstant.TARIFF_SELECTING_DATA + botId + "+3:false"));
        } else {
            list.add(Map.of(year + AppConstant.GREEN_TEXT, AppConstant.TARIFF_SELECTING_DATA + botId + "+3:true"));
        }

        String unlimited = langService.getMessage(LangFields.TARIFF_UNLIMITED_TEXT, userLang);
        if (!ordinals.contains(4)) {
            list.add(Map.of(unlimited, AppConstant.TARIFF_SELECTING_DATA + botId + "+4:false"));
        } else {
            list.add(Map.of(unlimited + AppConstant.GREEN_TEXT, AppConstant.TARIFF_SELECTING_DATA + botId + "+4:true"));
        }

        list.add(Map.of(langService.getMessage(LangFields.ACCEPT_TARIFFS_TEXT, userLang), AppConstant.ACCEPT_TARIFFS_DATA));

        return buttonService.callbackKeyboard(list);
    }

    public InlineKeyboardMarkup botsList(Long userId) {
        List<Group> groups = groupRepository.findAllByAdminIdDefault(userId);
        List<Map<String, String>> list = new ArrayList<>();
        for (Group group : groups) {
            list.add(Map.of("@" + group.getBotUsername(), AppConstant.BOT_DATA + group.getId()));
        }
        return buttonService.callbackKeyboard(list);
    }

    public InlineKeyboardMarkup botInfo(long botId, String userLang) {
        List<Map<String, String>> list = new ArrayList<>();

        list.add(Map.of(langService.getMessage(LangFields.TARIFF_LIST_TEXT, userLang), AppConstant.TARIFF_LIST_DATA + botId));
        list.add(Map.of(langService.getMessage(LangFields.PAYMENT_METHODS_TEXT, userLang), AppConstant.PAMYENT_MATHODS_DATA + botId));
        list.add(Map.of(langService.getMessage(LangFields.CARD_NUMBER_TEXT, userLang), AppConstant.CARD_NUMBER_DATA + botId));
        list.add(Map.of(langService.getMessage(LangFields.GENERATE_CODE_TEXT, userLang), AppConstant.GENERATE_CODE_DATA + botId));
        list.add(Map.of(langService.getMessage(LangFields.SEE_ALL_SCREENSHOTS, userLang), AppConstant.SEE_ALL_SCREENSHOTS + botId));
        Group group = groupRepository.getByIdDefault(botId);
        if (group.isWorked()) {
            list.add(Map.of(langService.getMessage(LangFields.STOP_BOT_TEXT, userLang), AppConstant.START_STOP_BOT_DATA + botId));
        } else
            list.add(Map.of(langService.getMessage(LangFields.START_BOT_TEXT, userLang), AppConstant.START_STOP_BOT_DATA + botId));

        list.add(Map.of(langService.getMessage(LangFields.BACK_TEXT, userLang), AppConstant.BACK_TO_BOT_LIST_DATA));

        return buttonService.callbackKeyboard(list);
    }

    public InlineKeyboardMarkup showTariffs(long botId, long userId, String data) {
        String userLang = commonUtils.getUserLang(userId);
        List<Tariff> tariffs = tariffRepository.findAllByBotIdDefault(botId);
        tariffs.sort(Comparator.comparing(t -> t.getType().ordinal()));
        List<Map<String, String>> list = new ArrayList<>();
        for (Tariff tariff : tariffs) {
            list.add(Map.of(responseText.getTariffExpireText(tariff.getType().ordinal(), userLang), data + tariff.getId()));
        }
        if (tariffs.size() != 5)
            list.add(Map.of(langService.getMessage(LangFields.ADD_TARIFF_TEXT, userLang), AppConstant.ADD_TARIFF_DATA + botId));
        list.add(Map.of(langService.getMessage(LangFields.BACK_TEXT, userLang), AppConstant.BACK_TO_BOT_INFO_DATA + botId));
        return buttonService.callbackKeyboard(list);
    }

    public InlineKeyboardMarkup screenshotsKeyboard(String userLang, long id) {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map = new LinkedHashMap<>();
        map.put(langService.getMessage(LangFields.ACCEPT_SCREENSHOT_TEXT, userLang),
                AppConstant.ACCEPT_SCREENSHOT_DATA + id);
        map.put(langService.getMessage(LangFields.REJECT_SCREENSHOT_TEXT, userLang),
                AppConstant.REJECT_SCREENSHOT_DATA + id);
        list.add(map);
        return buttonService.callbackKeyboard(list);
    }

    public InlineKeyboardMarkup showGroupPayments(String userLang, long botId) {
        Group group = groupRepository.getByIdDefault(botId);
        List<Map<String, String>> list = new ArrayList<>();
        String clickText = langService.getMessage(LangFields.STOP_PAYMENT_CLICK_TEXT, userLang);
        if (!group.isClick()) {
            clickText = langService.getMessage(LangFields.START_PAYMENT_CLICK_TEXT, userLang);
        }
        list.add(Map.of(clickText, AppConstant.CHANGE_CLICK_STATUS_DATA + botId));

        String paymeText = langService.getMessage(LangFields.STOP_PAYMENT_PAYME_TEXT, userLang);
        if (!group.isClick()) {
            paymeText = langService.getMessage(LangFields.START_PAYMENT_PAYME_TEXT, userLang);
        }
        list.add(Map.of(paymeText, AppConstant.CHANGE_PAYME_STATUS_DATA + botId));

        String screenshotText = langService.getMessage(LangFields.STOP_PAYMENT_SCREENSHOT_TEXT, userLang);
        if (!group.isScreenShot()) {
            screenshotText = langService.getMessage(LangFields.START_PAYMENT_SCREENSHOT_TEXT, userLang);
        }
        list.add(Map.of(screenshotText, AppConstant.CHANGE_SCREENSHOT_STATUS_DATA + botId));

        String codeText = langService.getMessage(LangFields.STOP_PAYMENT_CODE_TEXT, userLang);
        if (!group.isCode()) {
            codeText = langService.getMessage(LangFields.START_PAYMENT_CODE_TEXT, userLang);
        }
        list.add(Map.of(codeText, AppConstant.CHANGE_CODE_STATUS_DATA + botId));

        list.add(Map.of(langService.getMessage(LangFields.BACK_TEXT, userLang), AppConstant.BACK_TO_BOT_INFO_DATA + botId));
        return buttonService.callbackKeyboard(list);
    }

    public InlineKeyboardMarkup addTariff(Long userId, Long botId) {
        List<Integer> ordinals = tariffRepository.findAllByBotIdDefault(botId).stream().map(t -> t.getType().ordinal()).toList();
        List<Map<String, String>> list = new ArrayList<>();
        String userLang = commonUtils.getUserLang(userId);
        if (!ordinals.contains(0)) {
            list.add(Map.of(responseText.getTariffExpireText(0, userLang), AppConstant.CREATE_TARIFF_DATA + 0 + "+" + botId))
            ;
        }
        if (!ordinals.contains(1)) {
            list.add(Map.of(responseText.getTariffExpireText(1, userLang), AppConstant.CREATE_TARIFF_DATA + 1 + "+" + botId))
            ;
        }
        if (!ordinals.contains(2)) {
            list.add(Map.of(responseText.getTariffExpireText(2, userLang), AppConstant.CREATE_TARIFF_DATA + 2 + "+" + botId))
            ;
        }
        if (!ordinals.contains(3)) {
            list.add(Map.of(responseText.getTariffExpireText(3, userLang), AppConstant.CREATE_TARIFF_DATA + 3 + "+" + botId))
            ;
        }
        if (!ordinals.contains(4)) {
            list.add(Map.of(responseText.getTariffExpireText(4, userLang), AppConstant.CREATE_TARIFF_DATA + 4 + "+" + botId));
        }
        list.add(Map.of(langService.getMessage(LangFields.BACK_TEXT, userLang), AppConstant.BACK_TO_TARIFFS_DATA + botId));
        return buttonService.callbackKeyboard(list);
    }

    public InlineKeyboardMarkup showTariffInfo(String userLang, long tariffId) {
        Tariff tariff = tariffRepository.getById(tariffId);
        List<Map<String, String>> list = new ArrayList<>();
        list.add(Map.of(langService.getMessage(LangFields.CHANGE_TARIFF_PRICE_TEXT, userLang), AppConstant.CHANGE_TARIFF_PRICE_DATA + tariffId));
        list.add(Map.of(langService.getMessage(LangFields.DELETE_TARIFF_TEXT, userLang), AppConstant.DELETE_TARIFF_DATA + tariffId));
        list.add(Map.of(langService.getMessage(LangFields.BACK_TEXT, userLang), AppConstant.BACK_TO_TARIFFS_DATA + tariff.getBotId()));
        return buttonService.callbackKeyboard(list);
    }
}
