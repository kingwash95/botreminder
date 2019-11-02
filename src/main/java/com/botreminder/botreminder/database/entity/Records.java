package com.botreminder.botreminder.database.entity;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "records")
public class Records {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chatid")
    private Long chatId;

    @Column(name = "date")
    private Timestamp date;

    @Column(name = "text")
    private String text;

    @Column(name="notified")
    private String notified;


    public Records() { }

    public Records(Long chatId, Timestamp date, String text) {
        this.chatId = chatId;
        this.date = date;
        this.text = text;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNotified() { return notified; }

    public void setNotified(String notified) { this.notified = notified; }
}
