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
    @Cacheable(value = "groupEntityByOwnerId", key = "#adminId")
    List<Group> findAllByAdminId(Long adminId);

    @Cacheable(value = "groupEntityGroupId", key = "#groupId")
    Optional<Group> findByGroupId(Long groupId);

    @CacheEvict(value = {"groupEntityByOwnerId", "adminResponseServiceStart"}, allEntries = true)
    @CachePut(value = "groupEntityGroupId", key = "#group.groupId")
    default Optional<Group> saveOptional(Group group) {
        return Optional.of(save(group));
    }

    @Override
    @CacheEvict(value = {"groupEntityGroupId", "groupEntityByOwnerId", "adminResponseServiceStart"}, allEntries = true)
    void delete(Group group);

    Optional<Group> findByBotUsername(String botUsername);

    Optional<Group> findByBotToken(String text);
}