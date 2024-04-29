import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    public static final File rootProgramDirectory = new File(".");
    public static final File inputDirectory = new File(rootProgramDirectory, "Input");
    public static final File outputDirectory = new File(rootProgramDirectory, "Output");
    public static final File configurationDirectory = new File(rootProgramDirectory, "Configuration");
    public static final File tempDirectory = new File(rootProgramDirectory, "Temp");

    public static Map<String, File> conversionMapMap = new HashMap<>();
    public static void main(String[] args) throws IOException {
        String texPackPath = "";
        for (String arg : args){
            String[] split = arg.split("=");
            if (split.length != 2) continue;
            String key = split[0];
            String val = split[1];
            switch (key){
                case "texture-pack":
                    texPackPath = interpretString(val);
                    break;
            }
        }
        if (texPackPath.isEmpty()) throw new IllegalArgumentException("argument `texture-pack` must be assigned a value!");

        inputDirectory.mkdirs();
        outputDirectory.mkdirs();
        configurationDirectory.mkdirs();
        tempDirectory.mkdirs();
        deleteFolder(tempDirectory, true);

        File zipFile = new File(inputDirectory, "BTAGregPack 7.1.zip");
        File tempDir0 = new File(new File(tempDirectory, "0"), zipFile.getName().replace(".zip", ""));
        File tempDir1 = new File(new File(tempDirectory, "1"), zipFile.getName().replace(".zip", ""));
        UnzipUtility.unzip(zipFile, tempDir0);

        convert(tempDir0, tempDir1, new File(configurationDirectory, "test.txt"));


        FileOutputStream fos = new FileOutputStream(new File(outputDirectory, tempDir1.getName() + ".zip"));
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipFile(tempDir1, "", zipOut, true);

        zipOut.close();
        fos.close();



        deleteFolder(tempDirectory, true);
    }
    public static String interpretString(String str){
        if (str.startsWith("\"") && str.endsWith("\"")) return str.substring(1, str.length() -2);
        return str;
    }
    public static void convert(@NotNull File rootDir, @NotNull File outputDir, @NotNull File conversionMap) throws IOException {
        rootDir.mkdirs();
        outputDir.mkdirs();

        try (Scanner myReader = new Scanner(conversionMap)) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine().strip();
                if (data.isEmpty()) continue;
                if (data.startsWith("#") | data.startsWith("//")) continue;
                if (!data.contains("=")) continue;
                String[] vals = data.split("=");
                if (vals.length > 2) throw new RuntimeException("Malformed line '" + data + "'!");
                System.out.println(data);

                File oldFile = new File(rootDir, vals[0]);
                if (vals.length == 1) {
                    oldFile.delete();
                    continue;
                }

                File newFile = new File(outputDir, vals[1]);
                if (newFile.exists()) {
                    deleteFolder(newFile, false);
                }
                newFile.mkdirs();
                Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void deleteFolder(File folder, boolean deleteContentsOnly) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f, false);
                } else {
                    f.delete();
                }
            }
        }
        if (!deleteContentsOnly){
            folder.delete();
        }
    }
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, boolean skip) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            label0 : {
                if (skip) break label0;
                if (fileName.endsWith("/")) {
                    zipOut.putNextEntry(new ZipEntry(fileName));
                    zipOut.closeEntry();
                } else {
                    zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                    zipOut.closeEntry();
                }
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, (skip ? "":fileName + "/") + childFile.getName(), zipOut, false);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
}
