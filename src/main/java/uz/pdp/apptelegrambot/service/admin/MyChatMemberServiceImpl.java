package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import uz.pdp.apptelegrambot.entity.Group;
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
                Group group = groupRepository.getByBotToken(sender.token);
                if (group != null) {
                    if (group.getGroupId() == null) {
                        group.setGroupId(groupId);
                        group.setName(chatMember.getChat().getTitle());
                        groupRepository.saveOptional(group);
                    } else {
                        sender.leaveChat(groupId);
                    }
                }
            } else if (List.of(ChatMemberMember.STATUS, ChatMemberLeft.STATUS).contains(chatMember.getNewChatMember().getStatus())) {
                Group group = groupRepository.getByGroupId(groupId);
                if (group != null) {
                    if (group.getGroupId().equals(groupId)) {
                        group.setGroupId(null);
                        groupRepository.saveOptional(group);
                    }
                }
            }
        }
    }
}
