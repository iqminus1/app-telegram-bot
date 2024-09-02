package uz.pdp.apptelegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.ScreenshotGroup;
import uz.pdp.apptelegrambot.enums.ScreenshotStatus;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public interface ScreenshotGroupRepository extends JpaRepository<ScreenshotGroup, Long> {
    ConcurrentMap<Long, ScreenshotGroup> byId = new ConcurrentHashMap<>();

    default ScreenshotGroup getById(Long id) {
        if (byId.containsKey(id)) {
            return byId.get(id);
        }
        byId.put(id, findById(id).orElseThrow());
        return getById(id);
    }

    List<ScreenshotGroup> findAllByGroupIdAndStatus(Long groupId, ScreenshotStatus status);


    default ScreenshotGroup saveOptional(ScreenshotGroup screenshotGroup) {
        ScreenshotGroup save = save(screenshotGroup);
        byId.put(screenshotGroup.getId(), screenshotGroup);
        return save;
    }





}