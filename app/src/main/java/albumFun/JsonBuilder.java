package albumFun;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.*;
//读取格式为.readJsonFile("calibrate1.json"));和.readJsonFile("calibrate2.json"));
public class JsonBuilder {
    private static String pathName = Environment.getExternalStorageDirectory() + "/jsonConfig/";
    private static String fileName = "config.json";

    public void saveToLocal(JSONObject jsonObj) {
        //文件夹路径
        File file;
        File dir = new File(pathName);
        dir.mkdirs();

        JSONObject jsonObject = jsonObj;
        String json = jsonObject.toString();

        file = new File(dir, fileName);
        try {
            OutputStream out = new FileOutputStream(file,true);
            if (dir.exists()) {
                //创建文件夹
                out.write(json.getBytes());
                out.close();
                Log.d("TAG", "保存Config成功 path:" + file.getPath());
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
            if (dir.exists()) {
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
                OutputStream out = new FileOutputStream(file,false);
                out.write(json.getBytes());
                out.close();
                Log.d("json", "保存Config成功 path:" + file.getPath());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("json", "Config保存失败");
        }
    }

    public String readJsonFile(String fileName) {
        StringBuilder sb = new StringBuilder();
        String filePath = pathName + fileName;
        try {
            File file = new File(filePath);
            Log.d("json", String.valueOf(file.exists()));
        }
        catch (Exception e){
            Log.d("json",e.toString());
        }
        try {
            File file = new File(filePath);
            InputStream in = new FileInputStream(file);
            int tempbyte;
            while ((tempbyte = in.read()) != -1) {
                sb.append((char) tempbyte);
            }
            in.close();
        } catch (Exception e) {
            Log.d("json",e.toString());
        }
        return sb.toString();
    }
}
