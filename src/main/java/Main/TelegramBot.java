package Main;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramBot extends TelegramLongPollingBot {

    private final Manage manager = new Manage(this);

    @Override
    public String getBotUsername() {
        return "P2PCryptoBBot";
    }

    @Override
    public String getBotToken() {
        return "5559385419:AAHpZEcnKB2UsDK_5IRlRAw7JCioFzMYDpo";
    }

    @Override
    public void onUpdateReceived(Update update) {
        manager.manage(update);
    }
}
