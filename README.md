# 🎯 Fault-Tolerant Real-Time Stream Processing System
## E-Commerce Fraud Detection using Apache Flink

**A distributed system implementation demonstrating fault tolerance, checkpointing, and automatic recovery.**

---

## 📋 QUICK SUMMARY

This is a **4-member team project** that implements a real-world fraud detection system using Apache Flink. The system processes thousands of e-commerce transactions in real-time, detects fraudulent patterns, and recovers automatically from worker failures without losing any data.

---

## 📁 QUICK NAVIGATION

| Document | Purpose |
|----------|---------|
| **[TEAM_COORDINATION.md](TEAM_COORDINATION.md)** | Detailed 4-member plan + task breakdown |
| **[SETUP_GUIDE.md](SETUP_GUIDE.md)** | Step-by-step execution for each member |
| **[ARCHITECTURE.md](ARCHITECTURE.md)** | Algorithm details + diagrams |

---

## 👥 TEAM ROLES

| # | Role | Deliverable |
|---|------|-------------|
| 1️⃣ | Cluster Engineer | Flink setup, 4+ parallel workers |
| 2️⃣ | Core Developer ⭐ | Java application, checkpointing |
| 3️⃣ | Data Generator | Live transactions, failure simulation |
| 4️⃣ | Checkpoint Validator | State backend, recovery verification |

---

## 🚀 QUICK START

```bash
# Get Flink (Member 1)
wget https://archive.apache.org/dist/flink/flink-1.17.1/flink-1.17.1-bin-scala_2.12.tgz
tar xzf flink-1.17.1-bin-scala_2.12.tgz

# Build & deploy (Member 2)
mvn clean package
flink-1.17.1/bin/flink run --parallelism 4 target/fraud-detection.jar

# Generate data (Member 3)
python3 transaction_generator.py

# Simulate failure (after 30s)
kill -9 $(jps | grep TaskManager | head -1 | awk '{print $1}')
```

---

## 🎬 THREE MANDATORY DEMONSTRATIONS

1. **Parallel Processing** → 4 subtasks in Web UI 
2. **Checkpointing** → Checkpoint directory growth every 10s
3. **Failure & Recovery** → Kill a worker, watch automatic restart

---

## 📚 DETAILED DOCUMENTATION

**→ Start Here:** [TEAM_COORDINATION.md](TEAM_COORDINATION.md) - Complete 4-member plan  
**→ Then Use:** [SETUP_GUIDE.md](SETUP_GUIDE.md) - Step-by-step setup  
**→ Reference:** [ARCHITECTURE.md](ARCHITECTURE.md) - Algorithm details  

---

*Team Project | 4 Members | 4-5 Days | Advanced Distributed Systems | 57/60 Marks*
