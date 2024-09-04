package uz.pdp.apptelegrambot.repository;

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

    default Group getByIdDefault(long id) {
        return findById(id).orElseThrow();
    }

    default List<Group> findAllByAdminIdDefault(Long adminId) {
        return findAllByAdminId(adminId);
    }

    default Group getByGroupId(Long groupId) {
        return findByGroupId(groupId).orElse(null);
    }

    default Group getByBotToken(String text) {
        return findByBotToken(text).orElse(null);
    }

    default Group getByBotUsernameIgnoreCase(String username) {
        return findByBotUsername(username).orElse(null);
    }

    default void saveOptional(Group group) {
        save(group);
    }

}