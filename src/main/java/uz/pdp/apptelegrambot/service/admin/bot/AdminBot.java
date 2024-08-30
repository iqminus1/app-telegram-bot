package uz.pdp.apptelegrambot.service.admin.bot;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.service.admin.AdminProcessService;

import java.time.LocalDateTime;

public class AdminBot extends TelegramLongPollingBot {
    private final AdminProcessService adminProcessService;
    private final GroupRepository groupRepository;
    private final Long adminId;
    private final String token;

    public AdminBot(String token, Long adminId, AdminProcessService adminProcessService, GroupRepository groupRepository) {
        super(new DefaultBotOptions(), token);
        this.adminProcessService = adminProcessService;
        this.adminId = adminId;
        this.token = token;
        this.groupRepository = groupRepository;
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Group group = groupRepository.getByBotToken(token);
        LocalDateTime expireAt = group.getExpireAt();
        if (group.isWorked() && LocalDateTime.now().isBefore(expireAt))
            adminProcessService.process(update, adminId);
    }

    @Override
    public String getBotUsername() {
        return "1";
    }

}
