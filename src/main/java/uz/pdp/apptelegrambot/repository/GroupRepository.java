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
    default Group getById(Long id) {
        return findById(id).orElseThrow();
    }

    @Cacheable(value = "groupEntityByOwnerId", key = "#adminId")
    List<Group> findAllByAdminId(Long adminId);

    @Cacheable(value = "groupEntityGroupId", key = "#groupId")
    Optional<Group> findByGroupId(Long groupId);

    @CacheEvict(value = "groupEntityGroupId", allEntries = true)
    @CachePut(value = "groupEntityById", key = "#result.id")
    default Group saveOptional(Group group) {
        clearByOwnerId(group);
        return save(group);
    }

    @CacheEvict(value = "groupEntityByOwnerId", key = "#group.adminId")
    default void clearByOwnerId(Group group) {

    }


    Optional<Group> findByBotToken(String text);

    Optional<Group> findByBotUsername(String username);
}