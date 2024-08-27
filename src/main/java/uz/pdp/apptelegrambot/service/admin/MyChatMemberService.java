package uz.pdp.apptelegrambot.service.admin;

import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;

public interface MyChatMemberService {
    void process(ChatMemberUpdated chatMember);
}
