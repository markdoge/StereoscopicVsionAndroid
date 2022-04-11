package photoFun;


import android.content.Context;
import android.hardware.camera2.*;
import android.util.Log;
import java.util.Arrays;
import java.util.Set;

public class GetCamera {
    private Context context;
    private Object[] cameraID;
    private CameraManager manager;
    private String[] cameraIdList;
    private Set<String> physicalCameraIds;
    private String logicCameraId;

    public GetCamera(Context context_){
        context=context_;
        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        assert manager != null;
        try{
            cameraIdList = manager.getCameraIdList();//获取逻辑ID
            for (String id:cameraIdList){
                try{
                    CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(id);
                    physicalCameraIds = cameraCharacteristics.getPhysicalCameraIds();
                    Log.d("TAG","逻辑ID：" + id + " 下的物理ID: "
                            + Arrays.toString(physicalCameraIds.toArray()));
                    if (physicalCameraIds.size() >= 2){
                        setCamera();
                        logicCameraId=id;
                    }

                }catch (CameraAccessException e){
                    e.printStackTrace();
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void setCamera(){
        cameraID = physicalCameraIds.toArray();
    }
    public String[] getCameraID(){
        String[] camer = new String[cameraID.length];
        for (int i = 0; i< cameraID.length; i++)
            camer[i]= String.valueOf(cameraID[i]);
        return camer;
    }
    public String getLogicCameraId(){
        return logicCameraId;
    }
}
