package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrambot.service.admin.bot.AdminBot;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@EnableAsync
public class AdminController {
    private final AdminProcessService adminProcessService;
    Map<Long, AdminSender> adminSender = new ConcurrentHashMap<>();

    @Async
    public void addAdminBot(String token, Long adminId) {
        AdminSender sender = new AdminSender(token);
        adminSender.put(adminId, sender);
        new AdminBot(token, adminId, adminProcessService);
    }
}
