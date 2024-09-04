package uz.pdp.apptelegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrambot.entity.ScreenshotGroup;
import uz.pdp.apptelegrambot.enums.Status;

import java.util.List;

@Repository
public interface ScreenshotGroupRepository extends JpaRepository<ScreenshotGroup, Long> {

    default ScreenshotGroup getById(long id) {
        return findById(id).orElseThrow();
    }

    List<ScreenshotGroup> findAllByGroupIdAndStatus(Long groupId, Status status);


    default void saveOptional(ScreenshotGroup screenshotGroup) {
        ScreenshotGroup save = save(screenshotGroup);
    }


}