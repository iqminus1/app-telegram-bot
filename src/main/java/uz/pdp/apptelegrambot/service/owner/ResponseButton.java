package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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

    public InlineKeyboardMarkup botsList(Long userId) {
        List<Group> groups = groupRepository.findAllByAdminId(userId);
        List<Map<String, String>> list = new ArrayList<>();
        for (Group group : groups) {
            list.add(Map.of("@" + group.getBotUsername(), AppConstant.BOT_DATA + group.getId()));
        }
        return buttonService.callbackKeyboard(list);
    }

    public InlineKeyboardMarkup botInfo(long botId, long userId) {
        List<Map<String, String>> list = new ArrayList<>();

        list.add(Map.of(langService.getMessage(LangFields.TARIFF_LIST_TEXT, userId), AppConstant.TARIFF_LIST_DATA + botId));
        list.add(Map.of(langService.getMessage(LangFields.PAYMENT_METHODS_TEXT, userId), AppConstant.PAMYENT_MATHODS_DATA + botId));
        list.add(Map.of(langService.getMessage(LangFields.CARD_NUMBER_TEXT, userId), AppConstant.CARD_NUMBER_DATA + botId));
        list.add(Map.of(langService.getMessage(LangFields.GENERATE_CODE_TEXT, userId), AppConstant.GENERATE_CODE_DATA + botId));
        list.add(Map.of(langService.getMessage(LangFields.SEE_ALL_SCREENSHOTS, userId), AppConstant.SEE_ALL_SCREENSHOTS + botId));
        Group group = groupRepository.findById(botId).orElseThrow();
        if (group.isWorked()) {
            list.add(Map.of(langService.getMessage(LangFields.STOP_BOT_TEXT, userId), AppConstant.START_STOP_BOT_DATA + botId));
        } else
            list.add(Map.of(langService.getMessage(LangFields.START_BOT_TEXT, userId), AppConstant.START_STOP_BOT_DATA + botId));

        list.add(Map.of(langService.getMessage(LangFields.BACK_TEXT, userId), AppConstant.BACK_TO_BOT_LIST_DATA));

        return buttonService.callbackKeyboard(list);
    }

    public InlineKeyboardMarkup showTariffs(long botId, long userId, String data) {
        String userLang = commonUtils.getUserLang(userId);
        List<Tariff> tariffs = tariffRepository.findAllByBotId(botId);
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

    public InlineKeyboardMarkup screenshotsKeyboard(Long userId, long id) {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map = new LinkedHashMap<>();
        map.put(langService.getMessage(LangFields.ACCEPT_SCREENSHOT_TEXT, userId),
                AppConstant.ACCEPT_SCREENSHOT_DATA + id);
        map.put(langService.getMessage(LangFields.REJECT_SCREENSHOT_TEXT, userId),
                AppConstant.REJECT_SCREENSHOT_DATA + id);
        list.add(map);
        return buttonService.callbackKeyboard(list);
    }

    public InlineKeyboardMarkup showGroupPayments(Long userId, long botId) {
        Group group = groupRepository.findById(botId).orElseThrow();
        List<Map<String, String>> list = new ArrayList<>();
        String clickText = langService.getMessage(LangFields.STOP_PAYMENT_CLICK_TEXT, userId);
        if (!group.isClick()) {
            clickText = langService.getMessage(LangFields.START_PAYMENT_CLICK_TEXT, userId);
        }
        list.add(Map.of(clickText, AppConstant.CHANGE_CLICK_STATUS_DATA + botId));

        String paymeText = langService.getMessage(LangFields.STOP_PAYMENT_PAYME_TEXT, userId);
        if (!group.isClick()) {
            paymeText = langService.getMessage(LangFields.START_PAYMENT_PAYME_TEXT, userId);
        }
        list.add(Map.of(paymeText, AppConstant.CHANGE_PAYME_STATUS_DATA + botId));

        String screenshotText = langService.getMessage(LangFields.STOP_PAYMENT_SCREENSHOT_TEXT, userId);
        if (!group.isScreenShot()) {
            screenshotText = langService.getMessage(LangFields.START_PAYMENT_SCEEENSHOT_TEXT, userId);
        }
        list.add(Map.of(screenshotText, AppConstant.CHANGE_SCREENSHOT_STATUS_DATA + botId));

        String codeText = langService.getMessage(LangFields.STOP_PAYMENT_CODE_TEXT, userId);
        if (!group.isCode()) {
            codeText = langService.getMessage(LangFields.START_PAYMENT_CODE_TEXT, userId);
        }
        list.add(Map.of(codeText, AppConstant.CHANGE_CODE_STATUS_DATA + botId));

        list.add(Map.of(langService.getMessage(LangFields.BACK_TEXT, userId), AppConstant.BACK_TO_BOT_INFO_DATA + botId));
        return buttonService.callbackKeyboard(list);
    }

    public InlineKeyboardMarkup addTariff(Long userId, Long botId) {
        List<Integer> ordinals = tariffRepository.findAllByBotId(botId).stream().map(t -> t.getType().ordinal()).toList();
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

    public InlineKeyboardMarkup showTariffInfo(Long userId, long tariffId) {
        Tariff tariff = tariffRepository.findById(tariffId).orElseThrow();
        List<Map<String, String>> list = new ArrayList<>();
        list.add(Map.of(langService.getMessage(LangFields.CHANGE_TARIFF_PRICE_TEXT, userId), AppConstant.CHANGE_TARIFF_PRICE_DATA + tariffId));
        list.add(Map.of(langService.getMessage(LangFields.DELETE_TRAIFF_TEXT, userId), AppConstant.DELETE_TARIFF_DATA + tariffId));
        list.add(Map.of(langService.getMessage(LangFields.BACK_TEXT, userId), AppConstant.BACK_TO_TARIFFS_DATA + tariff.getBotId()));
        return buttonService.callbackKeyboard(list);
    }
}
