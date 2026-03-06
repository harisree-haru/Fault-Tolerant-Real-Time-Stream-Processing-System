#!/usr/bin/env python3
"""Submit Member 2's Fraud Detection Job to Flink"""
import requests
import json

FLINK = "http://localhost:8081"
JAR_PATH = "target/fraud-detection.jar"

# Upload
print("[*] Uploading fraud-detection.jar...")
with open(JAR_PATH, 'rb') as f:
    r = requests.post(f"{FLINK}/v1/jars/upload", files={'jarfile': f}, timeout=30)

if r.status_code != 200:
    print(f"[!] Upload failed: {r.status_code}")
    print(r.json())
    exit(1)

upload_resp = r.json()
jar_id = upload_resp.get('filename', '').split('\\')[-1] if '\\' in upload_resp.get('filename', '') else upload_resp.get('filename', '').split('/')[-1]
print(f"[+] Uploaded: {jar_id}")

# Submit
print("[*] Submitting job with parallelism 4...")
url = f"{FLINK}/v1/jars/{jar_id}/run"
data = {"parallelism": 4}
r = requests.post(url, json=data, timeout=30)

if r.status_code == 200:
    job_id = r.json().get('jobid')
    print(f"\n[✓✓✓ Job RUNNING ✓✓✓]")
    print(f"[✓] Job ID: {job_id}")
    print(f"[→] Monitor: {FLINK}/#/jobs/{job_id}\n")
else:
    print(f"[!] Error ({r.status_code}):")
    print(json.dumps(r.json(), indent=2))
    exit(1)
