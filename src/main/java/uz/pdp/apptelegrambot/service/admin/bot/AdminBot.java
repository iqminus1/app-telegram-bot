package uz.pdp.apptelegrambot.service.admin.bot;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.pdp.apptelegrambot.service.admin.AdminProcessService;

public class AdminBot extends TelegramLongPollingBot {
    private final AdminProcessService adminProcessService;
    private final Long adminId;

    public AdminBot(String token, Long adminId, AdminProcessService adminProcessService) {
        super(new DefaultBotOptions(), token);
        this.adminProcessService = adminProcessService;
        this.adminId = adminId;
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        adminProcessService.process(update, adminId);
    }

    @Override
    public String getBotUsername() {
        return "1";
    }
}
