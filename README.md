# CreditFlow

CreditFlow is a Java-based credit risk and capital allocation simulation
designed to model how fintech or banking systems evaluate customers,
score credit risk, and allocate capital under constraints.

## Tech Stack
- Java
- Data Structures & Algorithms

## Key Concepts Used
- Graph traversal (guarantor network)
- Union-Find (connected borrower groups)
- Segment Tree (risk aggregation)
- Scoring and ranking pipeline
- Rule-based approval engine

## How to Run
```bash
javac -d out $(find src -name "*.java")
java -cp out com.creditflow.runner.Main
