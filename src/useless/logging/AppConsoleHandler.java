package useless.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class AppConsoleHandler extends ConsoleHandler {
    @Override
    public void publish(LogRecord record) {
        //add own logic to publish
        super.publish(record);
    }


    @Override
    public void flush() {
        super.flush();
    }


    @Override
    public void close() throws SecurityException {
        super.close();
    }
}
