import java.util.HashMap;
public class HashTableService {

    HashMap<String, User> users = new HashMap<>();
    private FileHandler file;

    public HashTableService() {
        file = new FileHandler();
        file.loadUsers(this);
        ensureDefaultAdmin();
    }

    public void ensureDefaultAdmin() {
        if (users.isEmpty()) {
            System.out.println("System: No accounts found. Initializing default admin...");
            
            User defaultAdmin = new User("admin", "admin123", "Admin");
            
            users.put(defaultAdmin.username, defaultAdmin);
            file.saveAllUsers(users);
            
            System.out.println("System: Default account created (admin / admin123).");
        }
    }

    public void addUser(User user) {
        if (users.containsKey(user.username)) {
            return;
        }
        users.put(user.username, user);
        file.saveAllUsers(users);
    }

    public User login(String username, String password) {
        if (users.containsKey(username)) {
            User u = users.get(username);
            if (u.password.equals(password)) {
                return u;
            }
        }
        return null;
    }

    public void deleteUser(String username) {
        users.remove(username);
        file.saveAllUsers(users);
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public void showAllUsers() {
        System.out.println("\n===== USERS =====");
        for (User u : users.values()) {
            System.out.println(u.username + " | " + u.role);
        }
    }

    public void updateUser(String oldU, String newU, String newP) {
        if (users.containsKey(oldU)) {
            User u = users.get(oldU);
            users.remove(oldU);
            u.username = newU;
            u.password = newP;
            users.put(newU, u);

            file.saveAllUsers(users);

            System.out.println("User Updated!");
        }
        else {

            System.out.println("User not found!");
        }
    }
}