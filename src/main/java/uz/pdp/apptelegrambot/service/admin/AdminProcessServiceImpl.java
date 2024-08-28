package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
public class AdminProcessServiceImpl implements AdminProcessService {
    private final MyChatMemberService myChatMemberService;
    private final ChatJoinRequestService chatJoinRequestService;
    private final AdminMessageService adminMessageService;
    private final AdminCallbackService callbackService;

    @Override
    public void process(Update update, Long adminId) {
        if (update.hasMyChatMember()) {
            myChatMemberService.process(update.getMyChatMember());
        } else if (update.hasChatJoinRequest()) {
            chatJoinRequestService.process(update.getChatJoinRequest());
        } else if (update.hasMessage()) {
            adminMessageService.process(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            callbackService.process(update.getCallbackQuery());
        }
    }
}
