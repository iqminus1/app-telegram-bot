package uz.pdp.apptelegrambot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.Group;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Cacheable(value = "groupEntityById", key = "#id")
    default Group getByIdDefault(long id) {
        return findById(id).orElseThrow();
    }

    @Cacheable(value = "groupEntityByOwnerId", key = "#adminId")
    List<Group> findAllByAdminId(Long adminId);

    Optional<Group> findByGroupId(Long groupId);

    @Cacheable(value = "groupEntityGroupId", key = "#groupId")
    default Group getByGroupId(Long groupId) {
        return findByGroupId(groupId).orElse(null);
    }

    Optional<Group> findByBotToken(String text);

    @Cacheable(value = "groupEntityByBotToken", key = "#text")
    default Group getByBotToken(String text) {
        return findByBotToken(text).orElse(null);
    }

    Optional<Group> findByBotUsername(String username);

    @Cacheable(value = "groupEntityByBotUsername", key = "#username")
    default Group getByBotUsername(String username) {
        return findByBotUsername(username).orElse(null);
    }

    @CachePut(value = "groupEntityById", key = "#result.id")
    default Group saveOptional(Group group) {
        clearByOwnerId(group);
        putByGroupId(group);
        putByBotToken(group);
        putByBotUsername(group);
        return save(group);
    }

    @CachePut(value = "groupEntityByBotUsername", key = "#group.botUsername")
    default Group putByBotUsername(Group group) {
        return group;
    }

    @CachePut(value = "groupEntityByBotToken", key = "#group.botToken")
    default Group putByBotToken(Group group) {
        return group;
    }

    @CachePut(value = "groupEntityGroupId", key = "#group.groupId")
    default Group putByGroupId(Group group) {
        return group;
    }

    @CacheEvict(value = "groupEntityByOwnerId", key = "#group.adminId")
    default void clearByOwnerId(Group group) {
    }
}