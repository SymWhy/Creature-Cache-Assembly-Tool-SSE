package main;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Build {

    public static void AppendProjects(Boolean verbose) {

        long mark = System.currentTimeMillis();
        AssembleAnimationData(verbose);
        AssembleAnimationSetData(verbose);
        Util.Recall(mark);
    }

    public static void AssembleAnimationData(Boolean verbose) {

        int count = Main.countAnims;
        List<String> directory = Main.directoryAnims;
        // this gives us a headcount and a list of vanilla projects

        // get animation data folder and list all containing files
        File topFolder = Main.AnimDataOutput;

        // create a new text file, and a writer for said file
        File outFile = new File("meshes/animationdatasinglefile.txt");
        if (!outFile.exists())
            try {
                outFile.createNewFile();
                if (verbose)
                    System.out.println("Creating new cache file...");
            } catch (IOException e) {
                System.err.println("Could not write file to directory!");
                e.printStackTrace();
                return;
            }

        // put everything in an array except the dirlist.txt file
        FilenameFilter filter = (File f, String name) -> !name.startsWith("dirlist") && name.endsWith(".txt");

        // keep track of how many new projects
        int countNew = 0;
        List<File> filesNew = new ArrayList<>();

        if (directory.isEmpty()) {
            System.err.println("Folder " + topFolder.getName() + " appears to be missing files!");
            return;
        }

        // populate with the names of the files
        for (File file : topFolder.listFiles(filter)) {
            boolean matchFound = false;

            for (String d : Main.directoryAnims) {

                if (d.toLowerCase().equals(file.getName().toLowerCase())) {
                    matchFound = true;
                    break;
                }
            }

            if (matchFound == false) {
                filesNew.add(file);
                directory.add(file.getName());
                countNew++;
                count++;
                System.out.println("Found new file: " + file.getName());
            }
        }

        // make sure there actually is a project to cache
        if (countNew < 1) {
            System.out.println("Found no new projects!");
            return;
        }

        // let's start writing!
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, false))) {

            // append the number of files to the beginning of the outfile
            writer.append(Integer.toString(count));
            writer.newLine();
            System.out.println("Found " + count + " total projects.");

            // append file names
            for (String line : directory) {
                if (verbose)
                    System.out.println("Found: " + line);
                writer.append(line);
                writer.newLine();
            }

            System.out.println("Appending vanilla project data.");

            // append all the contents of the vanilla cache files
            // its a lot so we stream from file
            try (BufferedReader reader = new BufferedReader(new FileReader(Main.AnimCache))) {
                int i = 0;
                for (String line; (line = reader.readLine()) != null;) {
                    // skip the project count and file list
                    if (i > Main.directoryAnims.size() - 1) {
                        writer.append(line);
                        writer.newLine();
                    }
                    i++;
                }
            }

            // get each new file and append their content
            // topfolder.listfiles == all the individual text files
            for (File file : filesNew) {

                // count lines in the file
                int lnCount = Util.GetLinesInFile(file);

                if (lnCount == 0) {
                    System.err.println("File " + file.getName() + " is empty!");
                    return;
                }
                writer.append(Integer.toString(lnCount));
                writer.newLine();

                // append contents of the file
                System.out.println("Appending contents of: " + file.getName());
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    for (String line; (line = reader.readLine()) != null;) {
                        writer.append(line);
                        writer.newLine();
                    }
                }

                // check for boundanims and append those too
                if (verbose)
                    System.out.println("Checking for boundanims...");
                String path = topFolder.getPath() + "/boundanims/anims_" + file.getName();

                if (Files.exists(Paths.get(path))) {

                    File animFile = new File(path);

                    if (verbose)
                        System.out.println("Boundanims found! Appending contents of: " + animFile.getName());

                    int lnCountBounds = Util.GetLinesInFile(animFile);

                    if (lnCountBounds == 0) {
                        System.err.println("Boundanims file is empty!");
                        return;
                    }

                    writer.append(Integer.toString(lnCountBounds));
                    writer.newLine();

                    try (BufferedReader reader = new BufferedReader(new FileReader(animFile))) {
                        for (String line; (line = reader.readLine()) != null;) {
                            writer.append(line);
                            writer.newLine();
                        }
                    }
                } else {
                    if (verbose)
                        System.out.println("No boundanims found.");
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    public static void AssembleAnimationSetData(Boolean verbose) {

        // basically the same stuff as before
        int count = Main.countAnimSets;
        List<String> directory = Main.directoryAnimSets;

        File topFolder = Main.AnimSetDataOutput;
        FilenameFilter filter = (File f, String name) -> name.toLowerCase().contains("data") || !name.contains(".");
        // this should filter out everything that doesnt contain project data
        // maybe mention that files can't contain special characters?

        // keep track of how many new projects
        int countNew = 0;
        List<File> filesNew = new ArrayList<>();
        List<String> filePathsNew = new ArrayList<>();

        File outFile = new File("meshes/animationsetdatasinglefile.txt");
        if (!outFile.exists())
            try {
                outFile.createNewFile();
                if (verbose)
                    System.out.println("Creating new cache file...");
            } catch (IOException e) {
                System.err.println("Could not write file to directory!");
                e.printStackTrace();
                return;
            }

        if (directory.isEmpty()) {
            System.err.println("Directory appears to be missing files!");
            return;
        }

        // populate with the names of the files
        for (File folder : topFolder.listFiles(filter)) {
            boolean matchFound = false;

            // creatureprojectdata/creatureproject.txt
            String filePathName = (folder.getName().toLowerCase() + "\\" +
                    folder.getName().toLowerCase().replace("data", ".txt"));

            if (Main.directoryAnimSets.contains(filePathName.toLowerCase())) {
                matchFound = true;
                break;
            }

            if (matchFound == false) {
                filesNew.add(folder);
                filePathsNew.add(filePathName);
                countNew++;
                System.out.println("Found new project: " + filePathName);
            }
        }

        // make sure there actually is a project to cache
        if (countNew < 1) {
            System.out.println("Found no new projects!");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, false))) {

            // append the number of files to the beginning of the outfile
            writer.append(Integer.toString(count + countNew));
            writer.newLine();
            System.out.println("Found " + (count + countNew) + " total projects.");

            // append file names
            for (String line : directory) {
                if (verbose)
                    System.out.println("Found: " + line);
                writer.append(line);
                writer.newLine();
            }

            for (String path : filePathsNew) {
                if (verbose)
                    System.out.println("Found: " + path);
                writer.append(path);
                writer.newLine();
            }

            // write vanilla files
            // stream from vanilla animationsetdata cache reference
            System.out.println("Appending vanilla project set rdata.");
            try (BufferedReader reader = new BufferedReader(new FileReader(Main.AnimSetCache))) {
                int i = 0;
                for (String line; (line = reader.readLine()) != null;) {
                    // skip first n lines where n = directory size
                    // append everything else to outfile
                    if (i > directory.size() - 1) {
                        writer.append(line);
                        writer.newLine();
                    }

                    i++;
                }
            }

            // write custom files
            for (File folder : filesNew) {
                // get project file
                String projectName = folder.getName().split("data")[0];
                // takes the name of the folder, removes the word "data", excludes that file and
                // its contents
                
                System.out.println("Appending project: " + projectName);

                // filter out all files that do not match the project name, case insensitive
                FilenameFilter fileFilter = (File f,
                        String name) -> !name.toLowerCase().startsWith(projectName.toLowerCase());

                // list all the non-project-name files in the current project
                File[] projectFiles = folder.listFiles(fileFilter);

                // append # of project files
                writer.append(Integer.toString(projectFiles.length));
                writer.newLine();

                if (verbose)
                    System.out.println("Found " + Integer.toString(projectFiles.length) + " files in " + projectName);

                // for each of these files...
                for (File file : projectFiles) {

                    if (verbose)
                        System.out.println("Writing: " + file.getName());

                    // append file name
                    writer.append(file.getName());
                    writer.newLine();

                    // read project file
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        for (String line; (line = reader.readLine()) != null;) {
                            writer.append(line);
                            writer.newLine();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
            return;
        }
    }
}
