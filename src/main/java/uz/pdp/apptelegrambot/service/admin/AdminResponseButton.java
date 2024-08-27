package uz.pdp.apptelegrambot.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import uz.pdp.apptelegrambot.entity.Group;
import uz.pdp.apptelegrambot.enums.LangFields;
import uz.pdp.apptelegrambot.repository.GroupRepository;
import uz.pdp.apptelegrambot.service.ButtonService;
import uz.pdp.apptelegrambot.service.LangService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminResponseButton {
    private final ButtonService buttonService;
    private final GroupRepository groupRepository;
    private final LangService langService;

    @Cacheable(value = "adminResponseServiceStart",key = "#groupId+#lang")
    public ReplyKeyboard start(Long groupId, String lang) {
        Group group = groupRepository.findByGroupId(groupId).orElseThrow();
        List<String> list = new ArrayList<>();
        if (group.isPayment()) {
            list.add(langService.getMessage(LangFields.BUTTON_ADMIN_PAYMENT_CLICK_TEXT, lang));
            list.add(langService.getMessage(LangFields.BUTTON_ADMIN_PAYMENT_PAYME_TEXT, lang));
        }
        if (group.isScreenShot()) {
            list.add(langService.getMessage(LangFields.BUTTON_ADMIN_PAYMENT_SCREENSHOT_TEXT, lang));
        }
        if (group.isCode()) {
            list.add(langService.getMessage(LangFields.BUTTON_ADMIN_PAYMENT_CODE_TEXT, lang));
        }
        list.add(langService.getMessage(LangFields.BUTTON_LANG_SETTINGS, lang));

        return buttonService.withString(list);
    }
}
