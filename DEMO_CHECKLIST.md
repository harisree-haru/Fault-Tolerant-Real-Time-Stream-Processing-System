# Demo Checklist: Fault-Tolerant Real-Time Stream Processing System

**Presenter:** Member 2 (Core Streaming Developer)  
**Date:** March 4, 2026  
**Systems to Demo:** 3 different machines  

---

## PRE-DEMO TESTING (Current System)

### ✓ System Requirements Check
- [ ] Java 11+ installed: `java -version`
- [ ] Maven 3.9.12 available: `mvn -version`
- [ ] Python 3.7+ installed: `python --version`
- [ ] Git available: `git --version`
- [ ] Flink 1.17.1 downloaded and extracted

### ✓ Code Build Verification
```powershell
cd C:\github_ahara\Fault-Tolerant-Real-Time-Stream-Processing-System
mvn clean package -DskipTests
```
- [ ] BUILD SUCCESS message appears
- [ ] fraud-detection.jar created (~813 KB)
- [ ] No compilation errors

### ✓ Flink Cluster Startup
```powershell
# Terminal 1: Start cluster
flink-1.17.1/bin/start-cluster.bat

# Wait 10 seconds, then check
jps  # Should show JobManager and TaskManager processes
```
- [ ] JobManager started
- [ ] At least 4 TaskManagers visible in `jps`
- [ ] Web UI accessible at `http://localhost:8081`

### ✓ Web UI Verification
1. Open browser: `http://localhost:8081`
2. Check **Task Managers** tab
   - [ ] Shows exactly 4 Task Managers
   - [ ] Each shows "Slots Available: 1 / Slots Total: 1"
   - [ ] All show "Status: ALIVE"

### ✓ Job Deployment
```powershell
# Terminal 2: Deploy job
flink run --parallelism 4 target/fraud-detection.jar
```
- [ ] Output shows: "Job has been submitted with JobID: ..."
- [ ] No error messages
- [ ] Job appears in Web UI under "Running Jobs"

### ✓ Transaction Generator Starting
```powershell
# Terminal 3: Run generator
python transaction_generator.py --host localhost --port 9999 --count 100 --delay 0.5
```
- [ ] Python script runs without errors
- [ ] Fraud alerts start printing in Terminal 2 (job output)
- [ ] Example output:
  ```
  Fraud Alert for User: user_4
  Alert Type: MULTIPLE_FAILURES
  Risk Score: 0.85
  Last City: Bangkok
  ```

---

## DEMO PERFORMANCE CHECKLIST

### Parallel Processing Demo (5 min)
1. [ ] Show Web UI Task Managers (4 slots visible)
2. [ ] Point out job parallelism: 4
3. [ ] Explain: "Each payment type processed in parallel"
4. [ ] Screenshot: Flink Web UI with 4 active subtasks

### Real-Time Fraud Detection Demo (5 min)
1. [ ] Run transaction generator (see alerts printing live)
2. [ ] Answer: "How fast is it detecting fraud?"
   - Answer: "In real-time, within the 5-minute window. Faster than 2-hour batch processing."
3. [ ] Point out fraud indicators in alerts:
   - Multiple failures detected
   - Geographic anomalies
   - Amount thresholds exceeded
4. [ ] **Key talking point:** "We replaced a 2-hour batch job with real-time detection"

### Checkpoint/Fault Tolerance Demo (5 min)
1. [ ] Show checkpoint directory in Task Manager logs:
   ```powershell
   dir C:\tmp\flink-checkpoints\
   ```
2. [ ] Explain: "Every 10 seconds, we save exact state snapshot"
3. [ ] Optional (advanced): Kill a TaskManager
   ```powershell
   jps  # Find TaskManager PID
   taskkill /PID <PID> /F
   ```
4. [ ] Observe:
   - [ ] Job auto-restarts from checkpoint
   - [ ] No data loss
   - [ ] Generator can resume from same state
5. [ ] Screenshot: Checkpoint state on disk

---

## SETUP INSTRUCTIONS FOR NEW SYSTEMS

### Quick 5-Minute Setup

**If system has Java, Maven, Git:**
```powershell
# 1. Clone repo (2 min)
git clone <YOUR-REPO-URL>
cd Fault-Tolerant-Real-Time-Stream-Processing-System

# 2. Build (1 min)
mvn clean package -DskipTests

# 3. Download Flink (external - do before demo)
# https://flink.apache.org/downloads/
# Download: flink-1.17.1-bin.zip
# Extract to: flink-1.17.1/

# 4. Start cluster (1 min)
flink-1.17.1/bin/start-cluster.bat
flink run --parallelism 4 target/fraud-detection.jar
python transaction_generator.py
```

**If system is new/fresh:**
1. Install Java 11+ (5-10 min)
2. Install Maven 3.9.12 (2 min - just extract)
3. Download Flink 1.17.1 (3 min)
4. Proceed with Quick Setup above

---

## WHAT TO SHOW MA'AM

### Narrative Flow (15 minutes)

| Time | Demo | Show | Explain |
|------|------|------|---------|
| 0:00 | Architecture | Project structure on screen | "4-member team architecture matching distributed systems theory" |
| 2:00 | Flink Cluster | Web UI with 4 Task Managers | "Master-Worker setup: 1 JobManager + 4 TaskManagers = distributed" |
| 5:00 | Job Deployment | Running job in Web UI | "Real-time job consuming transactions from port 9999" |
| 7:00 | Live Fraud Alerts | Fraud alerts printing | "Real-time detection within 5-minute window (beat 2-hour batch)" |
| 12:00 | Checkpointing | Checkpoint directory | "Automatic state snapshots every 10s = exact-once semantics" |
| 14:00 | Failure Recovery* | Kill + auto-restart | "Automatic recovery without data loss (Chandy-Lamport)" |

_*Optional if time permits_

### Key V&V Points for Viva

**Parallel Processing:**
- ✓ Evidence: 4 subtasks in Web UI running simultaneously
- ✓ Explanation: "Transactions split by user ID; each TaskManager processes independently"

**Real-Time Processing:**
- ✓ Evidence: Fraud alerts appear within seconds of transaction
- ✓ Explanation: "Event-time windowing (5-min tumbling) vs 2-hour batch"

**Fault Tolerance:**
- ✓ Evidence: Checkpoint directory with snapshot files
- ✓ Explanation: "Chandy-Lamport snapshot protocol: pause → snapshot → resume"

**Exactly-Once Semantics:**
- ✓ Evidence: Code in StreamFraudDetectorJob.java line 35
- ✓ Explanation: "CheckpointingMode.EXACTLY_ONCE ensures no duplicate processing"

---

## TROUBLESHOOTING

### "Port 9999 already in use"
```powershell
# Find process using port
netstat -ano | findstr :9999
taskkill /PID <PID> /F
```

### "Flink Web UI won't open on localhost:8081"
```powershell
# Check if JobManager started
jps
# Restart cluster
flink-1.17.1/bin/stop-cluster.bat
flink-1.17.1/bin/start-cluster.bat
```

### "Job deployment fails with 'cannot find symbol'"
```powershell
# Rebuild
mvn clean package -DskipTests
# Then deploy again
flink run --parallelism 4 target/fraud-detection.jar
```

### "Python generator: Connection refused"
- [ ] Is port 9999 open for listening?
- [ ] Is the Flink job running? Check: `jps` or Web UI
- [ ] Try: `python transaction_generator.py --port 9999`

### "Checkpoint directory not found"
- [ ] Create it: `mkdir C:\tmp\flink-checkpoints\`
- [ ] Or edit flink-conf.yaml to use different path

---

## PRINT-READY SUMMARY

**For 3 Systems Demonstration:**

1. **System 1 (Your Current):** ✅ Already tested - run demo as-is
2. **System 2:** Clone repo → Build (1 min) → Download Flink → Run job
3. **System 3:** Same as System 2

**Total time per new system:** ~10 minutes setup, ~15 minutes demo = **25 min per system**

**Success Metric:** When ma'am sees:
- 4 parallel Task Managers running
- Real-time fraud alerts printing
- Checkpoint snapshots being created every 10 seconds

Then you've proven: **Parallel ✓ Real-Time ✓ Fault-Tolerant ✓**

---

## MEMBER 2 TALKING POINTS

*"This is my core contribution as Member 2 (Streaming Developer):*

1. **System Design**: Built complete Flink pipeline consuming real transactions
2. **Parallel Processing**: Keyed aggregation splits work across 4 workers
3. **Real-Time Detection**: 5-minute windows detect fraud faster than 2-hour batch
4. **State Management**: Implements exactly-once semantics with checkpointing
5. **Scalability**: Architecture supports 100+ TaskManagers (demonstrated locally with 4)
6. **Production-Ready**: Includes error handling, logging, POJO serialization"

---

**Good luck! 🚀 You've got this!**
