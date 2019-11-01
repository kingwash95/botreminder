package com.botreminder.botreminder.core;

import com.botreminder.botreminder.database.entity.Records;
import com.botreminder.botreminder.database.service.RecordsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


@Service
@EnableScheduling
@PropertySource("classpath:telegram.properties")
public class Bot extends TelegramLongPollingBot {
    @Autowired
    private RecordsService recordsService;

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String botToken;


    private long chatId;
    private String inText;
    private int key = 0;
    private String stringData;
    private String stringReminder;
    private String stringTime;
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);


    @PostConstruct
    private void init() {
        TelegramBotsApi telegram = new TelegramBotsApi();
        try {
            telegram.registerBot(this);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
        logger.info("Telegram Bot initialized");
    }


    //Handler method for incoming messages. @param update object containing information about incoming messages
    @Override
    public void onUpdateReceived(Update update) {

        //Check if there is a message and whether it is text
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;
        //Retrieving the Incoming Message Object
        Message inMessage = update.getMessage();
        //Retrieving chat id
        chatId = update.getMessage().getChatId();
        //Retrieving the text of the incoming message
        inText = update.getMessage().getText();
        if (key == 1) {
            toDataBase(inText);
        }
        commandDate(inText);

    }

    //Handler method for incoming commands
    public void commandDate(String inText) {
        if (inText.contains("/add")) {
            sendMessage("Введите дату в формате ДД-ММ-ГГГГ, время в формате ЧЧ:ММ и текст напоминания по следующему примеру:'22-08-2019, 14:30, купить хлеб");
            key = 1;
        }
    }

    //A method that adds data from an incoming message to the database
    public void toDataBase(String inText) {
        key = 0;
        //We divide the incoming message into components for entering into the database
        String[] parts = inText.split(", ");
        stringData = parts[0];
        stringTime = parts[1];
        stringReminder = parts[2];
        //sendMessage(stringData + " " + stringReminder);
        Date dateSql = getDate(stringData);
        //Create a new record in the database
        Records records = new Records(chatId, dateSql, stringReminder, stringTime );
        recordsService.createRecords(records);
        sendMessage("Ваше напоминание создано");
    }

    //Method that converts a string with a date of a reminder to the sql format
    public Date getDate(String stringData) {
        Date dateSql = null;
        try {
            //data in form is in this format
            DateFormat dF = new SimpleDateFormat("dd-MM-yyyy");
            // string data is converted into java util date
            java.util.Date datejava = dF.parse(stringData);
            //converted date is reformatted for conversion to sql.date
            DateFormat dFsql = new SimpleDateFormat("yyyy-MM-dd");
            // java util date is converted to compatible java sql date
            String ndt = dFsql.format(datejava);
            // finally data from the form is converted to java sql. date for placing in database
            dateSql = Date.valueOf(ndt);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateSql;
    }

    //Method that implements sending messages
    public void sendMessage(String outText) {
        try {
            //Create an outgoing message
            SendMessage outMessage = new SendMessage();
            //We indicate in which chat we will send a message
            outMessage.setChatId(chatId);
            //Specify the message text
            outMessage.setText(outText);
            //Send message
            execute(outMessage);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //The method, which runs every day at midnight, finds reminders for today and sends a message to the user who created the reminder at the user's time
    @Scheduled(cron = "0 0 0 * * ?")
    public void sendReminderMessage() throws ParseException {
        java.util.Date dateJava = new java.util.Date();
        DateFormat dFsql = new SimpleDateFormat("yyyy-MM-dd");
        String ndt = dFsql.format(dateJava);
        java.sql.Date dateSql = java.sql.Date.valueOf(ndt);
        List<Records> records = recordsService.findAllByDate(dateSql);
        for (Records record : records) {
            Date dateSqL = record.getDate();
            String stringDateSqL = String.valueOf(dateSqL);
            String time = record.getTime();
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            java.util.Date date = dateFormatter.parse(stringDateSqL + " " + time);
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    long chatId = record.getChatId();
                    String text = record.getText();
                    try {
                        SendMessage outMessage = new SendMessage();
                        outMessage.setChatId(chatId);
                        outMessage.setText(text);
                        execute(outMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                }
            };
            Timer timer = new Timer();
            timer.schedule(tt, date);

        }
    }


    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}