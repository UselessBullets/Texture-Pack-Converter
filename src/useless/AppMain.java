package useless;

import org.jetbrains.annotations.NotNull;
import useless.commands.ICommand;
import useless.commands.MoveCommand;
import useless.commands.RemoveCommand;
import useless.commands.SplitCommand;
import useless.logging.AppConsoleHandler;
import useless.logging.CustomFormatter;
import util.FileUtil;
import util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

public class AppMain {
    public static final Logger logger = Logger.getLogger("Converter");
    public static final File rootProgramDirectory = new File(".");
    public static final File inputDirectory = new File(rootProgramDirectory, "Input");
    public static final File outputDirectory = new File(rootProgramDirectory, "Output");
    public static final File configurationDirectory = new File(rootProgramDirectory, "Configuration");
    public static final File tempDirectory = new File(rootProgramDirectory, "Temp");
    public static final Map<Character, ICommand> commandMap = new HashMap<>();

    static {
        LogManager.getLogManager().reset();
        logger.setLevel(Level.FINE);
        Handler consoleHandler = new AppConsoleHandler();
        consoleHandler.setFormatter(new CustomFormatter(true));
        logger.addHandler(consoleHandler);
        try {
            File logDir = new File(rootProgramDirectory, "Logs");
            logDir.mkdirs();
            Handler fileHandler = new FileHandler(logDir.toPath() + "/log.txt", 1024 * 512, 1);
            fileHandler.setFormatter(new CustomFormatter(false));
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

        inputDirectory.mkdirs();
        outputDirectory.mkdirs();
        configurationDirectory.mkdirs();
        tempDirectory.mkdirs();
        FileUtil.deleteFolder(tempDirectory, true);

        File[] fileList;
        if (!texPackPath.isEmpty()){
            fileList = new File[]{new File(inputDirectory, texPackPath)};
        } else {
            fileList = inputDirectory.listFiles();
        }

        if (fileList == null) throw new RuntimeException("File list is null!");

        for (File file : fileList){
            logger.info("Starting conversion of file '" + file + "'");
            String packName;

            boolean isZip = !file.isDirectory() && file.getName().endsWith(".zip");
            if (!file.isDirectory() && !isZip) {
                logger.warning("Skipping!: File " + file.getName() + " not a valid texturepack!");
                continue;
            }
            if (isZip){
                packName = file.getName().replace(".zip", "");

            } else {
                packName = file.getName();
            }
            File tempDir0 = new File(new File(tempDirectory, "0"), packName);
            File tempDir1 = new File(new File(tempDirectory, "1"), packName);

            if (isZip){
                FileUtil.unzip(file, tempDir0);
            } else {
                Files.copy(file.toPath(), tempDir0.toPath());
            }

            recursiveConvert(tempDir0, tempDir1, new File(configurationDirectory, "test.txt"));

            File zippedPackConverted = new File(outputDirectory, tempDir1.getName() + ".zip");
            FileOutputStream fos = new FileOutputStream(zippedPackConverted);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            AppMain.logger.info("Zipping '" + tempDir1 + "' to '" + zippedPackConverted + "'");
            FileUtil.zipFile(tempDir1, "", zipOut, true);

            zipOut.close();
            fos.close();
        }

        FileUtil.deleteFolder(tempDirectory, true);
    }
    public static void recursiveConvert(@NotNull File rootDir, @NotNull File outputDir, @NotNull File conversionMap) throws IOException {
        versionConversion(rootDir, outputDir, conversionMap);
    }

    public static void versionConversion(@NotNull File rootDir, @NotNull File outputDir, @NotNull File conversionMap) throws IOException {
        rootDir.mkdirs();
        outputDir.mkdirs();

        try (Scanner myReader = new Scanner(conversionMap)) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine().strip();

                if (data.isEmpty()) continue; // Skip empty lines
                if (StringUtils.isComment(data)) continue; // Skip comments

                logger.info("Processing line '" + data + "'");

                char commandSymbol = data.toLowerCase().charAt(0);
                String commandArgs = data.substring(1).strip();

                ICommand command = commandMap.get(commandSymbol);
                if (command == null) {
                    logger.warning("Skipping!: Command symbol '" + commandSymbol + "' in " + data + " is not recognized!");
                    continue;
                }
                command.runCommand(rootDir, outputDir, commandArgs);
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Conversion failed!", e);
        }
    }
}
