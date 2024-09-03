package uz.pdp.apptelegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.Group;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    default Group getByIdDefault(long id) {
        return findById(id).orElseThrow();
    }

    List<Group> findAllByAdminId(Long adminId);

    default List<Group> findAllByAdminIdDefault(Long adminId) {
        return findAllByAdminId(adminId);
    }

    Optional<Group> findByGroupId(Long groupId);

    default Group getByGroupId(Long groupId) {
        return findByGroupId(groupId).orElse(null);
    }

    Optional<Group> findByBotToken(String text);

    default Group getByBotToken(String text) {
        return findByBotToken(text).orElse(null);
    }

    Optional<Group> findByBotUsername(String username);

    default Group getByBotUsernameIgnoreCase(String username) {
        return findByBotUsername(username).orElse(null);
    }

    default void saveOptional(Group group) {
        save(group);
    }

}