/**
 *
 */
package com.abhi.toyswap.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;


/**
 * @author abgupta4
 */
public class Utils {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    public static String createWebServiceURL(int requestType) {
        switch (requestType) {
            case Constants.LOGIN: {
                return Constants.URLPREFIX + "actionUser.php";
            }
            case Constants.REGISTER: {
                return Constants.URLPREFIX + "actionUser.php";
            }
            case Constants.SUBMIT_TOKEN: {
                return Constants.URLPREFIX + "addtokenws.php";
            }
            case Constants.GET_CATEGORIES: {
                return Constants.URLPREFIX + "actionCategory.php";
            }
            case Constants.GET_ITEMS: {
                return Constants.URLPREFIX + "actionItem.php";
            }
            case Constants.SAVE_MESSAGE: {
                return Constants.URLPREFIX + "actionMesssages.php";
            }
            case Constants.POST_ITEM: {
                return Constants.URLPREFIX + "actionItem.php";
            }case Constants.GET_MESSAGE_THREADS:{
                return Constants.URLPREFIX+"actionMesssages.php";
            }case Constants.GET_LIKES_DISLIKES:{
                return Constants.URLPREFIX+"actionUser.php";
            }case Constants.DELETE_OR_SOLD_ITEM:{
                return Constants.URLPREFIX+"actionItem.php";
            }case Constants.TEMPLATE_MESSAGES:{
                return Constants.URLPREFIX + "actionTemplateMessage.php";
            }case Constants.REPORT_MESSAGES:{
                return Constants.URLPREFIX + "actionReportedMsgs.php";
            }
        }
        return Constants.URLPREFIX;
    }

    public static String getDeviceId(Context appContext, Activity activityInstance) {

        final TelephonyManager tm = (TelephonyManager) appContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getDeviceId() == null || tm.getDeviceId().isEmpty() || tm.getDeviceId().equals("000000000000000")) {
            return "878766227872504";
        }

        return tm.getDeviceId();


    }

    public static byte[] convertFileToByteArray(File f) {
        byte[] byteArray = null;
        try {
            InputStream inputStream = new FileInputStream(f);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024 * 8];
            int bytesRead = 0;

            while ((bytesRead = inputStream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    public static void saveBooleanIntoSharedPreferences(Activity mainAppContext, String keyName, boolean booleanValue) {
        SharedPreferences sharedPref = mainAppContext.getSharedPreferences("NegiSportsApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(keyName, booleanValue);
        editor.commit();
    }

    public static boolean getBooleanDataFromSharedPreferences(Activity mainAppContext, String keyName) {
        SharedPreferences sharedPref = mainAppContext.getSharedPreferences("NegiSportsApp", Context.MODE_PRIVATE);
        return sharedPref.getBoolean(keyName, false);
    }

    public static void saveDataIntoSharedPreferences(Activity mainAppContext, String keyName, String keyValue) {
        SharedPreferences sharedPref = mainAppContext.getSharedPreferences("NegiSportsApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyName, keyValue);
        editor.commit();
    }
    public static void saveDataIntoSharedPreferences(Context mainAppContext, String keyName, String keyValue) {
        SharedPreferences sharedPref = mainAppContext.getSharedPreferences("NegiSportsApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyName, keyValue);
        editor.commit();
    }

    public static void deleteDataFromSharedPreferences(Activity mainAppContext, String keyName) {
        SharedPreferences sharedPref = mainAppContext.getSharedPreferences("NegiSportsApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(keyName);
        editor.commit();
    }

    public static String getDataFromSharedPreferences(Activity mainAppContext, String keyName) {
        SharedPreferences sharedPref = mainAppContext.getSharedPreferences("NegiSportsApp", Context.MODE_PRIVATE);
        return sharedPref.getString(keyName, null);
    }

    public static void log(String message) {
        Log.i("Abhi", message);
    }

    public static void writeLogIntoFile(String logMessage) {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd,MMM-yyyy HH:mm");

        File logFile= new File(Environment.getExternalStorageDirectory() + "/ToysAppLogFile.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
                    true));

            buf.append("-------------------->>> \r\n");
            buf.append(dateFormatGmt.format(new Date())+"\r\n");
            buf.append(logMessage);
            buf.append("\r\n<<<-------------------- \r\n");

            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
