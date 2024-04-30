package useless.commands;

import org.jetbrains.annotations.NotNull;
import useless.AppMain;
import util.FileUtil;

import java.io.File;
import java.io.IOException;

public class RemoveCommand implements ICommand {
    @Override
    public void runCommand(@NotNull File rootDirectory, @NotNull File outputDirectory, @NotNull String argString) throws IOException {
        File oldFile = new File(rootDirectory, argString);
        File oldFile2 = new File(outputDirectory, argString);
        if (!oldFile.exists() && !oldFile2.exists()) {
            AppMain.logger.warning("Skipping!: File '" + oldFile + "' does not exist!");
            return;
        }
        FileUtil.deleteFolder(oldFile, false);
        FileUtil.deleteFolder(oldFile2, false);
        AppMain.logger.info("Removed file '" + oldFile + "'");
    }
}
