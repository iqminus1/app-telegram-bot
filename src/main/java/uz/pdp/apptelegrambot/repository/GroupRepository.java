package uz.pdp.apptelegrambot.repository;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.Group;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findAllByAdminId(Long adminId);

    Optional<Group> findByGroupId(Long groupId);

    Optional<Group> findByBotToken(String text);

    Optional<Group> findByBotUsername(String username);

//    @Cacheable(value = "groupEntityById", key = "#id")
    default Group getByIdDefault(long id) {
        return findById(id).orElseThrow();
    }

//    @Cacheable(value = "groupEntitiesByAdminId", key = "#adminId")
    default List<Group> findAllByAdminIdDefault(Long adminId) {
        return findAllByAdminId(adminId);
    }

//    @Cacheable(value = "groupEntityByGroupId", key = "#groupId")
    default Group getByGroupId(Long groupId) {
        return findByGroupId(groupId).orElse(null);
    }

//    @Cacheable(value = "groupEntityByToken", key = "#text")
    default Group getByBotToken(String text) {
        return findByBotToken(text).orElse(null);
    }

//    @Cacheable(value = "groupEntityByUsername", key = "#username")
    default Group getByBotUsernameIgnoreCase(String username) {
        return findByBotUsername(username).orElse(null);
    }

    default void saveOptional(Group group) {
        save(group);
//        putById(group);
//        putByAdminId(group);
    }

//    @CachePut(value = "groupEntityById", key = "#result.adminId")
//    default Group putByAdminId(Group group) {
//        return group;
//    }
//
//    @CachePut(value = "groupEntityById", key = "#result.id")
//    default Group putById(Group group) {
//        return group;
//    }

}