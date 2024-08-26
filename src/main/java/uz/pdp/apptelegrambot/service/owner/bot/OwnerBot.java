package uz.pdp.apptelegrambot.service.owner.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.pdp.apptelegrambot.service.owner.ProcessService;
import uz.pdp.apptelegrambot.utils.AppConstant;

@Component
public class OwnerBot extends TelegramLongPollingBot {
    private final ProcessService processService;

    public OwnerBot(ProcessService processService) {
        super(new DefaultBotOptions(), AppConstant.TOKEN);
        this.processService = processService;
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        processService.process(update);
    }

    @Override
    public String getBotUsername() {
        return AppConstant.USERNAME;
    }
}
