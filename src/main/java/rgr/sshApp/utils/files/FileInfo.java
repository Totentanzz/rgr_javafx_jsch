package rgr.sshApp.utils.files;

import com.jcraft.jsch.ChannelSftp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;

@Getter
@Setter
@ToString
public class FileInfo {

    private String fileName;
    private String fileType;
    private long fileSize;
    private String lastModifiedDate;

    private FileInfo(String fileName, String fileType, long fileSize, String lastModifiedDate) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.lastModifiedDate = lastModifiedDate;
    }

    public static FileInfo parseFilePath(String filePath) {
        Path path = Path.of(filePath);
        String fileName = null, fileType = null, date = null;
        long fileSize = -1;
        fileName = getFileName(filePath);
        fileType = getFileType(fileName);
        if (fileType != "") {
            fileSize = getFileSize(filePath);
        }
        date = getLastModificationDate(filePath);
        return new FileInfo(fileName,fileType,fileSize,date);
    }

    public static FileInfo parseFilePath(Path filePath) {
        String path = filePath.toString();
        return parseFilePath(path);
    }

    public static FileInfo parseFilePath(ChannelSftp.LsEntry entry) {
        String fileName = null, fileType = null, date = null;
        long fileSize = -1;
        boolean isDir = entry.getAttrs().isDir();
        fileName = entry.getFilename();
        fileType = isDir ? "" : getFileType(fileName);
        fileSize = isDir ? -1 : entry.getAttrs().getSize();
        date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(entry.getAttrs().getMTime() * 1000L);
        return new FileInfo(fileName,fileType,fileSize,date);
    }

    public static String getFileName(String filePath) {
        Path curPath = Path.of(filePath);
        Path fileName = curPath.getFileName();
        return fileName==null ? curPath.getRoot().toString() : fileName.toString();
    }

    public static String getFileDir(String filePath) {
        Path curPath = Path.of(filePath);
        return curPath.getParent().toString();
    }

    public static String getFileType(String fileName) {
        String fileType = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            fileType = fileName.substring(dotIndex+1);
        }
        return fileType;
    }

    public static long getFileSize(String filePath) {
        Path curPath = Path.of(filePath);
        long fileSize = -1;
        try {
            if (getFileType(filePath)!="")
                fileSize = Files.size(curPath);
        } catch (IOException exc) {
            System.out.println("FileInfo.getFileSize: cannot get size of file");
            exc.printStackTrace();
        }
        return fileSize;
    }

    public static String getLastModificationDate(String filePath) {
        Path curPath = Path.of(filePath);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String date = null;
        try {
            date = dateFormat.format(Files.getLastModifiedTime(curPath).toMillis());
        } catch (IOException exc) {
            System.out.println("FileInfo.getLastModificationDate: cannot get formatted date");
            exc.printStackTrace();
        }
        return date;
    }

}
