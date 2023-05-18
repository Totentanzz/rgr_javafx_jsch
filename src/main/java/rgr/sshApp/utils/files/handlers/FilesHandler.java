package rgr.sshApp.utils.files.handlers;

import rgr.sshApp.model.ModelData;
import rgr.sshApp.web.SecureShellSession;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FilesHandler implements FilePath {

    protected ModelData modelData;
    protected SecureShellSession sshSession;

    public FilesHandler() {
        modelData= ModelData.getInstance();
        sshSession=modelData.getSshSession();
    }

    @Override
    public String getFileName(String filePath) {
        char separatorChar = filePath.contains("/") ? '/' : '\\';
        int lastSeparatorIndex = filePath.lastIndexOf(separatorChar);
        String fileName = null;
        if (filePath.length() > 1) {
            fileName = filePath.substring(lastSeparatorIndex + 1);
        } else if (filePath.length()==1) {
            fileName=filePath;
        }
        return fileName;
    }

    @Override
    public String getNextFileName(String fileName) {
        String regex = "-copy(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        String newFileName = null;
        if (matcher.find()) {
            int nextNumber = Integer.parseInt(matcher.group(1)) + 1;
            newFileName = fileName.replaceAll(regex, "-copy" + nextNumber);
        } else {
            String[] parts = fileName.split("\\.(?=[^\\.]+$)");
            if (parts.length==2 && parts[0].isEmpty()) newFileName= "." + parts[1] + "-copy1";
            else if (parts.length==1) newFileName=parts[0] + "-copy1";
            else newFileName=parts[0] + "-copy1." + parts[1];
        }
        return newFileName;
    }

    @Override
    public String getParentDirectory(String currentPath) {
        char separatorChar = currentPath.contains("/") ? '/' : '\\';
        String separator = String.valueOf(separatorChar);
        int lastSlashIndex = currentPath.lastIndexOf(separator);
        int separatorCount = currentPath.split(Pattern.quote(separator)).length-1;
        String parentDir = null;
        if (separatorCount==1 || separatorCount==0 || separatorCount==-1) {
            parentDir = currentPath.substring(0,lastSlashIndex+1);
        }
        else {
            parentDir = currentPath.substring(0,lastSlashIndex);
        }
        return parentDir;
    }

    @Override
    public String getResolvedDirectory(String currentPath, String fileName) {
        String separator = currentPath.contains("/") ? "/" : "\\";
        String resolvedDir = null;
        if (currentPath.equals(separator) || (currentPath.charAt(2)=='\\' && currentPath.length()==3)) {
            resolvedDir = currentPath + fileName;
        }
        else {
            resolvedDir = currentPath + separator + fileName;
        }
        return resolvedDir;
    }
}
