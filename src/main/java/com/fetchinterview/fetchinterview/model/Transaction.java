package com.fetchinterview.fetchinterview.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * This class represents a model used for a request body. 
 */


public class Transaction{
    public final String payer;
    public long points;
    public final LocalDateTime timeStamp;

    public Transaction(String payer, long points, String timeStamp){
        this.payer = payer;
        this.points = points;
        this.timeStamp = LocalDateTime.parse(timeStamp, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]'Z'"));
    }
    public Transaction(String payer, long points, LocalDateTime timeStamp){
        this.payer = payer;
        this.points = points;
        this.timeStamp = timeStamp;
    }
    public LocalDateTime getTimeStamp(){
        return this.timeStamp;
    }
    @Override
    public String toString(){
        return "Payer: "+ payer + "points: "+ points + "timeStamp: "+ timeStamp;
    }
}


