import java.time.LocalDate;
import java.util.ArrayList;

public class EvidenceList {
    private EvidenceNode head;
    private EvidenceNode tail;
    int size = 0;
    private FileHandling file;

    public EvidenceList() {
        head = null;
        tail = null;
        size = 0;
        file = new FileHandling();
        file.loadEvidence(this);
    }

    // setter
    public void setHead(EvidenceNode head) {
        this.head = head;
    }

    public void setTail(EvidenceNode tail) {
        this.tail = tail;
    }

    // getters
    public EvidenceNode getHead() {
        return head;
    }

    public EvidenceNode getTail() {
        return tail;
    }

    public EvidenceNode addEvidence(int id, int num, String description, String submittedBy,
            LocalDate dateAdded) {

        if (searchById(id) != null) {
            throw new IllegalArgumentException("Evidence with this id already exists");
        }
        // Id generators
        String caseId = String.format("C-%03d", num);
        String evidenceId = String.format("EV-%03d", id);

        EvidenceNode newNode = new EvidenceNode(evidenceId, caseId, description, STATUS.PENDING, submittedBy,
                dateAdded);

        if (head == null) {
            head = tail = newNode;
        } else {
            tail.setNext(newNode);
            newNode.setPrev(tail);
            tail = newNode;
        }
        size++;
        file.saveEvidence(this);
        return newNode;
    }

    public boolean deleteEvidenceById(int id) {
        String evidenceId = String.format("EV-%03d", id);
        EvidenceNode temp = head;
        while (temp != null && !evidenceId.equals(temp.getEvidenceId())) {
            temp = temp.getNext();
        }

        if (temp == null) {
            return false;
        } else if (temp == head && temp == tail) {
            head = tail = null;
        } else if (temp == head) {
            head = head.getNext();
            head.setPrev(null);

        } else if (temp == tail) {
            tail = tail.getPrev();
            tail.setNext(null);

        } else {
            temp.getPrev().setNext(temp.getNext());
            temp.getNext().setPrev((temp.getPrev()));

        }
        size--;
        file.saveEvidence(this);
        return true;
    }

    public ArrayList<EvidenceNode> displayForward() {
        ArrayList<EvidenceNode> list = new ArrayList<>();

        EvidenceNode temp = head;
        while (temp != null) {
            list.add(temp);
            temp = temp.getNext();
        }
        return list;
    }

    public ArrayList<EvidenceNode> displayReverse() {
        ArrayList<EvidenceNode> list = new ArrayList<>();

        EvidenceNode temp = tail;
        while (temp != null) {
            list.add(temp);
            temp = temp.getPrev();
        }
        return list;
    }

    public EvidenceNode searchById(int id) {
        String evidenceId = String.format("EV-%03d", id);
        EvidenceNode temp = head;
        while (temp != null && !evidenceId.equals(temp.getEvidenceId())) {
            temp = temp.getNext();
        }

        return temp;
    }

    public ArrayList<EvidenceNode> searchByCaseId(int id) {
        String caseId = String.format("C-%03d", id);
        EvidenceNode temp = head;
        ArrayList<EvidenceNode> list = new ArrayList<>();
        while (temp != null) {
            if (caseId.equals(temp.getCaseId())) {
                list.add(temp);
            }
            temp = temp.getNext();
        }

        return list;
    }

    public boolean changeStatus(int id, STATUS newStatus) {
        EvidenceNode found = searchById(id);
        if (found != null) {
            found.setStatus(newStatus);
            file.saveEvidence(this);
            return true;
        }

        return false;
    }

}
