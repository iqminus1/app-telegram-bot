package uz.pdp.apptelegrambot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.pdp.apptelegrambot.entity.CodeGroup;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.entity.ScreenshotGroup;
import uz.pdp.apptelegrambot.entity.User;
import uz.pdp.apptelegrambot.enums.Status;
import uz.pdp.apptelegrambot.repository.CodeGroupRepository;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.repository.ScreenshotGroupRepository;
import uz.pdp.apptelegrambot.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/controllers")
public class Controllers {
    private final UserRepository userRepository;
    private final ScreenshotGroupRepository screenshotGroupRepository;
    private final CodeGroupRepository codeGroupRepository;
    private final GroupRepository groupRepository;

    @GetMapping("/user/get-all")
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @GetMapping("/groups/get-all")
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @GetMapping("/groups/get/{adminId}")
    public List<Group> getGroups(@PathVariable long adminId) {
        return groupRepository.findAllByAdminIdDefault(adminId);
    }

    @PutMapping("/groups/get/{botId}")
    public Group update(@PathVariable long botId, @RequestParam LocalDate expire) {
        Group group = groupRepository.getByIdDefault(botId);
        group.setExpireAt(LocalDateTime.of(expire, LocalTime.of(0, 0)));
        groupRepository.save(group);
        return group;
    }

    //screenshots
    @GetMapping("/screenshots/get/{groupId}/{from}/{to}")
    public List<ScreenshotGroup> getScreenshots(@PathVariable LocalDate from, @PathVariable LocalDate to, @PathVariable Long groupId) {
        List<ScreenshotGroup> screenshots = screenshotGroupRepository.findAllByGroupIdAndStatus(groupId, Status.ACCEPT);
        return screenshots
                .stream()
                .filter(sc -> sc.getActiveAt().toLocalDate().isAfter(from) && sc.getActiveAt().toLocalDate().isBefore(to))
                .toList();
    }

    @GetMapping("/count/screenshots/get/{groupId}/{from}/{to}")
    public Integer getScreenshotsCount(@PathVariable LocalDate from, @PathVariable LocalDate to, @PathVariable Long groupId) {
        List<ScreenshotGroup> screenshots = screenshotGroupRepository.findAllByGroupIdAndStatus(groupId, Status.ACCEPT);
        return screenshots
                .stream()
                .filter(sc -> sc.getActiveAt().toLocalDate().isAfter(from) && sc.getActiveAt().toLocalDate().isBefore(to))
                .toList().size();
    }

    @GetMapping("/screenshots/get/{groupId}")
    public List<ScreenshotGroup> getScreenshots(@PathVariable Long groupId) {
        return screenshotGroupRepository.findAllByGroupIdAndStatus(groupId, Status.ACCEPT);
    }

    @GetMapping("/count/screenshots/get/{groupId}")
    public Integer getScreenshotsCount(@PathVariable Long groupId) {
        return screenshotGroupRepository.findAllByGroupIdAndStatus(groupId, Status.ACCEPT).size();
    }


    //Code Group
    @GetMapping("/code/get/{botId}/{from}/{to}")
    public List<CodeGroup> getCodeActives(@PathVariable Long botId, @PathVariable LocalDate from, @PathVariable LocalDate to) {
        LocalTime localTime = LocalTime.of(0, 0);
        return codeGroupRepository.findAllByBotIdAndActiveAtBetween(botId, LocalDateTime.of(from, localTime), LocalDateTime.of(to, localTime));
    }

    @GetMapping("/count/code/get/{botId}/{from}/{to}")
    public Integer getCodeActivesCount(@PathVariable Long botId, @PathVariable LocalDate from, @PathVariable LocalDate to) {
        LocalTime localTime = LocalTime.of(0, 0);
        return codeGroupRepository.findAllByBotIdAndActiveAtBetween(botId, LocalDateTime.of(from, localTime), LocalDateTime.of(to, localTime)).size();
    }

    @GetMapping("/code/get/{botId}")
    public List<CodeGroup> getCodeActives(@PathVariable Long botId) {
        return codeGroupRepository.findAllByBotIdAndActive(botId, true);
    }

    @GetMapping("/count/code/get/{botId}")
    public Integer getCodeActivesCount(@PathVariable Long botId) {
        return codeGroupRepository.findAllByBotIdAndActive(botId, true).size();
    }
}
