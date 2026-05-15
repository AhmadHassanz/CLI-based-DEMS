import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class CaseFileHandling {
    private final String file = "cases.csv";
    private final String folder = "data";
    private final Path path = Paths.get(folder,file);

    public void createFolder()
    {
        try
        {
            Path folderpath = Paths.get(folder);
            if(!Files.exists(folderpath))
            {
                Files.createDirectories(folderpath);
                System.out.println("folder created successfully");
            }
        }catch(IOException e)
        {
            System.out.println("Failed to create folder" + e.getMessage());
        }
    }

    public void saveCase(CaseBST cases)
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()));
            for(CaseNode node : cases.SortCases())
            {
                bw.write(
                    node.getId() + "," +
                    node.getTitle() + "," +
                    node.getInvestigator() + "," +
                    node.getStatus() + "," +
                    node.getDateOpened()                    
                );
                bw.newLine();
            }
            bw.close();
            System.out.println("Saved successfully");
        }
        catch(IOException e)
        {
            System.out.println("Failed to save file" + e.getMessage());
        }
    }

    public void loadCases(CaseBST cases)
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
                
                String caseId = data[0];
                String title = data[1];
                String Investigator = data[2];
                CaseStatus status = CaseStatus.valueOf(data[3]);
                LocalDate dateOpened = LocalDate.parse(data[4]);

                cases.loadCase(caseId, Investigator, title, dateOpened, status);
            }
         br.close();   
        }catch(IOException e)
        {
            System.out.println("failed to load cases" + e.getMessage());
        }
        
    }
}
