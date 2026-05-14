import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class FileHandling {
    private final String FileName = "evidence.csv";
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

    public void saveEvidence(EvidenceList evidenceList) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()));

            for (EvidenceNode node : evidenceList.displayForward()) {
                writer.write(
                        node.getEvidenceId() + "," +
                                node.getCaseId() + "," +
                                node.getDescription() + "," +
                                node.getStatus() + "," +
                                node.getSubmittedBy() + "," +
                                node.getDateAdded());

                writer.newLine();
            }
            writer.close();
            System.out.println("Evidence saved successfully");
        }

        catch (IOException e) {
            System.out.println("Error while saving file" + e.getMessage());
        }

    }

    public void loadEvidence(EvidenceList evidenceList) {
        // if file and folder not found then it will be created
        createFolder();
        if (!Files.exists(path)) {
            System.out.println("No evidence file found");

            try {
                Files.createFile(path);
            } catch (IOException e) {
                System.out.println("Failed to create file" + e.getMessage());
            }
            return;

        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                String evidenceId = data[0];
                String caseId = data[1];
                String description = data[2];
                STATUS status = STATUS.valueOf(data[3]);
                String submittedBy = data[4];
                LocalDate dataAdded = LocalDate.parse(data[5]);

                EvidenceNode newNode = new EvidenceNode(evidenceId, caseId, description, status, submittedBy,
                        dataAdded);

                if (evidenceList.getHead() == null) {
                    evidenceList.setHead(newNode);
                    evidenceList.setTail(newNode);
                } else {
                    evidenceList.getTail().setNext(newNode);
                    newNode.setPrev(evidenceList.getTail());
                    evidenceList.setTail(newNode);
                }
                evidenceList.size++;

            }

            reader.close();
            System.out.println("Data Loaded SuccessFully");

        }

        catch (IOException e) {
            System.out.println("No file found" + e.getMessage());
        }
    }
}
