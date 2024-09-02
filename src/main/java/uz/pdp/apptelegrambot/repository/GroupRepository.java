package uz.pdp.apptelegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    ConcurrentMap<Long, Group> byId = new ConcurrentHashMap<>();
    ConcurrentMap<Long, Group> byGroupId = new ConcurrentHashMap<>();
    ConcurrentMap<String, Group> byBotToken = new ConcurrentHashMap<>();
    ConcurrentMap<String, Group> byBotUsername = new ConcurrentHashMap<>();
    ConcurrentMap<Long, List<Group>> allByAdminId = new ConcurrentHashMap<>();

    default Group getByIdDefault(long id) {
        if (byId.containsKey(id)) {
            return byId.get(id);
        }
        byId.put(id, findById(id).orElseThrow());
        return getByIdDefault(id);
    }

    List<Group> findAllByAdminId(Long adminId);

    default List<Group> findAllByAdminIdDefault(Long adminId) {
        if (allByAdminId.containsKey(adminId)) {
            return allByAdminId.get(adminId);
        }
        allByAdminId.put(adminId, findAllByAdminId(adminId));
        return findAllByAdminIdDefault(adminId);
    }

    Optional<Group> findByGroupId(Long groupId);

    default Group getByGroupId(Long groupId) {
        if (byGroupId.containsKey(groupId)) {
            return byGroupId.get(groupId);
        }
        byGroupId.put(groupId, findByGroupId(groupId).orElse(null));
        return getByGroupId(groupId);
    }

    Optional<Group> findByBotToken(String text);

    default Group getByBotToken(String text) {
        if (byBotToken.containsKey(text)) {
            return byBotToken.get(text);
        }
        byBotToken.put(text, findByBotToken(text).orElse(null));
        return getByBotToken(text);
    }

    Optional<Group> findByBotUsername(String username);

    default Group getByBotUsername(String username) {
        if (byBotUsername.containsKey(username)) {
            return byBotUsername.get(username);
        }
        byBotUsername.put(username, findByBotUsername(username).orElse(null));
        return getByBotUsername(username);
    }

    default Group saveOptional(Group group) {
        Group savedGroup = save(group);
        putById(savedGroup);
        putByGroupId(savedGroup);
        putByBotToken(savedGroup);
        putByBotUsername(savedGroup);
        putAllByAdminId(savedGroup);
        return savedGroup;
    }

    @Async
    default void putById(Group group) {
        byId.put(group.getId(), group);
    }

    @Async
    default void putByGroupId(Group group) {
        if (group.getGroupId() != null) {
            byGroupId.put(group.getGroupId(), group);
        } else
            byGroupId.clear();
    }

    @Async
    default void putByBotToken(Group group) {
        byBotToken.put(group.getBotToken(), group);
    }

    @Async
    default void putByBotUsername(Group group) {
        byBotUsername.put(group.getBotUsername(), group);
    }


    @Async
    default void putAllByAdminId(Group group) {
        if (allByAdminId.containsKey(group.getAdminId())) {
            List<Group> groups = allByAdminId.get(group.getAdminId());
            Optional<Group> first = groups.stream().filter(g -> group.getAdminId().equals(g.getAdminId())).findFirst();
            if (first.isEmpty()) {
                groups.add(group);
            } else {
                groups.remove(first.get());
                groups.add(group);
                allByAdminId.put(group.getAdminId(), groups);
            }
        } else {
            allByAdminId.put(group.getAdminId(), new ArrayList<>(List.of(group)));
        }
    }

    @Scheduled(cron = "0 0 4 * * ?")
    default void clearCache() {
        byId.clear();
        byGroupId.clear();
        byBotToken.clear();
        byBotUsername.clear();
        allByAdminId.clear();
    }
}