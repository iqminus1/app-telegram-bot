package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.pdp.apptelegrambot.entity.Tariff;
import uz.pdp.apptelegrambot.entity.User;
import uz.pdp.apptelegrambot.enums.ExpireType;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.owner.bot.OwnerSender;
import uz.pdp.apptelegrambot.utils.AppConstant;
import uz.pdp.apptelegrambot.utils.owner.CommonUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CallbackServiceImpl implements CallbackService {
    private final CommonUtils commonUtils;
    private final Temp temp;
    private final OwnerSender sender;
    private final LangService langService;
    private final ResponseButton responseButton;
    private final ResponseText responseText;

    @Override
    public void process(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        String data = callbackQuery.getData();
        User user = commonUtils.getUser(userId);
        switch (user.getState()) {
            case SELECTING_TARIFF -> {
                if (data.startsWith(AppConstant.ACCEPT_TARIFFS_DATA)) {
                    acceptTariffs(callbackQuery);
                } else if (data.startsWith(AppConstant.TARIFF_SELECTING_DATA)) {
                    changeTariffStatus(userId, data, callbackQuery);
                }
            }
        }
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
        sender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
        List<Tariff> tempTariffs = temp.getTempTariffs(userId);
        if (tempTariffs.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            sender.sendMessage(userId, langService.getMessage(LangFields.NOT_ANY_TARIFF_TEXT, userId), responseButton.start(userId));
            return;
        }
        commonUtils.setState(userId, StateEnum.SENDING_TARIFF_PRICE);
        List<Integer> list = tempTariffs.stream().map(Tariff::getType).map(ExpireType::ordinal).toList();
        Integer i = list.get(0);
        String expireText = responseText.getSendExpireText(i, userId);
        sender.sendMessage(userId, expireText);
    }
}
