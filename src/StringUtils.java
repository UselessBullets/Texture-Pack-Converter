public class StringUtils {
    public static String interpretString(String str){
        if (str.startsWith("\"") && str.endsWith("\"")) return str.substring(1, str.length() -2);
        return str;
    }
}
