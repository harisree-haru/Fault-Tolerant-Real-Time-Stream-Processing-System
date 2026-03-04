# 🎯 Fault-Tolerant Real-Time Stream Processing System
## Real-World Application: E-Commerce Fraud Detection Platform
### 4-Member Coordination & Implementation Plan

---

## 📌 PROJECT OVERVIEW

### **Real-World Problem Statement**
A major online retailer processes **thousands of transactions per minute** across multiple payment gateways. Current fraud detection uses batch processing with a **2-hour delay**—too slow to prevent fraud. The company needs a **fault-tolerant, real-time fraud detection pipeline** that:

1. ✅ Streams real-time transactions from multiple payment gateways
2. ✅ Detects anomalies using windowed aggregations:
   - Multiple failed transactions from same user (>3 failures in 5 min window)
   - High-value purchases from new users (>$500)
   - Unusual geographic patterns (same user, different countries in <1 min)
3. ✅ Maintains user state across failures
4. ✅ Recovers automatically without losing/duplicating transactions
5. ✅ Outputs fraud flags immediately for real-time blocking

### **Why This Application?**
- Real need for fault tolerance (financial transactions can't be lost)
- Stateful processing (user history tracking = Chandy-Lamport advantage)
- Window aggregations (core streaming concept)
- Exactly-once guarantees required (no double-charging)
- Business-relevant problem (impresses evaluators)

---

## 👥 WORK DISTRIBUTION & ROLES

### **Member 1: Flink Cluster & Parallelism Engineer** 🛠️

**Role:** Set up and configure the distributed execution environment

**Timeline:** Days 1-2

**Responsibilities:**
1. Install Java JDK (version 11+)
2. Download and extract Apache Flink (v1.17+)
3. Start local Flink cluster:
   ```bash
   ./bin/start-cluster.sh
   ```
4. Configure parallelism → set TaskManager slots to 4-6 (simulate multiple workers)
5. Verify JobManager and TaskManagers are running
6. Access Flink Web UI (http://localhost:8081)
7. Monitor parallel subtasks execution

**Deliverables:**
- ✅ Cluster startup screenshot
- ✅ TaskManagers running (screenshot from Web UI)
- ✅ Parallelism config proof (flink-conf.yaml)
- ✅ Web UI showing parallel subtasks
- ✅ Heartbeat monitoring script (for detecting failures)

**Key Configuration (flink-conf.yaml):**
```yaml
taskmanager.numberOfTaskSlots: 4
parallelism.default: 4
jobmanager.web.submit.enable: true
```

**Validation Checklist:**
- [ ] `jps` command shows JobManager and TaskManager processes
- [ ] Web UI accessible and shows 4 slots
- [ ] Logs confirm successful startup

---

### **Member 2: Core Streaming Application Developer** 💻 (YOUR ROLE)

**Role:** Write the main Flink Java program with checkpointing and exactly-once semantics

**Timeline:** Days 3-4

**Responsibilities:**
1. Set up Maven project structure
2. Add Flink dependencies to pom.xml
3. Create transaction data model class
4. Implement socket data source reader
5. Build DataStream pipeline with transformations
6. Enable checkpointing (Chandy-Lamport algorithm)
7. Configure exactly-once semantics
8. Implement fraud detection logic
9. Add sink operators for results

**Deliverables:**
- ✅ Complete Java source code
- ✅ pom.xml with correct Flink versions
- ✅ Successful program execution proof
- ✅ Checkpoint directories created
- ✅ Console output showing processed transactions

**Core Code Structure:**

```
src/main/java/com/flink/fraud/
├── StreamFraudDetectorJob.java (Main class)
├── models/
│   └── Transaction.java (POJO)
├── operators/
│   └── FraudDetectionAggregator.java
└── utils/
    └── TransactionParser.java
```

**Key Implementation Points:**

1. **StreamFraudDetectorJob.java:**
```java
public class StreamFraudDetectorJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = 
            StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Enable Chandy-Lamport checkpointing
        env.enableCheckpointing(10000); // Every 10 seconds
        env.getCheckpointConfig().setCheckpointingMode(
            CheckpointingMode.EXACTLY_ONCE
        );
        
        env.getCheckpointConfig().setCheckpointStorage(
            "file:///path/to/checkpoints"
        );
        
        // Pipeline
        env.socketTextStream("localhost", 9999)
            .map(new TransactionParser())
            .keyBy(txn -> txn.getUserId())
            .window(TumblingEventTimeWindows.of(Time.minutes(5)))
            .aggregate(new FraudDetectionAggregator())
            .filter(alert -> alert.isFraudulent())
            .print();
        
        env.execute("Fraud Detection System");
    }
}
```

2. **Transaction.java:**
```java
public class Transaction {
    public String transactionId;
    public String userId;
    public double amount;
    public String city;
    public long timestamp;
    public boolean isSuccessful;
}
```

3. **Fraud Detection Logic:**
- Count failed transactions per user
- Sum amounts per user
- Track geographic locations per user
- Flag suspicious patterns

**Validation Checklist:**
- [ ] Code compiles without errors
- [ ] Application runs for 60+ seconds
- [ ] Checkpoints created in checkpoint directory
- [ ] Console shows processed transactions
- [ ] No resource leaks (verify in jps)

---

### **Member 3: Live Data Generator & Failure Recovery Engineer** 🔄

**Role:** Create live transaction stream and simulate failures to prove recovery

**Timeline:** Days 4-5 (concurrent with Member 2)

**Responsibilities:**
1. Create transaction data generator script
2. Send realistic transaction stream to localhost:9999
3. Simulate TaskManager failure (kill a worker)
4. Observe automatic job recovery
5. Verify state is restored from checkpoint
6. Monitor heartbeat detection of failure

**Deliverables:**
- ✅ Live stream generator script
- ✅ Transaction sample data (50+ transactions)
- ✅ Failure simulation screenshot (TaskManager stopped)
- ✅ Recovery screenshot (job resumes from checkpoint)
- ✅ Log proof of automatic restart
- ✅ Heartbeat log showing failure detection

**Live Data Generator (Python/Bash):**

```python
import socket
import json
import random
import time

# Send synthetic transactions to Flink
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect(('localhost', 9999))

users = ['user_001', 'user_002', 'user_003', 'user_004']
cities = ['NYC', 'LA', 'London', 'Tokyo']

for i in range(1000):
    transaction = {
        'transactionId': f'txn_{i}',
        'userId': random.choice(users),
        'amount': round(random.uniform(10, 5000), 2),
        'city': random.choice(cities),
        'timestamp': int(time.time() * 1000),
        'isSuccessful': random.choice([True, True, True, False])  # 75% success
    }
    
    sock.send(json.dumps(transaction).encode() + b'\n')
    time.sleep(0.5)

sock.close()
```

**Failure Simulation Steps:**
1. Start Flink cluster (Member 1)
2. Deploy fraud detection job (Member 2)
3. Start transaction generator
4. Let it run for 30 seconds
5. Find TaskManager PID: `jps | grep TaskManager`
6. Kill TaskManager: `kill -9 <PID>`
7. Observe console for recovery message
8. Verify transactions continue processing
9. Check checkpoint was used for recovery

**Validation Checklist:**
- [ ] 100+ transactions sent to socket successfully
- [ ] Transaction format valid JSON
- [ ] Failure detected within 10 seconds (heartbeat timeout)
- [ ] Job restarts automatically
- [ ] No transaction data lost
- [ ] No duplicate processed records

---

### **Member 4: State Backend & Checkpoint Validation Engineer** 🔐

**Role:** Configure checkpoint storage and validate fault tolerance

**Timeline:** Days 3-5 (concurrent with Members 2 & 3)

**Responsibilities:**
1. Set up checkpoint storage backend
2. Configure state backend strategy
3. Validate checkpoints are created
4. Inspect checkpoint directory structure
5. Monitor checkpoint statistics in Web UI
6. Verify exactly-once behavior post-recovery
7. Test state restoration accuracy

**Deliverables:**
- ✅ Checkpoint storage folder proof
- ✅ Checkpoint metadata files examination
- ✅ Web UI checkpoint metrics screenshot
- ✅ Recovery validation report
- ✅ State backend configuration proof

**Configuration (flink-conf.yaml):**
```yaml
# State Backend
state.backend: hashmap
state.backend.rocksdb.timer-service.factory: heap

# Checkpoint Configuration
state.checkpoints.dir: file:///checkpoints
state.checkpoints.enabled: true
state.savepoints.dir: file:///savepoints

# Checkpoint Tuning
execution.checkpointing.interval: 10000
execution.checkpointing.mode: EXACTLY_ONCE
execution.checkpointing.timeout: 600000
```

**Checkpoint Validation Process:**

1. **Verify Directory Structure:**
```bash
ls -la /checkpoints/
# Should show: chk-001/, chk-002/, chk-003/, ...
```

2. **Inspect Checkpoint Contents:**
```bash
ls -la /checkpoints/chk-001/
# Should contain: _metadata, __db/
```

3. **Manual State Recovery Test:**
- Let system run for 2 minutes (6+ checkpoints created)
- Kill JobManager: `pkill -f JobManager`
- Restart Flink: `./bin/start-cluster.sh`
- Resubmit job
- Verify it resumes from latest checkpoint (check logs)

4. **Exactly-Once Verification:**
- Track transaction IDs processed before failure
- Track IDs processed after recovery
- Confirm NO duplicates or missing entries

**Checkpoint Metrics to Screenshot (from Web UI):**
- Checkpoint count: 6+
- Latest checkpoint: timestamp and duration
- Checkpoint frequency: every ~10 seconds
- State size: should be consistent

**Validation Checklist:**
- [ ] Checkpoint directory has 5+ checkpoint folders
- [ ] Each checkpoint contains valid state files
- [ ] Web UI shows latest checkpoint time
- [ ] Job resumes from correct checkpoint after failure
- [ ] No data corruption in state files
- [ ] Recovery time < 30 seconds

---

## 📅 TIMELINE & MILESTONES

| Phase | Days | Owner | Deliverable |
|-------|------|-------|-------------|
| **Cluster Setup** | 1-2 | Member 1 | Flink running, Web UI active |
| **Core Development** | 2-4 | Member 2 | Working Java application |
| **Data Generator** | 3-4 | Member 3 | Live transaction stream |
| **State Backend Config** | 3-4 | Member 4 | Checkpoint storage ready |
| **Integration Testing** | 4-5 | All | Systems working together |
| **Failure Simulation** | 5 | Member 3 + All | Recovery proof |
| **Demo Prep** | 5-6 | All | Screenshots & validation |

---

## 🎬 THREE MANDATORY USE CASE DEMONSTRATIONS

### **Use Case 1: Normal Distributed Stream Processing** ✅
**Owner:** Member 1 + 2

**What to Show:**
- Transactions flowing through socket source
- Parallelism: Same transaction data processed by 4 different subtasks
- Console output showing real-time fraud detection results
- Web UI: 4 parallel subtasks running simultaneously

**Evidence Needed:**
- Screenshot: Web UI showing 4 active subtasks
- Screenshot: Console output with fraud flags
- Timestamp proof: Events processed in real-time

---

### **Use Case 2: Checkpoint Creation & State Save** ✅
**Owner:** Member 4 + 2

**What to Show:**
- Every 10 seconds, Flink saves state to checkpoint
- State contains: user transaction history, window aggregations
- Checkpoint directory growing with chk-001, chk-002, etc.
- Web UI: Checkpoint statistics updated

**Evidence Needed:**
- Screenshot: Checkpoint directory with 5+ folders
- Screenshot: Web UI checkpoint timeline
- Timestamp: Checkpoint intervals exactly 10 seconds apart
- File listing: `ls -la /checkpoints/chk-001/`

---

### **Use Case 3: Failure & Automatic Recovery** ✅
**Owner:** Member 3 + 4

**What to Show:**
1. System running normally, processing transactions
2. KILL TaskManager process (simulate worker failure)
3. **Within 10 seconds:** JobManager detects failure via heartbeat timeout
4. **Automatic recovery:** Job restarts from latest checkpoint
5. **Transaction continuity:** No data lost, no duplicates

**Evidence Needed:**
- Screenshot 1: Normal processing (transaction count: N)
- Screenshot 2: TaskManager killed, job restarting
- Screenshot 3: Job recovered, transaction count: N + (new ones)
- Log excerpt: "Recovered from checkpoint X"
- Validation: Same user states restored correctly

---

## 🏆 RUBRIC MAPPING

### **Problem Understanding & Relevance (10 Marks)**
| Component | Evidence |
|-----------|----------|
| Real-world problem | E-commerce fraud detection with real constraints |
| Distributed system need | Thousands of transactions/min = need parallelism |
| Fault tolerance need | Financial transactions = exactly-once critical |
| Time-critical processing | 2-hour delay = unacceptable for fraud prevention |

**Target: 8-10 Marks**

---

### **Architecture Design & Algorithms (10 Marks)**
| Component | Implementation |
|-----------|-----------------|
| **Chandy-Lamport** | Checkpoint every 10 seconds via `enableCheckpointing()` |
| **Master-Worker** | JobManager distributes tasks to 4 TaskManagers |
| **Exactly-Once** | Configured via `CheckpointingMode.EXACTLY_ONCE` |
| **Failure Detection** | Heartbeat timeout triggers recovery |
| **State Restoration** | Latest checkpoint restored on recovery |

**Architecture Diagram to Create:**
```
┌─────────────────────────────────┐
│    Payment Gateways (Input)     │
│    (HTTP → Socket → 9999)       │
└──────────────┬──────────────────┘
               │
       ┌───────▼────────┐
       │  JobManager    │   (Master)
       │  (Coordinator) │
       └───────┬────────┘
           ┌───┴───┐
    ┌──────▼──┐  ┌──▼───────┐
    │TaskMgr 1│  │TaskMgr 2 │  (Workers)
    │Slot 1-2 │  │Slot 3-4  │
    └──────┬──┘  └──┬───────┘
           │        │
      ┌────▼────┬───▼─────┐
      │ Partition 1-2  │ Partition 3-4 │ (KeyBy: userId)
      └────┬────────────┬──────────────┘
           │            │
       ┌───▼──────┐ ┌──▼──────────┐
       │ Window & │ │ Window &    │ (5-min windows)
       │ Aggr     │ │ Aggr        │
       └───┬──────┘ └──┬──────────┘
           └────┬───────┘
           ┌────▼────────────┐
           │ Fraud Detection  │
           └────┬────────────┘
           ┌────▼────────────┐
           │ Alert Output    │
           └─────────────────┘
    
    Checkpoints: Every 10 seconds → /checkpoints/chk-NNN/
    Recovery: Heartbeat timeout (>10s) → restart from latest chk
```

**Target: 8-10 Marks**

---

### **Implementation Code (10 Marks)**
| Criterion | Evidence |
|-----------|----------|
| Flink DataStream | Full working program with `socketTextStream()` |
| Checkpointing enabled | `env.enableCheckpointing(10000)` in code |
| Exactly-once configured | `CheckpointingMode.EXACTLY_ONCE` visible |
| Stream transformations | `map()`, `keyBy()`, `window()`, `aggregate()`, `filter()` |
| Proper state handling | Custom POJO with serializable fields |
| Multiple workers configured | Parallelism = 4, TaskSlots = 4 |

**Target: 9-10 Marks** (Full working code)

---

### **Output & Demonstrations (10 Marks)**
| Use Case | Evidence Required |
|----------|-------------------|
| **Use Case 1: Normal Processing** | 3 screenshots: Generator → Web UI (4 subtasks) → Console output |
| **Use Case 2: Checkpoint Creation** | 2 screenshots: Checkpoint folder structure + Web UI metrics |
| **Use Case 3: Failure & Recovery** | 4 screenshots: Normal state → Failure → Recovery → Verification |

**Target: 9-10 Marks** (All 3 use cases demonstrated)

---

### **Individual Presentation & QA (10 Marks)**
Each member explains:
- **Member 1:** How parallelism proves distributed execution
- **Member 2:** Why checkpointing enables exactly-once semantics
- **Member 3:** How heartbeat detects and triggers recovery
- **Member 4:** Why this architecture satisfies Chandy-Lamport + Master-Worker

**Knowledge Base for VivA:**
- "Chandy-Lamport algorithm saves consistent global state at intervals"
- "Master-Worker: JobManager orchestrates, TaskManagers execute"
- "Heartbeat timeout detects failure faster than waiting for response"
- "Exactly-once: Idempotent writes + deduplication = no duplicates"

**Target: 8-10 Marks**

---

## ✅ FINAL DELIVERABLES CHECKLIST

### **Code Repository (GitHub)**
- [ ] pom.xml with Flink 1.17+ dependency
- [ ] src/main/java/ structure with all classes
- [ ] flink-conf.yaml with cluster config
- [ ] README.md with setup instructions
- [ ] Checkpoint and savepoint directories
- [ ] All source code committed and documented

### **Documentation**
- [ ] Problem statement (E-commerce fraud detection)
- [ ] Architecture diagram (Master-Worker + Checkpoints)
- [ ] Team coordination plan (this document)
- [ ] Configuration tuning rationale
- [ ] Algorithm explanation (Chandy-Lamport, exactly-once)

### **Evidence Screenshots (20+ total)**
- [ ] Cluster startup (JPS, Web UI)
- [ ] 4 parallel subtasks running
- [ ] Live transaction stream flowing
- [ ] Checkpoints being created (5+)
- [ ] Checkpoint directory contents
- [ ] TaskManager failure simulation
- [ ] Automatic recovery in action
- [ ] Final metrics dashboard

### **Test Results**
- [ ] 100+ transactions processed successfully
- [ ] No data loss during failure
- [ ] No duplicate records after recovery
- [ ] State correctly restored
- [ ] Exactly-once verified

---

## 🎤 VivA TALKING POINTS BY MEMBER

### **Member 1 (Cluster Engineer):**
*"We configured Flink with 4 task slots to simulate distributed execution. The JobManager coordinates TaskManagers via heartbeats every 10 seconds. If a TaskManager goes down, the JobManager restarts the job from the latest checkpoint."*

### **Member 2 (Application Developer):**
*"We implemented a Chandy-Lamport style checkpoint system—every 10 seconds, Flink snapshots the exact state of all user transaction windows. This enables exact-once semantics: even if a worker fails mid-computation, we resume from the saved state without losing or duplicating any transactions."*

### **Member 3 (Failure Recovery Engineer):**
*"We simulated real-world failures by killing TaskManagers. The JobManager detected the failure within 10 seconds via heartbeat timeout and automatically resubmitted the job. The fraud detection system resumed from the latest checkpoint without missing any transactions."*

### **Member 4 (State Backend Engineer):**
*"We validated that checkpoints were created every 10 seconds and contained correct state snapshots. After failure recovery, we verified that user transaction history was accurately restored, proving exactly-once semantics."*

---

## 🚀 GET STARTED NOW

### **Member 1 - Start Here:**
```bash
# Install Java 11+
java -version

# Download Flink 1.17
wget https://archive.apache.org/dist/flink/flink-1.17.0/flink-1.17.0-bin-scala_2.12.tgz

# Extract and start
tar xzf flink-1.17.0-bin-scala_2.12.tgz
cd flink-1.17.0
./bin/start-cluster.sh

# Verify
jps  # Should show JobManager and TaskManager
```

### **Member 2 - Maven Setup:**
```bash
mvn archetype:generate \
  -DgroupId=com.flink.fraud \
  -DartifactId=fraud-detection-system \
  -DarchetypeArtifactId=maven-archetype-quickstart

# Add to pom.xml: Flink 1.17 dependency
```

### **Member 3 - Start Generator:**
```bash
# Run Python generator (sends transactions to localhost:9999)
python3 transaction_generator.py
```

### **Member 4 - Setup Checkpoints:**
```bash
# Create checkpoint directory
mkdir -p /tmp/flink-checkpoints
chmod 777 /tmp/flink-checkpoints

# Update flink-conf.yaml
state.checkpoints.dir: file:///tmp/flink-checkpoints
```

---

## 📞 COORDINATION NOTES

**Weekly Syncs:** Tuesday & Thursday @ [TIME]
**Slack Channel:** #flink-fraud-detection
**Code Review:** Every push to main
**Demo Rehearsal:** Day 5 evening

**If Stuck:**
- Member 1 → Flink setup issues: Check `./logs/flink*.log`
- Member 2 → Code compilation: Run `mvn clean install -X`
- Member 3 → Generator timeout: Check firewall on port 9999
- Member 4 → Checkpoints empty: Verify job parallelism > 1

---

## 🎯 SUCCESS CRITERIA

✅ **System is running** → All 4 members deployed  
✅ **Checkpoints exist** → 6+ checkpoint folders with state  
✅ **Failure detected** → Job restarts within 30 seconds  
✅ **Recovery proven** → Same state restored, no duplicates  
✅ **Code clean** → No compilation errors, proper comments  
✅ **Demo smooth** → All 3 use cases demonstrated flawlessly  
✅ **VivA confident** → Each member explains their part clearly  

---

**Good luck team! You've got this! 💪**

*Last updated: March 4, 2026*
