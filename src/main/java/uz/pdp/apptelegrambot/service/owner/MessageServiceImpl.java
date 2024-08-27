package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.entity.User;
import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.repository.UserRepository;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.AdminController;
import uz.pdp.apptelegrambot.service.owner.bot.OwnerSender;
import uz.pdp.apptelegrambot.utils.owner.CommonUtils;

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
                        if (checkStrings(text, LangFields.BUTTON_LANG_SETTINGS, userId)) {
                            selectLanguage(userId);
                        } else if (checkStrings(text, LangFields.BUTTON_CONTACT_US, userId)) {
                            contactUs(userId);
                        } else if (checkStrings(text, LangFields.BUTTON_ADD_BOT, userId)) {
                            sendAddBotText(userId, user);
                        }
                    }
                    case SELECT_LANGUAGE -> changeLanguage(text, userId);
                    case SENDING_BOT_TOKEN -> setToken(text, userId);
                    default ->
                            sender.sendMessage(userId, langService.getMessage(LangFields.EXCEPTION_BUTTON, userId), responseButton.start(userId));

                }


            }
            if (message.hasContact()) {
                if (user.getState().equals(StateEnum.SENDING_CONTACT_NUMBER)) {
                    setUserContactNumber(message);
                }
            }
        }
    }

    private void setUserContactNumber(Message message) {
        Long userId = message.getFrom().getId();
        if (message.getContact().getUserId().equals(message.getChat().getId())) {
            String phoneNumber = message.getContact().getPhoneNumber();
            User user = commonUtils.getUser(userId);
            user.setContactNumber(phoneNumber);
            userRepository.saveOptional(user);
            sendAddBotText(user.getId(), user);
            return;
        }
        sender.sendMessage(userId, langService.getMessage(LangFields.SEND_YOUR_PHONE_NUMBER_TEXT, userId), responseButton.contactNumber(userId));
    }

    private void setToken(String text, Long userId) {
        if (!text.matches("^[0-9]{8,10}:[a-zA-Z0-9_-]{35}$")) {
            sender.sendMessage(userId, langService.getMessage(LangFields.SEND_VALID_BOT_TOKEN_TEXT, userId));
            return;
        }
        if (groupRepository.findByBotToken(text).isPresent()) {
            sender.sendMessage(userId, langService.getMessage(LangFields.ALREADY_TOKEN_SEND_US_TEXT, userId), responseButton.start(userId));
            return;
        }
        Group group = new Group();
        group.setAdminId(userId);
        group.setBotToken(text);
        group.setCode(true);
        group.setScreenShot(true);
        temp.addTempGroup(group);
        adminController.addAdminBot(text, userId);
        commonUtils.setState(userId, StateEnum.SELECTING_TARIFF);
        Group savedGroup = groupRepository.findByBotToken(text).orElseThrow();
        InlineKeyboardMarkup markup = responseButton.tariffList(savedGroup.getId(), userId);
        sender.sendMessage(userId, langService.getMessage(LangFields.SELECT_TARIFF_TEXT, userId), markup);
    }


    private void sendAddBotText(Long userId, User user) {
        String contactNumber = user.getContactNumber();
        if (contactNumber == null || contactNumber.isEmpty() || contactNumber.isBlank()) {
            commonUtils.setState(userId, StateEnum.SENDING_CONTACT_NUMBER);
            sender.sendMessage(userId, langService.getMessage(LangFields.SEND_CONTACT_NUMBER_TEXT, userId), responseButton.contactNumber(userId));
            return;
        }
        commonUtils.setState(userId, StateEnum.SENDING_BOT_TOKEN);
        String message = langService.getMessage(LangFields.SEND_BOT_TOKEN_TEXT, userId);
        sender.sendMessageAndRemove(userId, message);
    }

    private boolean checkStrings(String text, LangFields field, Long userId) {
        return text.equals(langService.getMessage(field, userId));
    }

    private void contactUs(Long userId) {
        String message = langService.getMessage(LangFields.CONTACT_US_TEXT, userId);
        sender.sendMessage(userId, message);
    }

    private void changeLanguage(String text, Long userId) {
        LangEnum lang = langService.getLanguageEnum(text);
        if (lang == null) {
            sender.sendMessage(userId, langService.getMessage(LangFields.EXCEPTION_LANGUAGE, userId), responseButton.start(userId));
            return;
        }
        commonUtils.setLang(userId, lang);
        commonUtils.setState(userId, StateEnum.START);
        sender.sendMessage(userId, langService.getMessage(LangFields.SUCCESSFULLY_CHANGED_LANGUAGE, lang.name()), responseButton.start(userId));
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
        String text = langService.getMessage(LangFields.HELLO, userId);
        sender.sendMessage(userId, text, responseButton.start(userId));
    }
}
