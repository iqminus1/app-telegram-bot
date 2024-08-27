package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.repository.OrderRepository;
import uz.pdp.apptelegrambot.repository.UserLangRepository;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.bot.AdminBot;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;
import uz.pdp.apptelegrambot.service.owner.Temp;
import uz.pdp.apptelegrambot.utils.admin.AdminUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AdminController {
    private final GroupRepository groupRepository;
    private final OrderRepository orderRepository;
    private final LangService langService;
    private final ButtonService buttonService;
    private final UserLangRepository userLangRepository;
    private final Temp temp;
    Map<Long, AdminSender> adminSender = new ConcurrentHashMap<>();


    public void addAdminBot(String token, Long adminId) {
        AdminSender sender = new AdminSender(token);
        adminSender.put(adminId, sender);
        setBotToken(sender, adminId);
        AdminUtils adminUtils = new AdminUtils(userLangRepository);
        MyChatMemberService myChatMemberService = new MyChatMemberServiceImpl(groupRepository, sender);
        AdminResponseButton adminResponseButton = new AdminResponseButton(buttonService, groupRepository, langService);
        AdminMessageServiceImpl adminMessageService = new AdminMessageServiceImpl(sender, langService, adminUtils, adminResponseButton, groupRepository, buttonService);
        ChatJoinRequestService chatJoinRequestService = new ChatJoinRequestServiceImpl(sender, orderRepository, langService, adminResponseButton, groupRepository, adminUtils);
        AdminProcessService adminProcessService = new AdminProcessServiceImpl(myChatMemberService, chatJoinRequestService, adminMessageService);
        new AdminBot(token, adminId, adminProcessService);
    }
    @Async
    public void asyncAddAdminBot(String token,Long adminId){
        addAdminBot(token,adminId);
    }

    private void setBotToken(AdminSender sender, Long adminId) {
        Group tempGroup = temp.getTempGroup(adminId);
        if (tempGroup == null)
            return;
        tempGroup.setBotUsername(sender.getBotUsername());
        groupRepository.saveOptional(tempGroup);

    }

    public AdminSender getSenderByAdminId(long adminId) {
        if (adminSender.containsKey(adminId)) {
            return adminSender.get(adminId);
        }
        return null;
    }
}
