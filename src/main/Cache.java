package main;

import java.io.*;
import java.util.*;

public class Cache {
    public static int ParseCount(File file) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            for (String line; (line = reader.readLine()) != null;) {
                if (count == 0) {
                    System.out.println("Reading " + line + " projects.");
                    count = Integer.parseInt(line);
                    break;
                } else {
                    System.err.println("Weird error, this shouldn't be printed.");
                    break;
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }

        return count;
    }

    public static List<String> ParseDirectory(File file) {
        List<String> directory = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader((new FileReader(file)))) {
            reader.readLine(); // skip first line

            for (String line; (line = reader.readLine()) != null;) {
                if (line.contains(".txt")) {
                    System.out.println("Reading: " + line);
                    directory.add(line);
                } else {
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }

        return directory;
    }
}
