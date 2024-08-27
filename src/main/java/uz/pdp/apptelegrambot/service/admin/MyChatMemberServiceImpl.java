package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.service.admin.bot.AdminSender;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class MyChatMemberServiceImpl implements MyChatMemberService {
    private final GroupRepository groupRepository;
    private final AdminSender sender;

    @Override
    public void process(ChatMemberUpdated chatMember) {
        String username = sender.getBotUsername();
        Long groupId = chatMember.getChat().getId();
        if (Objects.equals(chatMember.getNewChatMember().getUser().getUserName(), username)) {
            if (List.of(ChatMemberOwner.STATUS, ChatMemberAdministrator.STATUS).contains(chatMember.getNewChatMember().getStatus())) {
                groupRepository.findByBotUsername(username).ifPresent(g -> {
                    if (g.getGroupId() == null) {
                        g.setGroupId(groupId);
                        g.setName(chatMember.getChat().getTitle());
                        groupRepository.saveOptional(g);
                    } else {
                        sender.leaveChat(groupId);
                    }
                });
            } else if (List.of(ChatMemberMember.STATUS, ChatMemberLeft.STATUS).contains(chatMember.getNewChatMember().getStatus())) {
                groupRepository.findByGroupId(groupId).ifPresent(g -> {
                    if (g.getGroupId().equals(groupId)) {
                        g.setGroupId(null);
                        groupRepository.saveOptional(g);
                    }
                });
            }
        }
    }
}
