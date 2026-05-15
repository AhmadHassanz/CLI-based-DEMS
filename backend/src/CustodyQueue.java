import java.util.ArrayList;

public class CustodyQueue {
    private QueueNode front;
    private QueueNode back;
    int size;
    
    public CustodyQueue()
    {
        front = back = null;
        size = 0;
    }

    //Setters
    public void setFront(QueueNode front) {
        this.front = front;
    }

    public void setBack(QueueNode back) {
        this.back = back;
    }

    //Getters
    public QueueNode getFront() {
        return front;
    }

     public QueueNode getBack() {
        return front;
    }

    public void enqueue(int eviId,int CId,String submittedBy,PRIORITY priority)
    {
        String evidenceId = String.format("EV-%03d",eviId);
        String caseId = String.format("C-%03d",eviId);
        QueueNode node = new QueueNode(evidenceId, caseId, submittedBy, priority);

        if(back == null)
        {
            front = back = node;
        }else
        {
            back.setNext(node);
            back = node;
        }
        size++;
    }

    public QueueNode dequeue()
    {
        if(front == null)
        {
            return null;
        }

        QueueNode temp = front;
        front = front.getNext();

        if(front == null)
        {
            back = null;
        }

        size--;

        return temp;
    }

    public QueueNode peek()
    {
        if(front == null)
        {
            return null;
        }

        return front;
    }

    public ArrayList<QueueNode> displayQueue()
    {
        ArrayList<QueueNode> list = new ArrayList<>();

        QueueNode temp = front;

        while (temp != null) {
            list.add(temp);
            temp = temp.getNext();            
        }
        return list;
    }
    
}
