package useless.logging;

import useless.AppMain;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ConsoleLogUpdateThread extends Thread{
    private List<String> messageQueue = new ArrayList<>();
    private JTextArea area;
    private boolean runThread = true;
    public ConsoleLogUpdateThread(JTextArea textArea){
        area = textArea;
    }

    public synchronized void run(){
        AppMain.logger.info("Starting Console Log thread");
        while (runThread){
            try {
                sleep(50);
            } catch (InterruptedException e) {
                AppMain.logger.warning("Gui logging thread interrupted, ending thread.");
            }
            String[] buffer = messageQueue.toArray(new String[0]);
            messageQueue.clear();
            if (buffer.length > 0){
                StringBuilder compiledMessage = new StringBuilder(area.getText());
                for (String s : buffer){
                    compiledMessage.append(s);
                }
                String msg = compiledMessage.toString();
                area.setText(msg);
                area.setCaretPosition(msg.length());
            }
        }
    }
    public void addMessageToQueue(String message){
        messageQueue.add(message);
    }
    public void shutDown(){
        AppMain.logger.info("Ending Console Log thread");
        runThread = false;
    }
}
