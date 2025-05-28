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
            System.err.println("Could not find animationdatasinglefile.txt!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not write to file!");
            e.printStackTrace();
        }
    }

    public static void ExtractProjectSets(Boolean vanilla, Boolean verbose) {
        
        try (
            BufferedReader vanillaReader = new BufferedReader(new FileReader(Main.AnimSetCache));
            BufferedReader cacheReader = new BufferedReader(new FileReader("meshes/animationsetdatasinglefile.txt"));
        )
        {
            int numVanilla = Integer.parseInt(vanillaReader.readLine());
            System.out.println("Expecting " + numVanilla + " vanilla projects.");
            int numTotal = Integer.parseInt(cacheReader.readLine());

            int numProjects = numTotal - numVanilla;
            System.out.println("Expecting " + numProjects + " new projects.");

            if (numProjects == 0 && !vanilla)
            {
                System.out.println("No new projects expected!");
                return;
            }

            List<String> listOfProjects = new ArrayList<>();

            // read list of vanilla projects
            for (int i = 0; i < numVanilla; i++) {
                vanillaReader.readLine();
                String cachedLine = cacheReader.readLine();

                if (vanilla)
                {
                    System.out.println("Found: " + cachedLine);
                    listOfProjects.add(cachedLine);
                }
            }
            
            //read list of new projects
            for (int i = 0; i < numProjects; i++)
            {
                String cachedLine = cacheReader.readLine();
                System.out.println("Found: " + cachedLine);
                listOfProjects.add(cachedLine);
            }

            if (!vanilla) {
                // read through the vanilla file all the way to the end
                // also read through the cache file
                for (String line; (line = vanillaReader.readLine()) != null;) {
                    cacheReader.readLine();
                }
                // the next line should be the file count for our first new project!
            }

            //set up expected file count and first expected file name
            String numFiles = cacheReader.readLine();
            String fileName = cacheReader.readLine();

            //for each project {
            for (String project : listOfProjects)  {
                
                //FIRST we want to setup our working outFolder
                String folderPath = Main.AnimSetDataOutput.toString() + "/" + project.split("\\\\")[0];
                File outFolder = new File(folderPath);
                if (!outFolder.exists()) {
                    outFolder.mkdir();
                }

                System.out.println("Printing to: " + outFolder.getPath());

                //# files to expect should be numFiles
                //first file name should be fileName
                List<String> expectedFiles = new ArrayList<>();
                expectedFiles.add(fileName);
                System.out.println("Expecting: " + expectedFiles.get(0));

                for (int i = 0; i < Integer.parseInt(numFiles) - 1; i++) // subtract 1 because we already added the first one
                {
                    //if there is only one file, its already been added so break
                    if (Integer.parseInt(numFiles) - 1 == 0) break;

                    expectedFiles.add(cacheReader.readLine());
                    System.out.println("Expecting: " + expectedFiles.get(i + 1));
                }
                //now we have a list of file names, and count of files to expect
                //we also know the next file is going to be a "V3", so we'll toss this one.
                cacheReader.readLine();

                //for each file in expectedFiles {
                for (String f : expectedFiles) {
                    Boolean toNext = false;

                    String path = outFolder + "/" + f;

                    // set up current working file
                    File outFile = new File(path);
                    if (!outFile.exists()) {
                        outFile.createNewFile();
                    }

                    // set up a list that contains all the lines we want to write
                    List<String> linesInFile = new ArrayList<>();

                    // add that missing "V3" back in.
                    linesInFile.add("V3");

                    String prevLine = null;
                    // for each line we see
                    for (String line; (line = cacheReader.readLine()) != null;) {
                        // first check that it does not contain "V3" (at the header of every cache file)
                        // this means we are starting to write another file
                        if (line.equals("V3")) {
                            break;
                        }

                        // then check if the line does not contain ".txt"
                        if (line.contains(".txt")) {
                            numFiles = prevLine;
                            fileName = line;

                            // remove the last line added
                            linesInFile.remove(linesInFile.size() - 1);

                            // break out of both if statements
                            toNext = true;
                            break;
                        }

                        // add current line to list of lines (List<String> linesInFile)
                        linesInFile.add(line);

                        prevLine = line;
                    }

                    // set up a new buffered writer for this file
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, false))) {
                        // write everything to the active file
                        for (String line : linesInFile) {
                            writer.write(line);
                            writer.newLine();
                        }
                    }
                    
                    // if there's no more files for this project, break.
                    if (toNext) {
                        break;
                    }
                }
            }           
        }
         catch (FileNotFoundException e) {
            System.err.println("Cache file missing!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Cannot read file!");
            e.printStackTrace();
        }
    }
}
