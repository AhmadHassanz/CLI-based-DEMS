package custody;

import evidence.PRIORITY;

public class QueueNode {
    private String evidenceId;
    private String caseId;
    private String submittedBy;
    private PRIORITY priority;
    private QueueNode next;
    public QueueNode(String evidenceId,String caseId,String submittedBy,PRIORITY priority)
    {
        this.evidenceId = evidenceId;
        this.caseId = caseId;
        this.submittedBy = submittedBy;
        this.priority = priority;
        next = null;
    }

    //Setters
    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }
    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }
    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }
    public void setPriority(PRIORITY priority) {
        this.priority = priority;
    }
    public void setNext(QueueNode next) {
        this.next = next;
    }

    //Getters
    public String getEvidenceId() {
        return evidenceId;
    }
    public String getCaseId() {
        return caseId;
    }
    public String getSubmittedBy() {
        return submittedBy;
    }
    public PRIORITY getPriority() {
        return priority;
    }
    public QueueNode getNext() {
        return next;
    }

    @Override
    public String toString()
    {
        return String.format("EVI-Id: %s C-Id: %s By: %s PRIORITY: %s",evidenceId,caseId,submittedBy,priority);
    } 
}
