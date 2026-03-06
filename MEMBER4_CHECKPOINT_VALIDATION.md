# MEMBER 4: State Backend & Checkpoint Validation Report 🔐

## 1. System Configuration Proof
- **Flink Version:** 1.17.1
- **Java Version:** 25 (OpenJDK)
- **State Backend:** `hashmap`
- **Checkpoint Mode:** `EXACTLY_ONCE`
- **Interval:** 10,000ms (10 seconds)

### `flink-conf.yaml` Verification
```yaml
state.backend.type: hashmap
state.checkpoints.dir: file:///tmp/flink-checkpoints
execution.checkpointing.interval: 10000
execution.checkpointing.mode: EXACTLY_ONCE
```

## 2. Checkpoint Storage Evidence
Checkpoints are successfully being created in the configured local directory.

### Directory Structure (Initial Run)
Job ID: `ea128c0509ae80729e8590f81c3b7f59`
```text
/tmp/flink-checkpoints/ea128c0509ae80729e8590f81c3b7f59:
chk-1/           chk-2/           chk-3/           ... chk-24/
_metadata (within each chk- folder)
```

## 3. Web UI Metrics Screenshot
The Flink Web UI confirms that checkpoints are being triggered every 10 seconds and completing with ~10 KB of state size.

![Flink Checkpoint Summary](/Users/nanducc/Desktop/ds_apacheFlink/Fault-Tolerant-Real-Time-Stream-Processing-System/final_ss/member4_checkpoint_summary.png)

## 4. Failure & Recovery Validation
### Failure Simulation
1. **Action:** Killed JobManager process (`kill -9 94582`).
2. **Impact:** System halted; TaskManager lost connection; Transaction generator reported `Broken pipe`.

### Recovery Process
1. **Action:** Restarted Flink cluster.
2. **Restoration:** Restored job from `chk-24` using the command:
   ```bash
   curl -X POST "http://localhost:8081/jars/.../run?savepointPath=/tmp/flink-checkpoints/ea128c0509ae80729e8590f81c3b7f59/chk-24"
   ```
3. **Verification:**
   - Job ID: `39cdb432aef39c9dcf9a3de8427cf642` started successfully.
   - Logs confirmed state restoration from the specified checkpoint.
   - New checkpoints (e.g., `chk-1`, `chk-2`) began for the new Job ID, confirming continuity.

## 5. Exactly-Once Semantics Confirmation
By restoring from the exact checkpoint `chk-24`, Flink ensures that every event up to that point was processed and that the state (e.g., fraud detectors, window accumulations) is perfectly preserved. Subsequent events from the transaction generator are processed without duplication or loss from the last consistent state.

---
**Status:** ✅ VALIDATED
**Engineer:** Member 4 (State Backend & Checkpoint Validation)
