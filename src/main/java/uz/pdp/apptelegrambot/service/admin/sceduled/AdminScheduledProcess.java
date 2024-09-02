package uz.pdp.apptelegrambot.service.admin.sceduled;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
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
        List<Order> orders = orderRepository.findAllByExpireDayBeforeAndUnlimited(LocalDateTime.now().plusDays(5).plusMinutes(1), false);
        kickUsers(orders, now);
        sendUsersExpire(orders, now);
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
                    sender.sendMessage(or.getUserId(), langService.getMessage(LangFields.YOU_ARE_KICKED_AT_CHANNEL_TEXT, adminUtils.getUserLang(or.getUserId())));
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
                    sender.sendMessage(order.getUserId(),
                            langService.getMessage(LangFields.EXPIRE_AT_TEXT, adminUtils.getUserLang(order.getUserId())).formatted(daysUntilExpire));
                });
            }
        }
    }
}