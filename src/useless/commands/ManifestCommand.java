package useless.commands;

import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.NotNull;
import useless.AppMain;
import useless.bta.TexturePackManifest;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;

public class ManifestCommand implements ICommand{
    @Override
    public void runCommand(@NotNull File rootDirectory, @NotNull File outputDirectory, @NotNull String argString) throws IOException {
        String[] args = argString.split(" ");
        if (args.length < 1) {
            throw new RuntimeException("Malformed argString '" + argString + "'!");
        }
        String version = args[0];
        File manifestFile = new File(rootDirectory, "manifest.json");
        TexturePackManifest manifest;
        if (!manifestFile.exists()){
            manifest = new TexturePackManifest();
            manifest.name = outputDirectory.getName();
            List<String> lines = new ArrayList<>();
            try {
                File packFile = new File(rootDirectory, "pack.txt");
                if (packFile.exists()){
                    try (Scanner myReader = new Scanner(packFile)){
                        while (myReader.hasNextLine()){
                            lines.add(myReader.nextLine());
                        }
                    }
                    if (lines.size() > 0){
                        manifest.line1 = lines.get(0);
                    }
                    if (lines.size() > 1){
                        manifest.line2 = lines.get(1);
                    }
                }
            } catch (Exception e){
                AppMain.logger.log(Level.WARNING, "Error while processing `pack.txt`, skipping read!", e);
            }
            manifest.packVersion = version;
            manifest.format = 1;
        } else {
            try (JsonReader reader =  new JsonReader(new FileReader(new File(rootDirectory, "manifest.json")))) {
                manifest = AppMain.GSON.fromJson(reader, TexturePackManifest.class);
                if (manifest.format != 1){
                    AppMain.logger.warning("Unsupported format version '" + manifest.format + "' treating file as though its format '1'");
                }
                manifest.packVersion = version;
            }
        }

        File manOut = new File(outputDirectory, "manifest.json");
        if (!manOut.exists()){
            manOut.createNewFile();
        }
        FileWriter writer = new FileWriter(manOut);
        AppMain.GSON.toJson(manifest, writer);
        writer.close();
        AppMain.logger.info("Updated manifest at '" + manOut + "'");
    }
}
