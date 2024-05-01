package useless;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import useless.bta.TexturePackManifest;
import useless.commands.ICommand;
import useless.commands.ManifestCommand;
import useless.commands.MoveCommand;
import useless.commands.RemoveCommand;
import useless.commands.SplitCommand;
import useless.gui.ConverterGui;
import useless.gui.GuiContainer;
import useless.logging.AppConsoleHandler;
import useless.logging.CustomFormatter;
import useless.version.Version;
import util.FileUtil;
import util.StringUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(TexturePackManifest.class, new TexturePackManifest())
            .create();

    static {
        LogManager.getLogManager().reset();
        logger.setLevel(Level.FINE);
        Handler consoleHandler = new AppConsoleHandler();
        consoleHandler.setFormatter(new CustomFormatter(true));
        logger.addHandler(consoleHandler);
        try {
            File logDir = new File(rootProgramDirectory, "Logs");
            createDirectoryIfMissing(logDir);
            Handler fileHandler = new FileHandler(logDir.toPath() + "/log.txt", 1024 * 512, 1);
            fileHandler.setFormatter(new CustomFormatter(false));
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        commandMap.put('r', new RemoveCommand());
        commandMap.put('m', new MoveCommand());
        commandMap.put('s', new SplitCommand());
        commandMap.put('g', new ManifestCommand());
    }

    public static void main(String[] args) throws IOException {
        createDirectoryIfMissing(inputDirectory);
        createDirectoryIfMissing(outputDirectory);
        createDirectoryIfMissing(configurationDirectory);
        createDirectoryIfMissing(tempDirectory);

        Version.init(new File(configurationDirectory, "versions.json"));

        String texPackPath = "";
        Version targetVersion = Version.getMostRecentVersion();
        boolean launchGUI = true;
        for (String arg : args){
            String[] split = arg.split("=");
            if (split.length < 1) continue;
            if (split.length > 2) continue;
            String key = split[0];
            String val = null;
            if (split.length > 1){
                val = split[1];
            }
            switch (key){
                case "texture-pack":
                    assert val != null;
                    texPackPath = StringUtils.interpretString(val);
                    break;
                case "nogui":
                    launchGUI = false;
                    break;
                case "target-version":
                    assert val != null;
                    targetVersion = Version.getVersion(val);
            }
        }


        try {
            if (launchGUI){
                openGUI();
            } else {
                File[] fileList;
                if (!texPackPath.isEmpty()){
                    fileList = new File[]{new File(inputDirectory, texPackPath)};
                } else {
                    fileList = inputDirectory.listFiles();
                }

                convertAll(fileList, targetVersion);
            }
        } catch (Exception e){
            logger.log(Level.SEVERE, "Program has encountered an unrecoverable error!", e);
        }
        if (!launchGUI){
            FileUtil.deleteFolder(tempDirectory, true);
        }
    }
    public static JFrame gui;
    public static void openGUI(){
        if (gui == null){
            new ConverterGui(new GuiContainer());
        }
    }
    public static void convertAll(@NotNull File[] fileList, Version targetVersion) throws InterruptedException {
        if (fileList == null) throw new RuntimeException("File list is null!");

        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (File file : fileList){
            threadPool.execute(() ->{
                try {
                    convertFile(file, targetVersion);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        threadPool.shutdown();
        boolean finished = threadPool.awaitTermination(5, TimeUnit.MINUTES);
        if (!finished){
            logger.warning("Process timed out before finishing all tasks!");
        } else {
            logger.info("Process finished all tasks successfully");
        }
    }
    public static void convertFile(@NotNull File texturePack, Version targetVersion) throws IOException {
        logger.info("Starting conversion of file '" + texturePack + "'");
        String packName;

        boolean isZip = !texturePack.isDirectory() && texturePack.getName().endsWith(".zip");
        if (!texturePack.isDirectory() && !isZip) {
            logger.warning("Skipping!: File " + texturePack.getName() + " not a valid texturepack!");
            return;
        }
        if (isZip){
            packName = texturePack.getName().replace(".zip", "");

        } else {
            packName = texturePack.getName();
        }
        File tempDir0 = new File(new File(tempDirectory, "0"), packName);
        File tempDir1 = new File(new File(tempDirectory, "1"), packName);

        createDirectoryIfMissing(tempDir0);
        createDirectoryIfMissing(tempDir1);

        FileUtil.deleteFolder(tempDir0, true);
        FileUtil.deleteFolder(tempDir1, true);

        if (isZip){
            FileUtil.unzip(texturePack, tempDir0);
        } else {
            FileUtils.copyDirectory(texturePack, tempDir0);
        }

        Version ver = Version.identifyVersion(tempDir0);
        if (ver == Version.UNKNOWN | ver == Version.NONE) {
            logger.severe("Skipping Conversion!: Failed to identify version for '" + texturePack + "'!");
            return;
        }

        if (ver.compare >= targetVersion.compare){
            logger.warning("Skipping Conversion!: Pack '" + texturePack + "' is at or exceeds target version!");
            return;
        }
        boolean canConvertToTarget = false;
        Version _ver = ver;
        while (_ver.compare < targetVersion.compare){
            if (_ver.next == null) break;
            _ver = Version.getVersion(_ver.next);
            if (_ver == targetVersion){
                canConvertToTarget = true;
                break;
            }
        }
        if (!canConvertToTarget){
            logger.warning("Skipping Conversion!: Pack '" + texturePack + "' has no conversion path to target!");
            return;
        }

        while (!(ver.next == null | ver.next.equals("NONE") | ver.mapLocation == null)){
            versionConversion(tempDir0, tempDir1, new File(configurationDirectory, ver.mapLocation));
            Version newVer = Version.identifyVersion(tempDir1);
            if (ver == newVer) {
                String message = "Recursion issue detected killing program!";
                logger.severe(message);
                throw new RuntimeException(message);
            }
            if (newVer == Version.UNKNOWN | newVer == Version.NONE) {
                logger.severe("Skipping Conversion!: Failed to identify version for '" + texturePack + "'!");
                return;
            }
            FileUtil.deleteFolder(tempDir0, true);
            File tempFile = tempDir0;
            tempDir0 = tempDir1;
            tempDir1 = tempFile;
            ver = newVer;
            if (ver == targetVersion | ver.compare > targetVersion.compare) break;
        }

        File zippedPackConverted = new File(outputDirectory, tempDir1.getName() + ".zip");
        FileOutputStream fos = new FileOutputStream(zippedPackConverted);
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        AppMain.logger.info("Zipping '" + tempDir0 + "' to '" + zippedPackConverted + "'");
        FileUtil.zipFile(tempDir0, "", zipOut, true);

        zipOut.close();
        fos.close();
    }

    public static void versionConversion(@NotNull File rootDir, @NotNull File outputDir, @NotNull File conversionMap) throws IOException {
        createDirectoryIfMissing(rootDir);
        createDirectoryIfMissing(outputDir);

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
    private static void createDirectoryIfMissing(File file){
        if (file.mkdirs()){
            logger.info("Created directory '" + file + "'");
        }
    }
}
