package albumFun;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.*;

public class JsonBuilder {
    private static String pathName = Environment.getExternalStorageDirectory() + "/jsonConfig/calibrate";
    private static String fileName = "config.json";

    public void saveToLocal(JSONObject jsonObj) {
        //文件夹路径
        File file;
        File dir = new File(pathName);
        try {
            //文件夹不存在和传入的value值为1时，才允许进入创建
            if (!dir.exists()) {
                //创建文件夹
                dir.mkdirs();

                JSONObject jsonObject = jsonObj;
                String json = jsonObject.toString();

                file = new File(dir, fileName);
                OutputStream out = new FileOutputStream(file);
                out.write(json.getBytes());
                out.close();
                Log.d("TAG", "保存Config成功 path:" + file.getPath());
            } else {
                Log.d("TAG", "Config已经存在 path:" + dir + "/" + fileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("TAG", "Config保存失败");
        }
    }

    public void saveName(String filename_) {
        fileName = filename_;
    }
    public void saveToLocal(double array1[], double array2[], String NodeName1, String NodeName2) {
        //文件夹路径
        File file;
        File dir = new File(pathName);
        JSONArray jsonArray1 = new JSONArray();
        JSONArray jsonArray2 = new JSONArray();
        try {
            if (!dir.exists()) {
                //创建文件夹
                dir.mkdirs();
                for (int i = 0; i < array1.length; i++) {  //依次将数组元素添加进JSONArray对象中
                    jsonArray1.put(array1[i]);
                    try {
                        jsonArray1.put(i, array1[i]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < array2.length; i++) {  //依次将数组元素添加进JSONArray对象中
                    jsonArray2.put(array2[i]);
                    try {
                        jsonArray2.put(i, array2[i]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(NodeName1, jsonArray1);
                jsonObject.put(NodeName2, jsonArray2);
                String json = jsonObject.toString();

                file = new File(dir, fileName);
                OutputStream out = new FileOutputStream(file,true);
                out.write(json.getBytes());
                out.close();
                Log.d("json", "保存Config成功 path:" + file.getPath());
            } else {
                Log.d("json", "Config已经存在 path:" + dir + "/" + fileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("json", "Config保存失败");
        }
    }

    public String readJsonFile(String fileName) {
        StringBuilder sb = new StringBuilder();
        String filePath = pathName + fileName;
        Log.d("json", "read path: "+filePath);
        try {
            File file = new File(filePath);
            InputStream in = new FileInputStream(file);
            int tempbyte;
            while ((tempbyte = in.read()) != -1) {
                sb.append((char) tempbyte);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
