package uz.pdp.apptelegrambot.service.admin;

import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;

public interface ChatJoinRequestService {
    void process(ChatJoinRequest chatJoinRequest);
}
