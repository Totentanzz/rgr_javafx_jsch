package rgr.sshApp.utils;

public abstract class Files implements FilePath {

    @Override
    public String getFileName(String filePath) {
        char separatorChar = filePath.contains("/") ? '/' : '\\';
        int lastSeparatorIndex = filePath.lastIndexOf(separatorChar);
        String fileName = null;
        if (lastSeparatorIndex!=0) {
            fileName = filePath.substring(lastSeparatorIndex + 1);
        }
        return fileName;
    }

}
