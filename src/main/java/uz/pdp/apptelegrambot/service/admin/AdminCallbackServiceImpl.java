package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.entity.ScreenshotGroup;
import uz.pdp.apptelegrambot.entity.Tariff;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.enums.ScreenshotStatus;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.repository.TariffRepository;
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
    private final TariffRepository tariffRepository;
    private final AdminTemp temp;

    @Override
    public void process(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        if (data.startsWith(AppConstant.SCREENSHOT)) {
            sendTextScreenshot(callbackQuery);
        } else if (data.startsWith(AppConstant.GET_LINK_FOR_JOIN_DATA)) {
            sendLinkForJoin(callbackQuery);
        }
    }

    private void sendLinkForJoin(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        sender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
        String userLang = utils.getUserLang(userId);
        if (sender.checkGroup(userId)) {
            sender.sendMessage(userId, langService.getMessage(LangFields.YOU_HAVE_THIS_CHANNEL_TEXT, userLang));
            return;
        }
        sender.sendMessage(userId,langService.getMessage(LangFields.SEND_VALID_ORDER_TEXT,userLang)+" "+sender.getLink());
    }

    private void sendTextScreenshot(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        sender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
        String userLang = utils.getUserLang(userId);
        Group group = sender.getGroup();
        if (group.getGroupId() == null || group.getCardNumber() == null || group.getCardName() == null) {
            sender.sendMessage(userId, langService.getMessage(LangFields.SECTION_DONT_WORK_TEXT, userLang), responseButton.start(group.getId(), userLang));
            return;
        }
        long tariffId = Long.parseLong(callbackQuery.getData().split("\\+")[1].split(":")[1]);
        Tariff tariff = tariffRepository.getById(tariffId);
        utils.setUserState(userId, StateEnum.SENDING_JOIN_REQ_SCREENSHOT);
        ScreenshotGroup screenshotGroup = new ScreenshotGroup();
        screenshotGroup.setGroupId(group.getGroupId());
        screenshotGroup.setStatus(ScreenshotStatus.DONT_SEE);
        screenshotGroup.setTariffId(tariffId);
        screenshotGroup.setTariffPrice(tariff.getPrice());
        screenshotGroup.setType(tariff.getType());
        screenshotGroup.setSendUserId(userId);
        temp.addTempScreenshot(userId, screenshotGroup);
        String message = langService.getMessage(LangFields.SEND_MONEY_TO_CARD_AND_SEND_SCREENSHOT_TEXT, userLang).formatted(tariff.getPrice(), group.getCardName(), group.getCardNumber());
        sender.sendMessage(userId, message);
    }
}
