# CreditFlow - Banking Credit Approval System

Full-stack credit card approval system for banks using 7 DSA algorithms, CIBIL-weighted scoring, and Basel III risk metrics. Processes 1,000 customers under ₹50L budget.

---

## 🏦 What This Project Does

Simulates real banking credit card approval for **1,000 customers** using rules from:
- **Indian banks**: HDFC, ICICI, SBI (CIBIL scores 300-900, tier-based limits)
- **International banks**: JP Morgan, Citi, Deutsche Bank, Barclays (Basel III PD/LGD/EL/RWA)

When a bank receives thousands of credit card applications daily, it must answer 3 questions:
1. **Should we approve this customer?** (credit score + risk flags)
2. **What limit should we give?** (tier-based multiplier on income)
3. **What is our risk exposure?** (Basel III PD/LGD/EL/RWA)

CreditFlow answers all 3 questions automatically.

---

## 🔑 Key Features

### ✅ CIBIL Credit Scoring (300-900)
Calculates score from 5 factors with real Indian weights:
- **Payment History**: 35% (most important)
- **Credit Utilization**: 30%
- **Debt-to-Income**: 20%
- **Spend Stability**: 10%
- **Salary Consistency**: 5%

### ✅ Tier-Based Approval

| Tier | Score Range | Interest | Credit Limit |
|------|-------------|----------|--------------|
| **A** | 750-900 | 18% | 3× monthly income |
| **B** | 600-749 | 24% | 1.5× income |
| **C** | 500-599 | 36% | 0.5× income |
| **Rejected** | < 500 | — | — |

### ✅ Capital Allocation Optimization
- Bank has **₹50L total budget** for credit limits
- Uses **0/1 Knapsack DP** to maximize revenue while staying under budget
- Allocates optimally to best customers (Tier A > Tier B > Tier C)

### ✅ Fraud Detection
- Detects **fraud clusters** (customers with linked accounts, same phone/address)
- Uses **Union-Find algorithm** for O(1) connectivity checks
- 1,000x faster than DFS/BFS for repeated queries

### ✅ Basel III Risk Metrics (International Banks)
- **PD** (Probability of Default): Chance of default in 1 year
- **LGD** (Loss Given Default): Loss if default occurs
- **EL** (Expected Loss): PD × LGD × EAD
- **RWA** (Risk Weighted Assets): EL × 0.75
- Required for JP Morgan, Citi, Deutsche Bank, Barclays

### ✅ Visual Reports
- **Dashboard** (`index.html`): All 1,000 customers with scores, tiers, limits
- **Individual Report** (`credit-analysis.html`): Detailed breakdown with Chart.js radar charts
- Shows score components, transaction analysis, peer comparison, recommendations

---

## 💻 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17 (immutable classes, streams, records) |
| **Frontend** | HTML5, CSS3, Chart.js (radar + bar charts) |
| **DSA** | Union-Find, Segment Tree, Graph DFS/BFS, Min-Heap, Knapsack DP, TreeMap, Deque |

---

## 📊 DSA Algorithms Used

| Algorithm | Use Case | Time Complexity |
|-----------|----------|-----------------|
| **Union-Find** | Fraud cluster detection | O(α(n)) ≈ O(1) |
| **Segment Tree** | Range sum/max queries on credit limits | O(log n) |
| **Graph DFS** | 3-color cycle detection in borrower network | O(V + E) |
| **Graph BFS** | Cascade default simulation | O(V + E) |
| **Min-Heap** | Top-k riskiest customers | O(log k) |
| **Knapsack DP** | Capital allocation under ₹50L budget | O(n × budget) |
| **TreeMap** | Score indexing (quick tier lookups) | O(log n) |

---

## 🏦 Real Banking Mapping

**Indian Banks (HDFC, ICICI, SBI):**
- CIBIL scores 300-900 (actual Indian credit bureau)
- Tier-based limits (3× / 1.5× / 0.5× income)
- Interest rates 18%/24%/36% (real credit card rates)

**International Banks (JP Morgan, Citi, Deutsche, Barclays):**
- Basel III compliance (PD/LGD/EL/RWA)
- Capital adequacy 8% per Basel III
- Risk-weighted pricing (Tier A = lower PD, lower rate)

**NBFCs (Bajaj Finance, IIFL):**
- Same approval logic for personal loans
- Fraud detection for loan applicants
- Capital optimization for limited budget

---

## 📈 Performance

- **1,000 customers**: ~200ms total processing time
- **O(n) pipeline**: Scoring + approval for all customers
- **O(log n) queries**: Segment Tree for range lookups
- **O(1) connectivity**: Union-Find for fraud detection

---

## 🚦 How to Run

```bash
cd CreditFlow
javac -d out src/com/creditflow/*.java
java -cp out com.creditflow.runner.Main
```

**Output:**
- `batch-report.txt`: Analysis of all 1,000 customers
- `frontend/index.html`: Open in browser for dashboard
- `frontend/credit-analysis.html?id=C00042`: Individual report

---


