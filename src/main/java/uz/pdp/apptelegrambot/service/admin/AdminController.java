package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.repository.*;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.bot.AdminBot;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;
import uz.pdp.apptelegrambot.service.admin.sceduled.AdminResponseText;
import uz.pdp.apptelegrambot.service.owner.ResponseText;
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
    private final TariffRepository tariffRepository;
    private final ResponseText responseText;
    private final Temp temp;
    private final CodeGroupRepository codeGroupRepository;
    private final AdminResponseText adminResponseText;
    private final ScreenshotGroupRepository screenshotGroupRepository;
    Map<Long, AdminSender> adminSender = new ConcurrentHashMap<>();


    public void addAdminBot(String token, Long adminId) {
        AdminSender sender = new AdminSender(token, groupRepository);
        adminSender.put(adminId, sender);
        setBotToken(sender, adminId);
        AdminTemp adminTemp = new AdminTemp();
        AdminUtils adminUtils = new AdminUtils(userLangRepository);
        MyChatMemberService myChatMemberService = new MyChatMemberServiceImpl(groupRepository, sender);
        AdminResponseButton adminResponseButton = new AdminResponseButton(buttonService, groupRepository, langService, tariffRepository, responseText);
        AdminMessageServiceImpl adminMessageService = new AdminMessageServiceImpl(sender, langService, adminUtils, adminResponseButton, groupRepository, buttonService, codeGroupRepository, orderRepository, adminResponseText, adminTemp, screenshotGroupRepository, token);
        ChatJoinRequestService chatJoinRequestService = new ChatJoinRequestServiceImpl(sender, orderRepository, langService, adminResponseButton, groupRepository, adminUtils);
        AdminCallbackServiceImpl callbackService = new AdminCallbackServiceImpl(sender, adminResponseButton, langService, adminUtils, tariffRepository, adminTemp);
        AdminProcessService adminProcessService = new AdminProcessServiceImpl(myChatMemberService, chatJoinRequestService, adminMessageService, callbackService);
        new AdminBot(token, adminId, adminProcessService, groupRepository);
    }

    @Async
    public void asyncAddAdminBot(String token, Long adminId) {
        addAdminBot(token, adminId);
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
