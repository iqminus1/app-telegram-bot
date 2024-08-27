package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import uz.pdp.apptelegrambot.entity.Order;
import uz.pdp.apptelegrambot.enums.LangEnum;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.repository.OrderRepository;
import uz.pdp.apptelegrambot.service.LangService;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;
import uz.pdp.apptelegrambot.utils.admin.AdminUtils;

import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
public class ChatJoinRequestServiceImpl implements ChatJoinRequestService {
    private final AdminSender sender;
    private final OrderRepository orderRepository;
    private final LangService langService;
    private final AdminResponseButton adminResponseButton;
    private final AdminUtils adminUtils;

    @Override
    public void process(ChatJoinRequest chatJoinRequest) {
        Long userId = chatJoinRequest.getUser().getId();
        Long groupId = chatJoinRequest.getChat().getId();
        Optional<Order> optionalOrder = orderRepository.findByUserIdAndGroupId(userId, groupId);
        if (optionalOrder.isPresent()) {
            if (optionalOrder.get().getExpireDay().after(new Date())) {
                sender.acceptJoinRequest(userId, groupId);
                return;
            }
        }
        sender.openChat(userId, groupId);
        String languageCode = chatJoinRequest.getUser().getLanguageCode();
        String defaultLang = null;
        for (LangEnum value : LangEnum.values()) {
            if (value.name().equalsIgnoreCase(languageCode)) {
                defaultLang = value.name();
            }
        }
        if (defaultLang == null) {
            defaultLang = LangEnum.RU.name();
        }
        adminUtils.setUserLang(userId, defaultLang);
        String message = langService.getMessage(LangFields.PAID_GROUP_TEXT, languageCode);
        sender.sendMessage(userId, message, adminResponseButton.start(groupId, defaultLang));
    }
}
