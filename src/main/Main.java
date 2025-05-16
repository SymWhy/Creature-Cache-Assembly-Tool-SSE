package main;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    // set filepaths
    public static File AnimDataOutput = new File("meshes/animationdata");
    public static File AnimSetDataOutput = new File("meshes/animationsetdata");

    public static File AnimCache = new File("src/main/resources/vanilla_files/animationdatasinglefile.txt");
    public static File AnimSetCache = new File("src/main/resources/vanilla_files/animationsetdatasinglefile.txt");

    public static int countAnims;
    public static int countAnimSets;
    public static List<String> directoryAnims;
    public static List<String> directoryAnimSets;

    public static void main(String[] args) {

        System.out.println("Parsing AnimationData.");
        countAnims = Cache.ParseCount(AnimCache);
        directoryAnims = Cache.ParseDirectory(AnimCache);

        System.out.println("Parsing AnimationSetData.");
        countAnimSets = Cache.ParseCount(AnimSetCache);
        directoryAnimSets = Cache.ParseDirectory(AnimSetCache);

        while (true) {
            Scanner input = new Scanner(System.in);

            // make sure we're in the right folder
            if (!AnimDataOutput.exists() || !AnimSetDataOutput.exists()) {
                System.err.println("Couldn't find animation files.");
                break;
            }

            System.out.println("Type A to build AnimationData cache.");
            System.out.println("Type S to build AnimationSetData cache.");
            System.out.println("Type E to extract existing project data.");
            System.out.println("Type Q to quit.");

            while (true) {
                String option = input.next().toLowerCase(Locale.ROOT);

                switch (option) {
                    case "a":
                        AssembleAnimationData();
                        break;
                    case "s":
                        AssembleAnimationSetData();
                        break;
                    case "e":
                        ExtractProjects();
                        break;
                    case "q":
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Unexpected command.");
                        break;
                }
            }
        }

        System.exit(0);
    }

    public static void AssembleAnimationData() {

        long tStart = System.currentTimeMillis();

        System.out.println("Building cache...");

        int count = countAnims;
        List<String> directory = directoryAnims;
        // this gives us a headcount and a list of vanilla projects

        // get animation data folder and list all containing files
        File topFolder = AnimDataOutput;

        // create a new text file, and a writer for said file
        File outFile = new File("meshes/animationdatasinglefile.txt");
        if (!outFile.exists())
            try {
                outFile.createNewFile();
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

            for (String d : directoryAnims) {

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
                System.out.println("Writing: " + line);
                writer.append(line);
                writer.newLine();
            }

            System.out.println("Appending vanilla project data.");

            // append all the contents of the vanilla cache files
            // its a lot so we stream from file
            try (BufferedReader reader = new BufferedReader(new FileReader(AnimCache))) {
                int i = 0;
                for (String line; (line = reader.readLine()) != null;) {
                    // skip the project count and file list
                    if (i > directoryAnims.size() - 1) {
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
                System.out.println("Checking for boundanims...");
                String path = topFolder.getPath() + "/boundanims/anims_" + file.getName();
                System.out.println("Looking for: " + path);

                if (Files.exists(Paths.get(path))) {

                    File animFile = new File(path);

                    System.out.println("Boundanims found! Appending contents of: " + animFile.getName());

                    int lnCountBounds = Util.GetLinesInFile(file);

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
                    System.out.println("No boundanims found.");
                }
            }

            long tFinish = System.currentTimeMillis();
            double tElapsed = (tFinish - tStart) * 0.001;
            System.out.println("Program completed in: " + Double.toString(tElapsed) + " seconds.");
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    public static void AssembleAnimationSetData() {

        long tStart = System.currentTimeMillis();
        System.out.println("Building cache...");

        // basically the same stuff as before
        int count = countAnimSets;
        List<String> directory = directoryAnimSets;

        File topFolder = AnimSetDataOutput;
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
            String filePathName = (folder.getName() + "/" + folder.getName().replace("data", ".txt"));

            filePathName = filePathName.replace("/", "\\");

            for (String d : directory) {

                // System.out.println("Comparing: " + filePathName.toLowerCase() + ";" +
                // d.toLowerCase());

                if (d.toLowerCase().equals(filePathName.toLowerCase())) {
                    matchFound = true;
                    break;
                }
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
                System.out.println("Writing: " + line);
                writer.append(line);
                writer.newLine();
            }

            for (String path : filePathsNew) {
                System.out.println("Writing: " + path);
                writer.append(path);
                writer.newLine();
            }

            // write vanilla files
            // stream from vanilla animationsetdata cache reference
            System.out.println("Writing vanilla cache.");
            try (BufferedReader reader = new BufferedReader(new FileReader(AnimSetCache))) {
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
                System.out.println("Found project: " + projectName);

                // filter out all files that do not match the project name, case insensitive
                FilenameFilter fileFilter = (File f,
                        String name) -> !name.toLowerCase().startsWith(projectName.toLowerCase());

                // list all the non-project-name files in the current project
                File[] projectFiles = folder.listFiles(fileFilter);

                // append # of project files
                writer.append(Integer.toString(projectFiles.length));
                writer.newLine();

                System.out.println("Found " + Integer.toString(projectFiles.length) + " files in " + projectName);

                // for each of these files...
                for (File file : projectFiles) {

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

        long tFinish = System.currentTimeMillis();
        double tElapsed = tFinish - tStart * 0.001;
        System.out.println("Program completed in: " + Double.toString(tElapsed) + " seconds.");
    }

    public static void ExtractProjects() {
        long tStart = System.currentTimeMillis();
        System.out.println("Extracting cached projects...");

        // count projects in animationdatasinglefile
        // for each, stream the file into a new file with the appropriate project name

        int numProjects;
        int numSetProjects;

        List<String> fileNames = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("meshes/animationdatasinglefile.txt"))) {
            // first line says number of projects to expect
            numProjects = Integer.parseInt(reader.readLine());

            System.out.println("Expecting " + numProjects + " projects.");

            // next lines are the names of the project files to print
            for (int i = 0; i < numProjects; i++) {
                fileNames.add(reader.readLine());
            }

            Boolean newFile = true;
            int projectIndex = 0;

            for (String fileName : fileNames) {
                System.out.println("Extracting: " + fileName);
                File outFile = new File(AnimDataOutput.toString() + "/" + fileName);

                if (!outFile.exists())
                    try {
                        outFile.createNewFile();
                        System.out.println("Creating new cache file.");
                    } catch (IOException e) {
                        System.err.println("Could not write file to directory!");
                        e.printStackTrace();
                        return;
                    }

                String lnCountStr = reader.readLine();
                int lnCount = Integer.parseInt(lnCountStr); // this line should be the expected length of the current
                                                            // project
                int lnWritten = 0;
                Boolean boundAnims = false;

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, false))) {
                    // we do not write the line count!!
                    // write the first line ("1")
                    writer.append(reader.readLine());
                    writer.newLine();
                    lnWritten++;

                    // write the hkx count
                    String hkxCount = reader.readLine();
                    writer.append(hkxCount);
                    writer.newLine();
                    lnWritten++;

                    System.out.println("Project has " + hkxCount + " hkx files.");

                    // write the relevant hkx files
                    for (int i = 0; i < Integer.parseInt(hkxCount); i++) {
                        writer.append(reader.readLine());
                        writer.newLine();
                        lnWritten++;
                    }

                    // should I expect boundanims?
                    // check if this line is a 0 or 1
                    String boundAnimsBool = reader.readLine();
                    boundAnims = false;

                    if (Integer.parseInt(boundAnimsBool) == 1) {
                        System.out.println("Expecting boundanims.");
                        boundAnims = true;
                    }

                    else
                        System.out.println("Not expecting boundanims.");

                    writer.append(boundAnimsBool);
                    writer.newLine();
                    lnWritten++;

                    for (int i = 0; i < lnCount - lnWritten; i++) {
                        String line = reader.readLine();
                        if (line.equals(""))
                            writer.newLine();
                        else {
                            writer.append(line);
                            writer.newLine();
                        }
                    }
                }

                // write the boundanims if they exist
                if (boundAnims) {
                    File boundAnimsFolder = new File(AnimDataOutput.toString() + "/boundanims");

                    if (!boundAnimsFolder.exists())
                        boundAnimsFolder.mkdir();

                    System.out.println("Creating new boundanims file...");

                    File boundAnimsFile = new File(
                            AnimDataOutput.toString() + "/boundanims/anims_" + outFile.getName());

                    if (!boundAnimsFile.exists())
                        try {
                            boundAnimsFile.createNewFile();
                            System.out.println("Creating new boundanims file...");
                        } catch (IOException e) {
                            System.err.println("Could not write file to directory!");
                            e.printStackTrace();
                            return;
                        }

                    System.out.println("Extracting: " + boundAnimsFile.getName());
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(boundAnimsFile, false))) {
                        String lnCountBoundStr = reader.readLine();
                        int lnCountBound = Integer.parseInt(lnCountBoundStr);

                        // we do not write the line count!!

                        for (int i = 0; i < lnCountBound; i++) {
                            String line = reader.readLine();
                            if (line.equals(""))
                                writer.newLine();
                            else {
                                writer.append(line);
                                writer.newLine();
                            }
                        }
                    }
                }
            }

            // producing a new dirlist for debugging purposes
            // System.out.println("Printing new dirlist.txt");

            // File dirlist = new File(AnimDataOutput.toString() + "/dirlist.txt");
            // if (!dirlist.exists())
            //     dirlist.createNewFile();

            // try (BufferedWriter writer = new BufferedWriter(new FileWriter(dirlist, false))) {
            //     for (String name : fileNames) {
            //         writer.append(name);
            //         writer.newLine();
            //     }
            // }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        long tFinish = System.currentTimeMillis();
        double tElapsed = tFinish - tStart * 0.001;
        System.out.println("Program completed in: " + Double.toString(tElapsed) + " seconds.");
    }
}
