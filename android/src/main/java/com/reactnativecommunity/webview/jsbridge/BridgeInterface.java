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

	private enum MessageType {
		MessageTypeOpen,
		MessageTypeClose,
		MessageTypeNavConfig,
		MessageTypeShareData,
		MessageTypeSessionShareData,
		MessageTypeTimelineShareData,
		MessageTypeFeedShareData,
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
				JSONObject result = new JSONObject();
				try {
					result.put("status", convertResponseStatus(ResponseStatus.ResponseStatusSuccess));
					result.put("msg", "getToken:ok");
					result.put("data", getStorageByKey("token"));
					function.onCallBack(result.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		// 获取唾液盒信息
		webView.registerHandler("getBoxInfo", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				JSONObject result = new JSONObject();
				try {
					result.put("status", convertResponseStatus(ResponseStatus.ResponseStatusSuccess));
					result.put("msg", "getBoxInfo:ok");
					result.put("data", getStorageByKey("box"));
					function.onCallBack(result.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		});
		// 获取设备信息
		webView.registerHandler("getDeviceInfo", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				JSONObject result = new JSONObject();
				JSONObject detail = new JSONObject();
				JSONObject safeAreaInsets = new JSONObject();
				try {
					safeAreaInsets.put("top", getStatusBarHeight());
					safeAreaInsets.put("right", 0);
					safeAreaInsets.put("bottom", 0);
					safeAreaInsets.put("left", 0);
					detail.put("width", getDisplay().get("width"));
					detail.put("height", getDisplay().get("height"));
					detail.put("version", getVersionName());
					detail.put("safeAreaInsets", safeAreaInsets);
					result.put("status", convertResponseStatus(ResponseStatus.ResponseStatusSuccess));
					result.put("msg", "getDeviceInfo:ok");
					result.put("data", detail);
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
		// 将 Base64 格式图片保存至本地
		webView.registerHandler("saveBase64ImgToLocal", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				try {
					String base64ImageString = new JSONObject(data).getString("data");
					if (base64ImageString != null) {
						base64ImageString = base64ImageString.replaceAll("data:image\\/\\w+;base64,", "");
						byte[] decodedString = Base64.decode(base64ImageString, Base64.DEFAULT);
						Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
						String fileName = "/genebox/share_image_" + System.currentTimeMillis() + ".png";
						String imagePath = Environment.getExternalStorageDirectory() + fileName;
						File file = Environment.getExternalStorageDirectory();
						File f = new File(file, fileName);
						try {
							// 生成文件
							FileOutputStream fo = new FileOutputStream(f);
							bitmap.compress(Bitmap.CompressFormat.PNG, 90, fo);
							fo.flush();
							fo.close();
							// 返回结果
							JSONObject result = new JSONObject();
							JSONObject path = new JSONObject();
							path.put("imagePath", "file://" + imagePath);
							try {
								result.put("status", convertResponseStatus(ResponseStatus.ResponseStatusSuccess));
								result.put("msg", "saveBase64ImgToLocal:ok");
								result.put("data", path);
								function.onCallBack(result.toString());
							} catch (JSONException e) {
								e.printStackTrace();
							}
							JSONObject event = new JSONObject();
							event.put("type", convertMessageType(MessageType.MessageTypeLocalImagePath));
							event.put("data", path);
							webView.onMessage(event.toString());
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}



	private String getStorageByKey(String storageKey) {
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
				return "";
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
			return jsonObject.get(storageKey).toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}

	private void handleMessage(String name, String data, MessageType messageType, CallBackFunction callBackFunction) {
		JSONObject event = new JSONObject();
		JSONObject result = new JSONObject();
		try {
			event.put("type", convertMessageType(messageType));
			event.put("data", data);
			webView.onMessage(event.toString());
			result.put("status", convertResponseStatus(ResponseStatus.ResponseStatusSuccess));
			result.put("msg", name + ":ok");
			result.put("data", event);
			callBackFunction.onCallBack(result.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private int convertResponseStatus(ResponseStatus responseStatus) {
		switch (responseStatus) {
			case ResponseStatusSuccess:
				return 0;
			case ResponseStatusCancel:
				return 1;
			default:
				return -1;
		}
	}

	private String convertMessageType(MessageType messageType) {
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
