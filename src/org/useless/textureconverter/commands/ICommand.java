package org.useless.textureconverter.commands;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public interface ICommand {
    void runCommand(@NotNull File rootDirectory, @NotNull File outputDirectory, @NotNull String argString) throws IOException;
}
