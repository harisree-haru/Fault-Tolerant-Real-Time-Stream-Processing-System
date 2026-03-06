#!/usr/bin/env python3
"""
Simple Flink Job Submission - Member 2's Fraud Detection Application
"""
import requests
import json
import os
import time
from pathlib import Path

FLINK_REST = "http://localhost:8081"
JAR_PATH = "target/fraud-detection.jar"

# Step 1: Upload JAR
print("[*] Uploading fraud-detection.jar...")
try:
    with open(JAR_PATH, 'rb') as f:
        files = {'jarfile': f}
        r = requests.post(f"{FLINK_REST}/v1/jars/upload", files=files, timeout=30)
        r.raise_for_status()
except Exception as e:
    print(f"[!] Upload failed: {e}")
    exit(1)

upload_resp = r.json()
jar_filename = upload_resp.get('filename', '')
print(f"[+] Uploaded: {jar_filename}")

# Extract just the filename for the run URL
jar_name = jar_filename.split('/')[-1] if '/' in jar_filename else jar_filename

# Step 2: Submit job
print(f"[*] Submitting job with parallelism 4...")
try:
    run_url = f"{FLINK_REST}/v1/jars/{jar_name}/run"
    run_data = {"parallelism": 4}
    r = requests.post(run_url, json=run_data, timeout=30)
    r.raise_for_status()
except Exception as e:
    print(f"[!] Submission failed: {e}")
    print(f"URL was: {run_url}")
    exit(1)

result = r.json()
job_id = result.get('jobid', 'Unknown')
print(f"\n[✓] JOB SUBMITTED SUCCESSFULLY!")
print(f"[✓] Job ID: {job_id}")
print(f"\n[→] Monitor at: {FLINK_REST}/#/jobs/{job_id}")
print(f"[→] Or visit: {FLINK_REST}\n")
