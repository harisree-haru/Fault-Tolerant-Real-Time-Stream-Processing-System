package com.flink.fraud.models;

import java.io.Serializable;

/**
 * POJO representing a transaction in the e-commerce system
 * This object needs to be serializable for Flink state management
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    public String transactionId;
    public String userId;
    public double amount;
    public String sourceCity;
    public String destinationCity;
    public long timestamp;
    public boolean isSuccessful;
    public String paymentMethod;

    // Default constructor (required for Flink serialization)
    public Transaction() {
    }

    // Full constructor
    public Transaction(String transactionId, String userId, double amount,
                       String sourceCity, String destinationCity, long timestamp,
                       boolean isSuccessful, String paymentMethod) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.sourceCity = sourceCity;
        this.destinationCity = destinationCity;
        this.timestamp = timestamp;
        this.isSuccessful = isSuccessful;
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", userId='" + userId + '\'' +
                ", amount=" + amount +
                ", sourceCity='" + sourceCity + '\'' +
                ", destinationCity='" + destinationCity + '\'' +
                ", timestamp=" + timestamp +
                ", isSuccessful=" + isSuccessful +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}
