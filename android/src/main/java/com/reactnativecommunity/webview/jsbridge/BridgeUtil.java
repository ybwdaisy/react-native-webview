package com.reactnativecommunity.webview.jsbridge;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BridgeUtil {
	public final static String CUSTOM_OVERRIDE_SCHEMA = "genebox://";
	public final static String CUSTOM_RETURN_DATA = CUSTOM_OVERRIDE_SCHEMA + "return/";//格式为   https://return/{function}/returncontent
	final static String CUSTOM_FETCH_QUEUE = CUSTOM_RETURN_DATA + "_fetchQueue/";
	final static String EMPTY_STR = "";
	final static String UNDERLINE_STR = "_";
	final static String SPLIT_MARK = "/";
	
	final static String CALLBACK_ID_FORMAT = "JAVA_CB_%s";
	final static String JS_HANDLE_MESSAGE_FROM_JAVA = "javascript:WebViewJavascriptBridge._handleMessageFromNative('%s');";
	final static String JS_FETCH_QUEUE_FROM_JAVA = "javascript:WebViewJavascriptBridge._fetchQueue();";
	
	public static String parseFunctionName(String jsUrl){
		return jsUrl.replace("javascript:WebViewJavascriptBridge.", "").replaceAll("\\(.*\\);", "");
	}
	
	
	public static String getDataFromReturnUrl(String url) {
		if(url.startsWith(CUSTOM_FETCH_QUEUE)) {
			return url.replace(CUSTOM_FETCH_QUEUE, EMPTY_STR);
		}
		
		String temp = url.replace(CUSTOM_RETURN_DATA, EMPTY_STR);
		String[] functionAndData = temp.split(SPLIT_MARK);

        if(functionAndData.length >= 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < functionAndData.length; i++) {
                sb.append(functionAndData[i]);
            }
            return sb.toString();
        }
		return null;
	}

	public static String getFunctionFromReturnUrl(String url) {
		String temp = url.replace(CUSTOM_RETURN_DATA, EMPTY_STR);
		String[] functionAndData = temp.split(SPLIT_MARK);
		if(functionAndData.length >= 1){
			return functionAndData[0];
		}
		return null;
	}


	public static String assetFile2Str(Context c, String urlStr){
		InputStream in = null;
		try{
			in = c.getAssets().open(urlStr);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder sb = new StringBuilder();
            do {
                line = bufferedReader.readLine();
                if (line != null && !line.matches("^\\s*\\/\\/.*")) {
                    sb.append(line);
                }
            } while (line != null);

            bufferedReader.close();
            in.close();
 
            return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
}
