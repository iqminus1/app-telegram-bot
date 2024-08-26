package uz.pdp.apptelegrambot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.ScreenshotGroup;
import uz.pdp.apptelegrambot.enums.ScreenshotStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenshotGroupRepository extends JpaRepository<ScreenshotGroup, Long> {
    @Cacheable(value = "screenshotGroupEntity", key = "#id")
    @Override
    Optional<ScreenshotGroup> findById(Long id);

    @Cacheable(value = "findAll", key = "#groupId")
    List<ScreenshotGroup> findAllByGroupIdAndStatus(Long groupId, ScreenshotStatus status);

    @CacheEvict(value = "findAll", key = "#screenshotGroup.groupId")
    @CachePut(value = "screenshotGroupEntity", key = "#screenshotGroup.id")
    default Optional<ScreenshotGroup> saveOptional(ScreenshotGroup screenshotGroup) {
        return Optional.of(save(screenshotGroup));
    }

}