package useless.commands;

import org.jetbrains.annotations.NotNull;
import useless.AppMain;
import util.FileUtil;

import java.io.File;
import java.io.IOException;

public class MoveCommand implements ICommand{
    @Override
    public void runCommand(@NotNull File rootDirectory, @NotNull File outputDirectory, @NotNull String argString) throws IOException {
        if (argString.equals("*")){
            FileUtil.moveFile(rootDirectory, outputDirectory);
            AppMain.logger.info("Moved '" + rootDirectory + "' to '" + outputDirectory + "'");
            return;
        }
        String[] vals = argString.split(" ");
        File oldFile = new File(rootDirectory, vals[0]);
        if (!oldFile.exists()) {
            AppMain.logger.warning("Skipping!: File '" + oldFile + "' does not exist!");
            return;
        }
        File newFile = new File(outputDirectory, vals[1]);
        FileUtil.moveFile(oldFile, newFile);
        AppMain.logger.info("Moved '" + oldFile + "' to '" + newFile + "'");
    }
}
