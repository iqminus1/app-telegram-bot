package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.service.LangService;

@Component
@RequiredArgsConstructor
public class ResponseText {
    private final LangService langService;

    public String getSendExpireText(Integer ordinal, String lang) {
        if (ordinal == 0) {
            return langService.getMessage(LangFields.SEND_WEEKLY_TARIFF_PRICE_TEXT, lang);
        }
        if (ordinal == 1) {
            return langService.getMessage(LangFields.SEND_DAY15_TARIFF_PRICE_TEXT, lang);
        }
        if (ordinal == 2) {
            return langService.getMessage(LangFields.SEND_MONTHLY_TARIFF_PRICE_TEXT, lang);
        }
        if (ordinal == 3) {
            return langService.getMessage(LangFields.SEND_YEAR_TARIFF_PRICE_TEXT, lang);
        }
        if (ordinal == 4) {
            return langService.getMessage(LangFields.SEND_UNLIMITED_TARIFF_PRICE_TEXT, lang);
        }
        return null;
    }

    public String getTariffExpireText(Integer ordinal, String lang) {
        if (ordinal == 0) {
            return langService.getMessage(LangFields.TARIFF_WEEK_TEXT, lang);
        }
        if (ordinal == 1) {
            return langService.getMessage(LangFields.TARIFF_15_DAY_TEXT, lang);
        }
        if (ordinal == 2) {
            return langService.getMessage(LangFields.TARIFF_MONTH_TEXT, lang);
        }
        if (ordinal == 3) {
            return langService.getMessage(LangFields.TARIFF_YEAR_TEXT, lang);
        }
        if (ordinal == 4) {
            return langService.getMessage(LangFields.TARIFF_UNLIMITED_TEXT, lang);
        }
        return null;
    }
}
