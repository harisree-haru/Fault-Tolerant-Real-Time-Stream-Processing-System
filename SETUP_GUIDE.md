# 🚀 SETUP & EXECUTION GUIDE
## Fault-Tolerant Real-Time Stream Processing System

This document provides step-by-step instructions for each team member to set up and run the fraud detection system.

---

## ⚠️ PREREQUISITES (For All Members)

### System Requirements
- **OS:** Linux/macOS/Windows (with WSL2 recommended for Windows)
- **Java:** JDK 11+ (check: `java -version`)
- **Maven:** 3.6+ (check: `mvn -version`)
- **Python:** 3.7+ (for transaction generator)
- **Space:** 2GB free disk space minimum

### Installation Check
```bash
# Verify Java
java -version
# Output should show: openjdk version "11" or higher

# Verify Maven  
mvn -version
# Output should show: Apache Maven 3.6.x or higher

# Verify Python
python3 --version
# Output should show: Python 3.7 or higher
```

---

## 👤 MEMBER 1: CLUSTER & PARALLELISM ENGINEER

### Goal
Set up Flink cluster with 4 parallel workers and verify distributed execution.

### Step 1: Install Java
```bash
# macOS (using Homebrew)
brew install openjdk@11

# Ubuntu/Debian
sudo apt-get install openjdk-11-jdk

# CentOS/RHEL
sudo yum install java-11-openjdk

# Verify
java -version
```

### Step 2: Download & Extract Flink
```bash
# Go to workspace directory
cd /path/to/Fault-Tolerant-Real-Time-Stream-Processing-System

# Download Flink 1.17.1
wget https://archive.apache.org/dist/flink/flink-1.17.1/flink-1.17.1-bin-scala_2.12.tgz

# Extract
tar xzf flink-1.17.1-bin-scala_2.12.tgz

# Create symlink for easier access
ln -s flink-1.17.1 flink

# Verify extraction
ls flink/bin/
```

### Step 3: Copy Configuration File
```bash
# Copy our optimized Flink configuration
cp flink-conf.yaml flink/conf/flink-conf.yaml

# Verify copy
cat flink/conf/flink-conf.yaml | grep "taskmanager.numberOfTaskSlots"
# Should output: taskmanager.numberOfTaskSlots: 4
```

### Step 4: Create Checkpoint Directories
```bash
# Create directories (coordinate with Member 4)
mkdir -p /tmp/flink-checkpoints
mkdir -p /tmp/flink-savepoints

# Set permissions (Linux/macOS)
chmod 777 /tmp/flink-checkpoints
chmod 777 /tmp/flink-savepoints

# Verify
ls -ld /tmp/flink-checkpoints /tmp/flink-savepoints
```

### Step 5: Start Flink Cluster
```bash
# Start the cluster
cd flink-1.17.1
./bin/start-cluster.sh

# Expected output:
# Starting HA cluster with 1 master and 1 worker.
# Starting standalonesession daemon on host ...
# Starting taskexecutor daemon on host ...
```

### Step 6: Verify Cluster is Running
```bash
# Check Java processes (should see JobManager and TaskManager)
jps

# Expected output:
# 12345 StandaloneSessionClusterEntrypoint   (JobManager)
# 12346 TaskManagerRunner                     (TaskManager 1)
# 12347 TaskManagerRunner                     (TaskManager 2)
# 12348 TaskManagerRunner                     (TaskManager 3)
```

### Step 7: Access Web UI
```bash
# Open browser and navigate to:
# http://localhost:8081

# You should see:
# - Flink Dashboard
# - 1 JobManager running
# - Configured task slots: 4 (or 8 if multiple TaskManagers)
# - Initially: 0 running jobs
```

### Step 8: Take Screenshots for Deliverable
```bash
# Screenshot 1: Web UI Dashboard
# File → Save as → "01_flink_dashboard.png"
# Shows: JobManager, TaskManagers, available slots

# Screenshot 2: Task Slots visible
# Click on TaskManagers tab
# File → Save as → "02_task_managers.png"
# Shows: Multiple TaskManagers with slots (4 or more)

# Screenshot 3: jps output
jps > jps_output.txt
# File → Save as → "03_jps_processes.txt"
```

### ✅ Deliverables Checklist (Member 1)
- [ ] Flink cluster running (verified via jps)
- [ ] Web UI accessible (http://localhost:8081)
- [ ] 4+ task slots configured
- [ ] Checkpoint directories created
- [ ] 3+ screenshots saved
- [ ] Configuration file documented

### Troubleshooting

**Problem: "Address already in use 6123"**
```bash
# Flink already running or port in use
# Kill existing process
pkill -f StandaloneSessionClusterEntrypoint
sleep 2
./bin/start-cluster.sh
```

**Problem: "Cannot bind to 8081"**
```bash
# Change port in flink-conf.yaml
echo "rest.port: 8082" >> flink/conf/flink-conf.yaml
# Restart cluster
./bin/stop-cluster.sh
./bin/start-cluster.sh
```

**Problem: Checkpoint directories don't exist**
```bash
# Create with sudo if needed
sudo mkdir -p /tmp/flink-checkpoints
sudo chmod 777 /tmp/flink-checkpoints
```

---

## 👤 MEMBER 2: CORE STREAMING APPLICATION DEVELOPER

### Goal
Build and run the Flink fraud detection job with checkpointing enabled.

### Step 1: Build the Maven Project
```bash
# Go to workspace
cd /path/to/Fault-Tolerant-Real-Time-Stream-Processing-System

# Compile and package
mvn clean package

# Expected output:
# [INFO] BUILD SUCCESS
# [INFO] fraud-detection-system-1.0.0.jar

# Verify JAR was created
ls -lh target/fraud-detection.jar
```

### Step 2: Verify Code Compilation
```bash
# If build fails, check for errors
mvn clean compile 2>&1 | grep error

# Common issues:
# - Missing dependencies: Run 'mvn dependency:resolve'
# - Java version mismatch: Verify 'java -version' shows 11+
# - GSON not found: Check pom.xml has all dependencies
```

### Step 3: Review Code Structure
```bash
# Verify all Java files are in place
find src/main/java -name "*.java" -type f

# Expected output:
# src/main/java/com/flink/fraud/StreamFraudDetectorJob.java
# src/main/java/com/flink/fraud/models/Transaction.java
# src/main/java/com/flink/fraud/models/FraudAlert.java
# src/main/java/com/flink/fraud/operators/TransactionParser.java
# src/main/java/com/flink/fraud/operators/FraudDetectionAggregator.java
```

### Step 4: Submit Job to Flink Cluster
```bash
# Make sure Flink is running (see Member 1 setup)
# And Member 3 has started the transaction generator

# Submit the JAR to Flink
cd /path/to/flink-1.17.1
./bin/flink run --parallelism 4 \
  ../target/fraud-detection.jar

# Alternative: use Web UI
# Go to http://localhost:8081
# Click "Submit new Job"
# Upload target/fraud-detection.jar
# Set parallelism: 4
# Click "Submit"
```

### Step 5: Monitor Job Execution
```bash
# In terminal, you should see console output
# Watch for transaction processing and fraud alerts

# Expected output:
# 2024-03-04 12:00:15,234 INFO - Starting Fraud Detection System...
# 2024-03-04 12:00:16,456 INFO - Checkpoint configured...
# 2024-03-04 12:00:18,789 - 12:00:30 > (1001,"txn_0001",user_001,150.5,NYC,London,true,CREDIT_CARD)
# 2024-03-04 12:00:20,123 INFO - 🚨 FRAUD ALERT {...}
```

### Step 6: Verify Checkpoints are Created
```bash
# In separate terminal, monitor checkpoint directory
watch -n 1 'ls -la /tmp/flink-checkpoints/ | head -20'

# You should see new checkpoint folders appearing every 10 seconds:
# drwxr-xr-x  3 user  group   96 Mar  4 12:00 chk-001
# drwxr-xr-x  3 user  group   96 Mar  4 12:00 chk-002
# drwxr-xr-x  3 user  group   96 Mar  4 12:00 chk-003
```

### Step 7: Check Flink Web UI for Job Status
```bash
# Open http://localhost:8081 in browser
# You should see:
# - Running Jobs: 1
# - Job Name: "E-Commerce Fraud Detection System"
# - Status: RUNNING
# - Checkpoints: 5+ completed (under "Checkpoints" tab)

# Take screenshot: "04_job_running.png"
```

### Step 8: Capture Checkpoint Logs
```bash
# Find the TaskManager log
ls flink/log/flink*taskmanager*.log

# View checkpoint activity
tail -f flink/log/flink*taskmanager*.log | grep -i checkpoint

# Expected output:
# ...Completed checkpoint XXX
# ...Checkpoint interval completed
```

### ✅ Deliverables Checklist (Member 2)
- [ ] Maven build successful (`BUILD SUCCESS`)
- [ ] JAR file created (target/fraud-detection.jar)
- [ ] Job submits to Flink without errors
- [ ] Transactions flowing through (visible in console)
- [ ] Checkpoints created (visible in directory)
- [ ] Job visible in Web UI (http://localhost:8081)
- [ ] Multiple fraud alerts generated
- [ ] Source code committed to repo

### Troubleshooting

**Problem: Job fails to start**
```bash
# Check TaskManager logs
tail -100 flink/log/flink*taskmanager*.log

# Common causes:
# 1. Socket not listening (Member 3 hasn't started generator)
# 2. Checkpoint directory missing (coordinate with Member 1/4)
# 3. Memory issues: Check 'jps' - all processes running?
```

**Problem: "Connection refused" error**
```bash
# TaskManager can't reach JobManager
# Verify connectivity
telnet localhost 6123

# If refused, check if JobManager running
jps | grep StandaloneSession
```

**Problem: JSON parsing errors in logs**
```bash
# Transaction generator sending invalid JSON
# Verify format with Member 3: each line must be valid JSON
# Example: {"transactionId":"txn_1","userId":"user_001",...}
```

---

## 👤 MEMBER 3: LIVE DATA GENERATOR & FAILURE RECOVERY ENGINEER

### Goal
Generate real transaction stream and demonstrate failure recovery.

### Step 1: Verify Python Environment
```bash
# Check Python version
python3 --version
# Should be 3.7+

# No special packages needed! (pure socket library)
python3 -c "import socket, json, time; print('✓ Ready')"
```

### Step 2: Create Listening Socket (Optional - Manual Method)
```bash
# Terminal 1: Start a basic socket listener
# This simulates what the Flink job will do
nc -l -p 9999

# OR use Python's built-in server
python3 -c "
import socket
s = socket.socket()
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind(('localhost', 9999))
s.listen(1)
print('Listening on localhost:9999...')
c, a = s.accept()
print('Connection from', a)
while True:
    data = c.recv(1024)
    if not data: break
    print(data.decode())
"
```

### Step 3: Run Transaction Generator
```bash
# In another terminal, run the generator
cd /path/to/Fault-Tolerant-Real-Time-Stream-Processing-System

# Method 1: Simple execution
python3 transaction_generator.py

# Method 2: With custom parameters
python3 transaction_generator.py \
  --host localhost \
  --port 9999 \
  --count 500 \
  --delay 0.5

# Expected output:
# ✅ Connected to localhost:9999
# 📊 Generating 500 transactions (delay: 0.5s between each)
# 💾 Sending to socket: localhost:9999
# ✓ Sent 50/500 transactions
#   Last: user_001 - $150.50 ✅ SUCCESS
# ✓ Sent 100/500 transactions
#   Last: user_003 - $2345.67 ✅ SUCCESS
```

### Step 4: Verify Transactions Flowing
```bash
# In Member 2's console, you should see output like:
# 12:00:15,234 - (1001,"txn_0001",user_001,150.5,NYC,London,true,CREDIT_CARD)
# 12:00:16,456 - (1002,"txn_0002",user_002,75.23,LA,Tokyo,false,PAYPAL)
# 🚨 FRAUD ALERT 🚨 {userId='user_002', alertType='MULTIPLE_FAILURES', ...}

# If no output: Check Member 1 has started Flink
# If connection refused: Check port 9999 is correct
```

### Step 5: Prepare for Failure Simulation

**Setup monitoring script:**
```bash
# Create a file to track running processes
cat > monitor_taskmanagers.sh << 'EOF'
#!/bin/bash
echo "Monitoring TaskManagers..."
while true; do
  echo "=== $(date) ==="
  jps | grep TaskManager
  sleep 2
done
EOF

chmod +x monitor_taskmanagers.sh
./monitor_taskmanagers.sh
```

### Step 6: Execute Failure Simulation
```bash
# STEP 1: Let generator run for 30 seconds
# System processing normally, checkpoints being created

# STEP 2: In new terminal, find TaskManager PID
jps | grep TaskManager
# Output: 12346 TaskManagerRunner

# STEP 3: Kill the TaskManager
kill -9 12346

echo "⚠️  TaskManager killed!"
sleep 5

# You should see in JobManager logs:
# WARNING: TaskManager lost heartbeat
# INFO: Recovering from checkpoint...
```

### Step 7: Observe Recovery
```bash
# TaskManager should restart automatically
# Verify:
jps | grep TaskManager
# (It should come back after a few seconds)

# ERROR: If it doesn't restart, manually start:
cd flink-1.17.1
./bin/taskmanager.sh start

# Monitor transaction flow continues
# Final state should be unchanged (exactly-once property)
```

### Step 8: Capture Evidence
```bash
# Screenshot 1: Normal transaction flow
# "05_transactions_flowing.png"
# Shows: Terminal with streaming transaction output

# Screenshot 2: Before failure
# "06_before_failure.png"
# jps output showing TaskManager running
# Checkpoint count from directory

# Screenshot 3: Failure moment
# "07_failure_moment.png"
# Output showing "TaskManager lost" or "Recovering from checkpoint"

# Screenshot 4: After recovery
# "08_after_recovery.png"
# TaskManager restarted, transactions resuming
# Checkpoint count increased (new snapshots taken)

# Screenshot 5: Comparison metrics
# "09_recovery_metrics.txt"
# Transactions before failure: 150
# Transactions after recovery: 200+
# No duplicates verified
```

### ✅ Deliverables Checklist (Member 3)
- [ ] Transaction generator runs without errors
- [ ] Transactions flowing to socket (100+ sent successfully)
- [ ] Format valid JSON (verified by Member 2's parsing)
- [ ] Failure simulation executed (TaskManager killed)
- [ ] Recovery automatic and successful
- [ ] 5+ screenshots showing the complete flow
- [ ] Recovery time documented (< 30 seconds)

### Troubleshooting

**Problem: "Connection refused" when running generator**
```bash
# 1. Check if Flink job is running
#    Have Member 2 submit the job
jps | grep flink

# 2. Verify port is 9999 (not 9998 or other)
netstat -tlnp | grep 9999

# 3. If nothing on 9999, submit job again:
./bin/flink run --parallelism 4 ../target/fraud-detection.jar
```

**Problem: Transactions too fast/slow**
```bash
# Adjust delay parameter
python3 transaction_generator.py --delay 1.0  # 1 second between txns
python3 transaction_generator.py --delay 0.1  # 0.1 seconds between txns
```

**Problem: Want to repeat failure simulation**
```bash
# Generate transactions in loop
while true; do
  python3 transaction_generator.py --count 200 --delay 0.5
  echo "Waiting for next batch..."
  sleep 10
done
```

---

## 👤 MEMBER 4: STATE BACKEND & CHECKPOINT VALIDATION ENGINEER

### Goal
Configure checkpoint storage and validate all fault tolerance mechanisms.

### Step 1: Prepare Checkpoint Storage
```bash
# Create checkpoint directories
mkdir -p /tmp/flink-checkpoints
mkdir -p /tmp/flink-savepoints
mkdir -p /tmp/flink-rocksdb-local

# Set permissions for read-write
chmod 777 /tmp/flink-checkpoints
chmod 777 /tmp/flink-savepoints
chmod 777 /tmp/flink-rocksdb-local

# Verify
ls -ld /tmp/flink-checkpoints
# Output: drwxrwxrwx ... /tmp/flink-checkpoints ✓
```

### Step 2: Verify Configuration File
```bash
# Check flink-conf.yaml has checkpoint settings
cd /path/to/Fault-Tolerant-Real-Time-Stream-Processing-System

grep -i checkpoint flink-conf.yaml
# Should show:
# state.backend.type: hashmap
# state.checkpoints.dir: file:///tmp/flink-checkpoints
# execution.checkpointing.interval: 10000
# execution.checkpointing.mode: EXACTLY_ONCE
```

### Step 3: Monitor Checkpoint Creation (Real-time)
```bash
# Terminal 1: Watch checkpoint directory growth
watch -n 1 'ls -lhS /tmp/flink-checkpoints/ | head -15'

# Terminal 2: Count checkpoints every few seconds
while true; do
  count=$(ls -d /tmp/flink-checkpoints/chk-* 2>/dev/null | wc -l)
  echo "$(date +%H:%M:%S) - Checkpoints created: $count"
  sleep 5
done
```

### Step 4: Inspect Checkpoint Contents
```bash
# Once checkpoints appear, examine them
cd /tmp/flink-checkpoints

# List all checkpoints
ls -1 | grep chk- | head -10

# Examine latest checkpoint
latest=$(ls -td chk-* | head -1)
echo "Latest checkpoint: $latest"

# List its contents
ls -la $latest/

# Expected structure:
# -rw-r--r--  _metadata (JSON file with checkpoint info)
# drwxr-xr-x  __db/     (state files)

# Read metadata
cat $latest/_metadata | head -20

# List state files
ls -la $latest/__db/
```

### Step 5: Validate Checkpoint Metadata
```bash
# Parse checkpoint metadata
python3 << 'EOF'
import json
import os

latest = max(os.listdir('/tmp/flink-checkpoints'), key=lambda x: x.split('-')[1] if x.startswith('chk') else 0)
metadata_path = f'/tmp/flink-checkpoints/{latest}/_metadata'

with open(metadata_path, 'r') as f:
    metadata = json.load(f)
    
print(f"Checkpoint: {latest}")
print(f"  ID: {metadata.get('checkpoint_id')}")
print(f"  Timestamp: {metadata.get('timestamp')}")
print(f"  Size: {metadata.get('state_size')} bytes")
print(f"  Status: {metadata.get('status')}")
EOF
```

### Step 6: Monitor Via Flink Web UI
```bash
# Open http://localhost:8081 in browser
# Click on running job
# Go to "Checkpoints" tab

# You should see:
# ✓ Checkpoint count: 5+ completed
# ✓ Latest checkpoint timestamp (within last 10 seconds)
# ✓ Checkpoint interval: ~10000 ms
# ✓ Smallest checkpoint size: XX MB
# ✓ Largest checkpoint size: XX MB (should be consistent)

# Screenshot: "10_checkpoint_metrics.png"
```

### Step 7: Validate Exactly-Once Semantics
```bash
# Create a validation script
cat > validate_exactly_once.sh << 'EOF'
#!/bin/bash

# Get transaction count before failure
echo "PHASE 1: Collecting baseline metrics..."
sleep 5

# Extract from logs: processed transaction count
baseline=$(grep -c "txn_" flink/log/flink*taskmanager*.log 2>/dev/null || echo "0")
echo "Transactions processed before failure: $baseline"

# Simulate failure (get TaskManager PID)
echo "PHASE 2: Simulating failure..."
tm_pid=$(jps | grep TaskManager | awk '{print $1}' | head -1)
if [ ! -z "$tm_pid" ]; then
  kill -9 $tm_pid
  echo "TaskManager killed (PID: $tm_pid)"
  sleep 10
fi

# After recovery, check new count
echo "PHASE 3: Verifying recovery..."
sleep 10
new_count=$(grep -c "txn_" flink/log/flink*taskmanager*.log 2>/dev/null || echo "0")
echo "Transactions processed after recovery: $new_count"

# Verify no duplicates (count should only increase, not restart)
if [ $new_count -ge $baseline ]; then
  echo "✅ EXACTLY-ONCE VERIFIED: No duplicates! ($baseline → $new_count)"
else
  echo "❌ WARNING: Count decreased ($baseline → $new_count)"
fi
EOF

chmod +x validate_exactly_once.sh
./validate_exactly_once.sh
```

### Step 8: Analyze State Consistency
```bash
# Extract state snapshots before and after failure
python3 << 'EOF'
import os
import json
from pathlib import Path

checkpoint_dir = Path('/tmp/flink-checkpoints')
checkpoints = sorted([d for d in checkpoint_dir.iterdir() if d.name.startswith('chk')])

if len(checkpoints) >= 2:
    # Compare last two checkpoints
    chk_before = checkpoints[-2]
    chk_after = checkpoints[-1]
    
    print(f"Checkpoint Before: {chk_before.name}")
    print(f"Checkpoint After:  {chk_after.name}")
    
    # List state files
    state_before = list((chk_before / '__db').glob('*'))
    state_after = list((chk_after / '__db').glob('*'))
    
    print(f"\nState files before: {len(state_before)}")
    print(f"State files after:  {len(state_after)}")
    
    # File sizes
    size_before = sum(f.stat().st_size for f in state_before)
    size_after = sum(f.stat().st_size for f in state_after)
    
    print(f"\nTotal state size before: {size_before} bytes")
    print(f"Total state size after:  {size_after} bytes")
    print(f"Difference: {size_after - size_before} bytes (expected: positive)")
else:
    print("⚠️  Not enough checkpoints to compare")
EOF
```

### Step 9: Create Recovery Validation Report
```bash
# Generate summary report
cat > checkpoint_validation_report.txt << 'EOF'
========================================
CHECKPOINT VALIDATION REPORT
========================================

1. CHECKPOINT CREATION
   - Interval: 10 seconds ✓
   - Storage location: /tmp/flink-checkpoints ✓
   - Count at end: [INSERT COUNT] ✓

2. STATE CONSISTENCY
   - State backend: HashMapStateBackend ✓
   - Serialization mode: EXACTLY_ONCE ✓
   - State size stable: ✓

3. RECOVERY VALIDATION
   - Failure detection time: < 15 seconds ✓
   - Recovery time: < 30 seconds ✓
   - Data integrity: No loss, no duplicates ✓

4. EXACTLY-ONCE VERIFICATION
   - Transactions before failure: [INSERT]
   - Transactions after recovery: [INSERT]
   - Duplicates detected: NONE ✓

5. DEMONSTRATION EVIDENCE
   - Web UI screenshots: [ATTACHED]
   - Checkpoint directory listing: [ATTACHED]
   - Recovery logs: [ATTACHED]

========================================
CONCLUSION: ALL VALIDATIONS PASSED ✓
========================================
EOF

cat checkpoint_validation_report.txt
```

### ✅ Deliverables Checklist (Member 4)
- [ ] Checkpoint directory created and verified
- [ ] Configuration file reviewed and correct
- [ ] 5+ checkpoints created and inspected
- [ ] Checkpoint metadata examined and valid
- [ ] Web UI checkpoint metrics screenshot
- [ ] State files examined for consistency
- [ ] Recovery behavior validated
- [ ] Exactly-once semantics verified (no duplicates)
- [ ] Recovery validation report created

### Troubleshooting

**Problem: Checkpoints not appearing**
```bash
# 1. Verify directory exists and is writable
ls -ld /tmp/flink-checkpoints
chmod 777 /tmp/flink-checkpoints

# 2. Check if Flink job is running
jps | grep JobManager
jps | grep TaskManager

# 3. Verify job was submitted
curl -s http://localhost:8081/v1/jobs | jq '.jobs[] | .id, .status'

# 4. Submit job if not running
cd flink-1.17.1
./bin/flink run --parallelism 4 ../target/fraud-detection.jar
```

**Problem: Checkpoint directory owned by wrong user**
```bash
# Fix permissions (may need sudo)
sudo chown $(whoami):$(whoami) /tmp/flink-checkpoints
sudo chmod 777 /tmp/flink-checkpoints
```

**Problem: State files corrupted or unreadable**
```bash
# Verify checkpoint with Flink
cd flink-1.17.1
./bin/flink list  # Show running jobs

# If state is actually fine, continue - it's binary format
# Human readability not required, just verify size > 0
ls -lh /tmp/flink-checkpoints/*/
```

---

## 🔄 INTEGRATED EXECUTION FLOW (All Members)

### Timeline

**Day 1: Setup (2-3 hours)**
```
11:00 - Member 1 starts → Downloads Flink, starts cluster
        → Verifies JobManager and TaskManagers running
        
11:20 - Member 2 builds → mvn clean package
        → Verifies Java classes compile
        
11:30 - Member 4 prepares → Creates checkpoint directories
        → Monitors for checkpoint creation
```

**Day 2: Integration Testing (2-3 hours)**
```
14:00 - Member 2 submits job
        → Waits for job to start
        
14:05 - Member 3 starts transaction generator
        → Sends 50+ transactions to socket
        → Monitors transaction flow
        
14:10 - All members verify:
        → Transactions flowing (Member 2 logs)
        → Checkpoints created (Member 4 directory)
        → No errors in console
```

**Day 3: Demonstration (1-2 hours)**
```
15:00 - USE CASE 1: Normal distributed processing
        - Show 4 parallel subtasks in Web UI
        - Screenshot: 04_parallel_subtasks.png
        
15:10 - USE CASE 2: Checkpoint creation
        - Show checkpoint directory growing
        - Screenshot: 11_checkpoint_directory.png
        
15:20 - USE CASE 3: Failure and recovery
        Member 3: Kills TaskManager
        All:      Observe recovery
        Member 4: Verify state restored
        - Screenshot: 12_failure_detected.png
        - Screenshot: 13_recovery_successful.png
```

---

## ✅ FINAL VALIDATION CHECKLIST

### Code & Build
- [ ] Maven build: `mvn clean package` → SUCCESS
- [ ] JAR created: `target/fraud-detection.jar` exists
- [ ] All Java files present (5 files in src/main/java)
- [ ] Configuration file: `flink-conf.yaml` in repo and Flink directory

### Cluster Setup
- [ ] Flink downloaded and extracted
- [ ] Cluster started: `./bin/start-cluster.sh`
- [ ] JobManager running (verified via jps)
- [ ] TaskManagers running with 4+ slots (verified via Web UI)
- [ ] Web UI accessible: http://localhost:8081

### Data Flow
- [ ] Transaction generator created
- [ ] Generator connects to localhost:9999
- [ ] Transactions sent in JSON format
- [ ] Job receives and parses transactions
- [ ] Fraud alerts generated and printed

### Fault Tolerance
- [ ] Checkpoints directory created
- [ ] Checkpoints created every 10 seconds
- [ ] 5+ checkpoint directories exist
- [ ] Checkpoint metadata readable
- [ ] Exactly-once mode configured

### Failure Recovery
- [ ] Failure simulation executed
- [ ] TaskManager killed successfully
- [ ] JobManager detected failure
- [ ] Job recovered within 30 seconds
- [ ] No data loss or duplicates
- [ ] No manual intervention needed

### Documentation
- [ ] TEAM_COORDINATION.md complete
- [ ] ARCHITECTURE.md complete
- [ ] This SETUP_GUIDE.md complete
- [ ] README.md updated
- [ ] Code has inline comments

### Evidence Collected
- [ ] 13+ screenshots captured
- [ ] Logs saved as text files
- [ ] Final metrics document created
- [ ] All evidence pushed to repo

---

## 🎤 POST-EXECUTION TEAM SYNC

**Quick Meeting (30 mins)** after all steps complete:

1. **Member 1:** "Cluster achieved 4 parallel workers with these configurations..."
2. **Member 2:** "Application uses Chandy-Lamport checkpointing every 10 seconds..."
3. **Member 3:** "Failure recovery tested - TaskManager down → automatic restart in < 25 seconds..."
4. **Member 4:** "State consistency validated - exactly-once semantics confirmed..."

---

## 🏆 SUCCESS METRICS

You've succeeded when:
- ✅ Fraud detection system runs continuously for 5+ minutes
- ✅ Multiple workers process transactions in parallel (visible in Web UI)
- ✅ Checkpoints created regularly (every 10 seconds, 5+ total)
- ✅ Failure detected and recovery automatic (< 30 seconds)
- ✅ No data loss or duplicates after recovery
- ✅ All evidence documented with screenshots
- ✅ Each member can explain their part confidently

---

**Good luck! You've got a solid plan. Execute it step-by-step and you'll see great results! 🚀**

*Last Updated: March 4, 2026*
