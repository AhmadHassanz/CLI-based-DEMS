import java.time.LocalTime;

public class LogEntry {
    private LocalTime time;
    private String username;
    private String action;
    private LogEntry next;

    public LogEntry(LocalTime time,String username,String action)
    {
        this.time = time;
        this.username = username;
        this.action = action;
        next = null;
    }
    //Getters
    public LocalTime getTime() {
        return time;
    }
    public String getUsername() {
        return username;
    }
    public String getAction() {
        return action;
    }
    public LogEntry getNext() {
        return next;
    }

    //Setters
    public void setTime(LocalTime time) {
        this.time = time;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public void setNext(LogEntry next) {
        this.next = next;
    }

    @Override
    public String toString()
    {
        return String.format("%s |  %s | %s ",time,username,action);
    }

    
}
