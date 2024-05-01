package org.useless.textureconverter.gui;

import com.formdev.flatlaf.FlatDarculaLaf;
import org.useless.textureconverter.AppMain;
import org.useless.textureconverter.logging.CustomFormatter;
import org.useless.textureconverter.logging.GuiConsoleHandler;
import org.useless.textureconverter.version.Version;
import util.FileUtil;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConverterGui extends JFrame {
    public GuiConsoleHandler handler;
    public File[] selectedPacks;
    public Version selectedVersion;
    public ConverterGui(GuiContainer container) {
        AppMain.gui = this;
        AppMain.logger.info("Opening GUI");

        List<Image> l = new ArrayList<>();
        l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/1024.png")));
        l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/512.png")));
        l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/256.png")));
        l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/128.png")));
        l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/64.png")));
        l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/32.png")));
        l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/16.png")));
        setIconImages(l);

        setContentPane(container.MainFrame);

        selectedVersion = Version.getMostRecentVersion();

        try {
            UIManager.setLookAndFeel( new FlatDarculaLaf() );
        } catch( Exception ex ) {
            AppMain.logger.log(Level.WARNING, "Failed to initialize LaF", ex);
        }

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
