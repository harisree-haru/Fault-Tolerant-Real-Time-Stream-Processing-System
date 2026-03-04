#!/usr/bin/env python3
"""
Real-Time Transaction Generator for Flink Fraud Detection System
Sends synthetic e-commerce transactions to localhost:9999

This script simulates:
- Multiple users making transactions
- Occasional failed transactions
- Geographic patterns
- Suspicious behavior for fraud testing

Usage:
    python3 transaction_generator.py
"""

import socket
import json
import random
import time
import argparse
import sys
from datetime import datetime

class TransactionGenerator:
    def __init__(self, host='localhost', port=9999, delay=0.5, transactions=1000):
        self.host = host
        self.port = port
        self.delay = delay  # Delay between transactions in seconds
        self.transactions = transactions
        
        # Realistic user segments
        self.users = [
            'user_001', 'user_002', 'user_003', 'user_004',
            'user_005', 'user_006', 'user_007', 'user_008'
        ]
        
        # Geographic locations
        self.cities = ['NYC', 'LA', 'London', 'Tokyo', 'Dubai', 'Singapore', 'Toronto', 'Berlin']
        
        # Payment methods
        self.payment_methods = ['CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'BANK_TRANSFER']
        
    def connect(self):
        """Connect to Flink socket source"""
        try:
            self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.sock.connect((self.host, self.port))
            print(f"✅ Connected to {self.host}:{self.port}")
            return True
        except Exception as e:
            print(f"❌ Failed to connect to {self.host}:{self.port}")
            print(f"   Error: {e}")
            print(f"\n   Make sure Flink socket source is listening on {self.host}:{self.port}")
            print(f"   You can start it with: nc -l {self.port}")
            return False
    
    def generate_transaction(self, txn_id):
        """Generate a realistic transaction"""
        user = random.choice(self.users)
        
        # 75% success rate, simulating real-world transaction failures
        is_successful = random.random() < 0.75
        
        # Realistic amount distribution
        # Most transactions $10-500, but some high-value
        if random.random() < 0.1:  # 10% high-value
            amount = round(random.uniform(1000, 5000), 2)
        else:
            amount = round(random.uniform(10, 500), 2)
        
        source_city = random.choice(self.cities)
        dest_city = random.choice(self.cities)
        
        timestamp = int(time.time() * 1000)
        payment_method = random.choice(self.payment_methods)
        
        transaction = {
            'transactionId': f'txn_{txn_id:06d}',
            'userId': user,
            'amount': amount,
            'sourceCity': source_city,
            'destinationCity': dest_city,
            'timestamp': timestamp,
            'isSuccessful': is_successful,
            'paymentMethod': payment_method
        }
        
        return transaction
    
    def send_transaction(self, transaction):
        """Send transaction as JSON to socket"""
        try:
            json_str = json.dumps(transaction)
            self.sock.send((json_str + '\n').encode('utf-8'))
            return True
        except Exception as e:
            print(f"❌ Failed to send transaction: {e}")
            return False
    
    def run(self):
        """Main loop: generate and send transactions"""
        if not self.connect():
            return
        
        print(f"\n📊 Generating {self.transactions} transactions (delay: {self.delay}s between each)")
        print(f"💾 Sending to socket: {self.host}:{self.port}")
        print(f"⏰ Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        
        try:
            for i in range(1, self.transactions + 1):
                transaction = self.generate_transaction(i)
                
                if self.send_transaction(transaction):
                    # Print progress every 50 transactions
                    if i % 50 == 0:
                        print(f"  ✓ Sent {i}/{self.transactions} transactions")
                        status = "✅ SUCCESS" if transaction['isSuccessful'] else "❌ FAILED"
                        print(f"    Last: {transaction['userId']} - ${transaction['amount']} {status}")
                else:
                    print(f"❌ Stopped at transaction {i}")
                    break
                
                time.sleep(self.delay)
            
            print(f"\n✅ Completed! Sent all {self.transactions} transactions")
            print(f"⏰ Finished at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            
        except KeyboardInterrupt:
            print(f"\n⏸️  Interrupted by user after {i-1} transactions")
        except Exception as e:
            print(f"\n❌ Error during generation: {e}")
        finally:
            self.sock.close()
            print("Socket connection closed")

def main():
    parser = argparse.ArgumentParser(
        description='Generate synthetic transactions for Flink fraud detection system'
    )
    parser.add_argument('--host', default='localhost', help='Socket host (default: localhost)')
    parser.add_argument('--port', type=int, default=9999, help='Socket port (default: 9999)')
    parser.add_argument('--delay', type=float, default=0.5, help='Delay between transactions in seconds (default: 0.5)')
    parser.add_argument('--count', type=int, default=1000, help='Number of transactions to generate (default: 1000)')
    
    args = parser.parse_args()
    
    generator = TransactionGenerator(
        host=args.host,
        port=args.port,
        delay=args.delay,
        transactions=args.count
    )
    
    generator.run()

if __name__ == '__main__':
    main()
