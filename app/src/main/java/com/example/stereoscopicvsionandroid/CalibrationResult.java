package com.example.stereoscopicvsionandroid;

import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import albumFun.JsonBuilder;

public abstract class CalibrationResult {
    private static final String TAG = "TAG";
    private static final int CAMERA_MATRIX_ROWS = 3;
    private static final int CAMERA_MATRIX_COLS = 3;
    private static final int DISTORTION_COEFFICIENTS_SIZE = 5;
    private static boolean counter = false;
    private static String name1="calibrate1.json";
    private static String name2="calibrate2.json";

    public static void save(Activity activity, Mat cameraMatrix, Mat distortionCoefficients) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        JsonBuilder jsonBuilder = new JsonBuilder();
        double[] cameraMatrixArray = new double[CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS];
        cameraMatrix.get(0, 0, cameraMatrixArray);
        for (int i = 0; i < CAMERA_MATRIX_ROWS; i++) {
            for (int j = 0; j < CAMERA_MATRIX_COLS; j++) {
                int id = i * CAMERA_MATRIX_ROWS + j;
                editor.putFloat(Integer.toString(id), (float) cameraMatrixArray[id]);
            }
        }

        double[] distortionCoefficientsArray = new double[DISTORTION_COEFFICIENTS_SIZE];
        distortionCoefficients.get(0, 0, distortionCoefficientsArray);
        int shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS;
        for (int i = shift; i < DISTORTION_COEFFICIENTS_SIZE + shift; i++) {
            editor.putFloat(Integer.toString(i), (float) distortionCoefficientsArray[i - shift]);
        }
        if (counter==false){
            jsonBuilder.saveName(name1);
            jsonBuilder.saveToLocal(cameraMatrixArray, distortionCoefficientsArray, "cameraMatrix",
                    "distortionCoefficients");}
        if (counter==true){
            jsonBuilder.saveName(name2);
            jsonBuilder.saveToLocal(cameraMatrixArray, distortionCoefficientsArray, "cameraMatrix",
                    "distortionCoefficients");}
        counter=!counter;
        editor.apply();
        editor.commit();
    }

    public static boolean tryLoad(Activity activity, Mat cameraMatrix, Mat distortionCoefficients) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        if (sharedPref.getFloat("0", -1) == -1) {
            return false;
        }
        double[] cameraMatrixArray = new double[CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS];
        for (int i = 0; i < CAMERA_MATRIX_ROWS; i++) {
            for (int j = 0; j < CAMERA_MATRIX_COLS; j++) {
                int id = i * CAMERA_MATRIX_ROWS + j;
                cameraMatrixArray[id] = sharedPref.getFloat(Integer.toString(id), -1);
            }
        }
        cameraMatrix.put(0, 0, cameraMatrixArray);

        double[] distortionCoefficientsArray = new double[DISTORTION_COEFFICIENTS_SIZE];
        int shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS;
        for (int i = shift; i < DISTORTION_COEFFICIENTS_SIZE + shift; i++) {
            distortionCoefficientsArray[i - shift] = sharedPref.getFloat(Integer.toString(i), -1);
        }
        distortionCoefficients.put(0, 0, distortionCoefficientsArray);

        return true;
    }
}
