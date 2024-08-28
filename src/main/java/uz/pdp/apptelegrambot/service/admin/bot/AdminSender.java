package uz.pdp.apptelegrambot.service.admin.bot;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StreamUtils;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.groupadministration.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.apptelegrambot.entity.Order;
import uz.pdp.apptelegrambot.utils.AppConstant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AdminSender extends DefaultAbsSender {
    private String username = null;
    public final String token;

    public AdminSender(String token) {
        super(new DefaultBotOptions(), token);
        this.token = token;
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

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void process() {
        username = null;
    }

    public String getFilePath(PhotoSize photoSize) {
        GetFile getFile = new GetFile(photoSize.getFileId());
        try {
            File execute = execute(getFile);

            String fileUrl = execute.getFileUrl(token);

            String fileName = UUID.randomUUID().toString();
            String[] split = fileUrl.split("\\.");
            String fileExtension = split[split.length - 1];
            String filePath = fileName + "." + fileExtension;

            Path targetPath = Paths.get(AppConstant.FILE_PATH, filePath);

            Files.createDirectories(targetPath.getParent());

            try (InputStream inputStream = new URL(fileUrl).openStream();
                 OutputStream outputStream = Files.newOutputStream(targetPath)) {
                StreamUtils.copy(inputStream, outputStream);
            }

            return targetPath.toString();
        } catch (TelegramApiException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLink(Long groupId) {
        try {
            EditChatInviteLink editChatInviteLink = new EditChatInviteLink();
            editChatInviteLink.setChatId(groupId);
            editChatInviteLink.setName(AppConstant.LINK_NAME);
            ChatInviteLink execute = execute(editChatInviteLink);
            return execute.getInviteLink();
        } catch (TelegramApiException e) {
            CreateChatInviteLink createChatInviteLink = new CreateChatInviteLink();
            createChatInviteLink.setChatId(groupId);
            createChatInviteLink.setCreatesJoinRequest(true);
            createChatInviteLink.setName(AppConstant.LINK_NAME);
            ChatInviteLink execute = null;
            try {
                execute = execute(createChatInviteLink);
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
            return execute.getInviteLink();
        }
    }
}
