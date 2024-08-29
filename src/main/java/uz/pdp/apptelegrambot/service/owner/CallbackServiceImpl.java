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
import uz.pdp.apptelegrambot.repository.*;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.AdminController;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;
import uz.pdp.apptelegrambot.service.owner.bot.OwnerSender;
import uz.pdp.apptelegrambot.utils.AppConstant;
import uz.pdp.apptelegrambot.utils.admin.AdminUtils;
import uz.pdp.apptelegrambot.utils.owner.CommonUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static uz.pdp.apptelegrambot.utils.AppConstant.updateOrderExpire;

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
    private final OrderRepository orderRepository;
    private final AdminController adminController;

    @Override
    public void process(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        String data = callbackQuery.getData();
        User user = commonUtils.getUser(userId);
        switch (user.getState()) {
            case START -> {
                if (data.startsWith(AppConstant.BOT_DATA)) {
                    showBotInfo(callbackQuery);
                } else if (data.startsWith(AppConstant.TARIFF_LIST_DATA)) {
                    showTariffList(callbackQuery);
                } else if (data.startsWith(AppConstant.SEE_ALL_SCREENSHOTS)) {
                    showScreenshots(callbackQuery);
                } else if (data.startsWith(AppConstant.GENERATE_CODE_FOR_TARIFF_DATA)) {
                    generateCode(callbackQuery);
                } else if (data.startsWith(AppConstant.ACCEPT_SCREENSHOT_DATA)) {
                    acceptScreenshot(callbackQuery);
                } else if (data.startsWith(AppConstant.REJECT_SCREENSHOT_DATA)) {
                    rejectScreenshot(callbackQuery);
                } else if (data.startsWith(AppConstant.PAMYENT_MATHODS_DATA)) {
                    showPaymentsInfo(callbackQuery);
                } else if (data.startsWith(AppConstant.GENERATE_CODE_DATA)) {
                    showTariffsForGenerateCode(callbackQuery);
                } else if (data.startsWith(AppConstant.CARD_NUMBER_DATA)) {
                    addCardNumber(callbackQuery);
                } else if (data.startsWith(AppConstant.SHOW_PRICE_INFO_DATA)) {
                    showTariffInfo(callbackQuery);
                } else if (data.startsWith(AppConstant.DELETE_TARIFF_DATA)) {
                    deleteTariff(callbackQuery);
                } else if (data.startsWith(AppConstant.CHANGE_TARIFF_PRICE_DATA)) {
                    changeTariffPrice(callbackQuery);
                } else if (data.startsWith(AppConstant.ADD_TARIFF_DATA)) {
                    addTariffs(callbackQuery);
                } else if (data.startsWith(AppConstant.CREATE_TARIFF_DATA)) {
                    createTariff(callbackQuery);
                } else if (data.startsWith(AppConstant.CHANGE_CLICK_STATUS_DATA)) {
                    changeStatusClick(callbackQuery);
                } else if (data.startsWith(AppConstant.CHANGE_PAYME_STATUS_DATA)) {
                    changeStatusPayme(callbackQuery);
                } else if (data.startsWith(AppConstant.CHANGE_SCREENSHOT_STATUS_DATA)) {
                    changeStatusScreenshot(callbackQuery);
                } else if (data.startsWith(AppConstant.CHANGE_CODE_STATUS_DATA)) {
                    changeStatusCode(callbackQuery);
                } else if (data.startsWith(AppConstant.BACK_TO_TARIFFS_DATA)) {
                    showTariffList(callbackQuery);
                } else if (data.equals(AppConstant.BACK_TO_BOT_LIST_DATA)) {
                    backToList(callbackQuery);
                } else if (data.startsWith(AppConstant.BACK_TO_BOT_INFO_DATA)) {
                    showBotInfo(callbackQuery);
                } else if (data.startsWith(AppConstant.START_STOP_BOT_DATA)) {
                    startStopBot(callbackQuery);
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

    private void changeTariffPrice(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        long tariffId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Tariff tariff = tariffRepository.findById(tariffId).orElseThrow();
        temp.addTempTariff(userId, tariff);
        commonUtils.setState(userId, StateEnum.SENDING_TARIFF_PRICE);
        sender.sendMessage(userId, responseText.getSendExpireText(tariff.getType().ordinal(), userId));
    }

    private void deleteTariff(CallbackQuery callbackQuery) {
        long tariffId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Tariff tariff = tariffRepository.findById(tariffId).orElseThrow();
        tariffRepository.delete(tariff);
        callbackQuery.setData(":" + tariff.getBotId());
        showTariffList(callbackQuery);
    }

    private void createTariff(CallbackQuery callbackQuery) {
        String[] split = callbackQuery.getData().split("\\+");
        int ordinal = Integer.parseInt(split[0].split(":")[1]);
        long botId = Long.parseLong(split[1]);
        Tariff tariff = new Tariff(botId, ExpireType.values()[ordinal], null);
        Long userId = callbackQuery.getFrom().getId();
        temp.addTempTariff(userId, tariff);
        commonUtils.setState(userId, StateEnum.SENDING_TARIFF_PRICE);
        sender.sendMessageAndRemove(userId, responseText.getSendExpireText(ordinal, userId));
    }

    private void addTariffs(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Long userId = callbackQuery.getFrom().getId();
        InlineKeyboardMarkup markup = responseButton.addTariff(userId, botId);
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), langService.getMessage(LangFields.SELECT_CHOOSE_TEXT, userId), markup);
    }

    private void showTariffInfo(CallbackQuery callbackQuery) {
        long tariffId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Long userId = callbackQuery.getFrom().getId();
        InlineKeyboardMarkup markup = responseButton.showTariffInfo(userId, tariffId);
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), langService.getMessage(LangFields.SELECT_CHOOSE_TEXT, userId), markup);
    }

    private void changeStatusClick(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.findById(botId).orElseThrow();
        if (group.isAllowPayment()) {
            group.setClick(!group.isClick());
            groupRepository.saveOptional(group);
            showPaymentsInfo(callbackQuery);
            return;
        }
        Long userId = callbackQuery.getFrom().getId();
        sender.sendMessage(userId, langService.getMessage(LangFields.NOT_ALLOWED_PAYMENT_TEXT, userId));

    }

    private void changeStatusPayme(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.findById(botId).orElseThrow();
        if (group.isAllowPayment()) {
            group.setPayme(!group.isPayme());
            groupRepository.saveOptional(group);
            showPaymentsInfo(callbackQuery);
            return;
        }
        Long userId = callbackQuery.getFrom().getId();
        sender.sendMessage(userId, langService.getMessage(LangFields.NOT_ALLOWED_PAYMENT_TEXT, userId));

    }

    private void changeStatusScreenshot(CallbackQuery callbackQuery) {

        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.findById(botId).orElseThrow();
        group.setScreenShot(!group.isScreenShot());
        groupRepository.saveOptional(group);
        showPaymentsInfo(callbackQuery);
    }

    private void changeStatusCode(CallbackQuery callbackQuery) {

        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.findById(botId).orElseThrow();
        group.setCode(!group.isCode());
        groupRepository.saveOptional(group);
        showPaymentsInfo(callbackQuery);
    }

    private void showPaymentsInfo(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Long userId = callbackQuery.getFrom().getId();
        InlineKeyboardMarkup markup = responseButton.showGroupPayments(userId, botId);
        String message = langService.getMessage(LangFields.PAYMENTS_LIST_TEXT, userId);
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), message, markup);
    }

    private void rejectScreenshot(CallbackQuery callbackQuery) {
        long userId = callbackQuery.getFrom().getId();
        long screenshotId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        ScreenshotGroup screenshotGroup = updateScreenshot(screenshotId, ScreenshotStatus.REJECT);
        String message = langService.getMessage(LangFields.REJECTED_SCREENSHOT_TEXT, userId);
        sender.changeCaption(userId, callbackQuery.getMessage().getMessageId(), message);

        AdminUtils adminUtils = adminController.getAdminUtils(userId);
        AdminSender adminSender = adminController.getSenderByAdminId(userId);
        Long sendUserId = screenshotGroup.getSendUserId();
        String userLang = adminUtils.getUserLang(sendUserId);
        String sendText = langService.getMessage(LangFields.SCREENSHOT_IS_INVALID_TEXT, userLang);
        adminSender.sendMessage(screenshotGroup.getSendUserId(), sendText);
    }

    private void acceptScreenshot(CallbackQuery callbackQuery) {
        long userId = callbackQuery.getFrom().getId();
        long screenshotId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        ScreenshotGroup screenshotGroup = updateScreenshot(screenshotId, ScreenshotStatus.ACCEPT);
        String message = langService.getMessage(LangFields.ACCEPTED_SCREENSHOT_TEXT, userId);
        sender.changeCaption(userId, callbackQuery.getMessage().getMessageId(), message);
        saveOrderAndSendLink(userId, screenshotGroup);

    }

    private void saveOrderAndSendLink(long userId, ScreenshotGroup screenshotGroup) {
        AdminUtils adminUtils = adminController.getAdminUtils(userId);
        AdminSender adminSender = adminController.getSenderByAdminId(userId);
        Long sendUserId = screenshotGroup.getSendUserId();
        String userLang = adminUtils.getUserLang(sendUserId);
        Optional<Order> optionalOrder = orderRepository.findByUserIdAndGroupId(sendUserId, screenshotGroup.getGroupId());
        if (optionalOrder.isPresent()) {
            Order order = updateOrderExpire(optionalOrder.get(), screenshotGroup.getType());
            orderRepository.save(order);
            String message = langService.getMessage(LangFields.SCREENSHOT_IS_VALID_TEXT, userLang) + adminSender.getLink(screenshotGroup.getGroupId());
            adminSender.sendMessage(sendUserId, message);
            return;
        }
        Order order = updateOrderExpire(new Order(), screenshotGroup.getType());
        order.setUserId(sendUserId);
        order.setGroupId(screenshotGroup.getGroupId());
        orderRepository.save(order);
        String message = langService.getMessage(LangFields.SCREENSHOT_IS_VALID_TEXT, userLang) + " -> " + adminSender.getLink(screenshotGroup.getGroupId());
        adminSender.sendMessage(sendUserId, message);
    }

    private ScreenshotGroup updateScreenshot(long id, ScreenshotStatus status) {
        ScreenshotGroup screenshotGroup = screenshotGroupRepository.findById(id).orElseThrow();
        screenshotGroup.setStatus(status);
        screenshotGroup.setActiveAt(LocalDateTime.now());
        screenshotGroupRepository.saveOptional(screenshotGroup);
        return screenshotGroup;
    }


    private void startStopBot(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.findById(botId).orElseThrow();
        group.setWorked(!group.isWorked());
        groupRepository.saveOptional(group);
        showBotInfo(callbackQuery);
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
            String message = langService.getMessage(LangFields.UN_CHECKED_SCREENSHOT_TEXT, userId)
                    .formatted(responseText.getTariffExpireText(screenshot.getType().ordinal(),
                            commonUtils.getUserLang(userId)), screenshot.getTariffPrice());
            sender.sendPhoto(userId, message, screenshot.getPath(), keyboard);
        }
    }

    private void generateCode(CallbackQuery callbackQuery) {
        long tariffId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Tariff tariff = tariffRepository.findById(tariffId).orElseThrow();
        Group group = groupRepository.findById(tariff.getBotId()).orElseThrow();
        if (!group.isCode()) {
            return;
        }
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
        InlineKeyboardMarkup markup = responseButton.showTariffs(botId, userId, AppConstant.SHOW_PRICE_INFO_DATA);
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
