package useless.logging;

import useless.AppMain;

import javax.swing.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.ErrorManager;
import java.util.logging.LogRecord;

public class GuiConsoleHandler extends ConsoleHandler {
    private ConsoleLogUpdateThread thread;
    public GuiConsoleHandler(JTextArea textArea){
        thread = new ConsoleLogUpdateThread(textArea);
        thread.start();
    }
    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        String msg;
        try {
            msg = getFormatter().format(record);
        } catch (Exception ex) {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }

        try {
            thread.addMessageToQueue(msg);
        } catch (Exception ex) {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }
    }
    public void dispose(){
        thread.shutDown();
        AppMain.logger.removeHandler(this);
    }
}
