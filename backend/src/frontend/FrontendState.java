package frontend;

import audit.AuditStack;
import cases.CaseBST;
import custody.CustodyQueue;
import evidence.EvidenceList;
import users.HashTableService;
import users.User;

public final class FrontendState {
    public static final HashTableService userService = new HashTableService();
    public static final EvidenceList evidenceList = new EvidenceList();
    public static final CaseBST caseBST = new CaseBST();
    public static final CustodyQueue custodyQueue = new CustodyQueue();
    public static final AuditStack auditStack = new AuditStack();

    private static User currentUser;

    private FrontendState() {
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}
