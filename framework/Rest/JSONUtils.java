package net.boigroup.bdd.framework.Rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JSONUtils {
    public static String readJson(String s) {
        String file = new File("src/main/resources/InputJson/").getAbsoluteFile().toString() + "/" + s;
        String allLine = "";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                allLine += sCurrentLine;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return allLine;
    }
}
