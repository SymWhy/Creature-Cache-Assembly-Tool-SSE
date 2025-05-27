package main;

import java.io.*;
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

    public static Boolean verbose = false;

    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        // make sure we're in the right folder
        if (!AnimDataOutput.exists() || !AnimSetDataOutput.exists()) {
            System.err.println("Couldn't find animation files.");
            return;
        }

        System.out.println("Parsing AnimationData.");
        countAnims = Misc.ParseCount(AnimCache);
        directoryAnims = Misc.ParseDirectory(AnimCache);

        System.out.println("Parsing AnimationSetData.");
        countAnimSets = Misc.ParseCount(AnimSetCache);
        directoryAnimSets = Misc.ParseDirectory(AnimSetCache);

        String input = "";

        while (true) {

            System.out.println("Type A to append projects to singlefiles.");
            System.out.println("Type E to extract existing project data.");
            System.out.println("Type R to reset the singlefile caches.");
            // System.out.println("Type D to print directory files from cache.");
            System.out.println("Type V to toggle verbose logging.");
            System.out.println("Type Q to quit.");

            input = scanner.nextLine().toLowerCase();

            switch (input) {
                case "a":
                    Build.AppendProjects(verbose);
                    break;

                case "e":
                    Extract.ExtractProjects(verbose);
                    scanner.nextLine();
                    break;

                case "r":
                    long MarkR = System.currentTimeMillis();
                    Misc.Reset();
                    Util.Recall(MarkR);
                    break;

                // case "d":
                //     Misc.PrintDirectories();
                //     break;
                
                case "v":
                    verbose = !verbose;
                    break;

                case "q":
                    System.exit(0);

                default:
                    System.out.println("Unexpected command.");
                    break;
            }
        }
    }
}
