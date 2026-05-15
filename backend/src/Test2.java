import java.time.LocalDate;
import java.util.ArrayList;

public class Test2 {

    public static void main(String[] args) {

        CaseBST tree = new CaseBST();

        // =========================
        // 1. ADD CASES
        // =========================
        System.out.println("=== ADDING CASES ===");

        tree.addCase(10, "Theft Case", "Ali", LocalDate.of(2024, 5, 10));
        tree.addCase(5, "Fraud Case", "Ahmed", LocalDate.of(2024, 5, 12));
        tree.addCase(20, "Cyber Crime", "Usman", LocalDate.of(2024, 5, 15));
        tree.addCase(15, "Robbery Case", "Zain", LocalDate.of(2024, 5, 18));
        tree.addCase(2, "Missing Person", "Hassan", LocalDate.of(2024, 5, 20));

        System.out.println("Cases inserted successfully.\n");

        // =========================
        // 2. DISPLAY SORTED (INORDER)
        // =========================
        System.out.println("=== SORTED CASES (INORDER) ===");

        ArrayList<CaseNode> list = tree.SortCases();

        for (CaseNode c : list) {
            System.out.println(c);
        }

        System.out.println();

        // =========================
        // 3. SEARCH TEST
        // =========================
        System.out.println("=== SEARCH TEST ===");

        CaseNode found = tree.SearchById(tree.getRoot(), 15);

        if (found != null) {
            System.out.println("Found: " + found);
        } else {
            System.out.println("Case not found");
        }

        System.out.println();

        // =========================
        // 4. DELETE TEST (LEAF)
        // =========================
        System.out.println("=== DELETE TEST (Leaf Node) ===");

        tree.deleteCase(2);

        printTree(tree);

        System.out.println();

        // =========================
        // 5. DELETE TEST (ONE CHILD / TWO CHILD)
        // =========================
        System.out.println("=== DELETE TEST (Node 10) ===");

        tree.deleteCase(10);

        printTree(tree);
    }

    // =========================
    // HELPER: PRINT TREE (INORDER)
    // =========================
    public static void printTree(CaseBST tree) {

        ArrayList<CaseNode> list = tree.SortCases();

        System.out.println("Current BST (Sorted):");

        for (CaseNode c : list) {
            System.out.println(c);
        }
    }
}