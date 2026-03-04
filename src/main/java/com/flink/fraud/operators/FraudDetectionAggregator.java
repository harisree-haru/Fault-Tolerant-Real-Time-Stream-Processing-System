package com.flink.fraud.operators;

import com.flink.fraud.models.FraudAlert;
import com.flink.fraud.models.Transaction;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Inner class: Accumulator to hold state during windowing
 * Defined first so it's available in the AggregateFunction signature
 */
class FraudAccumulator implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public String userId;
    public int transactionCount = 0;
    public double totalAmount = 0.0;
    public int failureCount = 0;
    public int geoAnomalies = 0;
    public double maxAmount = 0.0;
    public String lastCity = null;
    public long lastCityTimestamp = 0;
    public long windowEndTime = 0;

    @Override
    public String toString() {
        return "FraudAccumulator{" +
                "userId='" + userId + '\'' +
                ", transactionCount=" + transactionCount +
                ", totalAmount=" + totalAmount +
                ", failureCount=" + failureCount +
                ", geoAnomalies=" + geoAnomalies +
                '}';
    }
}

/**
 * Aggregate function for fraud detection
 * Accumulates transactions in a 5-minute window and detects fraudulent patterns
 * 
 * This is the core state management component that satisfies Chandy-Lamport checkpointing
 * State is preserved at regular intervals to enable recovery
 */
public class FraudDetectionAggregator implements AggregateFunction<Transaction, FraudAccumulator, FraudAlert> {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionAggregator.class);

    // Fraud detection thresholds
    private static final int FAILURE_THRESHOLD = 3;       // More than 3 failed transactions
    private static final double HIGH_AMOUNT_THRESHOLD = 500.0;  // More than $500
    private static final long GEO_TIME_THRESHOLD = 60000;  // Different country in less than 60 seconds

    @Override
    public FraudAccumulator createAccumulator() {
        return new FraudAccumulator();
    }

    @Override
    public FraudAccumulator add(Transaction transaction, FraudAccumulator accumulator) {
        // Add transaction to accumulator
        accumulator.transactionCount++;
        accumulator.totalAmount += transaction.amount;

        // Count failed transactions
        if (!transaction.isSuccessful) {
            accumulator.failureCount++;
        }

        // Track last city for geographic anomaly detection
        if (accumulator.lastCity == null) {
            accumulator.lastCity = transaction.destinationCity;
            accumulator.lastCityTimestamp = transaction.timestamp;
        } else {
            // Check for geographic anomaly: different city within 60 seconds
            if (!accumulator.lastCity.equals(transaction.destinationCity) &&
                (transaction.timestamp - accumulator.lastCityTimestamp) < GEO_TIME_THRESHOLD) {
                accumulator.geoAnomalies++;
            }
            accumulator.lastCity = transaction.destinationCity;
            accumulator.lastCityTimestamp = transaction.timestamp;
        }

        // Track highest transaction amount
        if (transaction.amount > accumulator.maxAmount) {
            accumulator.maxAmount = transaction.amount;
        }

        accumulator.windowEndTime = transaction.timestamp;

        logger.debug("Added transaction to accumulator - FailureCount: {}, TotalAmount: {}, GeoAnomalies: {}",
                accumulator.failureCount, accumulator.totalAmount, accumulator.geoAnomalies);

        return accumulator;
    }

    @Override
    public FraudAlert getResult(FraudAccumulator accumulator) {
        // Evaluate fraud indicators and generate alert if suspicious
        double riskScore = calculateRiskScore(accumulator);
        String alertType = determineAlertType(accumulator, riskScore);
        String description = buildDescription(accumulator);

        FraudAlert alert = new FraudAlert(
                accumulator.userId,
                alertType,
                description,
                riskScore,
                accumulator.windowEndTime,
                accumulator.failureCount,
                accumulator.totalAmount,
                accumulator.lastCity
        );

        logger.info("Generated fraud alert: {}", alert);
        return alert;
    }

    @Override
    public FraudAccumulator merge(FraudAccumulator a, FraudAccumulator b) {
        // Merge two accumulators (for sessionization or state combination)
        FraudAccumulator merged = new FraudAccumulator();
        merged.userId = a.userId;
        merged.transactionCount = a.transactionCount + b.transactionCount;
        merged.totalAmount = a.totalAmount + b.totalAmount;
        merged.failureCount = a.failureCount + b.failureCount;
        merged.geoAnomalies = a.geoAnomalies + b.geoAnomalies;
        merged.maxAmount = Math.max(a.maxAmount, b.maxAmount);
        merged.lastCity = b.lastCity != null ? b.lastCity : a.lastCity;
        merged.windowEndTime = Math.max(a.windowEndTime, b.windowEndTime);
        return merged;
    }

    /**
     * Calculate risk score based on multiple fraud indicators
     * Score ranges from 0.0 (no risk) to 1.0 (extremely high risk)
     */
    private double calculateRiskScore(FraudAccumulator acc) {
        double score = 0.0;

        // Indicator 1: Multiple failed transactions (weight: 0.4)
        if (acc.failureCount >= FAILURE_THRESHOLD) {
            score += 0.4 * Math.min(1.0, (double) acc.failureCount / 10.0);
        }

        // Indicator 2: High total transaction amount (weight: 0.3)
        if (acc.totalAmount > HIGH_AMOUNT_THRESHOLD * 2) {
            score += 0.3 * Math.min(1.0, acc.totalAmount / 5000.0);
        }

        // Indicator 3: Geographic anomalies (weight: 0.3)
        if (acc.geoAnomalies > 0) {
            score += 0.3 * Math.min(1.0, (double) acc.geoAnomalies / 3.0);
        }

        return Math.min(1.0, score);
    }

    /**
     * Determine alert type based on primary fraud indicator
     */
    private String determineAlertType(FraudAccumulator acc, double riskScore) {
        if (riskScore > 0.7) {
            return "CRITICAL";
        } else if (acc.failureCount >= FAILURE_THRESHOLD) {
            return "MULTIPLE_FAILURES";
        } else if (acc.totalAmount > HIGH_AMOUNT_THRESHOLD && acc.transactionCount <= 2) {
            return "HIGH_VALUE_NEW_USER";
        } else if (acc.geoAnomalies > 0) {
            return "GEOGRAPHIC_ANOMALY";
        } else {
            return "POTENTIAL_FRAUD";
        }
    }

    /**
     * Build descriptive message about fraud indicators
     */
    private String buildDescription(FraudAccumulator acc) {
        StringBuilder desc = new StringBuilder();

        if (acc.failureCount >= FAILURE_THRESHOLD) {
            desc.append("Failed transactions: ").append(acc.failureCount).append(". ");
        }

        if (acc.totalAmount > HIGH_AMOUNT_THRESHOLD) {
            desc.append("High total amount: $").append(String.format("%.2f", acc.totalAmount)).append(". ");
        }

        if (acc.geoAnomalies > 0) {
            desc.append("Geographic anomalies detected: ").append(acc.geoAnomalies).append(". ");
        }

        if (desc.length() == 0) {
            desc.append("Suspicious pattern detected. ");
        }

        desc.append("Window transactions: ").append(acc.transactionCount);

        return desc.toString();
    }
}
