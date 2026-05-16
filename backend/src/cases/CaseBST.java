package cases;

import java.time.LocalDate;
import java.util.ArrayList;

public class CaseBST {
    private CaseNode root;
    private CaseFileHandling file;

    public CaseBST() {
        root = null;
        file = new CaseFileHandling();
        file.loadCases(this);
    }

    // getter
    public CaseNode getRoot() {
        return root;
    }

    public void addCase(int id, String title, String Investigator, LocalDate dateOpened) {
        String caseId = String.format("C-%03d", id);
        root = insert(root, caseId, title, Investigator, dateOpened);
        file.saveCase(this);
    }

    private CaseNode insert(CaseNode root, String caseId, String title, String Investigator, LocalDate dateOpened) {
        if (root == null) {
            return new CaseNode(caseId, title, Investigator, dateOpened);
        }

        if (caseId.compareTo(root.getId()) < 0) {
            root.setLeft(insert(root.getLeft(), caseId, title, Investigator, dateOpened));
        } else if (caseId.compareTo(root.getId()) > 0) {
            root.setRight(insert(root.getRight(), caseId, title, Investigator, dateOpened));
        } else {
            throw new IllegalArgumentException("Case with this ID already exists" + caseId);
        }
        return root;
    }

    public CaseNode SearchById(CaseNode root, int id) {
        String caseId = String.format("C-%03d", id);
        if (root == null) {
            return null;
        }

        if (caseId.equals(root.getId())) {
            return root;
        }

        if (caseId.compareTo(root.getId()) < 0) {
            return SearchById(root.getLeft(), id);
        } else if (caseId.compareTo(root.getId()) > 0) {
            return SearchById(root.getRight(), id);
        }
        return root;
    }

    public ArrayList<CaseNode> SortCases() {
        ArrayList<CaseNode> list = new ArrayList<>();
        inorder(root, list);
        return list;
    }

    private void inorder(CaseNode root, ArrayList<CaseNode> list) {
        if (root == null) {
            return;
        }

        inorder(root.getLeft(), list);
        list.add(root);
        inorder(root.getRight(), list);
    }

    public void deleteCase(int id) {
        String caseId = String.format("C-%03d", id);
        root = deletion(root, caseId);
        file.saveCase(this);

    }

    private CaseNode deletion(CaseNode root, String caseId) {
        if (root == null) {
            return null;
        }

        if (caseId.compareTo(root.getId()) < 0) {
            root.setLeft(deletion(root.getLeft(), caseId));
        } else if (caseId.compareTo(root.getId()) > 0) {
            root.setRight(deletion(root.getRight(), caseId));
        } else {
            if (root.getLeft() == null && root.getRight() == null) {
                return null;
            }

            if (root.getLeft() == null) {
                return root.getRight();
            } else if (root.getRight() == null) {
                return root.getLeft();
            } else {
                CaseNode successor = findMin(root.getRight());
                root.setId(successor.getId());
                root.setInvestigator(successor.getInvestigator());
                root.setDateOpened(successor.getDateOpened());
                root.setTitle(successor.getTitle());
                root.setStatus(successor.getStatus());
                root.setRight(deletion(root.getRight(), successor.getId()));
            }

        }
        return root;
    }

    private CaseNode findMin(CaseNode root) {
        while (root.getLeft() != null) {
            root = root.getLeft();
        }
        return root;
    }

    public boolean updateStatus(int id, CaseStatus newStatus) {
        CaseNode temp = SearchById(root, id);

        if (temp != null) {
            temp.setStatus(newStatus);
            file.saveCase(this);
            return true;
        }

        return false;
    }

    // loaded case method for FileHandling of cases
    public void loadCase(String caseId, String investigator, String title, LocalDate dateOpened, CaseStatus status) {
        root = insertLoadCase(root, caseId, investigator, title, dateOpened, status);
    }

    private CaseNode insertLoadCase(CaseNode root, String caseId, String investigator, String title,
            LocalDate dateOpened, CaseStatus status) {
        if (root == null) {
            CaseNode node = new CaseNode(caseId, title, investigator, dateOpened);
            node.setStatus(status);
            return node;
        }

        if (caseId.compareTo(root.getId()) < 0) {
            root.setLeft(insertLoadCase(root.getLeft(), caseId, investigator, title, dateOpened, status));
        } else if (caseId.compareTo(root.getId()) > 0) {
            root.setRight(insertLoadCase(root.getRight(), caseId, investigator, title, dateOpened, status));

        }
        return root;
    }

}