import java.time.LocalDate;

public class CaseNode {
    private String id;
    private String title;
    private String Investigator;
    private CaseStatus status;
    private LocalDate dateOpened;
    private CaseNode left;
    private CaseNode right;

    public CaseNode(String id,String title,String Investigator,LocalDate dateOpened)
    {
        this.id = id;
        this.title = title;
        this.Investigator = Investigator;
        this.status = CaseStatus.OPENED;
        this.dateOpened = dateOpened;
        left = null;
        right = null;
    }

    //Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setInvestigator(String investigator) {
        Investigator = investigator;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public void setDateOpened(LocalDate dateOpened) {
        this.dateOpened = dateOpened;
    }

    public void setLeft(CaseNode left) {
        this.left = left;
    }

    public void setRight(CaseNode right) {
        this.right = right;
    }

    //Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getInvestigator() {
        return Investigator;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public LocalDate getDateOpened() {
        return dateOpened;
    }

    public CaseNode getLeft() {
        return left;
    }

    public CaseNode getRight() {
        return right;
    }

    @Override
    public String toString()
    {
        return String.format("Case Id: %s Title: %s Investigator: %s Status: %s Date Opened: %s",id,title,Investigator,status,dateOpened);
    }

}
