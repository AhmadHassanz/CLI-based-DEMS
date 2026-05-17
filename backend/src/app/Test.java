package app;

import audit.AuditStack;
import audit.LogEntry;
import cases.CaseBST;
import cases.CaseNode;
import cases.CaseStatus;
import custody.CustodyQueue;
import custody.QueueNode;
import evidence.EvidenceList;
import evidence.EvidenceNode;
import evidence.PRIORITY;
import evidence.STATUS;
import users.HashTableService;
import users.User;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Test {

   
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static HashTableService userService  = new HashTableService();
    private static EvidenceList     evidenceList = new EvidenceList();
    private static CaseBST          caseBST      = new CaseBST();
    private static CustodyQueue     custodyQueue = new CustodyQueue();
    private static AuditStack       auditStack   = new AuditStack(); // Newly added!


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
                auditStack.push(username.isEmpty() ? "UNKNOWN" : username, "Failed login attempt as " + selectedRole);
                continue;
            }
            if (!user.getRole().equalsIgnoreCase(selectedRole)) {
                System.out.println("  Access denied. Role mismatch.");
                auditStack.push(user.getUsername(), "Failed login (Role mismatch)");
                continue;
            }

            System.out.println("\n  Welcome, " + user.getUsername() + "  [" + user.getRole() + "]");
            auditStack.push(user.getUsername(), "Logged into system");

            switch (user.getRole().toLowerCase()) {
                case "admin"        -> adminPanel(sc, user);
                case "investigator" -> investigatorPanel(sc, user);
                case "analyst"      -> analystPanel(sc, user);
            }
        }

        System.out.println("\n  DEMS shutdown. Goodbye!");
        sc.close();
    }

  
    private static void adminPanel(Scanner sc, User admin) {
        while (true) {
            printHeader("ADMIN PANEL  —  " + admin.getUsername());
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
            System.out.println("  ── System Audit ──");
            System.out.println("  10. View Recent Audit Logs (Top 5)");
            System.out.println("  11. View Full Audit Trail");
            System.out.println("  0. Logout");
            printLine();
            System.out.print("  Choice: ");

            int ch = readInt(sc);
            if (ch == 0) {
                auditStack.push(admin.getUsername(), "Logged out");
                break;
            }

            switch (ch) {
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
                        System.out.println("  User created.");
                        auditStack.push(admin.getUsername(), "Created new user account: " + nu);
                    }
                }
                case 2 -> {
                    System.out.print("  Username to delete: ");
                    String du = sc.nextLine().trim();
                    if (du.equalsIgnoreCase(admin.getUsername())) {
                        System.out.println("  Cannot delete yourself.");
                        break;
                    }
                    if (userService.getUser(du) == null) {
                        System.out.println("  User not found.");
                    } else {
                        userService.deleteUser(du);
                        System.out.println("  User deleted.");
                        auditStack.push(admin.getUsername(), "Deleted user account: " + du);
                    }
                }
                case 3 -> {
                    System.out.print("  Current username: ");
                    String oldU = sc.nextLine().trim();
                    System.out.print("  New username: ");
                    String newU = sc.nextLine().trim();
                    System.out.print("  New password: ");
                    String newP = sc.nextLine().trim();
                    if (newU.isEmpty() || newP.isEmpty()) { System.out.println("  Fields cannot be empty."); break; }
                    userService.updateUser(oldU, newU, newP);
                    auditStack.push(admin.getUsername(), "Updated credentials for user: " + oldU);
                }
                case 4 -> {
                    userService.showAllUsers();
                    auditStack.push(admin.getUsername(), "Viewed all system users");
                }
                case 5 -> {
                    int cid = readPositiveInt(sc, "  Case Number (e.g. 1 -> C-001): ");
                    if (cid == -1) break;

                    System.out.print("  Case Title: ");
                    String title = sc.nextLine().trim();
                    if (title.isEmpty()) { System.out.println("  Title cannot be empty."); break; }

                    System.out.print("  Assigned Investigator username: ");
                    String inv = sc.nextLine().trim();

                    LocalDate date = readDate(sc, "  Date Opened (DD-MM-YYYY): ");
                    if (date == null) break;

                    try {
                        caseBST.addCase(cid, title, inv, date);
                        System.out.println("  Case added.");
                        auditStack.push(admin.getUsername(), "Added Case C-" + String.format("%03d", cid));
                    } catch (IllegalArgumentException e) {
                        System.out.println("  Error: " + e.getMessage());
                    }
                }
                case 6 -> {
                    int sid = readPositiveInt(sc, "  Case Number to search: ");
                    if (sid == -1) break;
                    CaseNode found = caseBST.SearchById(caseBST.getRoot(), sid);
                    if (found == null) System.out.println("  Case not found.");
                    else {
                        System.out.println("  " + found);
                        auditStack.push(admin.getUsername(), "Searched for Case C-" + String.format("%03d", sid));
                    }
                }
                case 7 -> {
                    ArrayList<CaseNode> cases = caseBST.SortCases();
                    if (cases.isEmpty()) { System.out.println("  No cases on record."); break; }
                    System.out.println();
                    for (CaseNode c : cases) System.out.println("  " + c);
                    auditStack.push(admin.getUsername(), "Viewed sorted list of all cases");
                }
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
                        System.out.println("  Status updated.");
                        auditStack.push(admin.getUsername(), "Updated status of Case C-" + String.format("%03d", uid) + " to " + ns);
                    } else {
                        System.out.println("  Case not found.");
                    }
                }
                case 9 -> {
                    int did = readPositiveInt(sc, "  Case Number to delete: ");
                    if (did == -1) break;
                    CaseNode check = caseBST.SearchById(caseBST.getRoot(), did);
                    if (check == null) { System.out.println("  Case not found."); break; }
                    caseBST.deleteCase(did);
                    System.out.println("  Case deleted.");
                    auditStack.push(admin.getUsername(), "Deleted Case C-" + String.format("%03d", did));
                }
                case 10 -> {
                    System.out.println("\n  ── Top 5 Audit Logs ──");
                    ArrayList<LogEntry> logs = auditStack.recentlog(5);
                    if(logs.isEmpty()) System.out.println("  No logs found.");
                    for(LogEntry l : logs) System.out.println("  " + l);
                }
                case 11 -> {
                    System.out.println("\n  ── Full Audit Trail ──");
                    ArrayList<LogEntry> logs = auditStack.viewLog();
                    if(logs.isEmpty()) System.out.println("  No logs found.");
                    for(LogEntry l : logs) System.out.println("  " + l);
                }
                default -> System.out.println("  Invalid choice.");
            }
        }
    }


    private static void investigatorPanel(Scanner sc, User inv) {
        while (true) {
            printHeader("INVESTIGATOR PANEL  —  " + inv.getUsername());
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
            if (ch == 0) {
                auditStack.push(inv.getUsername(), "Logged out");
                break;
            }

            switch (ch) {
                case 1 -> {
                    ArrayList<CaseNode> cases = caseBST.SortCases();
                    if (cases.isEmpty()) { System.out.println("  No cases on record."); break; }
                    System.out.println();
                    for (CaseNode c : cases) System.out.println("  " + c);
                    auditStack.push(inv.getUsername(), "Viewed all cases");
                }
                case 2 -> {
                    int sid = readPositiveInt(sc, "  Case Number to search: ");
                    if (sid == -1) break;
                    CaseNode found = caseBST.SearchById(caseBST.getRoot(), sid);
                    if (found == null) System.out.println("  Case not found.");
                    else {
                        System.out.println("  " + found);
                        auditStack.push(inv.getUsername(), "Searched for Case C-" + String.format("%03d", sid));
                    }
                }
                case 3 -> {
                    int evNum = readPositiveInt(sc, "  Evidence Number (e.g. 1 -> EV-001): ");
                    if (evNum == -1) break;

                    int caseNum = readPositiveInt(sc, "  Case Number this evidence belongs to: ");
                    if (caseNum == -1) break;

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

                    LocalDate date = readDate(sc, "  Date Added (DD-MM-YYYY): ");
                    if (date == null) break;

                    try {
                        evidenceList.addEvidence(evNum, caseNum, desc, priority, inv.getUsername(), date);
                        System.out.println("  Evidence added.");
                        auditStack.push(inv.getUsername(), "Added Evidence EV-" + String.format("%03d", evNum) + " to Case C-" + String.format("%03d", caseNum));
                    } catch (IllegalArgumentException e) {
                        System.out.println("  Error: " + e.getMessage());
                    }
                }
                case 4 -> {
                    printNodes(evidenceList.displayForward());
                    auditStack.push(inv.getUsername(), "Viewed evidence list (forward)");
                }
                case 5 -> {
                    printNodes(evidenceList.displayReverse());
                    auditStack.push(inv.getUsername(), "Viewed evidence list (reverse)");
                }
                case 6 -> {
                    int eid = readPositiveInt(sc, "  Evidence Number to search: ");
                    if (eid == -1) break;
                    EvidenceNode node = evidenceList.searchById(eid);
                    if(node != null) {
                        System.out.println("  " + node);
                        auditStack.push(inv.getUsername(), "Searched for Evidence EV-" + String.format("%03d", eid));
                    } else {
                        System.out.println("  Evidence not found.");
                    }
                }
                case 7 -> {
                    int cid = readPositiveInt(sc, "  Case Number to filter by: ");
                    if (cid == -1) break;
                    printNodes(evidenceList.searchByCaseId(cid));
                    auditStack.push(inv.getUsername(), "Filtered evidence by Case C-" + String.format("%03d", cid));
                }
                case 8 -> runStatusChange(sc, inv.getUsername());
                case 9 -> {
                    int delId = readPositiveInt(sc, "  Evidence Number to delete: ");
                    if (delId == -1) break;
                    if (evidenceList.deleteEvidenceById(delId)) {
                        System.out.println("  Evidence deleted.");
                        auditStack.push(inv.getUsername(), "Deleted Evidence EV-" + String.format("%03d", delId));
                    } else {
                        System.out.println("  Evidence not found.");
                    }
                }
                case 10 -> {
                    int eid = readPositiveInt(sc, "  Evidence Number to send for analysis: ");
                    if (eid == -1) break;
                    EvidenceNode node = evidenceList.searchById(eid);
                    if (node == null) { System.out.println("  Evidence not found."); break; }
                    
                    int caseInt = Integer.parseInt(node.getCaseId().replace("C-", ""));
                    custodyQueue.enqueue(eid, caseInt, inv.getUsername(), node.getPriority());
                    evidenceList.changeStatus(eid, STATUS.IN_QUEUE);
                    System.out.println("  Evidence queued for analysis.");
                    auditStack.push(inv.getUsername(), "Transferred Evidence EV-" + String.format("%03d", eid) + " to analysis queue");
                }
                default -> System.out.println("  Invalid choice.");
            }
        }
    }

  
    private static void analystPanel(Scanner sc, User analyst) {
        while (true) {
            printHeader("ANALYST PANEL  —  " + analyst.getUsername());
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
            if (ch == 0) {
                auditStack.push(analyst.getUsername(), "Logged out");
                break;
            }

            switch (ch) {
                case 1 -> {
                    printHeader("TRANSFER QUEUE");
                    ArrayList<QueueNode> qList = custodyQueue.displayQueue();
                    if(qList.isEmpty()) {
                        System.out.println("  Queue is empty.");
                    } else {
                        for(QueueNode q : qList) {
                            System.out.println("  " + q);
                        }
                    }
                    auditStack.push(analyst.getUsername(), "Viewed transfer queue");
                }
                case 2 -> {
                    QueueNode item = custodyQueue.dequeue();
                    if (item == null) {
                        System.out.println("  Queue is empty. Nothing to process.");
                    } else {
                        System.out.println("  Processing: " + item.getEvidenceId() +
                                "  |  Case: " + item.getCaseId() +
                                "  |  From: " + item.getSubmittedBy());
                        
                        try {
                            int numId = Integer.parseInt(item.getEvidenceId().replace("EV-", ""));
                            evidenceList.changeStatus(numId, STATUS.UNDER_ANALYSIS);
                            System.out.println("  Status set to UNDER_ANALYSIS.");
                            auditStack.push(analyst.getUsername(), "Dequeued and began processing Evidence " + item.getEvidenceId());
                        } catch (NumberFormatException e) {
                            System.out.println("  Failed to update evidence status.");
                        }
                    }
                }
                case 3 -> {
                    printNodes(evidenceList.displayForward());
                    auditStack.push(analyst.getUsername(), "Viewed all system evidence");
                }
                case 4 -> {
                    int eid = readPositiveInt(sc, "  Evidence Number to search: ");
                    if (eid == -1) break;
                    EvidenceNode node = evidenceList.searchById(eid);
                    if(node != null) {
                        System.out.println("  " + node);
                        auditStack.push(analyst.getUsername(), "Searched for Evidence EV-" + String.format("%03d", eid));
                    } else {
                        System.out.println("  Evidence not found.");
                    }
                }
                case 5 -> {
                    int cid = readPositiveInt(sc, "  Case Number to filter by: ");
                    if (cid == -1) break;
                    printNodes(evidenceList.searchByCaseId(cid));
                    auditStack.push(analyst.getUsername(), "Filtered evidence by Case C-" + String.format("%03d", cid));
                }
                case 6 -> runStatusChange(sc, analyst.getUsername());
                default -> System.out.println("  Invalid choice.");
            }
        }
    }

    private static void runStatusChange(Scanner sc, String currentUsername) {
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
            System.out.println("  Status updated.");
            auditStack.push(currentUsername, "Changed status of Evidence EV-" + String.format("%03d", uid) + " to " + ns);
        } else {
            System.out.println("  Evidence not found.");
        }
    }

    private static int readInt(Scanner sc) {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    private static int readPositiveInt(Scanner sc, String prompt) {
        System.out.print(prompt);
        int val = readInt(sc);
        if (val <= 0) { System.out.println("  Invalid number."); return -1; }
        return val;
    }

    private static LocalDate readDate(Scanner sc, String prompt) {
        int attempts = 0;
        while (attempts < 3) {
            System.out.print(prompt);
            try { return LocalDate.parse(sc.nextLine().trim(), DATE_FMT); }
            catch (DateTimeParseException e) {
                System.out.println("  Invalid date format. Use DD-MM-YYYY.");
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