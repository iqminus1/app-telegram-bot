package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;
import uz.pdp.apptelegrambot.utils.AppConstant;
import uz.pdp.apptelegrambot.utils.admin.AdminUtils;

@RequiredArgsConstructor
public class AdminCallbackServiceImpl implements AdminCallbackService {
    private final AdminSender sender;
    private final AdminResponseButton responseButton;
    private final LangService langService;
    private final AdminUtils utils;

    @Override
    public void process(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        if (data.startsWith(AppConstant.SCREENSHOT)) {
            sendTextScreenshot(callbackQuery);
        }
    }

    private void sendTextScreenshot(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        utils.setUserState(userId, StateEnum.SENDING_JOIN_REQ_SCREENSHOT);
        String userLang = utils.getUserLang(userId);
        sender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
        sender.sendMessage(userId,langService.getMessage(LangFields.SEND_MONEY_TO_CARD_AND_SEND_SCREENSHOT_TEXT,userLang));
    }
}
