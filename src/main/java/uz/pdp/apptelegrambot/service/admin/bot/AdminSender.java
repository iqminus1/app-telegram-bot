package uz.pdp.apptelegrambot.service.admin.bot;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.apptelegrambot.entity.Order;

import java.util.List;

@EnableAsync
public class AdminSender extends DefaultAbsSender {

    public AdminSender(String token) {
        super(new DefaultBotOptions(), token);
    }

    @Async
    public void kickUser(Long userId, Long groupId) {
        try {
            execute(new BanChatMember(groupId.toString(), userId));
            execute(new UnbanChatMember(groupId.toString(), userId));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    public void kickUsers(List<Order> orders) {
        for (Order order : orders) {
            kickUser(order.getUserId(), order.getGroupId());
        }
    }

}
