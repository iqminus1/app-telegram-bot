package uz.pdp.apptelegrambot.utils;

import uz.pdp.apptelegrambot.entity.Order;
import uz.pdp.apptelegrambot.enums.ExpireType;

import java.time.LocalDateTime;

public interface AppConstant {
    String TOKEN = "7403157157:AAGoSNnB4NRx38Hy1n_urX_F0qMir8X8sOY";
    String USERNAME = "obunamanager_bot";
    String TARIFF_SELECTING_DATA = "tariffSelectData:";
    String GREEN_TEXT = " \uD83D\uDFE2";
    String ACCEPT_TARIFFS_DATA = "acceptTariffs";
    String SCREENSHOT = "payment:Screenshot";
    String TARIFF_DATA = "tariffId:";
    String FILE_PATH = "/home/app-telegram-bot/files";
    String BOT_DATA = "botId:";
    String SHOW_PRICE_INFO_DATA = "showPriceInfoData:";
    String GENERATE_CODE_FOR_TARIFF_DATA = "codeByTariff:";
    String ACCEPT_SCREENSHOT_DATA = "acceptScreenshot:";
    String REJECT_SCREENSHOT_DATA = "rejectScreenshot:";
    String CHANGE_TARIFF_PRICE_DATA = "changeTariffPrice:";
    String DELETE_TARIFF_DATA = "deleteTariff:";
    //BO-> by ordinal
    String CREATE_TARIFF_DATA = "createTariffBO:";
    //backToBotList
    String BACK_TO_BOT_LIST_DATA = "backTBL";

    //BBI -> by bot id
    String TARIFF_LIST_DATA = "tariffLBBI:";
    String ADD_TARIFF_DATA = "addTariffBBI:";
    String CARD_NUMBER_DATA = "addCardNumberBBI:";
    String GENERATE_CODE_DATA = "generateCodeBBI:";
    String START_STOP_BOT_DATA = "startStopBotBBI:";
    String PAMYENT_MATHODS_DATA = "paymentMethodsBBI:";
    String BACK_TO_BOT_INFO_DATA = "backToBotInfoBBI:";
    String SEE_ALL_SCREENSHOTS = "seeAllScreenshotsBBI:";
    String CHANGE_CLICK_STATUS_DATA = "changeClickStatusBBI:";
    String CHANGE_PAYME_STATUS_DATA = "changePamyeStatusBBI:";
    String CHANGE_SCREENSHOT_STATUS_DATA = "changeScreenshotStatusBBI:";
    String CHANGE_CODE_STATUS_DATA = "changeCodeStatusBBI:";
    String BACK_TO_TARIFFS_DATA = "backToTariffsBBI:";
    String GET_LINK_FOR_JOIN_DATA = "getLinkForJoinBBI";
    String SEE_ALL_SENDED_MESSAGES_DATA = "seeAllSendedMessageBBI:";

    static Order updateOrderExpire(Order order, ExpireType type) {
        if (order.getExpireDay() == null) {
            order.setExpireDay(LocalDateTime.now());
        }
        if (type == ExpireType.WEEK)
            order.setExpireDay(order.getExpireDay().plusWeeks(1));
        if (type == ExpireType.DAY_15)
            order.setExpireDay(order.getExpireDay().plusDays(15));
        if (type == ExpireType.MONTH)
            order.setExpireDay(order.getExpireDay().plusMonths(1));
        if (type == ExpireType.YEAR)
            order.setExpireDay(order.getExpireDay().plusYears(1));
        if (type == ExpireType.UNLIMITED) {
            order.setExpireDay(LocalDateTime.now().plusYears(100));
            order.setUnlimited(true);
        }
        return order;
    }
}