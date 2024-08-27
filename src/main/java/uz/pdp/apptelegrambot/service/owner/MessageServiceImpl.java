package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.apptelegrambot.entity.User;
import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;
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
                        if (text.equals(langService.getMessage(LangFields.BUTTON_LANG_SETTINGS, userId))) {
                            selectLanguage(userId);
                        } else if (text.equals(langService.getMessage(LangFields.BUTTON_CONTACT_US, userId))) {
                            contactUs(userId);
                        }
                    }
                    case SELECT_LANGUAGE -> changeLanguage(text, userId);
                    default ->
                            sender.sendMessage(userId, langService.getMessage(LangFields.EXCEPTION_BUTTON, userId), responseButton.start(userId));

                }


            }
        }
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
        commonUtils.setState(userId, StateEnum.START);
        String text = langService.getMessage(LangFields.HELLO, userId);
        sender.sendMessage(userId, text, responseButton.start(userId));
    }
}
