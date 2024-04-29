package useless.commands;

import org.jetbrains.annotations.NotNull;
import util.FileUtil;

import java.io.File;
import java.io.IOException;

public class RemoveCommand implements ICommand {
    @Override
    public void runCommand(@NotNull File rootDirectory, @NotNull File outputDirectory, @NotNull String argString) throws IOException {
        File oldFile = new File(rootDirectory, argString);
        if (!oldFile.exists()) throw new RuntimeException("File '" + oldFile + "' does not exist!");
        FileUtil.deleteFolder(oldFile, false);
    }
}
