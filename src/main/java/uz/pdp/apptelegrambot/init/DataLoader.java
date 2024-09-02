package uz.pdp.apptelegrambot.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.service.admin.AdminController;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final GroupRepository groupRepository;
    private final AdminController adminController;

    @Override
    public void run(String... args) {
        groupsManage();
    }

    @Async
    public void groupsManage() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (Group group : groupRepository.findAll()) {
            adminController.asyncAddAdminBot(group.getBotToken(), group.getAdminId());
        }
    }
}
