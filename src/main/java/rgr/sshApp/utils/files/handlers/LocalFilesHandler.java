package rgr.sshApp.utils.files.handlers;

import com.jcraft.jsch.JSchException;

import com.jcraft.jsch.SftpException;
import rgr.sshApp.utils.files.FileInfo;
import rgr.sshApp.web.SecureFtpChannel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class LocalFilesHandler extends FilesHandler {

    public LocalFilesHandler(){
        super();
    }

    public LinkedList<FileInfo> getFileList(String path) throws IOException {
        Path curPath = Path.of(path).toAbsolutePath().normalize();
        Path parentPath = curPath.getParent();
        LinkedList<FileInfo> dirList = Files.list(curPath).map(FileInfo::parseFilePath)
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
        return System.getProperty("user.home");
    }

    @Override
    public boolean isExists(String path, String fileName) {
        String filePath = path + File.separatorChar + fileName;
        return Files.exists(Path.of(filePath).toAbsolutePath().normalize());
    }

    @Override
    public boolean isDir(String path, String fileName) {
        String filePath = path + File.separatorChar + fileName;
        return Files.isDirectory(Path.of(filePath));
    }

    @Override
    public void transferFile(String remoteTransferPath, String localFileDir, String fileName) throws JSchException, SftpException, FileNotFoundException {
        Path localFilePath = Path.of(localFileDir).toAbsolutePath().normalize().resolve(fileName);
        SecureFtpChannel newSftpChannel = null;
        try {
            newSftpChannel = new SecureFtpChannel(this.sshSession.getSession());
            newSftpChannel.connect();
            if (Files.exists(localFilePath)) {
                if (Files.isDirectory(localFilePath) && !fileName.equals("..")) {
                    uploadFolder(localFilePath, remoteTransferPath, fileName, newSftpChannel);
                } else if (!Files.isDirectory(localFilePath)) {
                    newSftpChannel.uploadFile(localFilePath.toString(), remoteTransferPath);
                }
            } else {
                throw new FileNotFoundException("File doesn't exist");
            }
        } finally {
            System.out.println("Upload finished");
            if (newSftpChannel!=null) newSftpChannel.disconnect();
        }
    }

    @Override
    public void deleteFile(String path, String fileName) throws FileNotFoundException {
        Path filePath = Path.of(path).toAbsolutePath().normalize().resolve(fileName);
        if (Files.exists(filePath)) {
            try {
                if (Files.isDirectory(filePath)) {
                    Files.walk(filePath)
                            .map(Path::toFile)
                            .sorted((o1,o2)-> -o1.compareTo(o2))
                            .forEach(File::delete);
                    System.out.println("folder = " + filePath + " removed");
                } else {
                    Files.delete(filePath);
                    System.out.println("Removed file = " + filePath);
                }
            } catch (IOException exc) {
                System.out.println("ManagerController.removeLocalFile: deleting error");
                exc.printStackTrace();
            }
        } else {
            throw new FileNotFoundException("File doesn't exist");
        }
    }

    @Override
    public void moveFile(String distDir, String srcDir, String fileName, boolean forceFlag, boolean createNewFlag) throws IOException {
        Path srcPath = Path.of(srcDir).toAbsolutePath().normalize().resolve(fileName);
        Path distPath = Path.of(distDir).toAbsolutePath().normalize().resolve(fileName);
        if (Files.exists(srcPath) && Files.exists(distPath)) {
            if (!Files.exists(distPath)) {
                Files.move(srcPath, distPath);
            } else if (forceFlag) {
                if (Files.isDirectory(srcPath)) {
                    this.moveFolder(srcPath, distPath);
                    this.deleteFile(srcDir, fileName);
                } else {
                    Files.move(srcPath, distPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } else if (createNewFlag) {
                String newFileName = getNextFileName(fileName);
                while (isExists(distDir, newFileName)) newFileName = getNextFileName(newFileName);
                Path newSrcPath = srcPath.resolveSibling(newFileName);
                Path newDistPath = distPath.resolveSibling(newFileName);
                Files.move(srcPath, newSrcPath);
                Files.move(newSrcPath, newDistPath);
            } else {
                throw new FileAlreadyExistsException("File already exists");
            }
        } else {
            throw new FileNotFoundException("File doesn't exist");
        }
    }

    private void uploadFolder(Path localPath, String remotePath, String fileName, SecureFtpChannel channel) throws SftpException {
        try {
            String createdRemoteFolder = remotePath + "/" + fileName;
            channel.makeDir(remotePath,fileName);
            System.out.println("Created dir path: " + createdRemoteFolder);
            LinkedList<Path> files = Files.list(localPath).collect(Collectors.toCollection(LinkedList::new));
            for (Path file : files) {
                Path localFilePath =  localPath.resolve(file);
                String localFileName = file.getFileName().toString();
                if (Files.isDirectory(localFilePath)) {
                    System.out.println("file = " + localFileName + " is Dir. Creating new dir");
                    uploadFolder(localFilePath,createdRemoteFolder,localFileName,channel);
                } else {
                    System.out.println("file = " + localFileName + " is file. Uploading file");
                    channel.uploadFile(localFilePath.toString(),createdRemoteFolder);
                    System.out.println("Uploaded file with name: " + localFileName + " to remote path " + createdRemoteFolder);
                }
            }
        } catch (IOException exc) {
            System.out.println("ManagerController.uploadFolder: recursive method/file listing error");
            exc.printStackTrace();
        }
    }

    private void moveFolder(Path srcPath, Path distPath) throws IOException {
        Files.walk(srcPath).forEach(file -> {
            try {
                if (!Files.isDirectory(file)) {
                    Path fileDist = distPath.resolve(srcPath.relativize(file));
                    Files.move(file, fileDist, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("FILE = " + file + " MOVED TO " + fileDist);
                } else if (!Files.exists(distPath.resolve(srcPath.relativize(file)))) {
                    System.out.println("TRYING TO CREATE DIRECTORY OF FOLDER = " + distPath.resolve(srcPath.relativize(file)));
                    Files.createDirectory(distPath.resolve(srcPath.relativize(file)));
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        });
    }
}
