package uz.pdp.apptelegrambot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class ProcessServiceImpl implements ProcessService {
    private final MessageService messageService;
    @Override
    public void process(Update update) {
        if (update.hasMessage()) {
            messageService.process(update.getMessage());
        }
    }
}
