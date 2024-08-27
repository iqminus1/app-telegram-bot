package uz.pdp.apptelegrambot.service.owner;

import org.springframework.stereotype.Component;
import uz.pdp.apptelegrambot.entity.Group;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class Temp {
    private final ConcurrentMap<Long, Group> tempGroup = new ConcurrentHashMap<>();

    public void clearTemp(Long userId) {
        tempGroup.remove(userId);
    }

    public void addTempGroup(Group group) {
        tempGroup.put(group.getAdminId(), group);
    }

    public Group getTempGroup(Long userId) {
        if (tempGroup.containsKey(userId)) {
            return tempGroup.get(userId);
        }
        return null;
    }
}
