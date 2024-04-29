package util;

public class StringUtils {
    public static String interpretString(String str){
        if (str.startsWith("\"") && str.endsWith("\"")) return str.substring(1, str.length() -2);
        return str;
    }
    public static boolean isComment(String str){
        str = str.strip();
        return str.startsWith("#") | str.startsWith("//");
    }
}
