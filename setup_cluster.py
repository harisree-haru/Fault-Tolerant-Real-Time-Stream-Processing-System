"""
Cluster Setup Script
Reads cluster-env.properties and updates all Flink config files with the correct IPs.
Run this after editing cluster-env.properties with the correct laptop IPs.

Usage: python setup_cluster.py
"""
import os
import re

ENV_FILE = "cluster-env.properties"
FLINK_CONF_FILES = ["flink-conf.yaml", os.path.join("conf", "flink-conf.yaml")]


def read_env():
    props = {}
    with open(ENV_FILE, "r") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            key, _, value = line.partition("=")
            props[key.strip()] = value.strip()
    return props


def update_flink_conf(filepath, props):
    if not os.path.exists(filepath):
        print(f"  [SKIP] {filepath} not found")
        return

    with open(filepath, "r") as f:
        content = f.read()

    replacements = {
        r"(jobmanager\.rpc\.address:\s*)[\d.]+\S*": rf"\g<1>{props['MASTER_IP']}",
        r"(jobmanager\.rpc\.port:\s*)\d+": rf"\g<1>{props['MASTER_PORT']}",
        r"(rest\.port:\s*)\d+": rf"\g<1>{props['REST_PORT']}",
        r"(taskmanager\.numberOfTaskSlots:\s*)\d+": rf"\g<1>{props['TASK_SLOTS']}",
        r"(parallelism\.default:\s*)\d+": rf"\g<1>{props['PARALLELISM']}",
    }

    for pattern, replacement in replacements.items():
        content = re.sub(pattern, replacement, content)

    with open(filepath, "w") as f:
        f.write(content)
    print(f"  [OK] {filepath} updated")


def update_workers(props):
    workers_file = os.path.join("conf", "workers")
    workers = [props["MASTER_IP"]]
    for key in ["WORKER1_IP", "WORKER2_IP", "WORKER3_IP"]:
        ip = props.get(key, "")
        if ip and ip != "CHANGE_ME":
            workers.append(ip)

    with open(workers_file, "w") as f:
        f.write("\n".join(workers) + "\n")
    print(f"  [OK] {workers_file} updated with {len(workers)} workers")


def main():
    print("=" * 50)
    print("  Flink Cluster Configuration Setup")
    print("=" * 50)

    if not os.path.exists(ENV_FILE):
        print(f"\nERROR: {ENV_FILE} not found!")
        print("Create it first with the correct IPs.")
        return

    props = read_env()

    print(f"\nMaster (JobManager): {props.get('MASTER_IP', '?')}:{props.get('MASTER_PORT', '?')}")
    print(f"Web UI:              http://{props.get('MASTER_IP', '?')}:{props.get('REST_PORT', '?')}")
    print(f"Workers:")
    for key in ["WORKER1_IP", "WORKER2_IP", "WORKER3_IP"]:
        ip = props.get(key, "not set")
        status = "CONFIGURED" if ip != "CHANGE_ME" and ip != "not set" else "NOT SET"
        print(f"  {key}: {ip} [{status}]")

    print(f"\nTask Slots: {props.get('TASK_SLOTS', '?')} | Parallelism: {props.get('PARALLELISM', '?')}")

    print("\nUpdating Flink configuration files...")
    for conf_file in FLINK_CONF_FILES:
        update_flink_conf(conf_file, props)

    print("\nUpdating workers file...")
    update_workers(props)

    print("\n" + "=" * 50)
    print("  Setup Complete!")
    print("=" * 50)
    print(f"\nOn MASTER ({props['MASTER_IP']}):")
    print("  .\\start-cluster.bat")
    print(f"\nOn WORKERS:")
    print("  .\\start-taskmanager-only.bat")
    print(f"\nDashboard: http://{props['MASTER_IP']}:{props['REST_PORT']}")


if __name__ == "__main__":
    main()
