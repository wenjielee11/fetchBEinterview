package com.fetchinterview.fetchinterview.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fetchinterview.fetchinterview.data.TransactionRepository;
import com.fetchinterview.fetchinterview.model.Transaction;

@RestController
public class PointsController {
    TransactionRepository db = new TransactionRepository();

    @PostMapping("/add")
    public ResponseEntity<?> addPoints(@RequestParam(value = "payer") String payer, @RequestParam(value = "points") String points, @RequestParam(value="timestamp") String timestamp){
        try{
            Transaction newTransaction = new Transaction(payer, Long.parseLong(points), timestamp);
             return db.addTransaction(newTransaction.payer, newTransaction);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.toString());
        } 
    }

    @PostMapping("/spend")
    public ResponseEntity<String> spendPoints(@RequestParam(value = "points") String points){
        return db.spendPoints(Long.valueOf(points));
    }
    @GetMapping("/balance")
    public ResponseEntity<String> getBalance(){
        return db.getBalance();
    }

}