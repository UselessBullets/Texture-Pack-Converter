package util;

import org.apache.commons.io.FileUtils;
import org.useless.textureconverter.AppMain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class FileUtil {
	public static void unzip(File zipFilePath, File destDirectory) throws IOException {
		AppMain.logger.info("Unzipping '" + zipFilePath + "' to '" + destDirectory + "'");
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			File newFile = newFile(destDirectory, zipEntry);
			if (zipEntry.isDirectory()) {
				if (!newFile.isDirectory() && !newFile.mkdirs()) {
					throw new IOException("Failed to create directory " + newFile);
				}
			} else {
				// fix for Windows-created archives
				File parent = newFile.getParentFile();
				if (!parent.isDirectory() && !parent.mkdirs()) {
					throw new IOException("Failed to create directory " + parent);
				}

				// write file content
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
			}
			zipEntry = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();
	}
	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}
	public static void deleteFolder(File folder, boolean deleteContentsOnly) {
		File[] files = folder.listFiles();
		if(files!=null) { //some JVMs return null for empty dirs
			for(File f: files) {
				if(f.isDirectory()) {
					deleteFolder(f, false);
				} else {
					f.delete();
				}
			}
		}
		if (!deleteContentsOnly){
			folder.delete();
		}
	}
	public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, boolean first) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			label0 : {
				if (first) break label0;
				if (fileName.endsWith("/")) {
					zipOut.putNextEntry(new ZipEntry(fileName));
					zipOut.closeEntry();
				} else {
					zipOut.putNextEntry(new ZipEntry(fileName + "/"));
					zipOut.closeEntry();
				}
			}
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, (first ? "":fileName + "/") + childFile.getName(), zipOut, false);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}
	public static void moveFile(File input, File output) throws IOException {
		if (output.exists()) {
			FileUtil.deleteFolder(output, false);
		}
		output.mkdirs();
		if (input.isDirectory()){
			FileUtils.copyDirectory(input, output);
		} else {
			Files.copy(input.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
