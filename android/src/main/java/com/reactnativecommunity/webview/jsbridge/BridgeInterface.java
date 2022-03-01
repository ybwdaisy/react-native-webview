package com.reactnativecommunity.webview.jsbridge;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.storage.ReactDatabaseSupplier;
import com.facebook.react.uimanager.ThemedReactContext;
import com.reactnativecommunity.webview.RNCWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class BridgeInterface {

	private ThemedReactContext reactContext;
	private RNCWebView webView;

	private enum ResponseStatus {
		ResponseStatusSuccess,
		ResponseStatusCancel,
		ResponseStatusFail,
	}

	public enum MessageType {
		MessageTypeOpen,
		MessageTypeClose,
		MessageTypeNavConfig,
		MessageTypeShareData,
		MessageTypeSessionShareData,
		MessageTypeTimelineShareData,
		MessageTypeFeedShareData,
		MessageTypeWechatAuth,
		MessageTypeShare,
		MessageTypeShareSession,
		MessageTypeShareTimeline,
		MessageTypeShareFeed,
		MessageTypeLocalImagePath,
	}

	public BridgeInterface(ThemedReactContext reactContext, RNCWebView webView) {
		this.reactContext = reactContext;
		this.webView = webView;
	}

	public void registerHandlers () {
		// 获取登录token
		webView.registerHandler("getToken", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				Object token = getStorageByKey("token");
				JSONObject result;
				if (token != null) {
					result = createResultObject(
							ResponseStatus.ResponseStatusSuccess,
							"getToken:ok",
							token
					);
				} else {
					result = createResultObject(
							ResponseStatus.ResponseStatusFail,
							"getToken:fail",
							null
					);
				}
				function.onCallBack(result.toString());
			}
		});
		// 获取唾液盒信息
		webView.registerHandler("getBoxInfo", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				Object box = getStorageByKey("box");
				JSONObject result;
				if (box != null) {
					result = createResultObject(
							ResponseStatus.ResponseStatusSuccess,
							"getBoxInfo:ok",
							box
					);
				} else {
					result = createResultObject(
							ResponseStatus.ResponseStatusFail,
							"getBoxInfo:fail",
							null
					);
				}

				function.onCallBack(result.toString());

			}
		});
		// 获取设备信息
		webView.registerHandler("getDeviceInfo", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				JSONObject safeAreaInsets = new JSONObject();
				try {
					safeAreaInsets.put("top", getStatusBarHeight());
					safeAreaInsets.put("right", 0);
					safeAreaInsets.put("bottom", 0);
					safeAreaInsets.put("left", 0);
					JSONObject detail = new JSONObject();
					detail.put("width", getDisplay().get("width"));
					detail.put("height", getDisplay().get("height"));
					detail.put("version", getVersionName());
					detail.put("safeAreaInsets", safeAreaInsets);
					JSONObject result = createResultObject(
							ResponseStatus.ResponseStatusSuccess,
							"getDeviceInfo:ok",
							detail
					);
					function.onCallBack(result.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		// 打开RN页面
		webView.registerHandler("openPage", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				handleMessage("openPage", data, MessageType.MessageTypeOpen, function);
			}
		});
		// 关闭浏览器窗口
		webView.registerHandler("closeWindow", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				handleMessage("closeWindow", data, MessageType.MessageTypeClose, function);
			}
		});
		// 配置导航
		webView.registerHandler("setNavConfig", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				handleMessage("setNavConfig", data, MessageType.MessageTypeNavConfig, function);
			}
		});
		// 配置分享到微信信息，如果不调用 updateSessionShareData 和 updateTimelineShareData 方法会取此配置
		webView.registerHandler("setShareData", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				handleMessage("setShareData", data, MessageType.MessageTypeShareData, function);
			}
		});
		// 配置分享到微信会话信息
		webView.registerHandler("updateSessionShareData", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				handleMessage("updateSessionShareData", data, MessageType.MessageTypeSessionShareData, function);
			}
		});
		// 配置分享到微信朋友圈信息
		webView.registerHandler("updateTimelineShareData", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				handleMessage("updateTimelineShareData", data, MessageType.MessageTypeTimelineShareData, function);
			}
		});
		// 配置分享到社区信息
		webView.registerHandler("updateFeedShareData", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				handleMessage("updateFeedShareData", data, MessageType.MessageTypeFeedShareData, function);
			}
		});
		// 微信授权
		webView.registerHandler("wechatAuth", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				handleMessage("wechatAuth", data, MessageType.MessageTypeWechatAuth, function);
			}
		});
		// 分享弹窗
		webView.registerHandler("share", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				handleMessage("share", data, MessageType.MessageTypeShare, function);
			}
		});
		// 分享到微信会话
		webView.registerHandler("shareToSession", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				handleMessage("shareToSession", data, MessageType.MessageTypeShareSession, function);
			}
		});
		// 分享到微信朋友圈
		webView.registerHandler("shareToTimeline", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				handleMessage("shareToTimeline", data, MessageType.MessageTypeShareTimeline, function);
			}
		});
		// 分享到社区
		webView.registerHandler("shareToFeed", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				handleMessage("shareToFeed", data, MessageType.MessageTypeShareFeed, function);
			}
		});
		// 分片上传 Base64 图片
		webView.registerHandler("uploadBase64Image", new BridgeHandler() {
			final ArrayList base64Array = new ArrayList<JSONObject>();
			@Override
			public void handler(String data, CallBackFunction function) {
				try {
					JSONObject params = new JSONObject(data);
					Integer totalCount = params.getInt("length");
					Integer arrayCount = base64Array.size();
					if (arrayCount < totalCount - 1) {
						base64Array.add(params);
						JSONObject result = createResultObject(
								ResponseStatus.ResponseStatusSuccess,
								"getDeviceInfo:ok",
								null
						);
						function.onCallBack(result.toString());
						return;
					}
					base64Array.add(params);
					JSONObject result = createResultObject(
							ResponseStatus.ResponseStatusSuccess,
							"getDeviceInfo:ok",
							null
					);
					function.onCallBack(result.toString());

					String base64ImageString = "";
					for(int i = 0; i < base64Array.size() ; i ++){
						JSONObject item = (JSONObject) base64Array.get(i);
						base64ImageString += item.getString("fragment");
					}

					String imagePath = saveBase64ImgToLocal(base64ImageString);
					JSONObject pathObject = new JSONObject();
					pathObject.put("imagePath", imagePath);
					JSONObject event = createEventObject(MessageType.MessageTypeLocalImagePath, pathObject);
					webView.onMessage(event.toString());
					base64Array.clear();

				} catch (JSONException e) {
					base64Array.clear();
					e.printStackTrace();
				}
			}
		});

	}

	public JSONObject handleCallJavaScriptMethod(String handlerName, String data) {
		return null;
	}

	private String saveBase64ImgToLocal(String base64ImageString) {
		String imageString = base64ImageString;
		if (imageString != null) {
			imageString = imageString.replaceAll("data:image\\/\\w+;base64,", "");
			byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
			Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
			String dir = Environment.getExternalStorageDirectory() + "/genebox/";
			File fileDir = new File(dir);
			if(!fileDir.exists()) {
				fileDir.mkdir();
			}
			String fileName = "share_image_" + System.currentTimeMillis() + ".png";
			String imagePath = dir + fileName;
			try {
				// 生成文件
				File f = new File(imagePath);
				f.createNewFile();
				FileOutputStream fo = new FileOutputStream(f);
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, fo);
				fo.flush();
				fo.close();
				return "file://" + imagePath;
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}
		}
		return "";
	}

	private JSONObject createResultObject(ResponseStatus responseStatus, String msg, Object data) {
		JSONObject result = new JSONObject();
		try {
			result.put("status", convertResponseStatus(responseStatus));
			result.put("msg", msg);
			result.put("data", data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	private JSONObject createEventObject(MessageType messageType, Object data) {
		JSONObject event = new JSONObject();
		try {
			event.put("type", convertMessageType(messageType));
			event.put("data", data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return event;
	}

	private Object getStorageByKey(String storageKey) {
		String[] columns = {"key", "value"};
		String[] keys = {"persist:primary"};

		HashSet<String> keysRemaining = new HashSet<>();
		WritableArray result = Arguments.createArray();
		for (int keyStart = 0; keyStart < keys.length; keyStart += 999) {
			int keyCount = Math.min(keys.length - keyStart, 999);
			Cursor cursor = ReactDatabaseSupplier.getInstance(reactContext).get()
					.query(
							"catalystLocalStorage",
							columns,
							buildKeySelection(keyCount),
							buildKeySelectionArgs(keys, keyStart, keyCount),
							null,
							null,
							null
					);
			keysRemaining.clear();
			try {
				if (cursor.getCount() != keys.length) {
					for (int keyIndex = keyStart; keyIndex < keyStart + keyCount; keyIndex++) {
						keysRemaining.add(keys[keyIndex]);
					}
				}

				if (cursor.moveToFirst()) {
					do {
						WritableArray row = Arguments.createArray();
						row.pushString(cursor.getString(0));
						row.pushString(cursor.getString(1));
						result.pushArray(row);
						keysRemaining.remove(cursor.getString(0));
					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				FLog.w(ReactConstants.TAG, e.getMessage(), e);
				return null;
			} finally {
				cursor.close();
			}

			for (String key : keysRemaining) {
				WritableArray row = Arguments.createArray();
				row.pushString(key);
				row.pushNull();
				result.pushArray(row);
			}
			keysRemaining.clear();
		}

		String jsonToken = result.getArray(0).getString(1);
		try {
			JSONObject jsonObject = new JSONObject(jsonToken);
			String targetJsonString = String.valueOf(jsonObject.get(storageKey));
			JSONObject storageObject = new JSONObject(targetJsonString);
			return storageObject;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void handleMessage(String name, String data, MessageType messageType, CallBackFunction callBackFunction) {
		JSONObject event = createEventObject(messageType, data);
		webView.onMessage(event.toString());
		JSONObject result = createResultObject(ResponseStatus.ResponseStatusSuccess, name + ":ok", event);
		callBackFunction.onCallBack(result.toString());
	}

	private int convertResponseStatus(ResponseStatus responseStatus) {
		switch (responseStatus) {
			case ResponseStatusSuccess:
				return 0;
			case ResponseStatusCancel:
				return 1;
			case ResponseStatusFail:
				return -1;
		}
		return 0;
	}

	public String convertMessageType(MessageType messageType) {
		switch (messageType) {
			case MessageTypeOpen:
				return "open";
			case MessageTypeClose:
				return "close";
			case MessageTypeNavConfig:
				return "navConfig";
			case MessageTypeShareData:
				return "shareData";
			case MessageTypeSessionShareData:
				return "sessionShareData";
			case MessageTypeTimelineShareData:
				return "timelineShareData";
			case MessageTypeFeedShareData:
				return "feedShareData";
			case MessageTypeWechatAuth:
				return "wechatAuth";
			case MessageTypeShare:
				return "share";
			case MessageTypeShareSession:
				return "shareSession";
			case MessageTypeShareTimeline:
				return "shareTimeline";
			case MessageTypeShareFeed:
				return "shareFeed";
			case MessageTypeLocalImagePath:
				return "localImagePath";
			default:
				return "";
		}
	}

	private static String buildKeySelection(int selectionCount) {
		String[] list = new String[selectionCount];
		Arrays.fill(list, "?");
		return "key IN (" + TextUtils.join(", ", list) + ")";
	}

	private static String[] buildKeySelectionArgs(String[] keys, int start, int count) {
		String[] selectionArgs = new String[count];
		for (int keyIndex = 0; keyIndex < count; keyIndex++) {
			selectionArgs[keyIndex] = keys[start + keyIndex];
		}
		return selectionArgs;
	}

	private String getVersionName() {
		PackageManager packageManager = reactContext.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(reactContext.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}

	private JSONObject getDisplay() {
		JSONObject screen = new JSONObject();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		Display display = reactContext.getCurrentActivity().getWindowManager().getDefaultDisplay();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			display.getRealMetrics(displayMetrics);
		} else {
			display.getMetrics(displayMetrics);
		}
		try {
			screen.put("width", displayMetrics.widthPixels / displayMetrics.density);
			screen.put("height", displayMetrics.heightPixels / displayMetrics.density);
			return screen;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return screen;
	}

	private int getStatusBarHeight() {
		int result = 0;
		DisplayMetrics displayMetrics = reactContext.getResources().getDisplayMetrics();
		int resourceId = reactContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = (int) (reactContext.getResources().getDimensionPixelOffset(resourceId) / displayMetrics.density);
		}
		return result;
	}
}
