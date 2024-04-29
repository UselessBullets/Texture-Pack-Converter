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
        inputDirectory.mkdirs();
        outputDirectory.mkdirs();
        configurationDirectory.mkdirs();
        tempDirectory.mkdirs();
        deleteFolder(tempDirectory, true);

        File zipFile = new File(inputDirectory, "test.zip");
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
    public static void convert(@NotNull File rootDir, @NotNull File outputDir, @NotNull File conversionMap){
        rootDir.mkdirs();
        outputDir.mkdirs();

        Scanner myReader = null;
        try {
            myReader = new Scanner(conversionMap);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.strip().startsWith("#") | data.strip().startsWith("//")) continue;
                if (!data.contains("=")) continue;
                String[] vals = data.split("=");
                if (vals.length != 2) throw new RuntimeException("Malformed line '" + data + "'!");
                System.out.println(data);

                File oldFile = new File(rootDir, vals[0]);
                File newFile = new File(outputDir, vals[1]);
                if (newFile.exists()){
                    deleteFolder(newFile, false);
                }
                newFile.mkdirs();
                Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (myReader != null){
                myReader.close();
            }
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
