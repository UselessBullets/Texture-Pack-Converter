package useless;
import useless.commands.ICommand;
import useless.commands.MoveCommand;
import useless.commands.RemoveCommand;
import useless.commands.SplitCommand;
import org.jetbrains.annotations.NotNull;
import util.FileUtil;
import util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipOutputStream;

public class AppMain {
    public static final File rootProgramDirectory = new File(".");
    public static final File inputDirectory = new File(rootProgramDirectory, "Input");
    public static final File outputDirectory = new File(rootProgramDirectory, "Output");
    public static final File configurationDirectory = new File(rootProgramDirectory, "Configuration");
    public static final File tempDirectory = new File(rootProgramDirectory, "Temp");
    public static final Map<Character, ICommand> commandMap = new HashMap<>();

    static {
        commandMap.put('r', new RemoveCommand());
        commandMap.put('m', new MoveCommand());
        commandMap.put('s', new SplitCommand());
    }

    public static void main(String[] args) throws IOException {
        String texPackPath = "";
        for (String arg : args){
            String[] split = arg.split("=");
            if (split.length != 2) continue;
            String key = split[0];
            String val = split[1];
            switch (key){
                case "texture-pack":
                    texPackPath = StringUtils.interpretString(val);
                    break;
            }
        }
        if (texPackPath.isEmpty()) throw new IllegalArgumentException("argument `texture-pack` must be assigned a value!");

        inputDirectory.mkdirs();
        outputDirectory.mkdirs();
        configurationDirectory.mkdirs();
        tempDirectory.mkdirs();
        FileUtil.deleteFolder(tempDirectory, true);

        File zipFile = new File(inputDirectory, "BTAGregPack 7.1.zip");
        File tempDir0 = new File(new File(tempDirectory, "0"), zipFile.getName().replace(".zip", ""));
        File tempDir1 = new File(new File(tempDirectory, "1"), zipFile.getName().replace(".zip", ""));
        FileUtil.unzip(zipFile, tempDir0);

        convert(tempDir0, tempDir1, new File(configurationDirectory, "test.txt"));


        FileOutputStream fos = new FileOutputStream(new File(outputDirectory, tempDir1.getName() + ".zip"));
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        FileUtil.zipFile(tempDir1, "", zipOut, true);

        zipOut.close();
        fos.close();



        FileUtil.deleteFolder(tempDirectory, true);
    }

    public static void convert(@NotNull File rootDir, @NotNull File outputDir, @NotNull File conversionMap) throws IOException {
        rootDir.mkdirs();
        outputDir.mkdirs();

        try (Scanner myReader = new Scanner(conversionMap)) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine().strip();

                if (data.isEmpty()) continue; // Skip empty lines
                if (StringUtils.isComment(data)) continue; // Skip comments

                char commandSymbol = data.toLowerCase().charAt(0);
                String commandArgs = data.substring(1).strip();

                ICommand command = commandMap.get(commandSymbol);
                if (command == null) throw new RuntimeException("Command symbol '" + commandSymbol + "' is not recognized!");
                command.runCommand(rootDir, outputDir, commandArgs);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}