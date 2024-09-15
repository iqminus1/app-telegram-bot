package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrambot.entity.Tariff;
import uz.pdp.apptelegrambot.repository.TariffRepository;
import uz.pdp.apptelegrambot.service.owner.ResponseText;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdminResponseText {
    private final TariffRepository tariffRepository;
    private final ResponseText responseText;

    public Map<String, String> getPaymentData(String lang, Long botId) {
        List<Tariff> tariffs = tariffRepository.findAllByBotIdDefault(botId);
        if (tariffs.isEmpty()) {
            return null;
        }
        tariffs.sort(Comparator.comparing(t -> t.getType().ordinal()));
        Map<String, String> map = new LinkedHashMap<>();
        for (Tariff tariff : tariffs) {
            String text = responseText.getTariffExpireText(tariff.getType().ordinal(), lang);
            String data = "&amount=" + tariff.getPrice() + "&additional_param4=" + tariff.getId();
            map.put(text, data);
        }
        return map;
    }

}
