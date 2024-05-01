# BTA Texture Pack Converter
A utility for updating BTA texturepacks easily!

# Usage

## GUI
### Interface
- Select Pack Button
  - The select pack button lets you select one or more packs that you want to run through the converter
  - It only supports selecting directories and zip files
- Convert Button
  - When pressed the convert button begins conversion of all selected packs to the selected version simultaneously
  - Conversion progress, warnings, and errors are logged to the console output on the GUI and saved to `/Logs/`
- Version Select
  - Version select is a drop down menu that lets you select any one of the defined versions from `/Configuration/versions.json`

## Command Line
### Arguments
- `nogui`
  - Disables the GUI startup process
  - If left unspecified it will launch the jar with the GUI
  - Example `java -jar Converter.jar nogui`
- `texture-pack`
  - Allows you to specify a specific texturepack from directory `/Input/` to use
  - If left unspecified it will default to converting all texturepacks in the `/Input/` directory
  - Example `java -jar Converter.jar texture-pack=TemplatePack.zip`
- `target-version`
  - Allows you to set the specific version for the program to attempt to convert the texture packs to
  - If left unspecified it defaults to the version in `/Configuration/versions.json` with the highest compareVal
  - Example `java -jar Converter.jar target-version=7.2`

## Configuration
### Formats
There are three types of configuration formats the Texture Pack Converter program uses to define conversion behavior
- Version Manifest
  - The version manifest found at `/Configuration/versions.json` is a map of version identifiers to `Version` objects
  - Version Objects are layed out as such
    - `next`
      - Is the version id for the next texture pack version
    - `map`
      - Is the name of the `Conversion Command Map` to be used to convert a pack from this version to the next
    - `compareVal`
      - Is a number used for comparisons, the `compareVal` of a newer pack version should always be higher then an older version
- Conversion Command Map
  - This map is a list of `Commands` to be performed to the texture pack in order from top to bottom
  - The Command Map supports the use of `#` and `//` for comments
  - Each `Command` starts with a command symbol, being a single case-insensitive character, with the remaining section of the line being passed into the command as its arguments
  - `Commands`
    - `R` Remove Files
      - Completely deletes a specified file or directory
      - Examples
        - `R pack.txt`
        - `R gui`
    - `M` Move Files
      - Moves file or directory, by default 0 files are moved into the updated texture pack unless specified
      - Special character `*` for specifying that you want to transfer all files
      - Examples:
        - `M armor assets/minecraft/textures/armor` Relocated the armor directory into one inside of `assets/minecraft/textures/armor`, the files after this point will no longer be accessible from the original `armor` directory
        - `M *` Copies all files from the source pack into the temporary pack folder
    - `S` Split Atlas
      - Splits a sprite sheet atlas into individual smaller texture files
      - Split takes in 4 seperate arguments `<atlasImageLocation> <outputDirectoryForImages> <atlasSplitterMapLocation> <numberOfTilesWide>`
      - Example:
        - `S terrain.png assets/minecraft/textures/block/ legacyBTA/blocks.txt 32`
    - `G` Generate Manifest
      - Generates/Updates manifest file to the version identifier specified
      - If a pack is missing an existing `manifest.json` file it will generate a new one using the texture pack folder/file name as the name given to the manifest and the contents of `pack.txt` (if present) as the `line1`
      - The command always generates a manifest with a `packVersion` set the provided version identifier
      - Example:
        `G 7.1`
- Atlas Splitter Map
  - This map is a list of individual textures to be created from the current atlas image
  - The Atlas Splitter Map supports the use of `#` and `//` for comments
  - Each line of the map consists of 3 arguments `<tileX,tileY> - <name> - <tilesWide,tilesTall>`, each argument is seperated by the sperater sequence of ` - `
    - `<tileX,tileY>` is the position (in tile widths) of the texture on the atlas
    - `<name>`  is the name the generate image will be given
    - `<tilesWide,tilesTall>` is an optional argument that specifies how many talls across or tall a given texture is
  - Examples:
    - `0,0 - grass_top`
    - `4,7 - tool_axe_gold`
    - `4,11 - donkey_kong - 4,3`
