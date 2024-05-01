package useless.bta;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class TexturePackManifest implements JsonSerializer<TexturePackManifest>, JsonDeserializer<TexturePackManifest> {
    public String name = "";
    public String line1 = "";
    public String line2 = "";
    public String packVersion = "";
    public int format = -1;


    @Override
    public TexturePackManifest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TexturePackManifest manifest = new TexturePackManifest();
        JsonObject object = json.getAsJsonObject().getAsJsonObject("description");
        manifest.name = object.has("name") ? object.get("name").getAsString() : "";
        manifest.line1 = object.has("line1") ? object.get("line1").getAsString() : "";
        manifest.line2 = object.has("line2") ? object.get("line2").getAsString() : "";
        manifest.packVersion = object.has("packVersion") ? object.get("packVersion").getAsString() : "";
        manifest.format = object.has("format") ? object.get("format").getAsInt() : -1;
        return manifest;
    }

    @Override
    public JsonElement serialize(TexturePackManifest src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", src.name);
        obj.addProperty("line1", src.line1);
        obj.addProperty("line2", src.line2);
        obj.addProperty("packVersion", src.packVersion);
        obj.addProperty("format", src.format);
        JsonObject outObject = new JsonObject();
        outObject.add("description", obj);
        return outObject;
    }
    @Override
    public String toString(){
        return new StringBuilder(name).append("\n")
                .append(line1).append("\n")
                .append(line2).append("\n")
                .append(packVersion).append(" ").append(format)
                .toString();
    }
}
