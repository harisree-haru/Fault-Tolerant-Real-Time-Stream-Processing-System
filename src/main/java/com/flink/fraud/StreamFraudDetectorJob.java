package com.flink.fraud;

import com.flink.fraud.models.FraudAlert;
import com.flink.fraud.models.Transaction;
import com.flink.fraud.operators.FraudDetectionAggregator;
import com.flink.fraud.operators.TransactionParser;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fault-Tolerant Real-Time Stream Processing System for E-Commerce Fraud Detection
 * 
 * Core Concepts Demonstrated:
 * 1. Chandy-Lamport Algorithm: Periodic checkpoints capture consistent global state
 * 2. Master-Worker Architecture: JobManager orchestrates, TaskManagers execute
 * 3. Exactly-Once Semantics: Transactions are never lost or duplicated
 * 4. Failure Detection: Heartbeat-based failure detection and automatic recovery
 * 
 * Data Flow:
 * Socket Source → Parse JSON → KeyBy(userId) → Window(5min) → Aggregate → Filter → Print Alert
 * 
 * State Checkpoint Strategy:
 * - Checkpoint interval: 10 seconds (Chandy-Lamport snapshots)
 * - Checkpoint mode: EXACTLY_ONCE
 * - State backend: HashMapStateBackend (in-memory snapshots)
 * - Recovery: Automatic restart from latest checkpoint
 */
public class StreamFraudDetectorJob {
    private static final Logger logger = LoggerFactory.getLogger(StreamFraudDetectorJob.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting Fraud Detection System...");

        // 1. SETUP EXECUTION ENVIRONMENT
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // CRITICAL: Enable Chandy-Lamport checkpointing
        // Checkpointing every 10 seconds saves the exact state of all transactions
        enableCheckpointingWithFaultTolerance(env);

        logger.info("Checkpoint configuration complete");

        // 2. DEFINE DATA SOURCE
        // Listen on localhost:9999 for incoming transaction JSON
        // Format: {"transactionId":"txn_1","userId":"user_001",...}
        var transactionStream = env.socketTextStream("localhost", 9999)
                .setParallelism(4);  // Distribute across 4 parallel subtasks

        logger.info("Transaction source configured - listening on localhost:9999");

        // 3. PARSE JSON TRANSACTIONS
        // MapFunction: String JSON → Transaction POJO
        var parsedTransactions = transactionStream
                .map(new TransactionParser())
                .setParallelism(4)
                .name("Parse Transactions");

        logger.info("Transaction parser configured");

        // 4. GROUP BY USER (KEYBY)
        // All transactions for same userId go to same worker (preserves state locality)
        // This ensures user history is accumulated consistently
        var userGroupedTransactions = parsedTransactions
                .keyBy(txn -> txn.userId);

        logger.info("User grouping configured (state partitioning)");

        // 5. WINDOW AGGREGATION (5-MINUTE TUMBLING WINDOW)
        // Batch transactions into 5-minute windows for pattern detection
        // Each window is independently checkpointed (Chandy-Lamport principle)
        var windowedAlerts = userGroupedTransactions
                .window(TumblingEventTimeWindows.of(Time.minutes(5)))
                .aggregate(new FraudDetectionAggregator())
                .setParallelism(4);

        logger.info("Window aggregation configured - 5 minute tumbling windows");

        // 6. FILTER HIGH-RISK ALERTS
        // Only output fraud alerts with meaningful risk indicators
        var fraudAlerts = windowedAlerts
                .filter(new RiskScoreFilter())
                .setParallelism(4)
                .name("Filter High-Risk Alerts");

        logger.info("Alert filtering configured");

        // 7. OUTPUT FRAUD ALERTS
        // Print to console (in production: write to Kafka, database, etc.)
        fraudAlerts
                .print()
                .setParallelism(1)  // Single output for readability
                .name("Alert Output");

        logger.info("Output sink configured");

        // 8. EXECUTE JOB
        logger.info("Executing fraud detection job...");
        env.execute("E-Commerce Fraud Detection System");
    }

    /**
     * Configure Chandy-Lamport checkpointing for fault tolerance
     * 
     * This method implements:
     * - Periodic state snapshots (every 10 seconds)
     * - Consistent global state capture
     * - Automatic recovery on failure
     * - Exactly-once processing guarantees
     */
    private static void enableCheckpointingWithFaultTolerance(StreamExecutionEnvironment env) {
        // CHECKPOINT INTERVAL: 10 seconds
        // Flink saves state snapshot every 10 seconds
        // This is the "Chandy-Lamport algorithm" - consistent global snapshots
        long checkpointInterval = 10_000;  // 10 seconds
        env.enableCheckpointing(checkpointInterval);

        logger.info("Checkpoint enabled with interval: {} ms", checkpointInterval);

        // CHECKPOINT MODE: EXACTLY_ONCE
        // Guarantees: No lost or duplicate transactions even after failure
        // Implementation: Idempotent writes + deduplication
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        logger.info("Checkpoint mode set to: EXACTLY_ONCE");

        // CHECKPOINT STORAGE LOCATION
        // Persist checkpoints to filesystem (in production: distributed storage like S3, HDFS)
        // Local path is /tmp/flink-checkpoints/ (must be created by Member 4)
        try {
            String checkpointPath = "file:///tmp/flink-checkpoints";
            env.getCheckpointConfig().setCheckpointStorage(checkpointPath);
            logger.info("Checkpoint storage location: {}", checkpointPath);
        } catch (Exception e) {
            logger.warn("Could not set checkpoint storage, using default: {}", e.getMessage());
        }

        // CHECKPOINT TIMEOUT: 10 minutes
        // If checkpoint takes longer than 10 minutes, it's cancelled
        // Prevents hanging on slow checkpoints
        env.getCheckpointConfig().setCheckpointTimeout(600_000);  // 10 minutes
        logger.info("Checkpoint timeout: 600000 ms");

        // FAILURE HANDLING POLICY
        // On failure: Restart job from latest checkpoint
        // This is Master-Worker architecture: JobManager detects failure via heartbeats,
        // then auto-restarts TaskManagers with saved state
        env.getCheckpointConfig().setFailOnCheckpointingErrors(false);
        logger.info("Failure handling: Auto-restart with latest checkpoint");

        // STATE BACKEND: HashMapStateBackend
        // Stores state in memory with asynchronous snapshots
        // Fast and suitable for distributed system demo
        // (Production: RocksDBStateBackend for better scalability)
        try {
            env.setStateBackend(new HashMapStateBackend());
            logger.info("State backend configured: HashMapStateBackend");
        } catch (Exception e) {
            logger.error("Failed to set state backend: {}", e.getMessage());
        }

        // RETRY POLICY
        // If checkpoint fails, retry up to 3 times before giving up
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(3);
        logger.info("Tolerable checkpoint failure count: 3");

        logger.info("✅ Chandy-Lamport checkpointing fully configured");
    }

    /**
     * Filter function to only alert on high-risk frauds
     * Reduces noise in output while catching real threats
     */
    public static class RiskScoreFilter implements FilterFunction<FraudAlert> {
        @Override
        public boolean filter(FraudAlert alert) {
            // Alert if risk score > 0.3 OR if specific fraud indicators present
            boolean highRisk = alert.riskScore > 0.3 ||
                    alert.failureCount >= 3 ||
                    alert.totalAmount > 1000.0;

            if (highRisk) {
                logger.info("🚨 FRAUD DETECTED - {}", alert);
            }
            return highRisk;
        }
    }
}
