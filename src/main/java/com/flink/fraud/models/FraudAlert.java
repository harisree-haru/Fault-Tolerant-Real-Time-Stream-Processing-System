package com.flink.fraud.models;

import java.io.Serializable;

/**
 * POJO representing a fraud alert triggered by the system
 */
public class FraudAlert implements Serializable {
    private static final long serialVersionUID = 1L;

    public String userId;
    public String alertType;
    public String description;
    public double riskScore;
    public long timestamp;
    public int failureCount;
    public double totalAmount;
    public String lastCity;

    // Default constructor
    public FraudAlert() {
    }

    // Full constructor
    public FraudAlert(String userId, String alertType, String description,
                      double riskScore, long timestamp, int failureCount,
                      double totalAmount, String lastCity) {
        this.userId = userId;
        this.alertType = alertType;
        this.description = description;
        this.riskScore = riskScore;
        this.timestamp = timestamp;
        this.failureCount = failureCount;
        this.totalAmount = totalAmount;
        this.lastCity = lastCity;
    }

    @Override
    public String toString() {
        return "🚨 FRAUD ALERT 🚨 {" +
                "userId='" + userId + '\'' +
                ", alertType='" + alertType + '\'' +
                ", description='" + description + '\'' +
                ", riskScore=" + riskScore +
                ", timestamp=" + timestamp +
                ", failureCount=" + failureCount +
                ", totalAmount=" + totalAmount +
                ", lastCity='" + lastCity + '\'' +
                '}';
    }
}
