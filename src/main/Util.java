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

    public static void Recall(long tStart)
    {
        long tFinish = System.currentTimeMillis();
        double tElapsed = tFinish - tStart * 0.001;
        System.out.println("Program completed in: " + Double.toString(tElapsed) + " seconds.");
    }
}
