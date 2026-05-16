import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;

public class AuditFileHandling {
    private final String file = "history.csv";
    private final String folder = "data";
    private final Path path = Paths.get(folder, file);

    public void createFolder() {
        try {
            Path folderName = Paths.get(folder);
            if (!Files.exists(folderName)) {
                Files.createDirectories(folderName);
                System.out.println("folder created successfully");
            }
        } catch (IOException e) {
            System.out.println("failed to create folder" + e.getMessage());
        }
    }

    public void saveAudit(AuditStack stack) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()));
            for (LogEntry node : stack.viewLog()) {
                bw.write(
                        node.getTime() + "," +
                                node.getUsername() + "," +
                                node.getAction());

                bw.newLine();
            }
            bw.close();
            System.out.println("saved to file successfully");
        } catch (IOException e) {
            System.out.println("failed to save" + e.getMessage());
        }
    }

    public void loadAudit(AuditStack stack) {
        createFolder();
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                System.out.println("Failed to create file" + e.getMessage());
            }
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(path.toFile()));
            String line;

            ArrayList<String[]> temp = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                temp.add(line.split(","));
            }
            br.close();
            for (int i = temp.size() - 1; i >= 0; i--) {
                String[] data = temp.get(i);
                LocalTime time = LocalTime.parse(data[0]);
                String username = data[1];
                String action = data[2];

                stack.loadEntry(username, action, time);
            }
        } catch (IOException e) {
            System.out.println("failed to load data" + e.getMessage());
        }
    }

}
