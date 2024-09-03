package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.apptelegrambot.entity.*;
import uz.pdp.apptelegrambot.enums.ExpireType;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.enums.ScreenshotStatus;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.repository.*;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.AdminController;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;
import uz.pdp.apptelegrambot.service.owner.bot.OwnerSender;
import uz.pdp.apptelegrambot.utils.AppConstant;
import uz.pdp.apptelegrambot.utils.admin.AdminUtils;
import uz.pdp.apptelegrambot.utils.owner.CommonUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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
    private final ButtonService buttonService;

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
                } else if (data.startsWith(AppConstant.SHOW_ADMIN_ORDER_INFO_DATA)) {
                    showAdminOrderInfo(callbackQuery);
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
            case SENDING_CARD_NUMBER -> {
                if (data.startsWith(AppConstant.BACK_TO_BOT_INFO_DATA)) {
                    showBotInfo(callbackQuery);
                }
            }
            case SENDING_TARIFF_PRICE -> {
                if (data.startsWith(AppConstant.ADD_TARIFF_DATA)) {
                    addTariffs(callbackQuery);
                } else if (data.startsWith(AppConstant.SHOW_PRICE_INFO_DATA)) {
                    showTariffInfo(callbackQuery);
                }
            }

        }
    }

    private void showAdminOrderInfo(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.getByIdDefault(botId);
        LocalDateTime expireAt = group.getExpireAt();
        Long userId = callbackQuery.getFrom().getId();
        String userLang = commonUtils.getUserLang(group.getAdminId());
        if (expireAt.isAfter(LocalDateTime.now())) {
            String message = langService.getMessage(LangFields.ADMIN_PERMISSION_EXPIRED_TEXT, userLang);
            sender.sendMessage(userId, message, responseButton.backToBotInfo(userLang, botId));
            return;
        }
        LocalDate localDate = expireAt.toLocalDate();
        LocalTime localTime = expireAt.toLocalTime();
        String date = localDate.getYear() + "-" + localDate.getMonth().getValue() + "-" + localDate.getDayOfMonth();
        String time = localTime.getHour() + ":" + localTime.getMinute() + ":" + localTime.getSecond();
        String message = langService.getMessage(LangFields.ADMIN_PERMISSION_EXPIRE_AT_TEXT, userLang).formatted(date, time);
        sender.sendMessage(userId, message, responseButton.backToBotInfo(userLang, botId));

    }

    private void changeTariffPrice(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        long tariffId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Tariff tariff = tariffRepository.getById(tariffId);
        temp.addTempTariff(userId, tariff);
        temp.addTempBotId(userId, tariff.getBotId());
        commonUtils.setState(userId, StateEnum.SENDING_TARIFF_PRICE);
        InlineKeyboardMarkup keyboard = buttonService.callbackKeyboard(List.of(Map.of(langService.getMessage(LangFields.BACK_TEXT, commonUtils.getUserLang(userId)), AppConstant.SHOW_PRICE_INFO_DATA + tariffId)));
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), responseText.getSendExpireText(tariff.getType().ordinal(), commonUtils.getUserLang(userId)), keyboard);
    }

    private void deleteTariff(CallbackQuery callbackQuery) {
        long tariffId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Tariff tariff = tariffRepository.getById(tariffId);
        tariffRepository.deleteDefault(tariff);
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
        temp.addTempBotId(userId, botId);
        commonUtils.setState(userId, StateEnum.SENDING_TARIFF_PRICE);
        InlineKeyboardMarkup keyboard = buttonService.callbackKeyboard(List.of(Map.of(langService.getMessage(LangFields.BACK_TEXT, commonUtils.getUserLang(userId)), AppConstant.ADD_TARIFF_DATA + botId)));
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), responseText.getSendExpireText(ordinal, commonUtils.getUserLang(userId)), keyboard);
    }

    private void addTariffs(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Long userId = callbackQuery.getFrom().getId();
        commonUtils.setState(userId, StateEnum.START);
        InlineKeyboardMarkup markup = responseButton.addTariff(userId, botId);
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), langService.getMessage(LangFields.SELECT_CHOOSE_TEXT, commonUtils.getUserLang(userId)), markup);
    }

    private void showTariffInfo(CallbackQuery callbackQuery) {
        long tariffId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Long userId = callbackQuery.getFrom().getId();
        temp.clearTemp(userId);
        commonUtils.setState(userId, StateEnum.START);
        String userLang = commonUtils.getUserLang(userId);
        InlineKeyboardMarkup markup = responseButton.showTariffInfo(userLang, tariffId);
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), langService.getMessage(LangFields.INFO_TARIFF_LIST_TEXT, userLang).formatted(tariffRepository.getById(tariffId).getPrice()), markup);
    }

    private void changeStatusClick(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.getByIdDefault(botId);
        if (group.isAllowPayment()) {
            if (group.isCode() || group.isScreenShot() || group.isClick()) {
                group.setClick(!group.isClick());
                groupRepository.saveOptional(group);
                showPaymentsInfo(callbackQuery);
                return;
            }
            Long userId = callbackQuery.getFrom().getId();
            sender.sendMessage(userId, langService.getMessage(LangFields.ONE_PAYMENT_WILL_BE_ACTIVE_TEXT, commonUtils.getUserLang(userId)));
            return;
        }
        Long userId = callbackQuery.getFrom().getId();
        sender.sendMessage(userId, langService.getMessage(LangFields.NOT_ALLOWED_PAYMENT_TEXT, commonUtils.getUserLang(userId)));

    }

    private void changeStatusPayme(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.getByIdDefault(botId);
        if (group.isAllowPayment()) {
            if (group.isCode() || group.isScreenShot() || group.isClick()) {
                group.setPayme(!group.isPayme());
                groupRepository.saveOptional(group);
                showPaymentsInfo(callbackQuery);
                return;
            }
            Long userId = callbackQuery.getFrom().getId();
            sender.sendMessage(userId, langService.getMessage(LangFields.ONE_PAYMENT_WILL_BE_ACTIVE_TEXT, commonUtils.getUserLang(userId)));
            return;
        }
        Long userId = callbackQuery.getFrom().getId();
        sender.sendMessage(userId, langService.getMessage(LangFields.NOT_ALLOWED_PAYMENT_TEXT, commonUtils.getUserLang(userId)));

    }

    private void changeStatusScreenshot(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.getByIdDefault(botId);
        if (group.isCode() || group.isClick() || group.isPayme()) {
            group.setScreenShot(!group.isScreenShot());
            groupRepository.saveOptional(group);
            showPaymentsInfo(callbackQuery);
            return;
        }
        Long userId = callbackQuery.getFrom().getId();
        sender.sendMessage(userId, langService.getMessage(LangFields.ONE_PAYMENT_WILL_BE_ACTIVE_TEXT, commonUtils.getUserLang(userId)));
    }

    private void changeStatusCode(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.getByIdDefault(botId);
        if (group.isScreenShot() || group.isClick() || group.isPayme()) {
            group.setCode(!group.isCode());
            groupRepository.saveOptional(group);
            showPaymentsInfo(callbackQuery);
            return;
        }
        Long userId = callbackQuery.getFrom().getId();
        sender.sendMessage(userId, langService.getMessage(LangFields.ONE_PAYMENT_WILL_BE_ACTIVE_TEXT, commonUtils.getUserLang(userId)));
    }

    private void showPaymentsInfo(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Long userId = callbackQuery.getFrom().getId();
        String userLang = commonUtils.getUserLang(userId);
        InlineKeyboardMarkup markup = responseButton.showGroupPayments(userLang, botId);
        String message = langService.getMessage(LangFields.PAYMENTS_LIST_TEXT, userLang);
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), message, markup);
    }

    private void rejectScreenshot(CallbackQuery callbackQuery) {
        long userId = callbackQuery.getFrom().getId();
        String userLang = commonUtils.getUserLang(userId);
        long screenshotId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        ScreenshotGroup screenshotGroup = updateScreenshot(screenshotId, ScreenshotStatus.REJECT);
        String message = langService.getMessage(LangFields.REJECTED_SCREENSHOT_TEXT, userLang);
        AdminSender adminSender = adminController.getSenderByAdminId(userId);
        message = getUsernameAndId(adminSender, screenshotGroup, message);
        sender.changeCaption(userId, callbackQuery.getMessage().getMessageId(), message);

        AdminUtils adminUtils = adminController.getAdminUtils(userId);
        Long sendUserId = screenshotGroup.getSendUserId();
        String adminUsersLang = adminUtils.getUserLang(sendUserId);
        String sendText = langService.getMessage(LangFields.SCREENSHOT_IS_INVALID_TEXT, adminUsersLang);
        sendText = sendText + " " + commonUtils.getUser(userId).getContactNumber();
        adminSender.sendMessage(screenshotGroup.getSendUserId(), sendText);
    }

    private String getUsernameAndId(AdminSender adminSender, ScreenshotGroup screenshotGroup, String message) {
        Chat chat = adminSender.getChat(screenshotGroup.getSendUserId());
        if (chat.getUserName() != null) {
            message = message + "@" + chat.getUserName();
        }
        message = message + "\n#" + chat.getId();
        return message;
    }

    private void acceptScreenshot(CallbackQuery callbackQuery) {
        long userId = callbackQuery.getFrom().getId();
        long screenshotId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        ScreenshotGroup screenshotGroup = updateScreenshot(screenshotId, ScreenshotStatus.ACCEPT);
        String message = langService.getMessage(LangFields.ACCEPTED_SCREENSHOT_TEXT, commonUtils.getUserLang(userId));
        AdminSender adminSender = adminController.getSenderByAdminId(userId);
        message = getUsernameAndId(adminSender, screenshotGroup, message);
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
        ScreenshotGroup screenshotGroup = screenshotGroupRepository.getById(id);
        screenshotGroup.setStatus(status);
        screenshotGroup.setActiveAt(LocalDateTime.now());
        screenshotGroupRepository.saveOptional(screenshotGroup);
        return screenshotGroup;
    }


    private void startStopBot(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.getByIdDefault(botId);
        group.setWorked(!group.isWorked());
        groupRepository.saveOptional(group);
        showBotInfo(callbackQuery);
    }

    private void addCardNumber(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        temp.addTempGroup(groupRepository.findById(botId).orElseThrow());
        temp.addTempBotId(userId, botId);
        String userLang = commonUtils.getUserLang(userId);
        String message = langService.getMessage(LangFields.SEND_CARD_NUMBER_TEXT, userLang);
        InlineKeyboardMarkup keyboard = buttonService.callbackKeyboard(List.of(Map.of(langService.getMessage(LangFields.BACK_TEXT, userLang), AppConstant.BACK_TO_BOT_INFO_DATA + botId)));
        sender.changeTextAndKeyboard(userId, messageId, message, keyboard);
        commonUtils.setState(userId, StateEnum.SENDING_CARD_NUMBER);
    }

    private void showScreenshots(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.getByIdDefault(botId);
        String userLang = commonUtils.getUserLang(userId);
        if (group.getGroupId() == null) {
            sender.sendMessage(userId, langService.getMessage(LangFields.FIRST_ADD_GROUP_TEXT, userLang));
            return;
        }
        List<ScreenshotGroup> screenshots = screenshotGroupRepository.findAllByGroupIdAndStatus(group.getGroupId(), ScreenshotStatus.DONT_SEE);
        if (screenshots.isEmpty()) {
            sender.sendMessage(userId, langService.getMessage(LangFields.SCREENSHOTS_EMPTY_TEXT, userLang));
            return;
        }
        AdminSender adminSender = adminController.getSenderByAdminId(group.getAdminId());
        for (ScreenshotGroup screenshot : screenshots) {
            Long screenshotId = screenshot.getId();
            InlineKeyboardMarkup keyboard = responseButton.screenshotsKeyboard(userLang, screenshotId);
            String message = langService.getMessage(LangFields.UN_CHECKED_SCREENSHOT_TEXT, userLang)
                    .formatted(responseText.getTariffExpireText(screenshot.getType().ordinal(),
                            userLang), screenshot.getTariffPrice()) + "\n\n";
            message = getUsernameAndId(adminSender, screenshot, message);
            sender.sendPhoto(userId, message, screenshot.getPath(), keyboard);
        }
    }

    private void generateCode(CallbackQuery callbackQuery) {
        long tariffId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Tariff tariff = tariffRepository.getById(tariffId);
        Group group = groupRepository.findById(tariff.getBotId()).orElseThrow();
        if (!group.isCode()) {
            return;
        }
        Long userId = callbackQuery.getFrom().getId();
        CodeGroup codeGroup = new CodeGroup(generateCode(), tariff.getBotId(), null, tariff.getType(), false, null, tariffId, tariff.getPrice());
        codeGroupRepository.save(codeGroup);
        String message = getCodeText(commonUtils.getUserLang(userId), tariff.getType().ordinal());
        sender.sendMessageWithMarkdown(userId, message + "`" + codeGroup.getCode() + "`");
    }

    private String getCodeText(String userLang, int ordinal) {
        if (ordinal == 0) {
            return langService.getMessage(LangFields.CODE_WEEK_TEXT, userLang);
        } else if (ordinal == 1) {
            return langService.getMessage(LangFields.CODE_DAY15_TEXT, userLang);
        } else if (ordinal == 2) {
            return langService.getMessage(LangFields.CODE_MONTH_TEXT, userLang);
        } else if (ordinal == 3) {
            return langService.getMessage(LangFields.CODE_YEAR_TEXT, userLang);
        } else if (ordinal == 4) {
            return langService.getMessage(LangFields.CODE_UNLIMITED_TEXT, userLang);
        }
        return null;
    }

    private void showTariffsForGenerateCode(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        long userId = callbackQuery.getFrom().getId();
        if (tariffRepository.findAllByBotIdDefault(botId).isEmpty()) {
            sender.sendMessage(userId, langService.getMessage(LangFields.DONT_HAVE_ANY_TARIFF_TEXT, commonUtils.getUserLang(userId)));
            return;
        }
        InlineKeyboardMarkup markup = responseButton.showTariffs(botId, userId, AppConstant.GENERATE_CODE_FOR_TARIFF_DATA);
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        if (keyboard.size() != 7) {
            keyboard.remove(keyboard.size() - 2);
            markup.setKeyboard(keyboard);
        }
        String message = langService.getMessage(LangFields.SELECT_CHOOSE_TEXT, commonUtils.getUserLang(userId));
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), message, markup);
    }

    private void showTariffList(CallbackQuery callbackQuery) {
        long userId = callbackQuery.getFrom().getId();
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        InlineKeyboardMarkup markup = responseButton.showTariffs(botId, userId, AppConstant.SHOW_PRICE_INFO_DATA);
        String message = langService.getMessage(LangFields.SELECT_CHOOSE_TEXT, commonUtils.getUserLang(userId));
        if (markup.getKeyboard().size() == 2)
            message = langService.getMessage(LangFields.SELECT_CHOOSE_EMPTY_TEXT, commonUtils.getUserLang(userId));
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), message, markup);
    }

    private void backToList(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        InlineKeyboardMarkup markup = responseButton.botsList(userId);
        String message = langService.getMessage(LangFields.SELECT_CHOOSE_BOT_TEXT, commonUtils.getUserLang(userId));
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), message, markup);
    }

    private void showBotInfo(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.getByIdDefault(botId);
        Long userId = callbackQuery.getFrom().getId();
        commonUtils.setState(userId, StateEnum.START);
        temp.clearTemp(userId);
        String userLang = commonUtils.getUserLang(userId);
        String message = langService.getMessage(LangFields.BOT_INFO_TEXT, userLang).formatted(group.getBotUsername(), group.getName());
        if (group.getName() == null || group.getName().isEmpty() || group.getName().isBlank())
            message = langService.getMessage(LangFields.BOT_INFO_NULL_TEXT, userLang).formatted(group.getBotUsername());
        InlineKeyboardMarkup markup = responseButton.botInfo(botId, userLang);
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
        List<Tariff> tempTariffs = temp.getTempTariffs(userId);
        String userLang = commonUtils.getUserLang(userId);
        if (tempTariffs.isEmpty()) {
            sender.sendMessage(userId, langService.getMessage(LangFields.NOT_ANY_TARIFF_TEXT, userLang));
            return;
        }
        sender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
        commonUtils.setState(userId, StateEnum.SENDING_TARIFF_PRICE);
        tempTariffs.sort(Comparator.comparing(t -> t.getType().ordinal()));
        List<Integer> list = tempTariffs.stream().map(Tariff::getType).map(ExpireType::ordinal).toList();
        Integer i = list.get(0);
        String expireText = responseText.getSendExpireText(i, userLang);
        sender.sendMessage(userId, expireText);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(1, 9));
        }
        return sb.toString();
    }
}
