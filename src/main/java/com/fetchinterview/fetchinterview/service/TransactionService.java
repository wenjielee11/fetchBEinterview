package com.fetchinterview.fetchinterview.service;

import org.springframework.stereotype.Service;

import com.fetchinterview.fetchinterview.data.TransactionRepository;

@Service
public class TransactionService {
    TransactionRepository repository;
    public TransactionService(TransactionRepository rep){
        repository=rep;
    }

}
