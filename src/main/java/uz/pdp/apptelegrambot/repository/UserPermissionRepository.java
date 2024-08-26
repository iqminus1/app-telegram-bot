package uz.pdp.apptelegrambot.repository;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.GroupPermission;

import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<GroupPermission, Long> {
    @Cacheable(value = "userPermission", key = "#groupId")
    Optional<GroupPermission> findByGroupId(Long groupId);

    @CachePut(value = "userPermission", key = "#permission.groupId")
    default Optional<GroupPermission> saveOptional(GroupPermission permission) {
        return Optional.of(save(permission));
    }

}