import java.time.LocalDate;

public class EvidenceNode {

    private String evidenceId;
    private String caseId;
    private String description;
    private STATUS status;
    private String submittedBy;
    private LocalDate dateAdded;
    private EvidenceNode next;
    private EvidenceNode prev;

    public EvidenceNode(String evidenceId, String caseId, String description, STATUS status, String submittedBy,LocalDate dateAdded) {
        this.evidenceId = evidenceId;
        this.caseId = caseId;
        this.description = description;
        this.status = status;
        this.submittedBy = submittedBy;
        this.dateAdded = dateAdded;
        next = null;
        prev = null;
    }

    // setters
    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setNext(EvidenceNode next) {
        this.next = next;
    }

    public void setPrev(EvidenceNode prev) {
        this.prev = prev;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded = dateAdded;
    }

    // Getters
    public String getCaseId() {
        return caseId;
    }

    public String getDescription() {
        return description;
    }

    public STATUS getStatus() {
        return status;
    }

    public EvidenceNode getNext() {
        return next;
    }

    public EvidenceNode getPrev() {
        return prev;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    //to String
    @Override
    public String toString()
    {
        return String.format("Evidence Id: %s Case Id: %s Description: %s Status: %s Submitted By: %s  Date: %s",evidenceId,caseId,description,status,submittedBy,dateAdded);
    }
 
}