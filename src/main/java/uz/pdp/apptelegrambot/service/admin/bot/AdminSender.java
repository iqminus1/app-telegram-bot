package uz.pdp.apptelegrambot.service.admin.bot;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ApproveChatJoinRequest;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.apptelegrambot.entity.Order;

import java.util.List;

@EnableAsync
public class AdminSender extends DefaultAbsSender {
    private String username = null;

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

    @Async
    public void leaveChat(Long groupId) {
        try {
            execute(new LeaveChat(groupId.toString()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public String getBotUsername() {
        try {
            if (username == null) {
                User bot = execute(new GetMe());
                username = bot.getUserName();
            }
            return username;
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void openChat(Long userId, Long groupId) {
        acceptJoinRequest(userId, groupId);
        kickChatMember(userId, groupId);
    }

    public void acceptJoinRequest(Long userId, Long groupId) {
        ApproveChatJoinRequest acceptJoinReq = new ApproveChatJoinRequest();
        acceptJoinReq.setUserId(userId);
        acceptJoinReq.setChatId(groupId);
        try {
            execute(acceptJoinReq);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void kickChatMember(Long userId, Long groupId) {
        try {
            String string = groupId.toString();
            BanChatMember banChatMember = new BanChatMember(string, userId);
            execute(banChatMember);
            UnbanChatMember unbanChatMember = new UnbanChatMember(string, userId);
            execute(unbanChatMember);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Long userId, String text, ReplyKeyboard button) {
        SendMessage sendMessage = new SendMessage(userId.toString(), text);
        sendMessage.setReplyMarkup(button);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Long userId, String text) {
        sendMessage(userId, text, null);
    }

    public void sendMessageAndRemove(Long userId, String text) {
        sendMessage(userId, text, new ReplyKeyboardRemove(true));
    }
}
