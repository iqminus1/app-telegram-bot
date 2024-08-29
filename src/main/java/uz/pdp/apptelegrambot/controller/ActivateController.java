package uz.pdp.apptelegrambot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.repository.GroupRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/activate")
public class ActivateController {
    private final GroupRepository groupRepository;

    @PostMapping("/{username}")
    public Group activate(@PathVariable String username,
                          @RequestParam(required = false) Integer day,
                          @RequestParam(required = false) Integer month,
                          @RequestParam(required = false) Integer year) {
        Optional<Group> optionalGroup = groupRepository.findByBotUsername(username);
        if (optionalGroup.isEmpty()) {
            throw new RuntimeException("group not found by username -> " + username);
        }
        Group group = optionalGroup.get();
        if (day != null) {
            if (group.getExpireAt().isBefore(LocalDateTime.now())) {
                group.setExpireAt(LocalDateTime.now().plusDays(day));
            } else
                group.setExpireAt(group.getExpireAt().plusDays(day));
        }
        if (month != null) {
            if (group.getExpireAt().isBefore(LocalDateTime.now())) {
                group.setExpireAt(LocalDateTime.now().plusMonths(month));
            } else
                group.setExpireAt(group.getExpireAt().plusMonths(month));
        }
        if (year != null) {
            if (group.getExpireAt().isBefore(LocalDateTime.now())) {
                group.setExpireAt(LocalDateTime.now().plusYears(year));
            } else
                group.setExpireAt(group.getExpireAt().plusYears(year));
        }
        groupRepository.saveOptional(group);
        return group;
    }
}
