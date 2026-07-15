# Secure Evidence Query Engine (SEQE)

A Java-based Digital Evidence Management System (DEMS) built as a semester project for a **Data Structures** course. SEQE simulates how a forensics/investigation unit tracks cases, evidence, chain-of-custody transfers, users, and audit logs — with every core feature backed by a hand-written data structure instead of built-in Java collections.

The project has both a **console (CLI) interface** (`app.Test`) and a **JavaFX GUI** (`frontend.FrontendApp`) sharing the same backend logic.

## Features

- **Role-based access** — Admin, Investigator, and Analyst roles with separate dashboards/menus.
- **User management** — create, update, delete, and authenticate users (Admin only).
- **Case management** — add, search, update status, delete, and view cases sorted by ID.
- **Evidence management** — add evidence linked to a case, track priority/status, search by ID or case, view forward/reverse.
- **Chain-of-custody transfer queue** — investigators submit evidence for analysis; analysts process it in FIFO order.
- **Audit trail** — every significant action (logins, CRUD operations, transfers) is logged with a timestamp and can be reviewed in full or as the most recent entries.
- **CSV-backed persistence** — users, cases, evidence, custody queue, and audit logs are loaded from and saved to CSV files under `backend/data/`.

## Data Structures Used

| Module      | Data Structure                          | Purpose                                      |
|-------------|------------------------------------------|-----------------------------------------------|
| `users`     | Hash Table (`HashTableService`)          | O(1) user lookup/authentication by username    |
| `cases`     | Binary Search Tree (`CaseBST`)           | Store cases ordered by case ID, in-order traversal for sorted listing |
| `evidence`  | Doubly Linked List (`EvidenceList`)       | Insert, delete, and traverse evidence forward/reverse |
| `custody`   | Queue (`CustodyQueue`)                   | FIFO chain-of-custody transfer between investigators and analysts |
| `audit`     | Stack (`AuditStack`)                     | Most-recent-first audit log of system activity |

## Project Structure

```
CLI-based DEMS/
├── backend/
│   ├── src/
│   │   ├── app/         # Console entry point (Test.java)
│   │   ├── users/       # User model + hash table service
│   │   ├── cases/       # Case model + BST
│   │   ├── evidence/    # Evidence model + linked list
│   │   ├── custody/     # Custody queue (chain of custody)
│   │   ├── audit/       # Audit log stack
│   │   └── frontend/    # JavaFX controllers + app entry (FrontendApp.java)
│   ├── out/             # Compiled classes (build output)
│   └── data/            # CSV data files (users, cases, evidence, queue, history)
└── frontend/
    ├── fxml/            # JavaFX scene layouts (Login, Admin/Investigator/Analyst dashboards)
    └── styles/          # CSS styling for the JavaFX UI
```

## Prerequisites

- **JDK 17+**
- **JavaFX SDK** (for the GUI) — e.g. `javafx-sdk-26.0.1`, available from [openjfx.io](https://openjfx.io/)

## Running the Console App

```bash
cd backend
javac -d out src/app/*.java src/users/*.java src/cases/*.java src/evidence/*.java src/custody/*.java src/audit/*.java
java -cp out app.Test
```

## Running the JavaFX GUI

```bash
cd backend
javac --module-path "<path-to-javafx-sdk>/lib" --add-modules javafx.controls,javafx.fxml -d out src/frontend/*.java src/users/*.java src/cases/*.java src/evidence/*.java src/custody/*.java src/audit/*.java
java --module-path "<path-to-javafx-sdk>/lib" --add-modules javafx.controls,javafx.fxml -cp out frontend.FrontendApp
```

> A ready-to-use VS Code launch configuration is provided in `.vscode/launch.json` — update the `javafx-sdk` path to match your local installation.

## Sample Roles

| Role         | Capabilities                                                      |
|--------------|---------------------------------------------------------------------|
| Admin        | Manage users, manage cases, view full audit trail                  |
| Investigator | View cases, add/manage evidence, send evidence to analysis queue   |
| Analyst      | Process the transfer queue, view/update evidence status            |

## Notes

- This project prioritizes demonstrating custom data structure implementations (BST, Queue, Stack, Linked List, Hash Table) over using Java's built-in `java.util` collections for core domain logic.
- Data files in `backend/data/` are used as lightweight persistent storage in place of a database.
