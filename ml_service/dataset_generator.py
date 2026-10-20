"""
Synthetic UPI Transaction Dataset Generator.
Generates reproducible, realistic legitimate and fraud scenarios.
Does NOT generate labels from the exact scoring formula, allowing the ML models to learn realistic feature interactions.
"""

import os
import json
import numpy as np
import pandas as pd
from schema import ALL_MODEL_FEATURES

def generate_synthetic_transactions(n_samples: int = 12000, random_seed: int = 42) -> pd.DataFrame:
    np.random.seed(random_seed)
    
    # 88% Legitimate, 12% Fraud
    n_fraud = int(n_samples * 0.12)
    n_legit = n_samples - n_fraud

    records = []

    # Normalized 24-hour distribution
    hour_probs = np.array([
        0.005, 0.005, 0.005, 0.005, 0.010, 0.020, # 0-5 AM
        0.040, 0.060, 0.080, 0.090, 0.080, 0.070, # 6-11 AM
        0.080, 0.070, 0.060, 0.060, 0.070, 0.080, # 12-17 PM
        0.080, 0.060, 0.030, 0.015, 0.010, 0.005  # 18-23 PM
    ])
    hour_probs = hour_probs / hour_probs.sum()

    # --- Scenario 1: Legitimate Everyday Retail & Peer Transactions ---
    for i in range(n_legit):
        tx_type = np.random.choice(["P2M", "P2P", "QR_DYNAMIC", "QR_STATIC"], p=[0.45, 0.35, 0.15, 0.05])
        user_avg = np.random.uniform(300, 4000)
        
        # High value legitimate purchases (e.g. flight, tech, luxury retail with verified invoices)
        is_high_value_purchase = (np.random.rand() < 0.05)
        if is_high_value_purchase:
            amount = np.random.uniform(15000, 75000)
            invoice_present = True
            invoice_amount = amount
            invoice_match = True
            merchant_verified = True
            merchant_rep = np.random.uniform(85, 100)
        else:
            amount = float(np.clip(np.random.normal(user_avg, user_avg * 0.4), 10, 15000))
            invoice_present = np.random.choice([True, False], p=[0.6, 0.4]) if tx_type in ["P2M", "QR_DYNAMIC"] else False
            invoice_amount = amount if invoice_present else 0.0
            invoice_match = True
            merchant_verified = np.random.choice([True, False], p=[0.88, 0.12]) if tx_type != "P2P" else True
            merchant_rep = np.random.uniform(70, 98)

        hour = int(np.random.choice(range(24), p=hour_probs))
        unusual_time = bool(hour in [1, 2, 3, 4])
        
        user_dev = round(amount / (user_avg + 1e-5), 2)
        dev_score = float(np.clip(user_dev / 5.0 * 0.25 + np.random.normal(0.04, 0.02), 0.0, 0.40))
        
        rec_known = bool(np.random.choice([True, False], p=[0.82, 0.18]))
        rec_tx_count = int(np.random.randint(1, 40)) if rec_known else int(np.random.randint(0, 2))
        rec_risk = float(np.random.uniform(0, 15))

        records.append({
            "transaction_id": f"TXN_LEGIT_{i+1:06d}",
            "amount": round(amount, 2),
            "currency": "INR",
            "transaction_type": tx_type,
            "merchant_verified": merchant_verified,
            "merchant_reputation_score": round(merchant_rep, 1),
            "merchant_mismatch": False,
            "recipient_known": rec_known,
            "recipient_risk_score": round(rec_risk, 1),
            "recipient_transaction_count": rec_tx_count,
            "domain_reputation_score": round(np.random.uniform(75, 99), 1),
            "suspicious_domain": False,
            "shortened_url": bool(np.random.choice([False, True], p=[0.97, 0.03])),
            "invoice_present": invoice_present,
            "invoice_amount": round(invoice_amount, 2),
            "invoice_match": invoice_match,
            "hour_of_day": hour,
            "unusual_time": unusual_time,
            "user_avg_amount": round(user_avg, 2),
            "user_amount_deviation": user_dev,
            "user_transaction_frequency": round(np.random.uniform(1.0, 6.0), 1),
            "behavioral_deviation_score": round(dev_score, 3),
            "new_device": bool(np.random.choice([False, True], p=[0.95, 0.05])),
            "unusual_location": bool(np.random.choice([False, True], p=[0.96, 0.04])),
            "location_deviation_score": round(np.random.uniform(0.0, 0.25), 2),
            "historical_transaction_count": int(np.random.randint(15, 250)),
            "historical_failed_count": int(np.random.choice([0, 1, 2], p=[0.85, 0.12, 0.03])),
            "historical_flagged_count": 0,
            "fraud_label": 0,
            "scenario": "LEGITIMATE_NORMAL" if not is_high_value_purchase else "LEGITIMATE_HIGH_VALUE"
        })

    # --- Fraud Scenarios ---
    fraud_scenarios = ["PHISHING_DOMAIN", "MERCHANT_TAMPERING", "ACCOUNT_TAKEOVER_DRAIN", "INVOICE_AMOUNT_MISMATCH"]
    
    for j in range(n_fraud):
        scenario = np.random.choice(fraud_scenarios, p=[0.30, 0.25, 0.25, 0.20])
        user_avg = np.random.uniform(500, 3000)

        if scenario == "PHISHING_DOMAIN":
            amount = np.random.uniform(1999, 25000)
            tx_type = "INTENT"
            suspicious_domain = True
            shortened_url = bool(np.random.choice([True, False], p=[0.75, 0.25]))
            domain_rep = np.random.uniform(5, 28)
            merch_verified = False
            merch_rep = np.random.uniform(10, 35)
            merch_mismatch = bool(np.random.choice([True, False], p=[0.8, 0.2]))
            rec_known = False
            rec_risk = np.random.uniform(65, 95)
            hour = np.random.randint(8, 23)
            unusual_time = False
            new_device = False
            inv_present = bool(np.random.choice([True, False], p=[0.3, 0.7]))
            inv_match = False if inv_present else True
            inv_amount = amount * 0.1 if inv_present else 0.0
            dev_score = np.random.uniform(0.55, 0.88)

        elif scenario == "MERCHANT_TAMPERING":
            amount = np.random.uniform(500, 9500)
            tx_type = "QR_STATIC"
            suspicious_domain = bool(np.random.choice([True, False], p=[0.4, 0.6]))
            shortened_url = False
            domain_rep = np.random.uniform(20, 50)
            merch_verified = False
            merch_rep = np.random.uniform(15, 42)
            merch_mismatch = True
            rec_known = False
            rec_risk = np.random.uniform(50, 85)
            hour = np.random.randint(10, 21)
            unusual_time = False
            new_device = False
            inv_present = False
            inv_match = False
            inv_amount = 0.0
            dev_score = np.random.uniform(0.40, 0.75)

        elif scenario == "ACCOUNT_TAKEOVER_DRAIN":
            amount = np.random.uniform(25000, 99000)
            tx_type = "P2P"
            suspicious_domain = False
            shortened_url = False
            domain_rep = np.random.uniform(50, 75)
            merch_verified = False
            merch_rep = 50.0
            merch_mismatch = False
            rec_known = False
            rec_risk = np.random.uniform(70, 99)
            hour = int(np.random.choice([1, 2, 3, 4]))
            unusual_time = True
            new_device = True
            inv_present = False
            inv_match = True
            inv_amount = 0.0
            dev_score = np.random.uniform(0.80, 0.98)

        else: # INVOICE_AMOUNT_MISMATCH
            stated = np.random.uniform(200, 1500)
            amount = stated * np.random.uniform(5, 20)
            tx_type = "QR_DYNAMIC"
            suspicious_domain = bool(np.random.choice([True, False], p=[0.5, 0.5]))
            shortened_url = True
            domain_rep = np.random.uniform(15, 40)
            merch_verified = False
            merch_rep = np.random.uniform(20, 45)
            merch_mismatch = True
            rec_known = False
            rec_risk = np.random.uniform(55, 90)
            hour = np.random.randint(9, 22)
            unusual_time = False
            new_device = False
            inv_present = True
            inv_match = False
            inv_amount = round(stated, 2)
            dev_score = np.random.uniform(0.65, 0.90)

        user_dev = round(amount / (user_avg + 1e-5), 2)
        rec_tx_count = 0
        failed_count = int(np.random.choice([0, 1, 2, 3], p=[0.4, 0.3, 0.2, 0.1]))
        flagged_count = int(np.random.choice([0, 1, 2], p=[0.6, 0.3, 0.1]))

        records.append({
            "transaction_id": f"TXN_FRAUD_{j+1:06d}",
            "amount": round(amount, 2),
            "currency": "INR",
            "transaction_type": tx_type,
            "merchant_verified": merch_verified,
            "merchant_reputation_score": round(merch_rep, 1),
            "merchant_mismatch": merch_mismatch,
            "recipient_known": rec_known,
            "recipient_risk_score": round(rec_risk, 1),
            "recipient_transaction_count": rec_tx_count,
            "domain_reputation_score": round(domain_rep, 1),
            "suspicious_domain": suspicious_domain,
            "shortened_url": shortened_url,
            "invoice_present": inv_present,
            "invoice_amount": round(inv_amount, 2),
            "invoice_match": inv_match,
            "hour_of_day": hour,
            "unusual_time": unusual_time,
            "user_avg_amount": round(user_avg, 2),
            "user_amount_deviation": user_dev,
            "user_transaction_frequency": round(np.random.uniform(1.0, 5.0), 1),
            "behavioral_deviation_score": round(dev_score, 3),
            "new_device": new_device,
            "unusual_location": bool(np.random.choice([False, True], p=[0.7, 0.3])),
            "location_deviation_score": round(np.random.uniform(0.2, 0.9), 2),
            "historical_transaction_count": int(np.random.randint(5, 120)),
            "historical_failed_count": failed_count,
            "historical_flagged_count": flagged_count,
            "fraud_label": 1,
            "scenario": scenario
        })

    df = pd.DataFrame(records)
    df = df.sample(frac=1.0, random_state=random_seed).reset_index(drop=True)
    return df

if __name__ == "__main__":
    os.makedirs("ml_service/data/v1", exist_ok=True)
    df = generate_synthetic_transactions(12000, 42)
    output_path = "ml_service/data/v1/synthetic_transactions.csv"
    df.to_csv(output_path, index=False)
    print(f"Generated {len(df)} synthetic transactions to {output_path}")
    print(f"Legitimate: {(df['fraud_label'] == 0).sum()} ({(df['fraud_label'] == 0).mean()*100:.1f}%)")
    print(f"Fraud: {(df['fraud_label'] == 1).sum()} ({(df['fraud_label'] == 1).mean()*100:.1f}%)")
    print("\nFraud Scenarios Breakdown:")
    print(df[df['fraud_label'] == 1]['scenario'].value_counts())
