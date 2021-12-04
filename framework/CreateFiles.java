package net.boigroup.bdd.framework;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class CreateFiles {

    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CreateFiles.class);
    public static void write(String p, String contant) {
        try{
            // Create new file
            String content = contant;
            String path = p + "/";
            File file = new File(path);

            // If file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            // Write in file
            bw.write(content);

            // Close connection
            bw.close();
        }
        catch(Exception e){
            LOGGER.info(e.getMessage());
        }
    }
}