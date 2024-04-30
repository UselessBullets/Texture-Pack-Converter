package useless.gui;

import useless.AppMain;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;

public class ConverterGui extends JFrame {
    public JButton convertButton;
    // Constructor to setup the GUI components and event handlers
    public ConverterGui() {
        AppMain.logger.info("Opening GUI");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Texture Pack Converter");
        setSize(300, 100);
        setVisible(true);

        initButtons();
    }

    public void initButtons(){
        convertButton = new JButton("Convert");
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File[] fileList = AppMain.inputDirectory.listFiles();

                if (fileList == null) throw new RuntimeException("File list is null!");

                for (File file : fileList){
                    try {
                        AppMain.convertFile(file);
                    } catch (Exception ex) {
                        AppMain.logger.log(Level.SEVERE, "Exception occurred while processing file '" + file + "'!", ex);
                    }
                }
            }
        });
        add(convertButton);
    }
}
