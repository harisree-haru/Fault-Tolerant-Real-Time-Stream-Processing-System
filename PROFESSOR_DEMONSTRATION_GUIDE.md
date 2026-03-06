# Professor Demonstration Guide: Real-Time Fraud Detection 🚀

This guide explains how our system implements and validates core Distributed Systems concepts: **Parallelism, Fault Tolerance (Chandy-Lamport), and Exactly-Once Semantics.**

---

## 1. How Parallelism Works
Our cluster is architected to handle high-volume streams by distributing work across multiple "Task Slots".

- **JobManager (Master):** Coordinates resources and distributes subtasks.
- **TaskManager (Worker):** Executes the actual code.
- **Task Slots:** We have configured **4 parallel slots**, allowing 4 instances of each fraud detection operator to run simultaneously.

### Visual Proof: Cluster Resources
![Task Manager Slots](file:///Users/nanducc/.gemini/antigravity/brain/05f35037-6ff1-4882-b3d0-0d3449614f08/task_manager_details.jpg)
*Observation: Notice "Available Task Slots: 4" in the Flink Web UI.*

---

## 2. How Fault Tolerance Works (Checkpointing)
We use the **Chandy-Lamport algorithm** to create "snapshots" of the entire system state without stopping the data stream.

- **Checkpointing:** Every 10 seconds, Flink saves the current "cursor" of the stream and the state of fraud detectors (e.g., aggregation counts) to persistent storage.
- **Exactly-Once Semantics:** By storing the exact stream offset and operator state together, we ensure that during recovery, no transaction is processed twice or missed.

### Visual Proof: Checkpoint History
![Checkpoint Metrics](file:///Users/nanducc/.gemini/antigravity/brain/05f35037-6ff1-4882-b3d0-0d3449614f08/flink_checkpoints_history_1772817859773.png)
*Observation: Regular 10-second intervals and consistent state sizes prove reliable checkpointing.*

---

## 3. The Failure & Recovery Demonstration
To prove the system is truly fault-tolerant, we simulated a "Hard Failure" (killing the JobManager).

### Phase 1: Failure Moment
![Simulated Failure](file:///Users/nanducc/.gemini/antigravity/brain/05f35037-6ff1-4882-b3d0-0d3449614f08/simulate_failure.jpg)
*Action: The JobManager process was forcibly terminated.*

### Phase 2: Recovery & Restoration
When the cluster restarts, it doesn't start from zero. It reads the latest checkpoint (e.g., `chk-24`) and resumes precisely where it left off.

![System Recovered](file:///Users/nanducc/.gemini/antigravity/brain/05f35037-6ff1-4882-b3d0-0d3449614f08/system_recovered.jpg)
*Proof: Notice the "Recovery" status in logs and the job resuming at the previous transaction count.*

---

## 4. Your Deliverables (Where to find them)

| Deliverable | Location | Purpose |
| :--- | :--- | :--- |
| **Recovery Report** | [MEMBER4_CHECKPOINT_VALIDATION.md](file:///Users/nanducc/Desktop/ds_apacheFlink/Fault-Tolerant-Real-Time-Stream-Processing-System/MEMBER4_CHECKPOINT_VALIDATION.md) | Technical proof of state restoration. |
| **Checkpoint Logs** | [member4_checkpoints_directory.txt](file:///Users/nanducc/Desktop/ds_apacheFlink/Fault-Tolerant-Real-Time-Stream-Processing-System/member4_checkpoints_directory.txt) | Filesystem proof of saved state. |
| **Screenshots** | `~/Desktop/.../screenshots/` | Visual sequence of the demonstration. |
| **Rubric Mapping** | [RUBRIC_MAPPING_ANALYSIS.md](file:///Users/nanducc/Desktop/ds_apacheFlink/Fault-Tolerant-Real-Time-Stream-Processing-System/RUBRIC_MAPPING_ANALYSIS.md) | How our implementation hits all 50 marks. |

---
**Summary for Presentation:**
*"Professor, we demonstrated parallelism using 4 task slots. For fault tolerance, we implemented Chandy-Lamport checkpointing every 10 seconds. We then forcibly killed the cluster and showed that Flink successfully restored the exact fraud detection state from the latest filesystem checkpoint, maintaining Exactly-Once Semantics."*
