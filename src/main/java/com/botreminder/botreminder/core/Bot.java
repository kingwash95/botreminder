package com.botreminder.botreminder.core;

import com.botreminder.botreminder.database.entity.Records;
import com.botreminder.botreminder.database.repository.RecordsRepository;
import com.botreminder.botreminder.database.service.RecordsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
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
            try {
                sendReminderMessage();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        commandDate(inText);

    }

    //Handler method for incoming commands
    public void commandDate(String inText) {
        if (inText.contains("/add")) {
            sendMessage("Введите дату в формате ДД-ММ-ГГГГ, время в формате ЧЧ:ММ и текст напоминания по следующему примеру:'22-08-2019 14:30, купить хлеб");
            key = 1;
        }
        else if(inText.contains("/show")){
            showRecordsOfUser(chatId);
        }
    }

    //A method that adds data from an incoming message to the database
    public void toDataBase(String inText) {
        key = 0;
        //We divide the incoming message into components for entering into the database
        String[] parts = inText.split(", ");
        stringData = parts[0];
        stringReminder = parts[1];
        Timestamp dateSql = getDate(stringData);
        //Create a new record in the database
        Records records = new Records(chatId, dateSql, stringReminder);
        recordsService.createRecords(records);
        sendMessage("Ваше напоминание создано");
    }

    //Method that converts a string with a date of a reminder to the sql format
    public Timestamp getDate(String stringData) {
        Timestamp dateSql = null;
        try {
            //data in form is in this format
            DateFormat dF = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            logger.info("stringData is " + stringData);
            // string data is converted into java util date
            java.util.Date datejava = dF.parse(stringData+":00");
            logger.info("dateJava is " + datejava);
            //converted date is reformatted for conversion to sql.timestamp
            DateFormat dFsql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // java util date is converted to compatible java sql timestamp
            String ndt = dFsql.format(datejava);
            logger.info("ndt is " + ndt);
            // finally data from the form is converted to java sql. timestamp for placing in database
            dateSql = Timestamp.valueOf(ndt);

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

    //Method that sends remind messages
    public void sendReminderMessage() throws ParseException {
        List<Records> records = recordsService.findAllByNotifiedIsNull();
        for (Records record : records) {
            Timestamp dateSqL = record.getDate();
            String stringDateSqL = String.valueOf(dateSqL);
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date date = dateFormatter.parse(stringDateSqL);
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    long chatId = record.getChatId();
                    String text = record.getText();
                    Timestamp date = record.getDate();
                    try {
                        SendMessage outMessage = new SendMessage();
                        outMessage.setChatId(chatId);
                        outMessage.setText(text);
                        execute(outMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    recordsService.updateNotifiedField(chatId, date, text);
                }
            };
            Timer timer = new Timer();
            timer.schedule(tt, date);
        }
    }

    //Method that shows all records of user
    public void showRecordsOfUser(long chatId){
        String shows = "Список ваших напоминаний: \n";
        List<Records> records = recordsService.findAllByNotifiedIsNullAndChatIdIs(chatId);
        if(records!=null && !records.isEmpty()) {
            for (Records record : records) {
                Timestamp dateSqL = record.getDate();
                DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                String dateUser = dateFormatter.format(dateSqL);
                String textUser = record.getText();
                shows = shows + "* " + dateUser + ", " + textUser + "\n";
            }
            sendMessage(shows);
        }
        else {sendMessage("*Список ваших напоминаний пуст*");}
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