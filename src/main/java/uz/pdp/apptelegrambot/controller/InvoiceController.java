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
import uz.pdp.apptelegrambot.enums.ExpireType;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.repository.OrderRepository;
import uz.pdp.apptelegrambot.repository.TariffRepository;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.AdminController;

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
        Optional<Order> optionalOrder = orderRepository.findByUserIdAndGroupId(invoice.getUserId(), group.getGroupId());
        if (optionalOrder.isPresent()) {
            String text = langService.getMessage(null,null);
            Order order = optionalOrder.get();
            if (tariff.getType().equals(ExpireType.UNLIMITED)) {
                order.setUnlimited(true);
            }
            return;
        }
    }
}
