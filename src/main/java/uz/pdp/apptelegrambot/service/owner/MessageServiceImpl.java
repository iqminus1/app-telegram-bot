package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.pdp.apptelegrambot.entity.User;
import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.owner.bot.OwnerSender;
import uz.pdp.apptelegrambot.utils.CommonUtils;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final ResponseButton responseButton;
    private final CommonUtils commonUtils;
    private final LangService langService;
    private final OwnerSender sender;

    @Override
    public void process(Message message) {
        if (message.getChat().getType().equals("private")) {
            Long userId = message.getFrom().getId();
            User user = commonUtils.getUser(userId);
            if (message.hasText()) {
                String text = message.getText();
                if (text.equals("/start")) {
                    start(userId);
                }
                switch (user.getState()) {
                    case START -> {
                        if (text.equals(langService.getMessage(LangFields.LANG_SETTINGS, userId))) {
                            selectLanguage(userId);
                        }
                    }
                    case SELECT_LANGUAGE -> changeLanguage(message, userId);
                }


            }
        }
    }

    private void changeLanguage(Message message, Long userId) {
        String text = message.getText();
        LangEnum lang = null;
        if (text.equals(langService.getMessage(LangFields.BUTTON_LANGUAGE_UZBEK, "Uz"))) {
            lang = LangEnum.UZ;
        } else if (text.equals(langService.getMessage(LangFields.BUTTON_LANGUAGE_RUSSIAN, "Uz"))) {
            lang = LangEnum.RU;
        } else if (text.equals(langService.getMessage(LangFields.BUTTON_LANGUAGE_ENGLISH, "Uz"))) {
            lang = LangEnum.ENG;
        }
        commonUtils.setState(userId, StateEnum.START);
        if (lang == null) {
            sender.sendMessage(userId, langService.getMessage(LangFields.EXCEPTION_LANGUAGE, userId), responseButton.start(userId));
            return;
        }
        commonUtils.setLang(userId, lang);
        sender.sendMessage(userId, langService.getMessage(LangFields.SUCCESSFULLY_CHANGED_LANGUAGE, lang.toString()), responseButton.start(userId));
    }

    private void selectLanguage(long userId) {
        commonUtils.setState(userId, StateEnum.SELECT_LANGUAGE);
        String userLang = commonUtils.getUserLang(userId);
        String message = langService.getMessage(LangFields.SELECT_LANGUAGE_TEXT, userLang);
        sender.sendMessage(userId, message, responseButton.language(userLang));
    }

    private void start(Long userId) {
        commonUtils.setState(userId, StateEnum.START);
        String text = langService.getMessage(LangFields.HELLO, userId);
        sender.sendMessage(userId, text, responseButton.start(userId));
    }
}
