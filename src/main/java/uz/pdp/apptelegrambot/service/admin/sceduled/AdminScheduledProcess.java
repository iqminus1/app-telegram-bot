package uz.pdp.apptelegrambot.service.admin.sceduled;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.entity.Order;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.repository.OrderRepository;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.AdminController;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;
import uz.pdp.apptelegrambot.utils.admin.AdminUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminScheduledProcess {

    private final OrderRepository orderRepository;
    private final GroupRepository groupRepository;
    private final AdminController adminController;
    private final LangService langService;

    @Scheduled(cron = "0 0 4 * * ?")
    public void kickUsersFromGroups() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> orders = orderRepository.findAllByExpireDayAfterAndExpireDayBeforeAndUnlimited(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5).plusMinutes(1), false);
        kickUsers(orders, now);
        sendUsersExpire(orders, now);
        sendBuyingText();
    }

    private void sendBuyingText() {
        Map<Long, AdminSender> adminSenders = adminController.getAdminSenders();
        for (Long adminId : adminSenders.keySet()) {
            AdminUtils adminUtils = adminController.getAdminUtils(adminId);
            AdminSender adminSender = adminSenders.get(adminId);
            Group group = adminSender.getGroup();
            for (byte i = 2; i >= 0; i--) {
                Set<Long> sendingMessage = adminUtils.getSendingMessage(i);
                for (Long userId : sendingMessage) {
                    String userLang = adminUtils.getUserLang(userId);
                    Chat chat = adminSender.getChat(group.getGroupId());
                    if (chat.getType().equals("channel")) {
                        String message = langService.getMessage(LangFields.YOU_ARE_SENT_JOIN_REQ_FOR_CHANNEL_TEXT, userLang).formatted(group.getName());
                        adminSender.sendMessage(userId, message);
                    } else if (chat.getType().equals("group")) {
                        String message = langService.getMessage(LangFields.YOU_ARE_SENT_JOIN_REQ_FOR_GROUP_TEXT, userLang).formatted(group.getName());
                        adminSender.sendMessage(userId, message);
                    }
                }
                adminUtils.updateSendingMessage((byte) (i + 1), i, sendingMessage);
            }
            adminUtils.remove((byte) 3);
        }

    }

    @Async
    public void kickUsers(List<Order> orders, LocalDateTime now) {
        Map<Long, List<Order>> ordersByGroup = orders.stream()
                .filter(o -> o.getExpireDay().isBefore(now))
                .collect(Collectors.groupingBy(Order::getGroupId));

        for (Long groupId : ordersByGroup.keySet()) {
            Group group = groupRepository.getByGroupId(groupId);
            if (group != null) {
                AdminSender sender = adminController.getSenderByAdminId(group.getAdminId());
                List<Order> expiredOrders = ordersByGroup.get(groupId);
                AdminUtils adminUtils = adminController.getAdminUtils(group.getAdminId());
                expiredOrders.forEach(or -> {
                    sender.kickUser(or.getUserId(), or.getGroupId());
                    sender.sendMessage(or.getUserId(), langService.getMessage(LangFields.YOU_ARE_KICKED_AT_CHANNEL_TEXT, adminUtils.getUserLang(or.getUserId()).formatted(group.getName())));
                });
            }
        }
    }

    @Async
    void sendUsersExpire(List<Order> orders, LocalDateTime now) {
        Map<Long, List<Order>> ordersByGroup = orders.stream()
                .filter(o -> {
                    long between = ChronoUnit.DAYS.between(now.toLocalDate(), o.getExpireDay().toLocalDate());
                    return 0 < between && between <= 5;
                })
                .collect(Collectors.groupingBy(Order::getGroupId));

        for (Long groupId : ordersByGroup.keySet()) {
            Group group = groupRepository.getByGroupId(groupId);
            if (group != null) {
                AdminSender sender = adminController.getSenderByAdminId(group.getAdminId());
                AdminUtils adminUtils = adminController.getAdminUtils(group.getAdminId());
                List<Order> ordersToNotify = ordersByGroup.get(groupId);
                ordersToNotify.forEach(order -> {
                    long daysUntilExpire = ChronoUnit.DAYS.between(now.toLocalDate(), order.getExpireDay().toLocalDate());
                    Chat chat = sender.getChat(groupId);
                    if (chat.getType().equals("channel")) {
                        String text = langService.getMessage(LangFields.EXPIRE_AT_CHANNEL_TEXT, adminUtils.getUserLang(order.getUserId())).formatted(group.getName(), daysUntilExpire);
                        sender.sendMessage(order.getUserId(), text);
                    } else if (chat.getType().equals("group")) {
                        String text = langService.getMessage(LangFields.EXPIRE_AT_GROUP_TEXT, adminUtils.getUserLang(order.getUserId())).formatted(group.getName(), daysUntilExpire);
                        sender.sendMessage(order.getUserId(), text);
                    }

                });
            }
        }
    }
}