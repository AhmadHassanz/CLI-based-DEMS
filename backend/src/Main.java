import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        HashTableService userService = new HashTableService();
        EvidenceList evidenceList = new EvidenceList();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== DEMS SYSTEM =====");
            System.out.println("Select Role To Authenticate:");
            System.out.println("1. Admin");
            System.out.println("2. Investigator");
            System.out.println("3. Analyst");
            System.out.println("4. Exit");
            System.out.print("Choice: ");

            int roleChoice = readIntInput(sc);
            if (roleChoice == 4) {
                break;
            }

            String selectedRole = "";
            if (roleChoice == 1) selectedRole = "Admin";
            else if (roleChoice == 2) selectedRole = "Investigator";
            else if (roleChoice == 3) selectedRole = "Analyst";
            else {
                System.out.println("Invalid structural role chosen.");
                continue;
            }

            System.out.print("Username: ");
            String u = sc.nextLine();

            System.out.print("Password: ");
            String p = sc.nextLine();

            User user = userService.login(u, p);

            if (user == null) {
                System.out.println("Login Failed! Invalid credentials.");
                continue;
            }

            if (!user.role.equalsIgnoreCase(selectedRole)) {
                System.out.println("Access Denied! Clear-role mismatch identified.");
                continue;
            }

            System.out.println("\nLogin Successful!");
            System.out.println("Authenticated Role Context: " + user.role);

            // ==========================================
            // 1. ADMIN PANEL SUB-LOOP
            // ==========================================
            if (user.role.equalsIgnoreCase("Admin")) {
                while (true) {
                    System.out.println("\n===== ADMIN PANEL =====");
                    System.out.println("1. Create User");
                    System.out.println("2. Delete User");
                    System.out.println("3. View Users");
                    System.out.println("4. Update User");
                    System.out.println("5. Logout");
                    System.out.print("Choice: ");

                    int ch = readIntInput(sc);
                    if (ch == 5 || ch == -1) break;

                    if (ch == 1) {
                        System.out.println("\nSelect Role For New User:");
                        System.out.println("1. Admin\n2. Investigator\n3. Analyst");
                        System.out.print("Choice: ");
                        int role = readIntInput(sc);

                        String nr = switch (role) {
                            case 1 -> "Admin";
                            case 2 -> "Investigator";
                            case 3 -> "Analyst";
                            default -> "";
                        };

                        if (nr.isEmpty()) {
                            System.out.println("Invalid Role Choice!");
                            continue;
                        }

                        System.out.print("Username: ");
                        String nu = sc.nextLine();
                        System.out.print("Password: ");
                        String np = sc.nextLine();

                        if (userService.getUser(nu) != null) {
                            System.out.println("Username already exists!");
                        } else {
                            User newUser = new User(nu, np, nr);
                            userService.addUser(newUser);
                            System.out.println("User Created!");
                        }
                    } else if (ch == 2) {
                        System.out.print("Username to delete: ");
                        String du = sc.nextLine();
                        if (userService.getUser(du) != null) {
                            userService.deleteUser(du);
                            System.out.println("User Deleted!");
                        } else {
                            System.out.println("User not found!");
                        }
                    } else if (ch == 3) {
                        userService.showAllUsers();
                    } else if (ch == 4) {
                        System.out.print("Old Username: ");
                        String oldU = sc.nextLine();
                        System.out.print("New Username: ");
                        String newU = sc.nextLine();
                        System.out.print("New Password: ");
                        String newP = sc.nextLine();
                        userService.updateUser(oldU, newU, newP);
                    }
                }
            } 
            
            // ==========================================
            // 2. INVESTIGATOR PANEL SUB-LOOP (Full Access)
            // ==========================================
            else if (user.role.equalsIgnoreCase("Investigator")) {
                int invInput = -1;
                while (invInput != 0) {
                    System.out.println("\n=========== Investigator Menu ==========");
                    System.out.println("1. Add Evidence");
                    System.out.println("2. Display Forward");
                    System.out.println("3. Display Reverse");
                    System.out.println("4. Search By Evidence Id");
                    System.out.println("5. Search By Case Id");
                    System.out.println("6. Change Evidence Status");
                    System.out.println("7. Delete Evidence Node");
                    System.out.println("0. Logout");
                    System.out.print("Enter option (0-7): ");

                    invInput = readIntInput(sc);
                    if (invInput == 0) break;

                    switch (invInput) {
                        case 1 -> {
                            System.out.println("\n==== Adding Evidence ====");
                            int evidenceNum, caseNum;
                            while (true) {
                                System.out.print("Enter Evidence Number Identifier: ");
                                evidenceNum = readIntInput(sc);
                                if (evidenceNum != -1) break;
                                System.out.println("Format error. Enter numerical values only.");
                            }
                            while (true) {
                                System.out.print("Enter Case Number Assignment: ");
                                caseNum = readIntInput(sc);
                                if (caseNum != -1) break;
                                System.out.println("Format error. Enter numerical values only.");
                            }
                            System.out.print("Description Statement: ");
                            String description = sc.nextLine();
                            
                            LocalDate validatedDate = null;
                            while (true) {
                                System.out.print("Enter Date Added (YYYY-MM-DD): ");
                                String dateInput = sc.nextLine();
                                try {
                                    validatedDate = LocalDate.parse(dateInput, DATE_FORMATTER);
                                    break;
                                } catch (DateTimeParseException e) {
                                    System.out.println("Invalid string parsing format. Please try again.");
                                }
                            }
                            try {
                                evidenceList.addEvidence(evidenceNum, caseNum, description, user.username, validatedDate);
                                System.out.println("Evidence saved into secure storage matrix.");
                            } catch (IllegalArgumentException e) {
                                System.out.println("Abort: " + e.getMessage());
                            }
                        }
                        case 2 -> displayNodes(evidenceList.displayForward());
                        case 3 -> displayNodes(evidenceList.displayReverse());
                        case 4 -> {
                            System.out.print("Search Target Evidence Number: ");
                            int targetId = readIntInput(sc);
                            EvidenceNode node = evidenceList.searchById(targetId);
                            System.out.println(node != null ? node : "Record absent from data maps.");
                        }
                        case 5 -> displayNodes(evidenceList.searchByCaseId(readIntInput(sc)));
                        case 6 -> runStatusAdjustment(evidenceList, sc);
                        case 7 -> {
                            System.out.print("Enter Evidence ID to Drop from Register: ");
                            int delId = readIntInput(sc);
                            if (evidenceList.deleteEvidenceById(delId)) {
                                System.out.println("Node successfully unlinked from persistence layers.");
                            } else {
                                System.out.println("Node deletion error: ID not detected.");
                            }
                        }
                        default -> System.out.println("Action selection invalid.");
                    }
                }
            } 
            
            // ==========================================
            // 3. ANALYST PANEL SUB-LOOP (Read-Only + Status Edit)
            // ==========================================
            else if (user.role.equalsIgnoreCase("Analyst")) {
                int analystInput = -1;
                while (analystInput != 0) {
                    System.out.println("\n=========== Analyst Registry Menu ==========");
                    System.out.println("1. Display Forward View");
                    System.out.println("2. Search By Evidence Id");
                    System.out.println("3. Search By Case Id");
                    System.out.println("4. Update Processing Status");
                    System.out.println("0. Logout");
                    System.out.print("Enter option (0-4): ");

                    analystInput = readIntInput(sc);
                    if (analystInput == 0) break;

                    switch (analystInput) {
                        case 1 -> displayNodes(evidenceList.displayForward());
                        case 2 -> {
                            System.out.print("Target Evidence Id Lookup: ");
                            int targetId = readIntInput(sc);
                            EvidenceNode node = evidenceList.searchById(targetId);
                            System.out.println(node != null ? node : "Record location evaluation failed.");
                        }
                        case 3 -> {
                            System.out.print("Target Case Grouping Id: ");
                            int caseTarget = readIntInput(sc);
                            displayNodes(evidenceList.searchByCaseId(caseTarget));
                        }
                        case 4 -> runStatusAdjustment(evidenceList, sc);
                        default -> System.out.println("Operational permission structure invalid.");
                    }
                }
            }
        }
        sc.close();
        System.out.println("DEMS Shutdown Process Finalized.");
    }

    // Modular Helper - Safe Line Buffer Interception
    private static int readIntInput(Scanner sc) {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Modular Helper - Dynamic Content Iteration 
    private static void displayNodes(ArrayList<EvidenceNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            System.out.println("Zero matching system traces identified.");
            return;
        }
        for (EvidenceNode node : nodes) {
            System.out.println(node);
        }
    }

    // Modular Helper - Inline State Mutation Interface
    private static void runStatusAdjustment(EvidenceList list, Scanner sc) {
        System.out.print("Target Status Alteration Evidence ID: ");
        int updateId = readIntInput(sc);
        System.out.println("Assign Target Processing Vector:\n1. PENDING\n2. IN_QUEUE\n3. ANALYZED\n4. CLOSED");
        System.out.print("Execution Vector Choice: ");
        int choice = readIntInput(sc);

        STATUS assignedStatus = switch (choice) {
            case 2 -> STATUS.IN_QUEUE;
            case 3 -> STATUS.ANALYZED;
            case 4 -> STATUS.CLOSED;
            default -> STATUS.PENDING;
        };

        if (list.changeStatus(updateId, assignedStatus)) {
            System.out.println("System state vector successfully saved.");
        } else {
            System.out.println("State transition failure: Missing index map reference.");
        }
    }
}