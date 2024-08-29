package uz.pdp.apptelegrambot.utils;

import uz.pdp.apptelegrambot.entity.Order;
import uz.pdp.apptelegrambot.enums.ExpireType;

import java.time.LocalDateTime;

public interface AppConstant {
    String TOKEN = "7526831468:AAFTZllAKdDJEhih22K-gcyUja4Da_pE_54";
    String USERNAME = "manager_group_father_bot";
    String TARIFF_SELECTING_DATA = "tariffSelectData:";
    String GREEN_TEXT = " \uD83D\uDFE2";
    String ACCEPT_TARIFFS_DATA = "acceptTariffs";
    String PAYME = "payment:Payme";
    String CLICK = "payment:Click";
    String SCREENSHOT = "payment:Screenshot";
    String TARIFF_DATA = "tariffId:";
    String FILE_PATH = "C:\\Users\\User\\Desktop\\app-telegram-bot\\files";
    String LINK_NAME = "create by bot";
    String BOT_DATA = "botId:";
    String FREE_DATA = "freeData:";
    String GENERATE_CODE_FOR_TARIFF_DATA = "codeByTariff:";
    String ACCEPT_SCREENSHOT_DATA = "acceptScreenshot:";
    String REJECT_SCREENSHOT_DATA = "rejectScreenshot:";
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