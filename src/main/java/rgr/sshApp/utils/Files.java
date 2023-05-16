package rgr.sshApp.utils;

import rgr.sshApp.model.ModelData;
import rgr.sshApp.web.SecureShellSession;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Files implements FilePath {

    protected ModelData modelData;
    protected SecureShellSession sshSession;

    public Files() {
        modelData=ModelData.getInstance();
        sshSession=modelData.getSshSession();
    }

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

    @Override
    public String getNextFileName(String fileName) {
        String regex = "-copy(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        String newFileName = null;
        if (matcher.find()) {
            int nextNumber = Integer.parseInt(matcher.group(1)) + 1;
            newFileName = fileName.replaceAll(regex, "-copy" + nextNumber);
            return newFileName;
        } else {
            String[] parts = fileName.split("\\.(?=[^\\.]+$)");
            newFileName = (parts.length==1 ?  parts[0] + "-copy1" : parts[0] + "-copy1." + parts[1]);
        }
        return newFileName;
    }
}
