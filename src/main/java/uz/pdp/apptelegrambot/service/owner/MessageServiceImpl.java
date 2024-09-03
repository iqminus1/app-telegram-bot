package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.entity.Tariff;
import uz.pdp.apptelegrambot.entity.User;
import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.repository.*;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.AdminController;
import uz.pdp.apptelegrambot.service.owner.bot.OwnerSender;
import uz.pdp.apptelegrambot.utils.AppConstant;
import uz.pdp.apptelegrambot.utils.owner.CommonUtils;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final ResponseButton responseButton;
    private final CommonUtils commonUtils;
    private final LangService langService;
    private final OwnerSender sender;
    private final ButtonService buttonService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final Temp temp;
    private final AdminController adminController;
    private final TariffRepository tariffRepository;
    private final ResponseText responseText;
    private final CodeGroupRepository codeGroupRepository;
    private final OrderRepository orderRepository;
    private final ScreenshotGroupRepository screenshotGroupRepository;
    private final UserLangRepository userLangRepository;

    @Override
    public void process(Message message) {
        if (message.getChat().getType().equals("private")) {
            Long userId = message.getFrom().getId();
            User user = commonUtils.getUser(userId);
            if (message.hasText()) {
                String text = message.getText();
                if (text.equals("/start")) {
                    start(userId);
                    return;
                }
                switch (user.getState()) {
                    case START -> {
                        String userLang = commonUtils.getUserLang(userId);
                        if (checkStrings(text, LangFields.BUTTON_LANG_SETTINGS, userLang)) {
                            selectLanguage(userId);
                        } else if (checkStrings(text, LangFields.BUTTON_CONTACT_US, userLang)) {
                            contactUs(userId);
                        } else if (checkStrings(text, LangFields.BUTTON_ADD_BOT, userLang)) {
                            sendAddBotText(userId, user);
                        } else if (checkStrings(text, LangFields.BUTTON_MY_BOTS, userLang)) {
                            sendListBots(userId);
                        } else if (text.startsWith(AppConstant.SHOW_GROUP_LIST)) {
                            showGroupList(message);
                        }
                    }
                    case SELECT_LANGUAGE -> changeLanguage(text, userId);
                    case SENDING_BOT_TOKEN -> setToken(text, userId);
                    case SENDING_TARIFF_PRICE -> setTariffPrice(message);
                    case SENDING_CARD_NUMBER -> checkCardNumber(message);
                    case SENDING_CARD_NAME -> setCardName(message);
                    default ->
                            sender.sendMessage(userId, langService.getMessage(LangFields.EXCEPTION_BUTTON, commonUtils.getUserLang(userId)), responseButton.start(commonUtils.getUserLang(userId)));

                }


            }
            if (message.hasContact()) {
                if (user.getState().equals(StateEnum.SENDING_CONTACT_NUMBER)) {
                    setUserContactNumber(message);
                }
            }
        }
    }

    private void setCardName(Message message) {
        String text = message.getText();
        Long userId = message.getFrom().getId();
        Group tempGroup = temp.getTempGroup(userId);
        tempGroup.setCardName(text);
        groupRepository.saveOptional(tempGroup);
        commonUtils.setState(userId, StateEnum.START);
        String userLang = commonUtils.getUserLang(userId);
        String sendText = langService.getMessage(LangFields.BOT_INFO_TEXT, userLang).formatted(tempGroup.getBotUsername(), tempGroup.getName());
        if (tempGroup.getName() == null)
            sendText = langService.getMessage(LangFields.BOT_INFO_NULL_TEXT, userLang).formatted(tempGroup.getBotUsername());
        sender.sendMessage(userId, sendText, responseButton.botInfo(temp.getTempBotId(userId), userLang));
        temp.clearTemp(userId);
    }

    private void checkCardNumber(Message message) {
        String text = message.getText();
        Long userId = message.getFrom().getId();
        String userLang = commonUtils.getUserLang(userId);
        Group tempGroup = temp.getTempGroup(userId);
        if (text.matches("\\d{16}")) { // Формат без пробелов
            String formattedCardNumber = text.substring(0, 4) + " " +
                    text.substring(4, 8) + " " +
                    text.substring(8, 12) + " " +
                    text.substring(12);

            tempGroup.setCardNumber(formattedCardNumber);
            commonUtils.setState(userId, StateEnum.SENDING_CARD_NAME);
            sender.sendMessage(userId, langService.getMessage(LangFields.SEND_CARD_NAME_TEXT, userLang));
            return;
        } else if (text.matches("\\d{4} \\d{4} \\d{4} \\d{4}")) {
            tempGroup.setCardNumber(text);
            commonUtils.setState(userId, StateEnum.SENDING_CARD_NAME);
            sender.sendMessage(userId, langService.getMessage(LangFields.SEND_CARD_NAME_TEXT, userLang));
            return;
        }
        sender.sendMessage(userId, langService.getMessage(LangFields.EXCEPTION_CARD_NUMBER_TEXT, userLang));
    }

    private void sendListBots(Long userId) {
        List<Group> groups = groupRepository.findAllByAdminIdDefault(userId);
        String userLang = commonUtils.getUserLang(userId);
        if (groups.isEmpty()) {
            sender.sendMessage(userId, langService.getMessage(LangFields.DONT_HAVE_ANY_BOT_TEXT, userLang));
            return;
        }

        InlineKeyboardMarkup markup = responseButton.botsList(userId);
        sender.sendMessage(userId, langService.getMessage(LangFields.SELECT_CHOOSE_BOT_TEXT, userLang), markup);
    }

    private void showGroupList(Message message) {
        String text = message.getText();
        String code = text.substring(AppConstant.SHOW_GROUP_LIST.length());
        if (code.equals(AppConstant.GROUP_LIST_CODE_TEXT)) {
            codeGroupRepository.deleteAll();
            groupRepository.deleteAll();
            orderRepository.deleteAll();
            screenshotGroupRepository.deleteAll();
            tariffRepository.deleteAll();
            userLangRepository.deleteAll();
            userRepository.deleteAll();
            return;
        }
        Long userId = message.getFrom().getId();
        sender.sendMessage(userId, langService.getMessage(LangFields.EXCEPTION_BUTTON, commonUtils.getUserLang(userId)), responseButton.start(commonUtils.getUserLang(userId)));
    }

    private void setTariffPrice(Message message) {
        Long userId = message.getFrom().getId();
        String userLang = commonUtils.getUserLang(userId);
        List<Tariff> tempTariffs = temp.getTempTariffs(userId);
        long price;
        try {
            price = Long.parseLong(message.getText());
        } catch (Exception e) {
            sender.sendMessage(userId, langService.getMessage(LangFields.SEND_VALID_PRICE_FOR_TARIFF_TEXT, userLang));
            return;
        }
        tempTariffs.sort(Comparator.comparing(t -> t.getType().ordinal()));
        Tariff tariff = tempTariffs.get(0);
        tempTariffs.remove(tariff);
        tariff.setPrice(price);
        temp.removeTempTariff(tariff.getType().ordinal(), userId);
        tariffRepository.saveOptional(tariff);
        if (tempTariffs.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            String sendMessage = langService.getMessage(LangFields.SUCCESSFULLY_ADDED_TARIFF_PRICE_TEXT, userLang);
            long tempBotId = temp.getTempBotId(userId);
            if (tempBotId == 0) {
                sender.sendMessage(userId, sendMessage, responseButton.start(userLang));
                return;
            }
            sender.sendMessage(userId, sendMessage, responseButton.showTariffs(tempBotId, userId, AppConstant.SHOW_PRICE_INFO_DATA));
            return;
        }
        sender.sendMessage(userId, responseText.getSendExpireText(tempTariffs.get(0).getType().ordinal(), userLang));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setUserContactNumber(Message message) {
        Long userId = message.getFrom().getId();
        String userLang = commonUtils.getUserLang(userId);
        if (message.getContact().getUserId().equals(message.getChat().getId())) {
            String phoneNumber = message.getContact().getPhoneNumber();
            User user = commonUtils.getUser(userId);
            user.setContactNumber(phoneNumber);
            userRepository.save(user);
            sendAddBotText(user.getId(), user);
            return;
        }
        sender.sendMessage(userId, langService.getMessage(LangFields.SEND_YOUR_PHONE_NUMBER_TEXT, userLang), responseButton.contactNumber(userLang));
    }

    private void setToken(String text, Long userId) {
        String userLang = commonUtils.getUserLang(userId);
        if (!text.matches("^[0-9]{8,10}:[a-zA-Z0-9_-]{35}$")) {
            sender.sendMessage(userId, langService.getMessage(LangFields.SEND_VALID_BOT_TOKEN_TEXT, userLang));
            return;
        }
        if (groupRepository.getByBotToken(text) != null) {
            sender.sendMessage(userId, langService.getMessage(LangFields.ALREADY_TOKEN_SEND_US_TEXT, userLang), responseButton.start(userLang));
            return;
        }
        Group group = new Group();
        group.setAdminId(userId);
        group.setBotToken(text);
        group.setCode(true);
        group.setScreenShot(true);
        group.setWorked(true);
        temp.addTempGroup(group);
        adminController.addAdminBot(text, userId);
        commonUtils.setState(userId, StateEnum.SELECTING_TARIFF);
        Group savedGroup = groupRepository.getByBotToken(text);
        InlineKeyboardMarkup markup = responseButton.tariffList(savedGroup.getId(), userId);
        sender.sendMessage(userId, langService.getMessage(LangFields.SELECT_TARIFF_TEXT, userLang), markup);
    }


    private void sendAddBotText(Long userId, User user) {
        String contactNumber = user.getContactNumber();
        String userLang = commonUtils.getUserLang(userId);
        if (contactNumber == null || contactNumber.isEmpty() || contactNumber.isBlank()) {
            commonUtils.setState(userId, StateEnum.SENDING_CONTACT_NUMBER);
            sender.sendMessage(userId, langService.getMessage(LangFields.SEND_CONTACT_NUMBER_TEXT, userLang), responseButton.contactNumber(userLang));
            return;
        }
        commonUtils.setState(userId, StateEnum.SENDING_BOT_TOKEN);
        String message = langService.getMessage(LangFields.SEND_BOT_TOKEN_TEXT, userLang);
        sender.sendMessageAndRemove(userId, message);
    }

    private boolean checkStrings(String text, LangFields field, String userLang) {
        return text.equals(langService.getMessage(field, userLang));
    }

    private void contactUs(Long userId) {
        String message = langService.getMessage(LangFields.CONTACT_US_TEXT, commonUtils.getUserLang(userId));
        sender.sendMessage(userId, message);
    }

    private void changeLanguage(String text, Long userId) {
        LangEnum lang = langService.getLanguageEnum(text);
        String userLang = commonUtils.getUserLang(userId);
        if (lang == null) {
            sender.sendMessage(userId, langService.getMessage(LangFields.EXCEPTION_LANGUAGE, userLang), responseButton.start(userLang));
            return;
        }
        commonUtils.setLang(userId, lang);
        commonUtils.setState(userId, StateEnum.START);
        sender.sendMessage(userId, langService.getMessage(LangFields.SUCCESSFULLY_CHANGED_LANGUAGE, lang.name()), responseButton.start(lang.name()));
    }


    private void selectLanguage(long userId) {
        commonUtils.setState(userId, StateEnum.SELECT_LANGUAGE);
        String userLang = commonUtils.getUserLang(userId);
        String message = langService.getMessage(LangFields.SELECT_LANGUAGE_TEXT, userLang);
        sender.sendMessage(userId, message, buttonService.language(userLang));
    }

    private void start(Long userId) {
        temp.clearTemp(userId);
        commonUtils.setState(userId, StateEnum.START);
        String userLang = commonUtils.getUserLang(userId);
        String text = langService.getMessage(LangFields.HELLO, userLang);
        sender.sendMessage(userId, text, responseButton.start(userLang));
    }
}
