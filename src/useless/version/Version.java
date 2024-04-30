package useless.version;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import useless.AppMain;
import useless.bta.TexturePackManifest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class Version {
    public static final Version UNKNOWN = new Version("UNKNOWN", null, null, -1);
    public static final Version NONE = new Version("NONE", null, null, -1);
    private static final Map<String, Version> versionMap = new HashMap<>();
    private static boolean hasInitialized = false;
    @NotNull
    public final String identifier;
    @Nullable
    public final String next;
    @Nullable
    public final String mapLocation;
    public final int compare;

    public Version(@NotNull String identifier, @Nullable String next, @Nullable String mapLocation, int compare){
        this.identifier = identifier;
        this.next = next;
        this.mapLocation = mapLocation;
        this.compare = compare;
    }
    public static void init(@NotNull File versionsManifest){
        if (hasInitialized) return;
        hasInitialized = true;
        versionMap.put(UNKNOWN.identifier.toUpperCase(Locale.US), UNKNOWN);
        versionMap.put(NONE.identifier.toUpperCase(Locale.US), NONE);
        try (JsonReader reader =  new JsonReader(new FileReader(versionsManifest))) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            Map<String, JsonElement> jsonMap = object.asMap();
            for (Map.Entry<String, JsonElement> entry : jsonMap.entrySet()){
                JsonObject obj = entry.getValue().getAsJsonObject();
                Version v = new Version(
                        entry.getKey().toUpperCase(Locale.US),
                        obj.get("next").getAsString(),
                        obj.has("map") ? obj.get("map").getAsString() : null,
                        obj.get("compareVal").getAsInt());
                versionMap.put(entry.getKey().toUpperCase(Locale.US), v);
                AppMain.logger.info("Found version entry for '" + v.identifier + "'");
            }
        } catch (IOException e) {
            AppMain.logger.log(Level.SEVERE, "Failed to read '" + versionsManifest + "'!", e);
        }
    }
    public static Version getVersion(String id){
        return versionMap.getOrDefault(id.toUpperCase(Locale.US), UNKNOWN);
    }
    public static Version identifyVersion(File pack){
        File manifestFile = new File(pack, "manifest.json");
        if (manifestFile.exists()){ // Post 7.0 pack
            try (JsonReader reader =  new JsonReader(new FileReader(manifestFile))) {
                TexturePackManifest manifest = AppMain.GSON.fromJson(reader, TexturePackManifest.class);
                AppMain.logger.info("Pack '" + pack + "' is for " + manifest.packVersion);
                return getVersion(manifest.packVersion);
            } catch (IOException e) {
                AppMain.logger.log(Level.SEVERE, "Failed to read `manifest.json` from '" + pack + "'!", e);
            } catch (Exception e){
                AppMain.logger.log(Level.SEVERE, "Unexpected error", e);
                return UNKNOWN;
            }
        }
        if (new File(pack, "pack.txt").exists()){
            AppMain.logger.info("Pack '" + pack + "' is for a pre 7.1 version");
            return getVersion("LEGACY");
        }
        AppMain.logger.log(Level.WARNING, "Could not locate a 'pack.txt' file or a 'manifest.json' file, defaulting to legacy version!");
        return getVersion("LEGACY");
    }
}
