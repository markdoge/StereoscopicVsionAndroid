package photoFun;


import android.content.Context;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.stereoscopicvsionandroid.MainActivity;
import com.example.stereoscopicvsionandroid.R;

import org.opencv.core.Point;

import java.util.*;

public class GetCamera {
    private Context context;
    private Object[] cameraID;
    private CameraManager manager;
    private String[] cameraIdList;
    private Set<String> physicalCameraIds;
    private String logicCameraId;
    private List<String> logicCameraIds=new ArrayList<>();
    private CameraCharacteristics characteristics;
    private StreamConfigurationMap streamConfigurationMap;
    private Size[]videoSize;
    private Point point;

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
                    if (physicalCameraIds.size() >= 2){
                        setCamera();
                        logicCameraId=id;
                    }
                    else{
                        logicCameraIds.add(id);
                        if(logicCameraId==null){
                            logicCameraId=logicCameraIds.get(0);
                            setWithLogicCamera();
                        }
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
        try {
            characteristics=manager.getCameraCharacteristics(String.valueOf(cameraID[0]));
            streamConfigurationMap=characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert streamConfigurationMap!=null;
            videoSize =streamConfigurationMap.getOutputSizes(MediaRecorder.class);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }
    private void setWithLogicCamera(){
        try{
            characteristics=manager.getCameraCharacteristics(String.valueOf(logicCameraId));
            streamConfigurationMap=characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert streamConfigurationMap!=null;
            videoSize =streamConfigurationMap.getOutputSizes(MediaRecorder.class);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    public String[] getCameraID(){
        if (cameraID==null){
            String[] camer = new String[1];
            camer[0]=String.valueOf(logicCameraIds.get(0));
            return camer;
        }
        String[] camer = new String[cameraID.length];
        for (int i = 0; i< cameraID.length; i++)
            camer[i]= String.valueOf(cameraID[i]);
        return camer;
    }
    public String getLogicCameraId(){
        return logicCameraId;
    }
    public Size[] getVideoSize(){
        return videoSize;
    }
    public List<String> getLogicCameraIds(){
        return logicCameraIds;
    }
}
