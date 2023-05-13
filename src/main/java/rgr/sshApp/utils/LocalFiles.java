package rgr.sshApp.utils;

import javafx.application.Platform;
import rgr.sshApp.model.ModelData;
import rgr.sshApp.web.SecureFileTransferChannel;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class LocalFiles extends Files {

    public LinkedList<FileInfo> getFileList(String path) throws IOException {
        Path curPath = Path.of(path).toAbsolutePath().normalize();
        Path parentPath = curPath.getParent();
        LinkedList<FileInfo> dirList = java.nio.file.Files.list(curPath).map(FileInfo::parseFilePath)
                                       .collect(Collectors.toCollection(LinkedList::new));
        FileInfo parentDirInfo = parentPath==null ? FileInfo.parseFilePath(curPath) : FileInfo.parseFilePath(parentPath);
        parentDirInfo.setFileName("..");
        dirList.add(parentDirInfo);
        return dirList;
    }

    @Override
    public LinkedList<String> getRootDirectories() {
        LinkedList<String> rootDirs = new LinkedList<>();
        for (Path path : FileSystems.getDefault().getRootDirectories()) {
            rootDirs.add(path.toString());
        }
        return rootDirs;
    }

    @Override
    public String getInitialPath() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }

    @Override
    public String getParentDirectory(String currentPath) {
        Path parentPath = Path.of(currentPath).getParent();
        java.lang.String parentDir = null;
        if (parentPath!=null) {
            parentDir = parentPath.toString();
        }
        return parentDir;
    }

    @Override
    public String getResolvedDirectory(String fileName, String currentPath) {
        return Path.of(currentPath).resolve(fileName).toString();
    }

    @Override
    public boolean isExists(String path, String fileName) {
        String filePath = this.getResolvedDirectory(fileName,path);
        return java.nio.file.Files.exists(Path.of(filePath).toAbsolutePath().normalize());
    }

    @Override
    public void deleteFile(String path, String fileName) {
        Path filePath = Path.of(path).toAbsolutePath().normalize().resolve(fileName);
        if (java.nio.file.Files.exists(filePath)) {
            try {
                if (java.nio.file.Files.isDirectory(filePath)) {
                    java.nio.file.Files.walk(filePath)
                            .map(Path::toFile)
                            .sorted((o1,o2)-> -o1.compareTo(o2))
                            .forEach(File::delete);
                    System.out.println("folder = " + filePath + " removed");
                } else {
                    java.nio.file.Files.delete(filePath);
                    System.out.println("Removed file = " + filePath);
                }
            } catch (IOException exc) {
                System.out.println("ManagerController.removeLocalFile: deleting error");
                exc.printStackTrace();
            }
        }
    }

    @Override
    public void transferFile(String transferPath, String fileDir, String fileName) {
        //FileInfo selectedFileInfo = localPanel.getSelectedFile();
        //if (selectedFileInfo!=null) {
   //         String selectedFileName = selectedFileInfo.getFileName();
            Path localFilePath = Path.of(fileDir).toAbsolutePath().normalize().resolve(fileName);
            //String remoteDir = remotePanel.getCurrentDir();
            SecureFileTransferChannel newSftpChannel = new SecureFileTransferChannel(ModelData
                                                       .getInstance().getSshSession().getSession());
            newSftpChannel.connect();
            System.out.println("New channel is connected = " + newSftpChannel.isConnnected());
            System.out.println("Uploading file = " + localFilePath);
            System.out.println("Uploading to the dir = " + transferPath);
            if (java.nio.file.Files.isDirectory(localFilePath) && !fileName.equals("..")) {
                uploadFolder(localFilePath,remoteDir,newSftpChannel);
            } else {
                newSftpChannel.uploadFile(localFilePath.toString(),transferPath);
            }
//            Platform.runLater(()->remotePanel.refresh());
            System.out.println("Upload finished");
            newSftpChannel.disconnect();
            System.out.println("New channel is disconnected = " + !newSftpChannel.isConnnected());
       // }
    }

    @Override
    public void transferFolder(String path, String fileName) {

    }
}
