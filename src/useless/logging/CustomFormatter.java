package useless.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class CustomFormatter extends SimpleFormatter {
    public static final String ANSI_ESCAPE = "\u001B";
    public static final String ANSI_RESET = ANSI_ESCAPE + "[0m";
    public static final String ANSI_BLACK = ANSI_ESCAPE + "[30m";
    public static final String ANSI_RED = ANSI_ESCAPE + "[31m";
    public static final String ANSI_GREEN = ANSI_ESCAPE + "[32m";
    public static final String ANSI_YELLOW = ANSI_ESCAPE + "[93m";
    public static final String ANSI_BLUE = ANSI_ESCAPE + "[34m";
    public static final String ANSI_PURPLE = ANSI_ESCAPE + "[35m";
    public static final String ANSI_CYAN = ANSI_ESCAPE + "[36m";
    public static final String ANSI_WHITE = ANSI_ESCAPE + "[97m";
    private static final String format;
    private static final String formatColored;
    static {
        format = new StringBuilder()
                .append("[").append("%1$tY").append(":").append("%1$tm").append(":").append("%1$td").append("::").append("%1$tl").append(":").append("%1$tM").append(":").append("%1$tS").append("]")
                .append(" ").append("%2$s").append(" || ")
                .append("%4$s").append(": ").append("%5$s").append("%6$s").append("\n")
                .toString();
        formatColored = new StringBuilder(ANSI_RESET)
                .append(ANSI_WHITE).append("[").append("%1$tY").append(":").append("%1$tm").append(":").append("%1$td").append("::").append("%1$tl").append(":").append("%1$tM").append(":").append("%1$tS").append("]").append(ANSI_RESET)
                .append(" ").append(ANSI_YELLOW).append("%2$s").append(ANSI_ESCAPE + "[37m").append(" || ")
                .append("%7$s").append("%4$s").append(": ").append(ANSI_WHITE).append("%5$s").append(ANSI_RED).append("%6$s").append("\n")
                .toString();
    }
    private final boolean useColors;
    public CustomFormatter(boolean useColors){
        this.useColors = useColors;
    }
    @Override
    public String format(LogRecord record) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(
                record.getInstant(), ZoneId.systemDefault());
        String source;
        if (record.getSourceClassName() != null) {
            String[] split = record.getSourceClassName().split("\\.");

            // Shorten source class name to simple name
            if (split.length > 0){
                source = split[split.length-1];
            } else {
                source = record.getSourceClassName();
            }

            if (record.getSourceMethodName() != null) {
                source += "$" + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }
        String message = formatMessage(record);
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        if (useColors){
            String color;
            Level l = record.getLevel();
            if (l.intValue() <= Level.INFO.intValue()){
                color = ANSI_GREEN;
            } else if (l.intValue() <= Level.WARNING.intValue()) {
                color = ANSI_YELLOW;
            } else {
                color = ANSI_RED;
            }
            return String.format(formatColored,
                    zdt,
                    source,
                    record.getLoggerName(),
                    record.getLevel().getLocalizedName(),
                    message,
                    throwable,
                    color);
        }
        return String.format(format,
                zdt,
                source,
                record.getLoggerName(),
                record.getLevel().getLocalizedName(),
                message,
                throwable);
    }
}
