package uz.pdp.apptelegrambot.service.admin.sceduled;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.entity.Order;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.repository.OrderRepository;
import uz.pdp.apptelegrambot.service.admin.AdminController;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminScheduledProcess {
    private final OrderRepository orderRepository;
    private final GroupRepository groupRepository;
    private final AdminController adminController;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void kickUsersFromGroups() {
        Map<Long, List<Order>> collect = orderRepository.findAll().stream()
                .filter(o -> o.getExpireDay().isBefore(LocalDateTime.now()))
                .collect(Collectors.groupingBy(Order::getGroupId));
        for (Long groupId : collect.keySet()) {
            Group group = groupRepository.findByGroupId(groupId).orElseThrow();
            AdminSender sender = adminController.getSenderByAdminId(group.getAdminId());
            List<Order> orders = collect.get(groupId);
            sender.kickUsers(orders);
        }
    }
}
