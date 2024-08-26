package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
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
            long in = System.currentTimeMillis();
            Long userId = message.getFrom().getId();
//            User user = commonUtils.getUser(userId);
            if (message.hasText()) {
                String text = message.getText();
                if (text.equals("/start")) {
                    start(userId);
                }
            }
            System.out.println(System.currentTimeMillis() - in + " voxt ketdi");
        }
    }

    private void start(Long userId) {
        commonUtils.setState(userId, StateEnum.START);
        String text = langService.getMessage(LangFields.HELLO, userId);
        sender.sendMessage(userId, text, responseButton.start(userId));
    }
}
