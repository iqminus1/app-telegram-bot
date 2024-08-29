package uz.pdp.apptelegrambot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.ScreenshotGroup;
import uz.pdp.apptelegrambot.enums.ScreenshotStatus;

import java.util.List;

@Repository
public interface ScreenshotGroupRepository extends JpaRepository<ScreenshotGroup, Long> {

    @Cacheable(value = "screenshotGroupEntity", key = "#id")
    default ScreenshotGroup getById(Long id) {
        return findById(id).orElseThrow();
    }

    @Cacheable(value = "findAll", key = "#groupId")
    List<ScreenshotGroup> findAllByGroupIdAndStatus(Long groupId, ScreenshotStatus status);

    @CacheEvict(value = "findAll", key = "#screenshotGroup.groupId")
    @CachePut(value = "screenshotGroupEntity", key = "#result.id")
    default ScreenshotGroup saveOptional(ScreenshotGroup screenshotGroup) {
        return save(screenshotGroup);
    }
}