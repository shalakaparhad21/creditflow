# CreditFlow — Banking DSA Pipeline

> A Java-based credit scoring and capital allocation system that simulates real-world banking workflows using core Data Structures & Algorithms, Basel III credit risk metrics, and an interactive frontend dashboard.

---

## 📌 Project Overview

CreditFlow processes **1,000 synthetic customer profiles** through a multi-stage credit evaluation pipeline — from feature extraction and CIBIL-style scoring to risk network analysis and capital allocation — all within a ₹50 lakh budget constraint.

Built as a Java DSA project with a BFSI (Banking, Financial Services & Insurance) domain focus, it demonstrates how algorithms like Segment Trees, Union-Find, Graph BFS/DFS, and 0/1 Knapsack DP are used in real credit risk systems.

---

## 🏗️ System Architecture
Customer Data (1000 profiles)
        │
        ▼
┌─────────────────────┐
│  Feature Extraction  │  ← Sliding Window (Deque) on transaction history
│  (TransactionStream) │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│   Credit Scoring     │  ← Weighted formula (CIBIL/FICO methodology)
│   (CreditScorer)     │     Payment 35% | Utilization 30% | DTI 20%
└────────┬────────────┘     Stability 10% | Salary 5%
         │
         ▼
┌─────────────────────┐
│   Approval Engine    │  ← Decision Tree: Hard reject rules → Tier A/B/C
│   (ApprovalEngine)   │     Basel III: PD, LGD, EL, RWA calculation
└────────┬────────────┘
         │
         ▼
┌──────────────────────────────────────────────────┐
│              Risk Analysis Layer                  │
│  ┌─────────────────┐  ┌──────────────────────┐   │
│  │  BorrowerGraph   │  │    CustomerIndex      │   │
│  │  BFS + DFS       │  │    TreeMap (Red-Black)│   │
│  │  Union-Find      │  │    SegmentTree        │   │
│  └─────────────────┘  └──────────────────────┘   │
└────────┬─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────┐
│  Capital Allocation  │  ← 0/1 Knapsack DP within ₹50L budget
│  (CapitalAllocator)  │
└────────┬────────────┘
         │
         ▼
  Console Report + Frontend Dashboard (HTML/CSS/JS)


---

## ✅ Features

### Core Pipeline
- **Batch Mode** — Score all 1,000 customers, generate aggregate report
- **Single Customer Mode** — Enter any Customer ID (e.g. `C00042`) for full credit analysis
- **Data Generation** — Seeded random customer profiles with realistic Indian banking parameters

### DSA Algorithms Implemented
| Algorithm | Class | Banking Use Case |
|---|---|---|
| **Segment Tree** | `SegmentTree.java` | O(log n) range sum queries on credit limits by score band |
| **TreeMap (Red-Black Tree)** | `CustomerIndex.java` | Sorted score indexing, percentile rank, range queries |
| **Graph + BFS** | `BorrowerGraph.java` | Cascade simulation — who gets impacted if one borrower defaults |
| **Graph + DFS (3-color)** | `BorrowerGraph.java` | Circular guarantor dependency detection |
| **Union-Find (DSU)** | `UnionFind.java` | Borrower risk cluster grouping (path compression + union by rank) |
| **0/1 Knapsack DP** | `CapitalAllocator.java` | Maximize expected revenue within capital budget |
| **Sliding Window (Deque)** | `TransactionStream.java` | Behavioral feature extraction from transaction history |
| **Min-Heap (PriorityQueue)** | `ScoringPipeline.java` | Top-N riskiest approved customers |
| **Decision Tree** | `ApprovalEngine.java` | Hard reject rules + tier classification |

### Basel III Credit Risk Metrics
Each approved customer gets:
- **PD** (Probability of Default) — based on credit score
- **LGD** (Loss Given Default) — based on credit tier
- **EAD** (Exposure at Default) — assigned credit limit
- **EL** (Expected Loss) = PD × LGD × EAD
- **RWA** (Risk Weighted Assets) = EL × 0.75

These are the same metrics used by JP Morgan, Citi, Deutsche Bank, and Barclays under Basel III regulation.

### Credit Scoring Formula (CIBIL/FICO-inspired)
| Component | Weight |
|---|---|
| Payment History | 35% |
| Credit Utilization | 30% |
| Debt-to-Income Ratio | 20% |
| Spend Stability | 10% |
| Salary Consistency | 5% |

Output: Integer score in range **[300, 900]**

### Approval & Tiering
| Tier | Score Range | Interest Rate | Max Limit |
|---|---|---|---|
| A | 750 – 900 | 18% p.a. | ₹10,00,000 |
| B | 600 – 749 | 24% p.a. | ₹5,00,000 |
| C | 500 – 599 | 36% p.a. | ₹2,00,000 |
| Rejected | < 500 | — | — |

Hard reject rules: Score < 500 | Missed payments > 3 | DTI > 55% | Utilization > 90%

### Frontend Dashboard
Interactive HTML/CSS/JS dashboard with:
- **Batch Report** — Scoring summary, segment tree range queries, risk network stats, capital allocation
- **Single Customer** — Full credit analysis with Basel III breakdown
- **Credit Analysis Page** — Visual score breakdown per component

---

---

## 🔧 Technologies Used

- **Language**: Java 17
- **Data Structures**: TreeMap, PriorityQueue, Deque, HashMap, ArrayList
- **Algorithms**: Segment Tree, Union-Find, BFS, DFS, 0/1 Knapsack DP, Sliding Window
- **Domain**: BFSI — Credit Risk, Basel III, CIBIL Scoring, Capital Allocation
- **Frontend**: Vanilla HTML5, CSS3, JavaScript (no frameworks)

---

## 🎯 Learning Outcomes

- Applied DSA in a real-world BFSI domain context (not just textbook problems)
- Implemented Basel III regulatory framework (PD/LGD/EL/RWA) used by global banks
- Designed a multi-layer decision system (feature extraction → scoring → approval → allocation)
- Built a modular, package-structured Java codebase with separation of concerns

---




  
