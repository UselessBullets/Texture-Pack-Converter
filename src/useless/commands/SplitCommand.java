package useless.commands;

import org.jetbrains.annotations.NotNull;
import useless.AppMain;
import util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class SplitCommand implements ICommand{
    @Override
    public void runCommand(@NotNull File rootDirectory, @NotNull File outputDirectory, @NotNull String argString) throws IOException {
        if (argString.split(" ").length != 4) throw new RuntimeException("Malformed argString '" + argString + "'!");

        String[] args = argString.split(" ");

        File conversionConfig = new File(AppMain.configurationDirectory, args[2]);
        File imageToSplit  = new File(rootDirectory, args[0]);

        if (!imageToSplit.exists()) {
            System.out.println("Skipping!: File '" + imageToSplit + "' does not exist!");
            return;
        }

        File outputDir = new File(outputDirectory, args[1]);
        outputDir.mkdirs();
        int atlasWidthTiles = Integer.parseInt(args[3]);

        BufferedImage atlas = ImageIO.read(imageToSplit);
        int atlasWidth = atlas.getWidth();
        int atlasHeight = atlas.getHeight();

        int tileSize = atlasWidth/atlasWidthTiles;

        try (Scanner myReader = new Scanner(conversionConfig)) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine().strip();

                if (data.isEmpty()) continue; // Skip empty lines
                if (StringUtils.isComment(data)) continue; // Skip comments
                String[] vals = data.split(" - ");
                if (vals.length < 2 | vals.length > 3) throw new RuntimeException("Malformed data '" + data + "'!"); //

                System.out.println(data);

                String[] pos = vals[0].strip().split(",");
                int cX = Integer.parseInt(pos[0]);
                int cY = Integer.parseInt(pos[1]);

                String textureName = vals[1] + ".png";

                int tileWidth = 1;
                int tileHeight = 1;
                if (vals.length == 3) {
                    String[] size = vals[2].strip().split(",");
                    tileWidth = Integer.parseInt(size[0]);
                    tileHeight = Integer.parseInt(size[1]);
                }
                int subWidth = tileWidth * tileSize;
                int subHeight = tileHeight * tileSize;

                int[] cropped = atlas.getRGB(cX * tileSize, cY * tileSize, subWidth, subHeight, null, 0, subWidth);

                BufferedImage bufferedimage = new BufferedImage(subWidth, subHeight, BufferedImage.TYPE_INT_ARGB);
                bufferedimage.setRGB(0, 0, subWidth, subHeight, cropped, 0, subWidth);
                ImageIO.write(bufferedimage, "png", new File(outputDir, textureName));
            }
        } catch (FileNotFoundException  e) {
            e.printStackTrace();
        }
    }
}
