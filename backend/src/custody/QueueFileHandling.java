package custody;

import evidence.PRIORITY;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QueueFileHandling {
    private final String file = "transferQueue.csv";
    private final String folder = "data";
    private final Path path = Paths.get(folder,file);


public void createFolder()
{
    try{
        Path folderPath = Paths.get(folder);
        if(!Files.exists(folderPath))
        {
            Files.createDirectories(folderPath);
            System.out.println("Folder created successfully");
        } 
    }catch(IOException e)
    {
        System.out.println("Failed to create foler " + e.getMessage());
    }
}

public void saveQueue(CustodyQueue queue)
{
    try
    {
        BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()));
        
            for(QueueNode node : queue.displayQueue())
            {
                bw.write(
                    node.getEvidenceId() + "," +
                    node.getCaseId() + "," +
                    node.getSubmittedBy() + "," +
                    node.getPriority()
                );
                bw.newLine();
            }
            bw.close();
            System.out.println("File saved successfully");
        }
    catch(IOException e)
        {
            System.out.println("Failed to create file" + e.getMessage());
        }
}

public void loadQueue(CustodyQueue queue)
{
    createFolder();
     if(!Files.exists(path))
        {
            try{
            Files.createFile(path);
            }catch(IOException e)
            {
                System.out.println("Failed to create file" + e.getMessage());
            }
            return;
        }

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(path.toFile()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                
                String evidenceID = data[0];
                String caseId = data[1];
                String submittedBy = data[2];
                PRIORITY priority = PRIORITY.valueOf(data[3]);

                queue.loadNode(evidenceID, caseId, submittedBy, priority);
            }
            br.close();
            System.out.println("Custody Queue data has been loaded");
        }catch(IOException e)
        {
            System.out.println("Failed to load data" + e.getMessage());
        }
}

}