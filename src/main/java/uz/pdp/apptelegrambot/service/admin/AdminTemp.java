package uz.pdp.apptelegrambot.service.admin;

import uz.pdp.apptelegrambot.entity.ScreenshotGroup;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AdminTemp {
    private final ConcurrentMap<Long, ScreenshotGroup> tempScreenshots = new ConcurrentHashMap<>();

    public void addTempScreenshot(long userId, ScreenshotGroup screenshotGroup) {
        tempScreenshots.put(userId, screenshotGroup);
    }

    public void clearTemp(Long userId) {
        tempScreenshots.remove(userId);
    }

    public ScreenshotGroup getTempScreenshot(Long userId) {
        if (tempScreenshots.containsKey(userId)) {
            return tempScreenshots.get(userId);
        }
        return null;
    }

}
