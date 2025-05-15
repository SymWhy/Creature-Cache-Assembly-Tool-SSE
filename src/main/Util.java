package main;

import java.io.*;

public class Util {
    public static int GetLinesInFile(File file) {
        int i = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            for (; (reader.readLine()) != null;) {
                i++;
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return i;
    }
}
