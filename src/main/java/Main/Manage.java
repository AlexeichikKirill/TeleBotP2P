package Main;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Manage {

    private final TelegramBot bot;

    public Manage(TelegramBot bot) {
        this.bot = bot;
    }

    public void manage(Update update) {
        long chatId = getMessage(update).getChatId();
        String request = getRequest(update);

        if (update.hasCallbackQuery()) {
            deleteMessage(chatId, getMessage(update).getMessageId());
        }

        switch (request) {
            case "/start" -> sendMessage(chatId, "Выберите",
                    createKeyboardMarkup(mainMenu()));
            case "!получить" -> sendMessage(chatId, getInfo("BTC/USDT"),
                    createKeyboardMarkup(List.of(buttonBack())));
            case "!обменять BTC на USDT" -> sendInvoice(chatId);

            default -> sendMessage(chatId, "Не понимаю)", createKeyboardMarkup(List.of(buttonBack())));
        }
    }

    private void sendInvoice(long chatId){
        SendInvoice sendInvoice = new SendInvoice();
        sendInvoice.setChatId(chatId);
        sendInvoice.setPrices(List.of(LabeledPrice.builder().amount(10000).label("Label").build()));
        sendInvoice.setPayload("Payload");
        sendInvoice.setDescription("Description");
        sendInvoice.setTitle("Title");
        sendInvoice.setProviderToken("284685063:TEST:MzkzZjI1YjY5YmY4");
        sendInvoice.setCurrency("USD");
        sendMessage(chatId, "Выберите",
                createKeyboardMarkup(mainMenu()));

        try {
            bot.execute(sendInvoice);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String getInfo(String pair) {
        String out = "";
        Connection connection = Jsoup
                .connect("https://api.wavesplatform.com/v0/pairs" + getIdAssets(pair))
                .ignoreContentType(true);
        try {
            String str = connection.get().text();
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(str);
            JSONObject object = (JSONObject) jsonObject.get("data");
            out = object.get("lastPrice").toString();

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return out;
    }

    private String getIdAssets(String pair){
        String path = "/";
        for (String nameAsset : pair.split("/")) {
            path += getIdAsset(nameAsset) + "/";
        }
        return path;
    }

    private String getIdAsset(String nameAsset){
        Connection connection = Jsoup.connect("https://api.wavesplatform.com/v0/assets?ticker=" + nameAsset)
                .ignoreContentType(true);
        String out = "";
        try {
            String jsonString = connection.get().text();
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonString);
            JSONArray listAssets = (JSONArray) jsonObject.get("data");
            JSONObject paramData = (JSONObject) listAssets.get(0);
            JSONObject asset = (JSONObject) paramData.get("data");
            out = asset.get("id").toString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return out;
    }

    private void sendMessage(long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(markup);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private static InlineKeyboardMarkup createKeyboardMarkup(List<List<InlineKeyboardButton>> lists) {
        return new InlineKeyboardMarkup(lists);
    }

    private static InlineKeyboardButton createButton(String buttonText, String buttonCallbackData) {
        return InlineKeyboardButton.builder()
                .callbackData(buttonCallbackData)
                .text(buttonText)
                .build();
    }

    private static InlineKeyboardButton createPayButton(String buttonText, String buttonCallbackData) {
        InlineKeyboardButton button = createButton(buttonText, buttonCallbackData);
        button.setPay(true);
        return button;
    }

    private void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try {
            bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static List<List<InlineKeyboardButton>> mainMenu() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton getAmount = createButton("Получить", "!получить");
        InlineKeyboardButton exchange = createPayButton("Обменять BTC на USDT", "!обменять BTC на USDT");
        keyboard.add(List.of(getAmount));
        keyboard.add(List.of(exchange));
        return keyboard;
    }

    private static List<InlineKeyboardButton> buttonBack() {
        return List.of(InlineKeyboardButton.builder()
                .callbackData("/start")
                .text("К меню")
                .build());
    }

    private String getRequest(Update update) {
        return update.hasCallbackQuery() ?
                update.getCallbackQuery().getData() : update.getMessage().getText();
    }

    private Message getMessage(Update update) {
        return update.hasCallbackQuery() ?
                update.getCallbackQuery().getMessage() : update.getMessage();
    }
}
