package com.flink.fraud.operators;

import com.flink.fraud.models.Transaction;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.flink.api.common.functions.MapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MapFunction to parse JSON transaction strings into Transaction POJOs
 * This is the first transformation in our pipeline
 */
public class TransactionParser implements MapFunction<String, Transaction> {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(TransactionParser.class);
    private transient Gson gson;

    @Override
    public Transaction map(String jsonString) throws Exception {
        // Initialize Gson lazily (transient fields are re-initialized after deserialization)
        if (gson == null) {
            gson = new Gson();
        }

        try {
            Transaction transaction = gson.fromJson(jsonString, Transaction.class);
            if (transaction == null) {
                logger.warn("Parsed transaction is null from string: {}", jsonString);
                throw new IllegalArgumentException("Failed to parse transaction from JSON");
            }
            logger.debug("Parsed transaction: {}", transaction);
            return transaction;
        } catch (JsonSyntaxException e) {
            logger.error("Failed to parse JSON: {} - Error: {}", jsonString, e.getMessage());
            throw new RuntimeException("JSON parsing failed for: " + jsonString, e);
        }
    }
}
