package uz.pdp.apptelegrambot.service.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class AdminProcessServiceImpl implements AdminProcessService {
    @Override
    public void process(Update update,Long adminId) {

    }
}
