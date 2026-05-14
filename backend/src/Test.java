import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Test {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        EvidenceList evidenceList = new EvidenceList();

        int input = -1;
        while (input != 0) {
            System.out.println("\n===========Investigator Menu==========");
            System.out.println("1. Add Evidence");
            System.out.println("2. Display Forward");
            System.out.println("3. Display Reverse");
            System.out.println("4. Search By Evidence Id");
            System.out.println("5. Search By Case Id");
            System.out.println("6. Change Status");
            System.out.println("0. Exit");
            System.out.print("Enter any option (0-6): ");

            input = readIntInput();
            if (input == -1) {
                System.out.println("Invalid choice. Please enter a valid menu number.");
                continue;
            }

            switch (input) {
                case 1:
                    System.out.println("\n==== Adding Evidence ====");
                    
                    int evidenceNum;
                    while (true) {
                        System.out.print("Enter Evidence Number (Integer): ");
                        evidenceNum = readIntInput();
                        if (evidenceNum != -1) break;
                        System.out.println("Invalid ID. Please enter numbers only.");
                    }

                    int caseNum;
                    while (true) {
                        System.out.print("Enter Case Number (Integer): ");
                        caseNum = readIntInput();
                        if (caseNum != -1) break;
                        System.out.println("Invalid Case ID. Please enter numbers only.");
                    }

                    System.out.print("Enter Description: ");
                    String description = sc.nextLine();

                    System.out.print("Enter Submitted By: ");
                    String submittedBy = sc.nextLine();

                    LocalDate validatedDate = null; 
                    while (true) {
                        System.out.print("Enter Date Added (YYYY-MM-DD): ");
                        String dateInput = sc.nextLine();
                        try {
                            validatedDate = LocalDate.parse(dateInput, DATE_FORMATTER);
                            break; 
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date format or value! Use YYYY-MM-DD (e.g., 2026-05-14).");
                        }
                    }

                    try {
                        evidenceList.addEvidence(evidenceNum, caseNum, description, submittedBy, validatedDate);
                        System.out.println("Evidence added successfully!");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case 2:
                    System.out.println("\n===== Displaying List in Forward =====");
                    ArrayList<EvidenceNode> forwardList = evidenceList.displayForward();
                    if (forwardList.isEmpty()) {
                        System.out.println("No evidence records found.");
                    } else {
                        for (EvidenceNode node : forwardList) {
                            System.out.println(node);
                        }
                    }
                    break;

                case 3:
                    System.out.println("\n===== Displaying List in Reverse =====");
                    ArrayList<EvidenceNode> reverseList = evidenceList.displayReverse();
                    if (reverseList.isEmpty()) {
                        System.out.println("No evidence records found.");
                    } else {
                        for (EvidenceNode node : reverseList) {
                            System.out.println(node);
                        }
                    }
                    break;

                case 4:
                    System.out.println("\n===== Search By Evidence Id =====");
                    System.out.print("Enter Evidence Id Number: ");
                    int eviId = readIntInput();
                    if (eviId == -1) {
                        System.out.println("Invalid ID format.");
                        break;
                    }
                    
                    EvidenceNode foundEvi = evidenceList.searchById(eviId);
                    if (foundEvi != null) {
                        System.out.println(foundEvi);
                    } else {
                        System.out.println("No evidence found with that ID.");
                    }
                    break;

                case 5:
                    System.out.println("\n===== Search By Case Id =====");
                    System.out.print("Enter Case Id Number: ");
                    int caseId = readIntInput();
                    if (caseId == -1) {
                        System.out.println("Invalid Case ID format.");
                        break;
                    }

                    ArrayList<EvidenceNode> caseRecords = evidenceList.searchByCaseId(caseId);
                    if (caseRecords.isEmpty()) {
                        System.out.println("No evidence found matching that Case ID.");
                    } else {
                        for (EvidenceNode node : caseRecords) {
                            System.out.println(node);
                        }
                    }
                    break;

                case 6:
                    System.out.println("\n===== Change Status =====");
                    System.out.print("Enter Evidence Id to update: ");
                    int updateId = readIntInput();
                    if (updateId == -1) {
                        System.out.println("Invalid ID format.");
                        break;
                    }

                    System.out.println("Select New Status:");
                    System.out.println("1. PENDING\n2. IN_QUEUE\n3. ANALYZED\n4. CLOSED");
                    System.out.print("Choice: ");
                    int statusChoice = readIntInput();

                    STATUS selectedStatus;
                    switch (statusChoice) {
                        case 1 -> selectedStatus = STATUS.PENDING;
                        case 2 -> selectedStatus = STATUS.IN_QUEUE;
                        case 3 -> selectedStatus = STATUS.ANALYZED;
                        case 4 -> selectedStatus = STATUS.CLOSED;
                        default -> {
                            System.out.println("Invalid choice. Status defaulting to PENDING.");
                            selectedStatus = STATUS.PENDING;
                        }
                    }

                    if (evidenceList.changeStatus(updateId, selectedStatus)) {
                        System.out.println("Status updated successfully!");
                    } else {
                        System.out.println("Evidence ID not found.");
                    }
                    break;

                case 0:
                    System.out.println("Exiting system...");
                    break;

                default:
                    System.out.println("Invalid option selected.");
                    break;
            }
        }
        sc.close();
    }

   
    private static int readIntInput() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}