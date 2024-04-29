package useless.commands;

import org.jetbrains.annotations.NotNull;
import util.FileUtil;

import java.io.File;
import java.io.IOException;

public class MoveCommand implements ICommand{
    @Override
    public void runCommand(@NotNull File rootDirectory, @NotNull File outputDirectory, @NotNull String argString) throws IOException {
        if (argString.split(" ").length != 2) throw new RuntimeException("Malformed argString '" + argString + "'!");
        String[] vals = argString.split(" ");
        File oldFile = new File(rootDirectory, vals[0]);
        File newFile = new File(outputDirectory, vals[1]);
        FileUtil.moveFile(oldFile, newFile);
    }
}
