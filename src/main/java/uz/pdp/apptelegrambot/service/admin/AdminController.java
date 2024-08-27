package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.repository.OrderRepository;
import uz.pdp.apptelegrambot.repository.UserLangRepository;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.bot.AdminBot;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;
import uz.pdp.apptelegrambot.utils.admin.AdminUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@EnableAsync
public class AdminController {
    private final GroupRepository groupRepository;
    private final OrderRepository orderRepository;
    private final LangService langService;
    private final ButtonService buttonService;
    private final UserLangRepository userLangRepository;
    Map<Long, AdminSender> adminSender = new ConcurrentHashMap<>();

    @Async
    public void addAdminBot(String token, Long adminId) {
        AdminSender sender = new AdminSender(token);
        adminSender.put(adminId, sender);
        AdminUtils adminUtils = new AdminUtils(userLangRepository);
        MyChatMemberService myChatMemberService = new MyChatMemberServiceImpl(groupRepository, sender);
        AdminMessageServiceImpl adminMessageService = new AdminMessageServiceImpl();
        AdminResponseButton adminResponseButton = new AdminResponseButton(buttonService, groupRepository, langService);
        ChatJoinRequestService chatJoinRequestService = new ChatJoinRequestServiceImpl(sender, orderRepository, langService, adminResponseButton, adminUtils);
        AdminProcessService adminProcessService = new AdminProcessServiceImpl(myChatMemberService, chatJoinRequestService, adminMessageService);
        new AdminBot(token, adminId, adminProcessService);
    }

    public AdminSender getSenderByAdminId(long adminId) {
        if (adminSender.containsKey(adminId)) {
            return adminSender.get(adminId);
        }
        return null;
    }
}
