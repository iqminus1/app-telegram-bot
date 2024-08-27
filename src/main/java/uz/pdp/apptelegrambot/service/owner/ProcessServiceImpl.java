package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class ProcessServiceImpl implements ProcessService {
    private final MessageService messageService;
    private final CallbackService callbackService;

    @Override
    public void process(Update update) {
        if (update.hasMessage()) {
            messageService.process(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            callbackService.process(update.getCallbackQuery());
        }
    }
}
