import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class FileHandler {
    private final String FileName = "users.csv";
    private final String FolderName = "D:/data";
    private final Path path = Paths.get(FolderName, FileName);

    public void createFolder() {
        try {
            Path folderPath = Paths.get(FolderName);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
                System.out.println("folder created successfully");
            }
        } catch (IOException e) {
            System.out.println("Failed to create directory" + e.getMessage());
        }
    }

    public void loadUsers(HashTableService service) {
        createFolder();
        File file = path.toFile();

        if (!file.exists()) {
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    User user = new User(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim());
                    service.users.put(user.username, user);
                }
            }
            br.close();
        }

        catch (Exception e) {
            System.out.println("Error loading file!");
        }
    }

    public void saveAllUsers(HashMap<String, User> users) {
        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter(path.toFile()));
            for (User u : users.values()) {
                fw.write(u.username + "," + u.password + "," + u.role + "\n");
            }
            fw.close();
        }

        catch (Exception e) {
            System.out.println("Error saving file!");
        }
    }
}