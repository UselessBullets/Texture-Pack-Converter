package useless.gui;

import useless.AppMain;
import useless.logging.CustomFormatter;
import useless.logging.GuiConsoleHandler;
import useless.version.Version;
import util.FileUtil;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.logging.Level;

public class ConverterGui extends JFrame {
    public GuiConsoleHandler handler;
    public File[] selectedPacks;
    public Version selectedVersion;
    // Constructor to setup the GUI components and event handlers
    public ConverterGui(GuiContainer container) {
        AppMain.gui = this;
        setContentPane(container.MainFrame);

        selectedVersion = Version.getMostRecentVersion();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        AppMain.logger.info("Opening GUI");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                close();
            }
        });
        setTitle("Texture Pack Converter");
        setMinimumSize(new Dimension(540, 400));

        handler = new GuiConsoleHandler(container.ConsoleOutput);
        handler.setFormatter(new CustomFormatter(false));

        AppMain.logger.addHandler(handler);

        container.convertButton.addActionListener(e -> {
            File[] fileList = selectedPacks == null ? AppMain.inputDirectory.listFiles() :selectedPacks;
            Thread t = new Thread(() -> {
                container.convertButton.setEnabled(false);
                try {
                    AppMain.convertAll(fileList, selectedVersion);
                } catch (InterruptedException ex) {
                    AppMain.logger.log(Level.SEVERE, "Interrupt exception occurred! Ending conversion tasks!", ex);
                }
                container.convertButton.setEnabled(true);
            });
            t.start();
        });
        container.selectPackButton.addActionListener(e ->{
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);
            chooser.setCurrentDirectory(AppMain.inputDirectory);
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() | f.getName().endsWith(".zip");
                }

                @Override
                public String getDescription() {
                    return "Texture Packs [Zip and Directories]";
                }
            });
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedPacks = chooser.getSelectedFiles();
            }
        });

        container.progressBar1.setVisible(false); // TODO implement progress bar

        for (Version v : Version.getAllVersions()){
            container.OutputVersionSelect.addItem(v);
        }

        container.OutputVersionSelect.setSelectedItem(selectedVersion);
        container.OutputVersionSelect.addActionListener(e -> selectedVersion = (Version) container.OutputVersionSelect.getSelectedItem());

        pack();
        setVisible(true);
    }
    public void close(){
        AppMain.logger.info("Closing GUI");
        AppMain.gui = null;
        handler.dispose();
        System.exit(0);
        FileUtil.deleteFolder(AppMain.tempDirectory, false);
    }
}
