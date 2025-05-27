package main;

import java.io.*;
import java.util.*;

import javax.annotation.processing.FilerException;

public class Extract {
    public static void ExtractProjects(Boolean verbose) {

        Boolean vanilla = null;

        while (vanilla == null) {
            System.out.println("Include vanilla? Y/N");
            String option = Main.scanner.next().toLowerCase(Locale.ROOT);

            switch (option) {
                case "y":
                    vanilla = true;
                    break;
                case "n":
                    vanilla = false;
                    break;
                default:
                    System.out.println("Unexpected command.");
                    break;
            }
        }

        long mark = System.currentTimeMillis();

        ExtractProjectData(vanilla, verbose);
        ExtractProjectSets(vanilla, verbose);

        Util.Recall(mark);
    }

    public static void ExtractProjectData(Boolean vanilla, Boolean verbose) {

        System.out.println("Extracting cached projects...");

        // count projects in animationdatasinglefile
        // for each, stream the file into a new file with the appropriate project name

        int numProjects;

        List<String> fileNames = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("meshes/animationdatasinglefile.txt"))) {
            // first line says number of projects to expect
            numProjects = Integer.parseInt(reader.readLine());

            System.out.println("Expecting " + numProjects + " projects.");

            // next lines are the names of the project files to print
            for (int i = 0; i < numProjects; i++) {
                String line = reader.readLine();
                if (verbose)
                    System.out.println(line);
                fileNames.add(line);
            }

            if (!vanilla)
                System.out.println("Skipping vanilla files...");

            for (String fileName : fileNames) {

                if (!vanilla) {

                    if (!Main.directoryAnims.contains(fileName.split("\\.")[0].toLowerCase())) {
                        continue;
                    }

                    return;
                }

                System.out.println("Extracting: " + fileName);
                File outFile = new File(Main.AnimDataOutput.toString() + "/" + fileName);

                if (!outFile.exists())
                    try {
                        outFile.createNewFile();
                        if (verbose)
                            System.out.println("Creating new cache file.");
                    } catch (IOException e) {
                        System.err.println("Could not write file to directory!");
                        e.printStackTrace();
                        return;
                    }

                String lnCountStr = reader.readLine();

                // this line should be theexpected length of the current project
                int lnCount = Integer.parseInt(lnCountStr);

                int lnWritten = 0;
                Boolean boundAnims = false;

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile,
                        false))) {
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

                    if (verbose)
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
                        if (verbose)
                            System.out.println("Expecting boundanims.");
                        boundAnims = true;
                    }

                    else if (verbose)
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
                    File boundAnimsFolder = new File(Main.AnimDataOutput.toString() + "/boundanims");

                    if (!boundAnimsFolder.exists())
                        boundAnimsFolder.mkdir();

                    if (verbose)
                        System.out.println("Creating new boundanims file...");

                    File boundAnimsFile = new File(
                            Main.AnimDataOutput.toString() + "/boundanims/anims_" + outFile.getName());

                    if (!boundAnimsFile.exists())
                        try {
                            boundAnimsFile.createNewFile();
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
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void ExtractProjectSets(Boolean vanilla, Boolean verbose) {

        String cachedLine = null;
        
        try (
            BufferedReader vanillaReader = new BufferedReader(new FileReader(Main.AnimSetCache));
            BufferedReader cacheReader = new BufferedReader(new FileReader("meshes/animationsetdatasinglefile.txt"));
        )
        {
            int numVanilla = Integer.parseInt(vanillaReader.readLine());
            int numCached = Integer.parseInt(cacheReader.readLine());

            int numProjects = numCached - numVanilla;

            for (String line; (line = cacheReader.readLine().toLowerCase()) != null;)
                {
                    if (line != vanillaReader.readLine().toLowerCase())
                    {
                        cachedLine = line;
                        System.out.println("Stopping at: " + cachedLine);
                        break;
                    }
                }

            List<String> listProjects = new ArrayList<>();

            //add in the last read line
            listProjects.add(cachedLine);

            //get the remaining projects in the cached file
            //this should also catch up the cached file to the vanilla file
            for (int i = 0; i < numProjects - 1; i++)
            {
                listProjects.add(cacheReader.readLine().toLowerCase());
            }

            for (String line; (line = cacheReader.readLine().toLowerCase()) != null;)
            {
                if (line != vanillaReader.readLine().toLowerCase())
                {
                    cachedLine = line;
                    System.out.println("Stopping at: " + line);
                    break;
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("Cache file missing!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Cannot read file!");
            e.printStackTrace();
        }
    }

    

    public static void ExtractProjectSetsOLD(Boolean vanilla, Boolean verbose) {

        // TIME FOR PART II

        int numSetProjects;
        List<String> projectNames = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("meshes/animationsetdatasinglefile.txt"))) {

            // first line says number of projects to expect
            numSetProjects = Integer.parseInt(reader.readLine());
            System.out.println("Expecting " + numSetProjects + " project sets.");

            // next lines are the names of the project files to print
            for (int i = 0; i < numSetProjects; i++) {
                String project = reader.readLine();
                // System.out.println("Found: " + project);
                projectNames.add(project);
            }

            String lastLastLine = reader.readLine(); // cache the number of projects beforehand
            String lastLine = reader.readLine(); // cache the first txt file

            System.out.println("Expected files for " + projectNames.get(0) + ": " + lastLastLine);
            System.out.println("First file in list: " + lastLine);

            //for each project
            for (String projectName : projectNames) {

                // check how many txts to expect
                int expectedFiles = Integer.parseInt(lastLastLine);

                // cache expected txt names
                List<String> exFileNames = new ArrayList<>();
                exFileNames.add(lastLine);

                if (expectedFiles == 0) {
                    System.err.println("Weird number of files, is your source file corrupt?");
                }

                if (expectedFiles > 1) {
                    for (int i = 0; i < expectedFiles; i++) {
                        exFileNames.add(reader.readLine());
                    }
                }

                for (int i = 0; i < expectedFiles; i++) {

                    List<String> lines = new ArrayList<>();

                    // expectedFiles is usually 1 if there are more than one expected files
                    // and we arent on the first iteration, i must be greater than 0
                    if (expectedFiles > 1 && i > 0) {
                        lines.add("V3"); // add missing V3
                    }

                    for (String line; (line = reader.readLine()) != null;) {

                        if (line.toLowerCase().contains("v3") && !lastLine.toLowerCase().contains(".txt")) {

                            lastLastLine = lastLine;
                            lastLine = line;

                            break;
                        }

                        if (line.toLowerCase().contains(".txt")) {
                            lines.remove(lines.size() - 1);

                            lastLastLine = lastLine;
                            lastLine = line;

                            break;
                        }

                        lines.add(line);

                        lastLastLine = lastLine;
                        lastLine = line;
                    }

                    // System.out.println("Testing project: " + projectName.toLowerCase());

                    if (!vanilla && Main.directoryAnimSets.contains(projectName.toLowerCase())) 
                        System.out.println("Skipping vanilla file: " + projectName.toLowerCase());

                    // only print if we want vanilla files or if the directory does not contain the project
                    if (vanilla || !Main.directoryAnimSets.contains(projectName.toLowerCase())) {

                        // make sure we can write to the outfolder
                        File outFolder = new File(
                                Main.AnimSetDataOutput.toString() + "/" + projectName.split("\\\\")[0]);
                        if (!outFolder.exists()) {
                            outFolder.mkdir();
                        }

                        System.out.println("Found project data: " + outFolder.getName());

                        // make expected file
                        String path = outFolder.getPath() + "/" + exFileNames.get(i);
                        if (verbose) System.out.println("Extracting: " + path);

                        File outFile = new File(path);
                        if (!outFile.exists()) {
                            try {
                                outFile.createNewFile();
                                if (verbose)
                                    System.out.println("Creating new cache file.");
                            } catch (IOException e) {
                                System.err.println("Could not write file to directory!");
                                e.printStackTrace();
                                return;
                            }
                        }

                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, false))) {

                            for (String line : lines) {
                                writer.append(line);
                                writer.newLine();
                            }
                        }
                    }

                    // reset in case we repeat
                    lines.clear();
                }
            }
        } catch (FileNotFoundException e1) {
            System.err.println("Could not find animationsetdatasinglefile.txt!");
            e1.printStackTrace();
            return;

        } catch (IOException e1) {
            System.err.println("Could not write to file!");
            e1.printStackTrace();
            return;
        }
    }
}
