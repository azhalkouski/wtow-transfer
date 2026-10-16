#!/usr/bin/env python3

import concurrent.futures
import requests
from datetime import date
import time
import os
from dotenv import load_dotenv

# load .env from current directory
load_dotenv()

BASE_URL = "http://localhost:8080"

BILL_EMAIL = os.getenv("BILL_EMAIL")
BILL_PASSWORD = os.getenv("BILL_PASSWORD")
BILL_ACC = os.getenv("BILL_ACC")

JACK_EMAIL = os.getenv("JACK_EMAIL")
JACK_PASSWORD = os.getenv("JACK_PASSWORD")
JACK_ACC = os.getenv("JACK_ACC")

ALICE_EMAIL = os.getenv("ALICE_EMAIL")
ALICE_PASSWORD = os.getenv("ALICE_PASSWORD")
ALICE_ACC = os.getenv("ALICE_ACC")

def login(email: str, password: str) -> requests.Session:
    session = requests.Session()
    resp = session.post(f"{BASE_URL}/auth/login", json={"email": email, "password": password})
    resp.raise_for_status()
    print(f"Logged in as {email}, userId: {resp.json()['userId']}")
    return session

def transfer(session: requests.Session, source: str, destination: str, amount: str) -> dict:
    resp = session.post(f"{BASE_URL}/transfer", json={
     "source": source,
     "destination": destination,
     "amount": amount,
     "recipientName": "Happy receiver",
     "description": "Money on stuff",
     "scheduledFor": str(date.today()),
     "immediate": True
    })
    return {"status": resp.status_code, "body": resp.json() if resp.ok else resp.text}

def run_transfers(session: requests.Session, source: str, destination: str, count: int, label: str):
    results = {"success": 0, "failed": 0, "errors": []}

    with concurrent.futures.ThreadPoolExecutor(max_workers=50) as executor:
        futures = [
            executor.submit(transfer, session, source, destination, "100.00")
            for _ in range(count)
        ]
        for f in concurrent.futures.as_completed(futures):
            try:
                result = f.result()
                if result["status"] == 200:
                    results["success"] += 1
                else:
                    results["failed"] += 1
                    results["errors"].append(result)
            except Exception as e:
                results["failed"] += 1
                results["errors"].append(str(e))

    print(f"{label}: {results['success']} success, {results['failed']} failed")
    if results["errors"][:5]:
        print(f"    Sample errors: {results['errors'][:5]}")

    return results

if __name__ == "__main__":
    bill_session = login(BILL_EMAIL, BILL_PASSWORD)
    jack_session = login(JACK_EMAIL, JACK_PASSWORD)
    alice_session = login(ALICE_EMAIL, ALICE_PASSWORD)

    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as executor:
        bill_jack_future = executor.submit(run_transfers, bill_session, BILL_ACC, JACK_ACC, 1000, "Bill -> Jack")
        jack_bill_future = executor.submit(run_transfers, jack_session, JACK_ACC, BILL_ACC, 1000, "Jack -> Bill")
        alice_bill_future = executor.submit(run_transfers, alice_session, ALICE_ACC, BILL_ACC, 500, "Alice -> Bill")
        alice_jack_future = executor.submit(run_transfers, alice_session, ALICE_ACC, JACK_ACC, 500, "Alice -> Jack")

        bill_jack_future.result()
        jack_bill_future.result()
        alice_bill_future.result()
        alice_jack_future.result()

        print("Execution completed")
