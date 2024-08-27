package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;
import uz.pdp.apptelegrambot.utils.admin.AdminUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
public class AdminMessageServiceImpl implements AdminMessageService {
    private final AdminSender sender;
    private final LangService langService;
    private final AdminUtils adminUtils;
    private final AdminResponseButton responseButton;
    private final GroupRepository groupRepository;
    private final ButtonService buttonService;

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
                        }
                    }
                    case SELECT_LANGUAGE -> changeLanguage(text, userId, userLang);
                    default ->
                            sender.sendMessage(userId, langService.getMessage(LangFields.EXCEPTION_BUTTON, userLang), responseButton.start(getGroupId(), userLang));

                }
            }
        }
    }

    private void changeLanguage(String text, Long userId, String userLang) {
        LangEnum lang = langService.getLanguageEnum(text);

        Long groupId = getGroupId();
        if (lang == null) {
            sender.sendMessage(userId, langService.getMessage(LangFields.EXCEPTION_LANGUAGE, userLang), responseButton.start(groupId, userLang));
            return;
        }
        adminUtils.setUserLang(userId, lang.name());
        adminUtils.setUserState(userId, StateEnum.START);
        sender.sendMessage(userId, langService.getMessage(LangFields.SUCCESSFULLY_CHANGED_LANGUAGE, lang.name()), responseButton.start(groupId, lang.name()));
    }


    private void selectLanguage(long userId) {
        adminUtils.setUserState(userId, StateEnum.SELECT_LANGUAGE);
        String userLang = adminUtils.getUserLang(userId);
        String message = langService.getMessage(LangFields.SELECT_LANGUAGE_TEXT, userLang);
        sender.sendMessage(userId, message, buttonService.language(userLang));
    }

    private void start(Long userId) {
        long groupId = getGroupId();
        String userLang = adminUtils.getUserLang(userId);
        String message = langService.getMessage(LangFields.HELLO, userLang);
        ReplyKeyboard start = responseButton.start(groupId, userLang);
        sender.sendMessage(userId, message, start);
    }

    private long getGroupId() {
        String botUsername = sender.getBotUsername();
        return groupRepository.findByBotUsername(botUsername).orElseThrow().getGroupId();
    }
}
