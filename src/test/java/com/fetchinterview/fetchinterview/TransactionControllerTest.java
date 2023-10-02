package com.fetchinterview.fetchinterview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.util.LinkedList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fetchinterview.fetchinterview.controller.TransactionController;
import com.fetchinterview.fetchinterview.data.TransactionRepository;
import com.fetchinterview.fetchinterview.model.Transaction;

/**
 * This class tests the transaction service implemented. REST api tests may be done using curl via terminal. More information given at read.me
 */
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    TransactionRepository transactionRepository;

    LinkedList <Transaction> requests;

    @BeforeEach
    public void createRequest(){
        transactionRepository = new TransactionRepository();
        // Prepare list of requests
        requests = new LinkedList<>();
                // 0 points should not cause any errors.
                requests.add(new Transaction("UNILEVER", 0, "2022-11-01T14:00:01Z"));
                requests.add(new Transaction("DANNON", 300, "2022-10-31T10:00:00Z"));
                requests.add(new Transaction("UNILEVER", 200, "2022-10-31T11:00:00Z"));
                requests.add(new Transaction("DANNON", -200, "2022-10-31T15:00:00Z"));    
                requests.add(new Transaction("MILLER COORS", 10000, "2022-11-01T14:00:00Z"));
                requests.add(new Transaction("DANNON", 1000, "2022-11-02T14:00:00Z"));

        for(Transaction request : requests){
            transactionRepository.addTransaction(request.payer, request);
        }
    }

    @Test
    public void testAddPointsToHashMap(){
            long points = 0L;
            // Send each request and verify the response
            for (Transaction request : requests) {
                //Calculating total points
                points += request.points;
                // No content expected
                // Asserting and checking the database.
                assertTrue(transactionRepository.transactionMap.containsKey(request.payer));
                LinkedList<Transaction> transactions = transactionRepository.transactionMap.get(request.payer);
                boolean hasTransaction = false;
                for(Transaction transaction: transactions){
                    if( request.points == transaction.points && request.timeStamp.equals(transaction.timeStamp)){
                        hasTransaction = true;
                        break;
                    }
                }
                assertTrue(hasTransaction, "HashMap Does not contain transaction" + request);
            }
            assertEquals(points, transactionRepository.totalPoints);
            // Comparing the total points of every payer
            assertEquals(1100, transactionRepository.transactionMap.get("DANNON").getFirst().points);
            assertEquals(200, transactionRepository.transactionMap.get("UNILEVER").getFirst().points);
            assertEquals(10000, transactionRepository.transactionMap.get("MILLER COORS").getFirst().points);


    }

    @Test
    public void testAddPointsToQueue(){
        // Checking the top of the queue. Should not be the 0 points payer.
        assertEquals(transactionRepository.transactionQueue.peek().payer, "DANNON");
        assertEquals(transactionRepository.transactionQueue.peek().points, 300);
        boolean hasElement = false;
        for(Transaction request:requests){
            for(Transaction trans: transactionRepository.transactionQueue){
                if(request.equals(trans)){
                    hasElement = true;
                    break;
                }
            }
            assertTrue(hasElement, "Error, queue does not contain "+ request);
        }
        // Test if queue is ordered by time stamp.
        requests = new LinkedList<>();
                // 0 points should not cause any errors. Elements popped should be in this order
                requests.add(new Transaction("DANNON", 300, "2022-10-31T10:00:00Z"));
                requests.add(new Transaction("UNILEVER", 200, "2022-10-31T11:00:00Z"));
                requests.add(new Transaction("DANNON", -200, "2022-10-31T15:00:00Z"));
                requests.add(new Transaction("MILLER COORS", 10000, "2022-11-01T14:00:00Z"));
                requests.add(new Transaction("UNILEVER", 0, "2022-11-01T14:00:01Z"));
                requests.add(new Transaction("DANNON", 1000, "2022-11-02T14:00:00Z"));
        for(Transaction request:requests){
            assertEquals(request, transactionRepository.transactionQueue.poll());
        }
        
    }
    
    @Test
    public void testSpendPointsAtQueue() {
        // Getting the total number of transactions stored in the map.
    

        transactionRepository.spendPoints(5000);
        // Check that Miller Coors and other payers still remains in the queue, and the balance is correct.
        assertEquals(transactionRepository.transactionQueue.peek().payer, "MILLER COORS", "ERROR transaction does not exist after spending in queue");
        assertEquals(transactionRepository.transactionQueue.peek().points, 5300, "ERROR transaction points does not match after spending 200 in queue");
        assertTrue(transactionRepository.transactionQueue.contains(new Transaction("UNILEVER", 0, "2022-11-01T14:00:01Z")));
        assertTrue(transactionRepository.transactionQueue.contains(new Transaction("DANNON", 1000, "2022-11-02T14:00:00Z")));
        assertEquals(transactionRepository.transactionQueue.size(), 3, "queue size does not match after spending.");

        createRequest(); // Brand new repository.

        transactionRepository.spendPoints(200);
        assertEquals(transactionRepository.transactionQueue.peek().payer, "DANNON", "ERROR transaction does not exist after spending 200 in queue");
        assertEquals(transactionRepository.transactionQueue.peek().points, 100, "ERROR transaction points does not match after spending 200 in queue");

    }

    @Test
    public void testSpendPointsAtHashMap(){
        transactionRepository.spendPoints(5000);
        int[] sizes =  
            new int[]{ transactionRepository.transactionMap.get("DANNON").size() -1, transactionRepository.transactionMap.get("UNILEVER").size() -1, transactionRepository.transactionMap.get("MILLER COORS").size() -1};
         // Check that the sizes remain the same after spending points, no transaction was lost.
        int[] result = new int[]{ transactionRepository.transactionMap.get("DANNON").size() -1, transactionRepository.transactionMap.get("UNILEVER").size() -1, transactionRepository.transactionMap.get("MILLER COORS").size() -1};
        for(int i =0; i< sizes.length;i++){
            assertEquals(sizes[i], result[i],  "Error transaction was lost after spending");
        }
        for (Transaction request : requests) {
                // Asserting and checking the database to ensure no value was affected after spending.
                assertTrue(transactionRepository.transactionMap.containsKey(request.payer));
                LinkedList<Transaction> transactions = transactionRepository.transactionMap.get(request.payer);
                boolean hasTransaction = false;
                for(Transaction transaction: transactions){
                    if( request.points == transaction.points && request.timeStamp.equals(transaction.timeStamp)){
                        hasTransaction = true;
                        break;
                    }
                }
            assertTrue(hasTransaction, "HashMap Does not contain transaction" + request);
        }

        // Testing the total points of all payers to ensure they are not negative
        assertEquals(transactionRepository.transactionMap.get("DANNON").getFirst().points, 1000);
        assertEquals(transactionRepository.transactionMap.get("UNILEVER").getFirst().points, 0);
        assertEquals(transactionRepository.transactionMap.get("MILLER COORS").getFirst().points, 5300);
    }

    public void testSpendPointsNegative(){
        transactionRepository.spendPoints(100000);
        for (Transaction request : requests) {
                // Asserting and checking the database to ensure no value was affected after spending.
                assertTrue(transactionRepository.transactionMap.containsKey(request.payer));
                LinkedList<Transaction> transactions = transactionRepository.transactionMap.get(request.payer);
                boolean hasTransaction = false;
                for(Transaction transaction: transactions){
                    if( request.points == transaction.points && request.timeStamp.equals(transaction.timeStamp)){
                        hasTransaction = true;
                        break;
                    }
                }
            assertTrue(hasTransaction, "HashMap Does not contain transaction" + request);
        }
         // Testing the total points of all payers to ensure they are not negative
        assertEquals(transactionRepository.transactionMap.get("DANNON").getFirst().points, 1300);
        assertEquals(transactionRepository.transactionMap.get("UNILEVER").getFirst().points, 200);
        assertEquals(transactionRepository.transactionMap.get("MILLER COORS").getFirst().points, 10000);

    }


    @Test
    public void testGetBalance()  {
        transactionRepository.spendPoints(5000);
        // After spending, since we have checked the balance in previous methods, we will check the balance again but
        // after adding more points.
         requests = new LinkedList<>();
                // negative points should not cause any errors.
                requests.add(new Transaction("UNILEVER", 150, "2022-11-01T14:00:01Z"));
                requests.add(new Transaction("UNILEVER", 200, "2022-10-31T11:00:00Z"));    
                requests.add(new Transaction("DANNON", 400, "2022-11-02T14:00:00Z"));
                requests.add(new Transaction("DANNON", -400, "2022-11-02T14:00:00Z"));

        for(Transaction request : requests){
            transactionRepository.addTransaction(request.payer, request);
        }
         // Testing the total points of all payers to ensure they are not negative
        assertEquals(transactionRepository.transactionMap.get("DANNON").getFirst().points, 1000);
        assertEquals(transactionRepository.transactionMap.get("UNILEVER").getFirst().points, 350);
        assertEquals(transactionRepository.transactionMap.get("MILLER COORS").getFirst().points, 5300);

    }
    
}