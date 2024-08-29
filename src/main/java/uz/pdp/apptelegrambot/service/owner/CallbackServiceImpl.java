package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.apptelegrambot.entity.*;
import uz.pdp.apptelegrambot.enums.ExpireType;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.enums.ScreenshotStatus;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.repository.CodeGroupRepository;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.repository.ScreenshotGroupRepository;
import uz.pdp.apptelegrambot.repository.TariffRepository;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.owner.bot.OwnerSender;
import uz.pdp.apptelegrambot.utils.AppConstant;
import uz.pdp.apptelegrambot.utils.owner.CommonUtils;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CallbackServiceImpl implements CallbackService {
    private final CommonUtils commonUtils;
    private final Temp temp;
    private final OwnerSender sender;
    private final LangService langService;
    private final ResponseButton responseButton;
    private final ResponseText responseText;
    private final GroupRepository groupRepository;
    private final TariffRepository tariffRepository;
    private final Random random;
    private final CodeGroupRepository codeGroupRepository;
    private final ScreenshotGroupRepository screenshotGroupRepository;

    @Override
    public void process(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        String data = callbackQuery.getData();
        User user = commonUtils.getUser(userId);
        switch (user.getState()) {
            case START -> {
                if (data.startsWith(AppConstant.BOT_DATA)) {
                    showBotInfo(callbackQuery);
                } else if (data.equals(AppConstant.BACK_TO_BOT_LIST_DATA)) {
                    backToList(callbackQuery);
                } else if (data.startsWith(AppConstant.TARIFF_LIST_DATA)) {
                    showTariffList(callbackQuery);
                } else if (data.equals(AppConstant.FREE_DATA)) {
                    System.out.println("1");
                } else if (data.startsWith(AppConstant.PAMYENT_MATHODS_DATA)) {

                } else if (data.startsWith(AppConstant.BACK_TO_BOT_INFO_DATA)) {
                    showBotInfo(callbackQuery);
                } else if (data.startsWith(AppConstant.GENERATE_CODE_DATA)) {
                    showTariffsForGenerateCode(callbackQuery);
                } else if (data.startsWith(AppConstant.GENERATE_CODE_FOR_TARIFF_DATA)) {
                    generateCode(callbackQuery);
                } else if (data.startsWith(AppConstant.SEE_ALL_SCREENSHOTS)) {
                    showScreenshots(callbackQuery);
                } else if (data.startsWith(AppConstant.CARD_NUMBER_DATA)) {
                    addCardNumber(callbackQuery);
                }
            }
            case SELECTING_TARIFF -> {
                if (data.startsWith(AppConstant.ACCEPT_TARIFFS_DATA)) {
                    acceptTariffs(callbackQuery);
                } else if (data.startsWith(AppConstant.TARIFF_SELECTING_DATA)) {
                    changeTariffStatus(userId, data, callbackQuery);
                }
            }
        }
    }

    private void addCardNumber(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        sender.deleteMessage(userId, messageId);
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        temp.addTempGroup(groupRepository.findById(botId).orElseThrow());
        temp.addTempBotId(userId, botId);
        String message = langService.getMessage(LangFields.SEND_CARD_NUMBER_TEXT, userId);
        sender.sendMessageAndRemove(userId, message);
        commonUtils.setState(userId, StateEnum.SENDING_CARD_NUMBER);
    }

    private void showScreenshots(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.findById(botId).orElseThrow();
        List<ScreenshotGroup> screenshots = screenshotGroupRepository.findAllByGroupIdAndStatus(group.getGroupId(), ScreenshotStatus.DONT_SEE);
        if (screenshots.isEmpty()) {
            sender.sendMessage(userId, langService.getMessage(LangFields.SCREENSHOTS_EMPTY_TEXT, userId));
            return;
        }
        for (ScreenshotGroup screenshot : screenshots) {
            Long screenshotId = screenshot.getId();
            InlineKeyboardMarkup keyboard = responseButton.screenshotsKeyboard(userId, screenshotId);
            String message = langService.getMessage(LangFields.UN_CHECKED_SCREENSHOT_TEXT, userId);
            sender.sendPhoto(userId, message, screenshot.getPath(), keyboard);
        }
    }

    private void generateCode(CallbackQuery callbackQuery) {
        long tariffId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Tariff tariff = tariffRepository.findById(tariffId).orElseThrow();
        Long userId = callbackQuery.getFrom().getId();
        CodeGroup codeGroup = new CodeGroup(generateCode(), tariff.getBotId(), null, tariff.getType(), false, null, tariffId, tariff.getPrice());
        codeGroupRepository.saveOptional(codeGroup);
        String message = getCodeText(userId, tariff.getType().ordinal());
        sender.sendMessage(userId, message + " " + codeGroup.getCode());
    }

    private String getCodeText(Long userId, int ordinal) {
        if (ordinal == 0) {
            return langService.getMessage(LangFields.CODE_WEEK_TEXT, userId);
        } else if (ordinal == 1) {
            return langService.getMessage(LangFields.CODE_DAY15_TEXT, userId);
        } else if (ordinal == 2) {
            return langService.getMessage(LangFields.CODE_MONTH_TEXT, userId);
        } else if (ordinal == 3) {
            return langService.getMessage(LangFields.CODE_YEAR_TEXT, userId);
        } else if (ordinal == 4) {
            return langService.getMessage(LangFields.CODE_UNLIMITED_TEXT, userId);
        }
        return null;
    }

    private void showTariffsForGenerateCode(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        if (tariffRepository.findAllByBotId(botId).isEmpty()) {
            return;
        }
        long userId = callbackQuery.getFrom().getId();
        InlineKeyboardMarkup markup = responseButton.showTariffs(botId, userId, AppConstant.GENERATE_CODE_FOR_TARIFF_DATA);
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        if (keyboard.size() != 6) {
            keyboard.remove(keyboard.size() - 2);
            markup.setKeyboard(keyboard);
        }
        String message = langService.getMessage(LangFields.SELECT_CHOOSE_TEXT, userId);
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), message, markup);
    }

    private void showTariffList(CallbackQuery callbackQuery) {
        long userId = callbackQuery.getFrom().getId();
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        InlineKeyboardMarkup markup = responseButton.showTariffs(botId, userId, AppConstant.FREE_DATA);
        String message = langService.getMessage(LangFields.SELECT_CHOOSE_TEXT, userId);
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), message, markup);
    }

    private void backToList(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        InlineKeyboardMarkup markup = responseButton.botsList(userId);
        String message = langService.getMessage(LangFields.SELECT_CHOOSE_BOT_TEXT, userId);
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), message, markup);
    }

    private void showBotInfo(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.findById(botId).orElseThrow();
        Long userId = callbackQuery.getFrom().getId();
        String message = langService.getMessage(LangFields.BOT_INFO_TEXT, userId).formatted(group.getBotUsername(), group.getName());
        InlineKeyboardMarkup markup = responseButton.botInfo(botId, userId);
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), message, markup);
    }

    private void changeTariffStatus(Long userId, String data, CallbackQuery callbackQuery) {
        String[] split = data.split("\\+");
        long botId = Long.parseLong(split[0].split(":")[1]);
        String[] ordinalAndStatus = split[1].split(":");
        int ordinal = Integer.parseInt(ordinalAndStatus[0]);
        boolean status = Boolean.parseBoolean(ordinalAndStatus[1]);
        if (status) {
            temp.removeTempTariff(ordinal, userId);
        } else {
            temp.addTempTariff(userId, new Tariff(botId, ExpireType.values()[ordinal], null));
        }
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), callbackQuery.getMessage().getText(), responseButton.tariffList(botId, userId));

    }

    private void acceptTariffs(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        sender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
        List<Tariff> tempTariffs = temp.getTempTariffs(userId);
        if (tempTariffs.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            sender.sendMessage(userId, langService.getMessage(LangFields.NOT_ANY_TARIFF_TEXT, userId), responseButton.start(userId));
            return;
        }
        commonUtils.setState(userId, StateEnum.SENDING_TARIFF_PRICE);
        List<Integer> list = tempTariffs.stream().map(Tariff::getType).map(ExpireType::ordinal).toList();
        Integer i = list.get(0);
        String expireText = responseText.getSendExpireText(i, userId);
        sender.sendMessage(userId, expireText);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (random.nextBoolean()) {
                sb.append(random.nextInt(0, 9));
            } else {
                if (random.nextBoolean()) {
                    sb.append((char) random.nextInt(65, 90));
                } else {
                    sb.append((char) random.nextInt(97, 122));
                }
            }
        }
        return sb.toString();
    }
}
