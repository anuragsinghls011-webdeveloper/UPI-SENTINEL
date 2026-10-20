const express = require('express');
const cors = require('cors');
const http = require('http');

const app = express();
const PORT = process.env.PORT || 5000;
const ML_SERVICE_URL = process.env.ML_SERVICE_URL || 'http://127.0.0.1:8000';

app.use(cors());
app.use(express.json());

// In-memory Audit & Transaction Log (Can be backed by MongoDB / Postgres)
const AUDIT_STORE = [];
const FEEDBACK_STORE = [];

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'HEALTHY',
    service: 'upi-sentinel-node-backend',
    timestamp: new Date().toISOString()
  });
});

// Orchestration Endpoint: Forward transaction to Python ML Service with timeout and fallback
app.post('/api/transactions/analyze', async (req, res) => {
  const transaction = req.body.transaction;

  if (!transaction || !transaction.transaction_id || transaction.amount === undefined) {
    return res.status(400).json({
      error: 'INVALID_REQUEST',
      message: 'transaction object with transaction_id and amount is required'
    });
  }

  const startTime = Date.now();

  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 1500); // 1.5s timeout

    const mlResponse = await fetch(`${ML_SERVICE_URL}/api/v1/risk/analyze`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ transaction }),
      signal: controller.signal
    });

    clearTimeout(timeoutId);

    if (!mlResponse.ok) {
      throw new Error(`ML service returned status ${mlResponse.status}`);
    }

    const analysis = await mlResponse.json();
    const processingTimeMs = Date.now() - startTime;

    // Record Audit entry
    const auditRecord = {
      audit_id: `AUDIT_${Date.now()}_${Math.random().toString(36).substr(2, 6)}`,
      transaction_id: transaction.transaction_id,
      amount: transaction.amount,
      risk_score: analysis.risk_score,
      risk_level: analysis.risk_level,
      fraud_probability: analysis.fraud_probability,
      reasons_count: analysis.reasons ? analysis.reasons.length : 0,
      model_version: analysis.model_version,
      processing_time_ms: processingTimeMs,
      timestamp: new Date().toISOString()
    };
    AUDIT_STORE.unshift(auditRecord);

    return res.json({
      ...analysis,
      orchestration_latency_ms: processingTimeMs
    });
  } catch (error) {
    console.error('ML Service Error or Timeout. Engaging Graceful Fallback:', error.message);

    // Rule-based fallback when ML is temporarily unreachable
    const fallbackRisk = (transaction.amount > 50000 || transaction.suspicious_domain) ? 75 : 35;
    const fallbackLevel = fallbackRisk > 60 ? 'HIGH' : 'MEDIUM';

    return res.status(200).json({
      transaction_id: transaction.transaction_id,
      risk_score: fallbackRisk,
      risk_level: fallbackLevel,
      fraud_probability: fallbackRisk / 100.0,
      anomaly: { score: 0.5, level: 'MEDIUM' },
      reasons: [
        {
          type: 'fallback',
          severity: 'medium',
          message: 'Real-time ML service is currently in degraded fallback mode; preliminary heuristic check performed.',
          evidence: { degraded_mode: true }
        }
      ],
      recommendation: {
        action: 'VERIFY_OR_STOP',
        message: 'ML analysis in degraded mode. Verify the recipient details carefully before proceeding.',
        advisory: 'Do not share UPI PIN or click untrusted links.'
      },
      model_version: 'fallback-heuristics-v1',
      processing_time_ms: Date.now() - startTime
    });
  }
});

// Audit Log endpoint
app.get('/api/transactions/audit', (req, res) => {
  res.json({
    total_audits: AUDIT_STORE.length,
    audits: AUDIT_STORE.slice(0, 50)
  });
});

// User Feedback Endpoint
app.post('/api/transactions/feedback', async (req, res) => {
  const { transaction_id, user_action, user_notes } = req.body;
  if (!transaction_id || !user_action) {
    return res.status(400).json({ error: 'Missing transaction_id or user_action' });
  }

  const record = {
    transaction_id,
    user_action,
    user_notes,
    timestamp: new Date().toISOString()
  };
  FEEDBACK_STORE.push(record);

  // Asynchronously notify ML service of feedback
  fetch(`${ML_SERVICE_URL}/api/v1/feedback`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(record)
  }).catch(e => console.warn('Could not forward feedback to ML service:', e.message));

  res.json({ status: 'SUCCESS', message: 'User action recorded for model feedback loop.' });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`UPI Sentinel Node.js Orchestrator running on port ${PORT}`);
  console.log(`Connected ML Service URL: ${ML_SERVICE_URL}`);
});
