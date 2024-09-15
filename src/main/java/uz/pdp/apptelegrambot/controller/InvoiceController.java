package uz.pdp.apptelegrambot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.entity.Invoice;
import uz.pdp.apptelegrambot.entity.Order;
import uz.pdp.apptelegrambot.entity.Tariff;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.repository.OrderRepository;
import uz.pdp.apptelegrambot.repository.TariffRepository;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.AdminController;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;
import uz.pdp.apptelegrambot.utils.AppConstant;
import uz.pdp.apptelegrambot.utils.admin.AdminUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invoice")
public class InvoiceController {
    private final TariffRepository tariffRepository;
    private final OrderRepository orderRepository;
    private final AdminController adminController;
    private final GroupRepository groupRepository;
    private final LangService langService;

    @PostMapping
    @Async
    public void payment(@RequestBody Invoice invoice) {
        Tariff tariff = tariffRepository.getById(invoice.getId());
        if (!tariff.getPrice().equals(invoice.getAmount())) {
            return;
        }
        Group group = groupRepository.getByIdDefault(invoice.getBotId());
        Long userId = invoice.getUserId();
        Optional<Order> optionalOrder = orderRepository.findByUserIdAndGroupId(userId, group.getGroupId());
        if (optionalOrder.isPresent()) {
            Order order = AppConstant.updateOrderExpire(optionalOrder.get(), tariff.getType());
            orderRepository.save(order);
            return;
        }
        Order order = AppConstant.updateOrderExpire(new Order(userId, group.getGroupId(), LocalDateTime.now(), false), tariff.getType());
        orderRepository.save(order);
        AdminUtils adminUtils = adminController.getAdminUtils(group.getAdminId());
        String userLang = adminUtils.getUserLang(userId);
        AdminSender sender = adminController.getSenderByAdminId(group.getAdminId());
        String link = sender.getLink();
        sender.sendMessage(userId, langService.getMessage(LangFields.SEND_VALID_ORDER_TEXT, userLang) + link);
    }
}
