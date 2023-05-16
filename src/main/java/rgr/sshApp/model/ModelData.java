package rgr.sshApp.model;

import lombok.Data;

import rgr.sshApp.web.SecureShellSession;

@Data
public class ModelData {

    private static volatile ModelData modelData;
    private SecureShellSession SshSession;

    public static ModelData getInstance() {
        ModelData localModelData = modelData;
        if (localModelData ==null){
            synchronized (ModelData.class){
                localModelData = modelData;
                if (localModelData ==null){
                    modelData = localModelData = new ModelData();
                }
            }
        }
        return localModelData;
    }

}
