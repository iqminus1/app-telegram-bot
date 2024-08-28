package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import uz.pdp.apptelegrambot.entity.CodeGroup;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.entity.Order;
import uz.pdp.apptelegrambot.entity.ScreenshotGroup;
import uz.pdp.apptelegrambot.enums.ExpireType;
import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.repository.CodeGroupRepository;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.repository.OrderRepository;
import uz.pdp.apptelegrambot.repository.ScreenshotGroupRepository;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;
import uz.pdp.apptelegrambot.service.admin.sceduled.AdminResponseText;
import uz.pdp.apptelegrambot.utils.AppConstant;
import uz.pdp.apptelegrambot.utils.admin.AdminUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
public class AdminMessageServiceImpl implements AdminMessageService {
    private final AdminSender sender;
    private final LangService langService;
    private final AdminUtils adminUtils;
    private final AdminResponseButton responseButton;
    private final GroupRepository groupRepository;
    private final ButtonService buttonService;
    private final CodeGroupRepository codeGroupRepository;
    private final OrderRepository orderRepository;
    private final AdminResponseText responseText;
    private final AdminTemp temp;
    private final ScreenshotGroupRepository screenshotGroupRepository;
    private final String token;

    @Override
    public void process(Message message) {
        if (message.getChat().getType().equals("private")) {
            if (message.hasText()) {
                String text = message.getText();
                Long userId = message.getChat().getId();
                String userLang = adminUtils.getUserLang(userId);
                StateEnum state = adminUtils.getUserState(userId);
                if (text.equals("/start")) {
                    start(userId);
                    return;
                }
                switch (state) {
                    case START -> {
                        if (text.equals(langService.getMessage(LangFields.BUTTON_LANG_SETTINGS, userLang))) {
                            selectLanguage(userId);
                        } else if (text.equals(langService.getMessage(LangFields.BUTTON_ADMIN_PAYMENT_PAYME_TEXT, userLang))) {
                            sendPaymePaymentLink(userId, userLang);
                        } else if (text.equals(langService.getMessage(LangFields.BUTTON_ADMIN_PAYMENT_CLICK_TEXT, userLang))) {
                            sendClickPaymentLink(userId, userLang);
                        } else if (text.equals(langService.getMessage(LangFields.BUTTON_ADMIN_PAYMENT_CODE_TEXT, userLang))) {
                            sendCodeText(userId, userLang);
                        } else if (text.equals(langService.getMessage(LangFields.BUTTON_ADMIN_PAYMENT_SCREENSHOT_TEXT, userLang))) {
                            checkAndSendTariffs(userId, userLang, AppConstant.SCREENSHOT);
                        }
                    }
                    case SELECT_LANGUAGE -> changeLanguage(text, userId, userLang);
                    case SENDING_JOIN_REQ_CODE -> checkCode(text, userId, userLang);
                    default ->
                            sender.sendMessage(userId, langService.getMessage(LangFields.EXCEPTION_BUTTON, userLang), responseButton.start(sender.getGroup().getId(), userLang));
                }


            } else if (message.hasPhoto()) {
                Long userId = message.getFrom().getId();
                ScreenshotGroup tempScreenshot = temp.getTempScreenshot(userId);
                List<PhotoSize> photo = message.getPhoto();
                PhotoSize photoSize = photo.stream().max(Comparator.comparing(PhotoSize::getFileSize)).get();
                String filePath = sender.getFilePath(photoSize);
                tempScreenshot.setPath(filePath);

                screenshotGroupRepository.saveOptional(tempScreenshot);

                adminUtils.setUserState(userId, StateEnum.START);
                sender.sendMessage(userId, langService.getMessage(LangFields.SUCCESSFULLY_GETTING_SCREENSHOT_TEXT, adminUtils.getUserLang(userId)), null);
            }
        }
    }

    private void sendPaymePaymentLink(Long userId, String userLang) {
        String link = "https://payme.uz/";
        Map<String, String> paymentData = responseText.getPaymentData(userLang, sender.getGroup().getId(), userId);
        if (paymentData == null) {
            return;
        }
        Base64.Encoder encoder = Base64.getEncoder();
        paymentData.forEach((text, data) -> {
            byte[] encode = encoder.encode(("perId=1/payment=1/amount=2000" + data).getBytes(StandardCharsets.UTF_8));
            paymentData.put(text, link + new String(encode));
        });
        sender.sendMessage(userId, langService.getMessage(LangFields.SELECT_CHOOSE_TARIFF_FOR_JOIN_TEXT, userLang), responseButton.callbackWithLink(paymentData));
    }

    private void sendClickPaymentLink(Long userId, String userLang) {
        String link = "https://click.uz/perId=1/payment=1/amount=2000";
        Map<String, String> paymentData = responseText.getPaymentData(userLang, sender.getGroup().getId(), userId);
        if (paymentData == null) {
            return;
        }
        paymentData.forEach((text, data) -> {
            paymentData.put(text, link + data);
        });
        sender.sendMessage(userId, langService.getMessage(LangFields.SELECT_CHOOSE_TARIFF_FOR_JOIN_TEXT, userLang), responseButton.callbackWithLink(paymentData));
    }

    private void checkCode(String text, Long userId, String userLang) {
        Group group = groupRepository.findByBotToken(token).orElseThrow();
        if (group.isScreenShot()) {
            adminUtils.setUserState(userId, StateEnum.START);
            sender.sendMessage(userId, langService.getMessage(LangFields.SECTION_DONT_WORK_TEXT, userLang), responseButton.start(group.getId(), userLang));
            return;
        }
        Optional<CodeGroup> optionalCodeGroup = codeGroupRepository.findByCodeAndBotId(text, group.getId());
        if (optionalCodeGroup.isEmpty()) {
            adminUtils.setUserState(userId, StateEnum.START);
            sender.sendMessage(userId, langService.getMessage(LangFields.JOIN_REQ_CODE_INVALID_TEXT, userLang), responseButton.start(group.getId(), userLang));
            return;
        }
        CodeGroup codeGroup = optionalCodeGroup.get();
        Optional<Order> optionalOrder = orderRepository.findByUserIdAndGroupId(userId, group.getGroupId());
        if (optionalOrder.isPresent()) {
            Order order = updateOrderExpire(optionalOrder.get(), codeGroup);
            orderRepository.save(order);
        }
        Order order = updateOrderExpire(new Order(), codeGroup);
        order.setUserId(userId);
        order.setGroupId(group.getGroupId());
        orderRepository.save(order);
        adminUtils.setUserState(userId, StateEnum.START);
        sender.sendMessage(userId, langService.getMessage(LangFields.JOIN_REQ_CODE_VALID_TEXT, userLang) + " -> " + sender.getLink(group.getGroupId()), responseButton.start(group.getId(), userLang));
    }

    private static Order updateOrderExpire(Order order, CodeGroup codeGroup) {
        ExpireType type = codeGroup.getType();
        if (type == ExpireType.WEEK)
            order.setExpireDay(order.getExpireDay().plusWeeks(1L));
        if (type == ExpireType.DAY_15)
            order.setExpireDay(order.getExpireDay().plusDays(15L));
        if (type == ExpireType.MONTH)
            order.setExpireDay(order.getExpireDay().plusMonths(1L));
        if (type == ExpireType.YEAR)
            order.setExpireDay(order.getExpireDay().plusYears(1L));
        if (type == ExpireType.UNLIMITED) {
            order.setExpireDay(LocalDateTime.MAX);
            order.setUnlimited(true);
        }
        return order;
    }

    private void sendCodeText(Long userId, String userLang) {
        adminUtils.setUserState(userId, StateEnum.SENDING_JOIN_REQ_CODE);
        sender.sendMessageAndRemove(userId, langService.getMessage(LangFields.SEND_CODE_FOR_JOIN_REQ_TEXT, userLang));
    }

    private void checkAndSendTariffs(Long userId, String userLang, String data) {
        Group group = groupRepository.findByBotToken(token).orElseThrow();
        if (!group.isScreenShot())
            return;


        InlineKeyboardMarkup markup = responseButton.tariffList(group.getId(), data, userLang);
        sender.deleteKeyboard(userId);
        sender.sendMessage(userId, langService.getMessage(LangFields.SELECT_CHOOSE_TARIFF_FOR_JOIN_TEXT, userLang), markup);
    }

    private void changeLanguage(String text, Long userId, String userLang) {
        LangEnum lang = langService.getLanguageEnum(text);

        Long botId = sender.getGroup().getId();
        if (lang == null) {
            sender.sendMessage(userId, langService.getMessage(LangFields.EXCEPTION_LANGUAGE, userLang), responseButton.start(botId, userLang));
            return;
        }
        adminUtils.setUserLang(userId, lang.name());
        adminUtils.setUserState(userId, StateEnum.START);
        sender.sendMessage(userId, langService.getMessage(LangFields.SUCCESSFULLY_CHANGED_LANGUAGE, lang.name()), responseButton.start(botId, lang.name()));
    }


    private void selectLanguage(long userId) {
        adminUtils.setUserState(userId, StateEnum.SELECT_LANGUAGE);
        String userLang = adminUtils.getUserLang(userId);
        String message = langService.getMessage(LangFields.SELECT_LANGUAGE_TEXT, userLang);
        sender.sendMessage(userId, message, buttonService.language(userLang));
    }

    private void start(Long userId) {
        long botId = sender.getGroup().getId();
        ;
        String userLang = adminUtils.getUserLang(userId);
        String message = langService.getMessage(LangFields.HELLO, userLang);
        ReplyKeyboard start = responseButton.start(botId, userLang);
        sender.sendMessage(userId, message, start);
        temp.clearTemp(userId);
    }

}
