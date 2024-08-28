package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.entity.Tariff;
import uz.pdp.apptelegrambot.entity.User;
import uz.pdp.apptelegrambot.enums.ExpireType;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.enums.StateEnum;
import uz.pdp.apptelegrambot.repository.GroupRepository;
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
    private final GroupRepository groupRepository;

    @Override
    public void process(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        String data = callbackQuery.getData();
        User user = commonUtils.getUser(userId);
        switch (user.getState()) {
            case START -> {
                if (data.startsWith(AppConstant.BOT_DATA)) {
                    showBotInfo(callbackQuery);
                } else if (data.equals(AppConstant.BACK_TO_BOT_LIST_DATA)) {
                    backToList(callbackQuery);
                }
            }
            case SELECTING_TARIFF -> {
                if (data.startsWith(AppConstant.ACCEPT_TARIFFS_DATA)) {
                    acceptTariffs(callbackQuery);
                } else if (data.startsWith(AppConstant.TARIFF_SELECTING_DATA)) {
                    changeTariffStatus(userId, data, callbackQuery);
                }
            }
        }
    }

    private void backToList(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        InlineKeyboardMarkup markup = responseButton.botsList(userId);
        String message = langService.getMessage(LangFields.SELECT_CHOOSE_BOT_TEXT, userId);
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), message, markup);
    }

    private void showBotInfo(CallbackQuery callbackQuery) {
        long botId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.findById(botId).orElseThrow();
        Long userId = callbackQuery.getFrom().getId();
        String message = langService.getMessage(LangFields.BOT_INFO_TEXT, userId).formatted(group.getBotUsername(), group.getName());
        InlineKeyboardMarkup markup = responseButton.botInfo(botId, userId);
        sender.changeTextAndKeyboard(userId, callbackQuery.getMessage().getMessageId(), message, markup);
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
