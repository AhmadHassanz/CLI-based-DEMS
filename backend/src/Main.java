import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    // ─────────────────────────────────────────────
    //  AUDIT STACK  (linked-list based, no java.util)
    // ─────────────────────────────────────────────
    static class LogEntry {
        String time, username, action;
        LogEntry next;
        LogEntry(String t, String u, String a) { time = t; username = u; action = a; }
    }

    static class AuditStack {
        private LogEntry top = null;

        void push(String username, String action) {
            LogEntry e = new LogEntry(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), username, action);
            e.next = top;
            top = e;
        }

        void display() {
            if (top == null) { System.out.println("  Audit log is empty."); return; }
            LogEntry curr = top;
            int i = 1;
            while (curr != null) {
                System.out.printf("  %2d. [%s] %-15s %s%n", i++, curr.time, curr.username, curr.action);
                curr = curr.next;
            }
        }

        void clear() { top = null; }
        boolean isEmpty() { return top == null; }
    }

    // ─────────────────────────────────────────────
    //  CUSTODY QUEUE  (linked-list based, no java.util)
    // ─────────────────────────────────────────────
    static class QueueNode {
        String evidenceId, caseId, submittedBy;
        QueueNode next;
        QueueNode(String eid, String cid, String by) { evidenceId = eid; caseId = cid; submittedBy = by; }
    }

    static class CustodyQueue {
        private QueueNode front = null, rear = null;
        private int size = 0;

        void enqueue(String eid, String cid, String by) {
            QueueNode node = new QueueNode(eid, cid, by);
            if (rear == null) { front = rear = node; }
            else { rear.next = node; rear = node; }
            size++;
        }

        // returns the dequeued node, or null if empty
        QueueNode dequeue() {
            if (front == null) return null;
            QueueNode removed = front;
            front = front.next;
            if (front == null) rear = null;
            size--;
            return removed;
        }

        void display() {
            if (front == null) { System.out.println("  Transfer queue is empty."); return; }
            QueueNode curr = front;
            int i = 1;
            while (curr != null) {
                System.out.printf("  %2d. Evidence: %-10s Case: %-10s Submitted By: %s%n",
                        i++, curr.evidenceId, curr.caseId, curr.submittedBy);
                curr = curr.next;
            }
        }

        boolean isEmpty() { return front == null; }
        int size()        { return size; }
    }

    // ─────────────────────────────────────────────
    //  SHARED STATE
    // ─────────────────────────────────────────────
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static HashTableService userService  = new HashTableService();
    private static EvidenceList     evidenceList = new EvidenceList();
    private static CaseBST          caseBST      = new CaseBST();
    private static AuditStack       auditLog     = new AuditStack();
    private static CustodyQueue     custodyQueue = new CustodyQueue();

    // ─────────────────────────────────────────────
    //  ENTRY POINT
    // ─────────────────────────────────────────────
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            printHeader("DIGITAL EVIDENCE MANAGEMENT SYSTEM");
            System.out.println("  1. Login");
            System.out.println("  2. Exit");
            printLine();
            System.out.print("  Choice: ");

            int choice = readInt(sc);
            if (choice == 2) break;
            if (choice != 1) { System.out.println("  Invalid choice."); continue; }

            // ── role selection ──
            printHeader("SELECT ROLE");
            System.out.println("  1. Admin");
            System.out.println("  2. Investigator");
            System.out.println("  3. Analyst");
            System.out.println("  0. Back");
            printLine();
            System.out.print("  Choice: ");

            int roleChoice = readInt(sc);
            if (roleChoice == 0) continue;

            String selectedRole = switch (roleChoice) {
                case 1 -> "Admin";
                case 2 -> "Investigator";
                case 3 -> "Analyst";
                default -> "";
            };

            if (selectedRole.isEmpty()) { System.out.println("  Invalid role."); continue; }

            System.out.print("  Username: ");
            String username = sc.nextLine().trim();
            System.out.print("  Password: ");
            String password = sc.nextLine().trim();

            User user = userService.login(username, password);

            if (user == null) {
                System.out.println("  Login failed. Invalid credentials.");
                auditLog.push(username, "Failed login attempt");
                continue;
            }
            if (!user.role.equalsIgnoreCase(selectedRole)) {
                System.out.println("  Access denied. Role mismatch.");
                auditLog.push(username, "Role mismatch on login");
                continue;
            }

            auditLog.push(user.username, "Logged in as " + user.role);
            System.out.println("\n  Welcome, " + user.username + "  [" + user.role + "]");

            // ── route to panel ──
            switch (user.role.toLowerCase()) {
                case "admin"        -> adminPanel(sc, user);
                case "investigator" -> investigatorPanel(sc, user);
                case "analyst"      -> analystPanel(sc, user);
            }

            auditLog.push(user.username, "Logged out");
        }

        System.out.println("\n  DEMS shutdown. Goodbye.");
    }

    // ═══════════════════════════════════════════════
    //  ADMIN PANEL
    // ═══════════════════════════════════════════════
    private static void adminPanel(Scanner sc, User admin) {
        while (true) {
            printHeader("ADMIN PANEL  —  " + admin.username);
            System.out.println("  ── User Management ──");
            System.out.println("  1. Create User");
            System.out.println("  2. Delete User");
            System.out.println("  3. Update User");
            System.out.println("  4. View All Users");
            System.out.println("  ── Case Management ──");
            System.out.println("  5. Add Case");
            System.out.println("  6. Search Case by ID");
            System.out.println("  7. View All Cases (sorted)");
            System.out.println("  8. Update Case Status");
            System.out.println("  9. Delete Case");
            System.out.println("  ── System ──");
            System.out.println("  10. View Audit Log");
            System.out.println("  11. Clear Audit Log");
            System.out.println("  0. Logout");
            printLine();
            System.out.print("  Choice: ");

            int ch = readInt(sc);
            if (ch == 0) break;

            switch (ch) {

                // ── 1. Create User ──
                case 1 -> {
                    System.out.println("\n  Select role for new user:");
                    System.out.println("  1. Admin   2. Investigator   3. Analyst");
                    System.out.print("  Choice: ");
                    int r = readInt(sc);
                    String nr = switch (r) {
                        case 1 -> "Admin";
                        case 2 -> "Investigator";
                        case 3 -> "Analyst";
                        default -> "";
                    };
                    if (nr.isEmpty()) { System.out.println("  Invalid role."); break; }

                    System.out.print("  Username: ");
                    String nu = sc.nextLine().trim();
                    System.out.print("  Password: ");
                    String np = sc.nextLine().trim();

                    if (nu.isEmpty() || np.isEmpty()) { System.out.println("  Username/password cannot be empty."); break; }

                    if (userService.getUser(nu) != null) {
                        System.out.println("  Username already exists.");
                    } else {
                        userService.addUser(new User(nu, np, nr));
                        auditLog.push(admin.username, "Created user: " + nu + " [" + nr + "]");
                        System.out.println("  User created.");
                    }
                }

                // ── 2. Delete User ──
                case 2 -> {
                    System.out.print("  Username to delete: ");
                    String du = sc.nextLine().trim();
                    if (du.equalsIgnoreCase(admin.username)) {
                        System.out.println("  Cannot delete yourself.");
                        break;
                    }
                    if (userService.getUser(du) == null) {
                        System.out.println("  User not found.");
                    } else {
                        userService.deleteUser(du);
                        auditLog.push(admin.username, "Deleted user: " + du);
                        System.out.println("  User deleted.");
                    }
                }

                // ── 3. Update User ──
                case 3 -> {
                    System.out.print("  Current username: ");
                    String oldU = sc.nextLine().trim();
                    System.out.print("  New username: ");
                    String newU = sc.nextLine().trim();
                    System.out.print("  New password: ");
                    String newP = sc.nextLine().trim();
                    if (newU.isEmpty() || newP.isEmpty()) { System.out.println("  Fields cannot be empty."); break; }
                    userService.updateUser(oldU, newU, newP);
                    auditLog.push(admin.username, "Updated user: " + oldU + " -> " + newU);
                }

                // ── 4. View All Users ──
                case 4 -> {
                    userService.showAllUsers();
                    auditLog.push(admin.username, "Viewed all users");
                }

                // ── 5. Add Case ──
                case 5 -> {
                    int cid = readPositiveInt(sc, "  Case Number (e.g. 1 -> C-001): ");
                    if (cid == -1) break;

                    System.out.print("  Case Title: ");
                    String title = sc.nextLine().trim();
                    if (title.isEmpty()) { System.out.println("  Title cannot be empty."); break; }

                    System.out.print("  Assigned Investigator username: ");
                    String inv = sc.nextLine().trim();

                    LocalDate date = readDate(sc, "  Date Opened (YYYY-MM-DD): ");
                    if (date == null) break;

                    try {
                        caseBST.addCase(cid, title, inv, date);
                        auditLog.push(admin.username, "Added case C-" + String.format("%03d", cid));
                        System.out.println("  Case added.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("  Error: " + e.getMessage());
                    }
                }

                // ── 6. Search Case by ID ──
                case 6 -> {
                    int sid = readPositiveInt(sc, "  Case Number to search: ");
                    if (sid == -1) break;
                    CaseNode found = caseBST.SearchById(caseBST.getRoot(), sid);
                    if (found == null) System.out.println("  Case not found.");
                    else               System.out.println("  " + found);
                    auditLog.push(admin.username, "Searched case C-" + String.format("%03d", sid));
                }

                // ── 7. View All Cases ──
                case 7 -> {
                    ArrayList<CaseNode> cases = caseBST.SortCases();
                    if (cases.isEmpty()) { System.out.println("  No cases on record."); break; }
                    System.out.println();
                    for (CaseNode c : cases) System.out.println("  " + c);
                    auditLog.push(admin.username, "Viewed all cases");
                }

                // ── 8. Update Case Status ──
                case 8 -> {
                    int uid = readPositiveInt(sc, "  Case Number to update: ");
                    if (uid == -1) break;
                    System.out.println("  1. OPENED   2. CLOSED");
                    System.out.print("  Choice: ");
                    int sc2 = readInt(sc);
                    CaseStatus ns = switch (sc2) {
                        case 1 -> CaseStatus.OPENED;
                        case 2 -> CaseStatus.CLOSED;
                        default -> null;
                    };
                    if (ns == null) { System.out.println("  Invalid status."); break; }
                    if (caseBST.updateStatus(uid, ns)) {
                        auditLog.push(admin.username, "Updated case C-" + String.format("%03d", uid) + " -> " + ns);
                        System.out.println("  Status updated.");
                    } else {
                        System.out.println("  Case not found.");
                    }
                }

                // ── 9. Delete Case ──
                case 9 -> {
                    int did = readPositiveInt(sc, "  Case Number to delete: ");
                    if (did == -1) break;
                    CaseNode check = caseBST.SearchById(caseBST.getRoot(), did);
                    if (check == null) { System.out.println("  Case not found."); break; }
                    caseBST.deleteCase(did);
                    auditLog.push(admin.username, "Deleted case C-" + String.format("%03d", did));
                    System.out.println("  Case deleted.");
                }

                // ── 10. View Audit Log ──
                case 10 -> {
                    printHeader("AUDIT LOG");
                    auditLog.display();
                }

                // ── 11. Clear Audit Log ──
                case 11 -> {
                    auditLog.clear();
                    System.out.println("  Audit log cleared.");
                }

                default -> System.out.println("  Invalid choice.");
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  INVESTIGATOR PANEL
    // ═══════════════════════════════════════════════
    private static void investigatorPanel(Scanner sc, User inv) {
        while (true) {
            printHeader("INVESTIGATOR PANEL  —  " + inv.username);
            System.out.println("  ── Cases (view only) ──");
            System.out.println("  1. View All Cases");
            System.out.println("  2. Search Case by ID");
            System.out.println("  ── Evidence Management ──");
            System.out.println("  3. Add Evidence");
            System.out.println("  4. View Evidence (forward)");
            System.out.println("  5. View Evidence (reverse)");
            System.out.println("  6. Search Evidence by ID");
            System.out.println("  7. Search Evidence by Case ID");
            System.out.println("  8. Change Evidence Status");
            System.out.println("  9. Delete Evidence");
            System.out.println("  ── Transfer ──");
            System.out.println("  10. Send Evidence for Analysis");
            System.out.println("  0. Logout");
            printLine();
            System.out.print("  Choice: ");

            int ch = readInt(sc);
            if (ch == 0) break;

            switch (ch) {

                // ── 1. View All Cases ──
                case 1 -> {
                    ArrayList<CaseNode> cases = caseBST.SortCases();
                    if (cases.isEmpty()) { System.out.println("  No cases on record."); break; }
                    System.out.println();
                    for (CaseNode c : cases) System.out.println("  " + c);
                    auditLog.push(inv.username, "Viewed all cases");
                }

                // ── 2. Search Case by ID ──
                case 2 -> {
                    int sid = readPositiveInt(sc, "  Case Number to search: ");
                    if (sid == -1) break;
                    CaseNode found = caseBST.SearchById(caseBST.getRoot(), sid);
                    if (found == null) System.out.println("  Case not found.");
                    else               System.out.println("  " + found);
                    auditLog.push(inv.username, "Searched case C-" + String.format("%03d", sid));
                }

                // ── 3. Add Evidence ──
                case 3 -> {
                    int evNum = readPositiveInt(sc, "  Evidence Number (e.g. 1 -> EV-001): ");
                    if (evNum == -1) break;

                    int caseNum = readPositiveInt(sc, "  Case Number this evidence belongs to: ");
                    if (caseNum == -1) break;

                    // verify case exists
                    if (caseBST.SearchById(caseBST.getRoot(), caseNum) == null) {
                        System.out.println("  Case C-" + String.format("%03d", caseNum) + " does not exist. Ask admin to create it first.");
                        break;
                    }

                    System.out.print("  Description: ");
                    String desc = sc.nextLine().trim();
                    if (desc.isEmpty()) { System.out.println("  Description cannot be empty."); break; }

                    System.out.println("  Priority:  1. HIGH   2. MEDIUM   3. LOW");
                    System.out.print("  Choice: ");
                    PRIORITY priority = switch (readInt(sc)) {
                        case 1 -> PRIORITY.HIGH;
                        case 3 -> PRIORITY.LOW;
                        default -> PRIORITY.MEDIUM;
                    };

                    LocalDate date = readDate(sc, "  Date Added (YYYY-MM-DD): ");
                    if (date == null) break;

                    try {
                        evidenceList.addEvidence(evNum, caseNum, desc, priority, inv.username, date);
                        auditLog.push(inv.username, "Added evidence EV-" + String.format("%03d", evNum));
                        System.out.println("  Evidence added.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("  Error: " + e.getMessage());
                    }
                }

                // ── 4. View Forward ──
                case 4 -> {
                    printNodes(evidenceList.displayForward());
                    auditLog.push(inv.username, "Viewed all evidence (forward)");
                }

                // ── 5. View Reverse ──
                case 5 -> {
                    printNodes(evidenceList.displayReverse());
                    auditLog.push(inv.username, "Viewed all evidence (reverse)");
                }

                // ── 6. Search by Evidence ID ──
                case 6 -> {
                    int eid = readPositiveInt(sc, "  Evidence Number to search: ");
                    if (eid == -1) break;
                    EvidenceNode node = evidenceList.searchById(eid);
                    System.out.println(node != null ? "  " + node : "  Evidence not found.");
                    auditLog.push(inv.username, "Searched evidence EV-" + String.format("%03d", eid));
                }

                // ── 7. Search by Case ID ──
                case 7 -> {
                    int cid = readPositiveInt(sc, "  Case Number to filter by: ");
                    if (cid == -1) break;
                    printNodes(evidenceList.searchByCaseId(cid));
                    auditLog.push(inv.username, "Searched evidence for case C-" + String.format("%03d", cid));
                }

                // ── 8. Change Evidence Status ──
                case 8 -> {
                    runStatusChange(sc, inv.username);
                }

                // ── 9. Delete Evidence ──
                case 9 -> {
                    int delId = readPositiveInt(sc, "  Evidence Number to delete: ");
                    if (delId == -1) break;
                    if (evidenceList.deleteEvidenceById(delId)) {
                        auditLog.push(inv.username, "Deleted evidence EV-" + String.format("%03d", delId));
                        System.out.println("  Evidence deleted.");
                    } else {
                        System.out.println("  Evidence not found.");
                    }
                }

                // ── 10. Send for Analysis ──
                case 10 -> {
                    int eid = readPositiveInt(sc, "  Evidence Number to send for analysis: ");
                    if (eid == -1) break;
                    EvidenceNode node = evidenceList.searchById(eid);
                    if (node == null) { System.out.println("  Evidence not found."); break; }
                    custodyQueue.enqueue(node.getEvidenceId(), node.getCaseId(), inv.username);
                    // mark as IN_QUEUE
                    evidenceList.changeStatus(eid, STATUS.IN_QUEUE);
                    auditLog.push(inv.username, "Sent " + node.getEvidenceId() + " for analysis");
                    System.out.println("  Evidence queued for analysis. Queue size: " + custodyQueue.size());
                }

                default -> System.out.println("  Invalid choice.");
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  ANALYST PANEL
    // ═══════════════════════════════════════════════
    private static void analystPanel(Scanner sc, User analyst) {
        while (true) {
            printHeader("ANALYST PANEL  —  " + analyst.username);
            System.out.println("  ── Transfer Queue ──");
            System.out.println("  1. View Transfer Queue");
            System.out.println("  2. Take Next Evidence (dequeue)");
            System.out.println("  ── Evidence ──");
            System.out.println("  3. View All Evidence (forward)");
            System.out.println("  4. Search Evidence by ID");
            System.out.println("  5. Search Evidence by Case ID");
            System.out.println("  6. Update Evidence Status");
            System.out.println("  0. Logout");
            printLine();
            System.out.print("  Choice: ");

            int ch = readInt(sc);
            if (ch == 0) break;

            switch (ch) {

                // ── 1. View Queue ──
                case 1 -> {
                    printHeader("TRANSFER QUEUE  (" + custodyQueue.size() + " items)");
                    custodyQueue.display();
                    auditLog.push(analyst.username, "Viewed transfer queue");
                }

                // ── 2. Dequeue ──
                case 2 -> {
                    QueueNode item = custodyQueue.dequeue();
                    if (item == null) {
                        System.out.println("  Queue is empty. Nothing to process.");
                    } else {
                        System.out.println("  Processing: " + item.evidenceId +
                                "  |  Case: " + item.caseId +
                                "  |  From: " + item.submittedBy);
                        // auto-mark as UNDER_ANALYSIS
                        String rawId = item.evidenceId.replace("EV-", "");
                        try {
                            int numId = Integer.parseInt(rawId);
                            evidenceList.changeStatus(numId, STATUS.UNDER_ANALYSIS);
                        } catch (NumberFormatException ignored) {}
                        auditLog.push(analyst.username, "Dequeued " + item.evidenceId + " for analysis");
                        System.out.println("  Status set to UNDER_ANALYSIS.");
                    }
                }

                // ── 3. View All Evidence ──
                case 3 -> {
                    printNodes(evidenceList.displayForward());
                    auditLog.push(analyst.username, "Viewed all evidence");
                }

                // ── 4. Search by Evidence ID ──
                case 4 -> {
                    int eid = readPositiveInt(sc, "  Evidence Number to search: ");
                    if (eid == -1) break;
                    EvidenceNode node = evidenceList.searchById(eid);
                    System.out.println(node != null ? "  " + node : "  Evidence not found.");
                    auditLog.push(analyst.username, "Searched evidence EV-" + String.format("%03d", eid));
                }

                // ── 5. Search by Case ID ──
                case 5 -> {
                    int cid = readPositiveInt(sc, "  Case Number to filter by: ");
                    if (cid == -1) break;
                    printNodes(evidenceList.searchByCaseId(cid));
                    auditLog.push(analyst.username, "Searched evidence for case C-" + String.format("%03d", cid));
                }

                // ── 6. Update Evidence Status ──
                case 6 -> {
                    runStatusChange(sc, analyst.username);
                }

                default -> System.out.println("  Invalid choice.");
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  SHARED HELPERS
    // ═══════════════════════════════════════════════

    /** Shared status-change flow used by both Investigator and Analyst */
    private static void runStatusChange(Scanner sc, String username) {
        int uid = readPositiveInt(sc, "  Evidence Number to update: ");
        if (uid == -1) return;
        System.out.println("  1. PENDING   2. IN_QUEUE   3. UNDER_ANALYSIS   4. ANALYZED   5. CLOSED");
        System.out.print("  Choice: ");
        STATUS ns = switch (readInt(sc)) {
            case 1 -> STATUS.PENDING;
            case 2 -> STATUS.IN_QUEUE;
            case 3 -> STATUS.UNDER_ANALYSIS;
            case 4 -> STATUS.ANALYZED;
            case 5 -> STATUS.CLOSED;
            default -> null;
        };
        if (ns == null) { System.out.println("  Invalid status."); return; }
        if (evidenceList.changeStatus(uid, ns)) {
            auditLog.push(username, "Changed status of EV-" + String.format("%03d", uid) + " -> " + ns);
            System.out.println("  Status updated.");
        } else {
            System.out.println("  Evidence not found.");
        }
    }

    /** Read a valid integer from a full line; returns -1 on bad input */
    private static int readInt(Scanner sc) {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    /** Prompt until a positive integer is entered; returns -1 if user types 0 or bad input once */
    private static int readPositiveInt(Scanner sc, String prompt) {
        System.out.print(prompt);
        int val = readInt(sc);
        if (val <= 0) { System.out.println("  Invalid number."); return -1; }
        return val;
    }

    /** Prompt until a valid date is entered; returns null after 3 failed attempts */
    private static LocalDate readDate(Scanner sc, String prompt) {
        int attempts = 0;
        while (attempts < 3) {
            System.out.print(prompt);
            try { return LocalDate.parse(sc.nextLine().trim(), DATE_FMT); }
            catch (DateTimeParseException e) {
                System.out.println("  Invalid date format. Use YYYY-MM-DD.");
                attempts++;
            }
        }
        System.out.println("  Too many invalid attempts. Returning to menu.");
        return null;
    }

    private static void printNodes(ArrayList<EvidenceNode> nodes) {
        if (nodes == null || nodes.isEmpty()) { System.out.println("  No evidence found."); return; }
        System.out.println();
        for (EvidenceNode n : nodes) System.out.println("  " + n);
    }

    private static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  " + title);
        System.out.println("=".repeat(50));
    }

    private static void printLine() {
        System.out.println("-".repeat(50));
    }
}