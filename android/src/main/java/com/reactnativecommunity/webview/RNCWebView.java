package com.reactnativecommunity.webview;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.ContentSizeChangeEvent;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.facebook.react.views.scroll.OnScrollDispatchHelper;
import com.facebook.react.views.scroll.ScrollEvent;
import com.facebook.react.views.scroll.ScrollEventType;
import com.reactnativecommunity.webview.events.TopMessageEvent;
import com.reactnativecommunity.webview.jsbridge.BridgeHandler;
import com.reactnativecommunity.webview.jsbridge.BridgeHelper;
import com.reactnativecommunity.webview.jsbridge.BridgeInterface;
import com.reactnativecommunity.webview.jsbridge.CallBackFunction;
import com.reactnativecommunity.webview.jsbridge.WebViewJavascriptBridge;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.Nullable;

/**
 * Subclass of {@link WebView} that implements {@link LifecycleEventListener} interface in order
 * to call {@link WebView#destroy} on activity destroy event and also to clear the client
 */

public class RNCWebView extends WebView implements LifecycleEventListener, WebViewJavascriptBridge {
	protected @Nullable
	String injectedJS;
	protected boolean messagingEnabled = false;
	protected @Nullable
	RNCWebViewClient mRNCWebViewClient;
	protected boolean sendContentSizeChangeEvents = false;
	private OnScrollDispatchHelper mOnScrollDispatchHelper;
	protected boolean hasScrollEvent = false;
	protected static final String JAVASCRIPT_INTERFACE = "ReactNativeWebView";
	public BridgeHelper bridgeHelper;

	/**
	 * WebView must be created with an context of the current activity
	 * <p>
	 * Activity Context is required for creation of dialogs internally by WebView
	 * Reactive Native needed for access to ReactNative internal system functionality
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public RNCWebView(ThemedReactContext reactContext) {
		super(reactContext);
		this.setVerticalScrollBarEnabled(false);
		this.setHorizontalScrollBarEnabled(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}
		WebSettings settings = this.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);
		settings.setAllowFileAccess(false);
		settings.setAllowContentAccess(false);
		settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setSupportZoom(true);
		settings.setDefaultTextEncodingName("utf-8");
		bridgeHelper = new BridgeHelper(this);
		setWebViewClient(new RNCWebViewClient(this));
	}

	public void setSendContentSizeChangeEvents(boolean sendContentSizeChangeEvents) {
		this.sendContentSizeChangeEvents = sendContentSizeChangeEvents;
	}

	public void setHasScrollEvent(boolean hasScrollEvent) {
		this.hasScrollEvent = hasScrollEvent;
	}

	@Override
	public void onHostResume() {
		// do nothing
	}

	@Override
	public void onHostPause() {
		// do nothing
	}

	@Override
	public void onHostDestroy() {
		cleanupCallbacksAndDestroy();
	}

	@Override
	protected void onSizeChanged(int w, int h, int ow, int oh) {
		super.onSizeChanged(w, h, ow, oh);

		if (sendContentSizeChangeEvents) {
			dispatchEvent(
					this,
					new ContentSizeChangeEvent(
							this.getId(),
							w,
							h
					)
			);
		}
	}

	@Override
	public void setWebViewClient(WebViewClient client) {
		super.setWebViewClient(client);
		if (client instanceof RNCWebViewClient) {
			mRNCWebViewClient = new RNCWebViewClient(this);
		}
	}

	public @Nullable
	RNCWebViewClient getRNCWebViewClient() {
		return mRNCWebViewClient;
	}

	public void setInjectedJavaScript(@Nullable String js) {
		injectedJS = js;
	}

	protected RNCWebViewBridge createRNCWebViewBridge(RNCWebView webView) {
		return new RNCWebViewBridge(webView);
	}

	@SuppressLint("AddJavascriptInterface")
	public void setMessagingEnabled(boolean enabled) {
		if (messagingEnabled == enabled) {
			return;
		}

		messagingEnabled = enabled;

		if (enabled) {
			addJavascriptInterface(createRNCWebViewBridge(this), JAVASCRIPT_INTERFACE);
		} else {
			removeJavascriptInterface(JAVASCRIPT_INTERFACE);
		}
	}

	protected void evaluateJavascriptWithFallback(String script) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			evaluateJavascript(script, null);
			return;
		}

		try {
			loadUrl("javascript:" + URLEncoder.encode(script, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// UTF-8 should always be supported
			throw new RuntimeException(e);
		}
	}

	public void callInjectedJavaScript() {
		if (getSettings().getJavaScriptEnabled() &&
				injectedJS != null &&
				!TextUtils.isEmpty(injectedJS)) {
			evaluateJavascriptWithFallback("(function() {\n" + injectedJS + ";\n})();");
		}
	}

	public void callJavaScriptBridgeHandler(String handlerName, String data, BridgeInterface bridgeInterface) {
		this.callHandler(handlerName, data, new CallBackFunction() {
			@Override
			public void onCallBack(String data) {
				// 区分不同 hanlderName 处理不同逻辑
				JSONObject message = bridgeInterface.handleCallJavaScriptMethod(handlerName, data);
				if (message != null) {
					onMessage(message.toString());
				}
			}
		});
	}

	public void onMessage(String message) {
		if (mRNCWebViewClient != null) {
			WebView webView = this;
			webView.post(new Runnable() {
				@Override
				public void run() {
					if (mRNCWebViewClient == null) {
						return;
					}
					WritableMap data = mRNCWebViewClient.createWebViewEvent(webView, webView.getUrl());
					data.putString("data", message);
					dispatchEvent(webView, new TopMessageEvent(webView.getId(), data));
				}
			});
		} else {
			WritableMap eventData = Arguments.createMap();
			eventData.putString("data", message);
			dispatchEvent(this, new TopMessageEvent(this.getId(), eventData));
		}
	}

	protected void onScrollChanged(int x, int y, int oldX, int oldY) {
		super.onScrollChanged(x, y, oldX, oldY);

		if (!hasScrollEvent) {
			return;
		}

		if (mOnScrollDispatchHelper == null) {
			mOnScrollDispatchHelper = new OnScrollDispatchHelper();
		}

		if (mOnScrollDispatchHelper.onScrollChanged(x, y)) {
			ScrollEvent event = ScrollEvent.obtain(
					this.getId(),
					ScrollEventType.SCROLL,
					x,
					y,
					mOnScrollDispatchHelper.getXFlingVelocity(),
					mOnScrollDispatchHelper.getYFlingVelocity(),
					this.computeHorizontalScrollRange(),
					this.computeVerticalScrollRange(),
					this.getWidth(),
					this.getHeight());

			dispatchEvent(this, event);
		}
	}

	protected void cleanupCallbacksAndDestroy() {
		setWebViewClient(null);
		destroy();
	}

	public void setDefaultHandler(BridgeHandler handler) {
		bridgeHelper.setDefaultHandler(handler);
	}

	public void send(String data) {
		bridgeHelper.send(data);
	}

	public void send(String data, CallBackFunction responseCallback) {
		bridgeHelper.send(data, responseCallback);
	}

	public void registerHandler(String handlerName, BridgeHandler handler) {
		bridgeHelper.registerHandler(handlerName, handler);
	}

	public void unregisterHandler(String handlerName) {
		bridgeHelper.unregisterHandler(handlerName);
	}

	public void callHandler(String handlerName, String data, CallBackFunction callBack) {
		bridgeHelper.callHandler(handlerName, data, callBack);
	}

	protected class RNCWebViewBridge {
		RNCWebView mContext;

		RNCWebViewBridge(RNCWebView c) {
			mContext = c;
		}

		/**
		 * This method is called whenever JavaScript running within the web view calls:
		 * - window[JAVASCRIPT_INTERFACE].postMessage
		 */
		@JavascriptInterface
		public void postMessage(String message) {
			mContext.onMessage(message);
		}
	}

	protected static void dispatchEvent(WebView webView, Event event) {
		ReactContext reactContext = (ReactContext) webView.getContext();
		EventDispatcher eventDispatcher =
				reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
		eventDispatcher.dispatchEvent(event);
	}
}