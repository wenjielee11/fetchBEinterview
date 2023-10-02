package com.fetchinterview.fetchinterview.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fetchinterview.fetchinterview.data.TransactionRepository;
import com.fetchinterview.fetchinterview.model.Transaction;

/**
 * This class contains the REST API that listens to PORT 8080.
 */
@RestController
public class TransactionController {
    TransactionRepository db = new TransactionRepository();

    @PostMapping("/add")
    public ResponseEntity<?> addPoints(@RequestBody PointTransactionRequest request){
        try{
            Transaction newTransaction = new Transaction(request.getPayer(), Long.parseLong(request.getPoints()), request.timestamp());
             return db.addTransaction(newTransaction.payer, newTransaction);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.toString());
        } 
    }

    @PostMapping("/spend")
    public ResponseEntity<String> spendPoints(@RequestBody PointTransactionRequest request){
        return db.spendPoints(Long.valueOf(request.getPoints()));
    }
    @GetMapping("/balance")
    public ResponseEntity<String> getBalance(){
        return db.getBalance();
    }

}
/**
 * Springboot deserializes the body into this class
 */
class PointTransactionRequest {
    
    private String payer;
    private String points;
    private String timestamp;

    // Getter and setter methods
    @JsonGetter
    public String getPayer(){
        return payer;
    }
    @JsonGetter
    public String getPoints(){
        return points;
    }
    @JsonGetter
    public String timestamp(){
        return timestamp;
    }
}

