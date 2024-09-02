package uz.pdp.apptelegrambot.service.owner;

import org.springframework.stereotype.Component;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.entity.Tariff;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
public class Temp {
    private final ConcurrentMap<Long, Group> tempGroup = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, List<Tariff>> tempTariffs = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Long> tempBotId = new ConcurrentHashMap<>();

    public void clearTemp(Long userId) {
        tempGroup.remove(userId);
        tempTariffs.remove(userId);
        tempBotId.remove(userId);
    }

    public void addTempGroup(Group group) {
        tempGroup.put(group.getAdminId(), group);
    }

    public Group getTempGroup(Long userId) {
        return tempGroup.getOrDefault(userId, null);
    }

    public void addTempTariff(Long userId, Tariff tariff) {
        tempTariffs.compute(userId, (key, tariffs) -> {
            if (tariffs == null) {
                tariffs = new ArrayList<>();
            }
            tariffs.add(tariff);
            return tariffs;
        });
    }

    public List<Tariff> getTempTariffs(Long userId) {
        return tempTariffs.getOrDefault(userId, new ArrayList<>());
    }

    public void removeTempTariff(Integer ordinal, Long userId) {
        tempTariffs.computeIfPresent(userId, (key, tariffs) -> tariffs.stream()
                .filter(t -> t.getType().ordinal() != ordinal)
                .collect(Collectors.toList()));
    }

    public void addTempBotId(long userId, long botId) {
        tempBotId.put(userId, botId);
    }

    public long getTempBotId(long userId) {

        return tempBotId.getOrDefault(userId, 0L);
    }
}
