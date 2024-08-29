package uz.pdp.apptelegrambot.service.owner.bot;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.apptelegrambot.utils.AppConstant;

import java.io.File;

@Component
public class OwnerSender extends DefaultAbsSender {
    public OwnerSender() {
        super(new DefaultBotOptions(), AppConstant.TOKEN);
    }

    public void sendMessage(Long userId, String text, ReplyKeyboard button) {
        SendMessage sendMessage = new SendMessage(userId.toString(), text);
        sendMessage.setReplyMarkup(button);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Long userId, String text) {
        sendMessage(userId, text, null);
    }

    public void sendMessageAndRemove(Long userId, String text) {
        sendMessage(userId, text, new ReplyKeyboardRemove(true));
    }

    @Async
    public void autoRemoveKeyboard(Long userId) {
        try {
            SendMessage sendMessage = new SendMessage(userId.toString(), ".");
            sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
            Message message = execute(sendMessage);
            deleteMessage(userId, message.getMessageId());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(Long userId, Integer messageId) {
        try {
            execute(new DeleteMessage(userId.toString(), messageId));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeTextAndKeyboard(Long userId, Integer messageId, String text, InlineKeyboardMarkup keyboardMarkup) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText(text);
        editMessageText.setMessageId(messageId);
        editMessageText.setChatId(userId);
        editMessageText.setReplyMarkup(keyboardMarkup);
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPhoto(Long userId, String caption, String path, InlineKeyboardMarkup keyboard) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setCaption(caption);
        InputFile photo = new InputFile();
        photo.setMedia(new File(path));
        sendPhoto.setPhoto(photo);
        sendPhoto.setChatId(userId);
        sendPhoto.setReplyMarkup(keyboard);
        executeAsync(sendPhoto);
    }
}
