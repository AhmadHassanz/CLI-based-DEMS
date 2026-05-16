package audit;

import java.time.LocalTime;
import java.util.ArrayList;

public class AuditStack {
    private LogEntry top;
    private int size;
    private AuditFileHandling file;

    public AuditStack()
    {
        top = null;
        size = 0;
        file = new AuditFileHandling();
        file.loadAudit(this);
    }

    public void push(String username,String action)
    {
        LocalTime time = LocalTime.now().withNano(0);
        LogEntry entry = new LogEntry(time, username, action);
        entry.setNext(top);
        top = entry;
        size++;
        file.saveAudit(this);
    }

    public LogEntry pop()
    {
        if(top == null)
        {
            return null;
        }else
        {
            LogEntry temp = top;
            top = top.getNext();
            size--;
            file.saveAudit(this);
            return temp;
        }
    }

    public LogEntry peek()
    {
        return top;
    }

    public ArrayList<LogEntry> recentlog(int count)
    {
        ArrayList<LogEntry> list = new ArrayList<>();
        LogEntry temp = top;
        int i = 0;
        while (temp != null && i < count) {
            list.add(temp);
            temp = temp.getNext();
            i++;
        }

        return list;
    }

    public ArrayList<LogEntry> viewLog()
    {
        ArrayList<LogEntry> list = new ArrayList<>();
        LogEntry temp = top;
        while (temp != null) {
            list.add(temp);
            temp = temp.getNext();
        }
        return list;
    }

    public int getSize()
    {
        return size;
    }

       public void loadEntry(String username,String action,LocalTime time)
    {
        LogEntry entry = new LogEntry(time, username, action);
        entry.setNext(top);
        top = entry;
        size++;
    }


}
