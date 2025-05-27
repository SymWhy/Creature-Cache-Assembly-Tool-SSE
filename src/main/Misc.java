package main;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Misc {
    public static void Reset() {
        System.out.println("Resetting cache files...");
        try {
            Files.copy(Main.AnimCache.toPath(), new File("meshes/animationdatasinglefile.txt").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            System.err.println("Could not copy file!");
            e.printStackTrace();
            return;
        }

        try {
            Files.copy(Main.AnimSetCache.toPath(), new File("meshes/animationsetdatasinglefile.txt").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Could not copy file!");
            e.printStackTrace();
            return;
        }
    }

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
                    directory.add(line.toLowerCase());
                } else {
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }

        return directory;
    }

    // produce a new dirlist for debugging purposes
    public static void PrintDirectories() {

        long mark = System.currentTimeMillis();
        
        System.out.println("Printing new dirlist.txt");

        File dirlistAnims = new File(Main.AnimDataOutput.toString() + "/dirlist.txt");
        if (!dirlistAnims.exists())
            try {
                dirlistAnims.createNewFile();
            } catch (IOException e) {
                System.err.println("Unable to create dirlist!");
                e.printStackTrace();
                return;
            }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dirlistAnims,
                false))) {
            for (String name : Main.directoryAnims) {
                writer.append(name);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Unable to write to file!");
            e.printStackTrace();
            return;
        }

        File dirlistAnimSets = new File(Main.AnimSetDataOutput.toString() + "/dirlist.txt");
        if (!dirlistAnimSets.exists())
            try {
                dirlistAnimSets.createNewFile();
            } catch (IOException e) {
                System.err.println("Unable to create dirlist!");
                e.printStackTrace();
                return;
            }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dirlistAnimSets,
                false))) {
            for (String name : Main.directoryAnims) {
                writer.append(name);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Unable to write to file!");
            e.printStackTrace();
            return;
        }

        Util.Recall(mark);
    }
}
