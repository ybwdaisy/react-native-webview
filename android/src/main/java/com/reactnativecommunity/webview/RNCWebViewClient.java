package com.reactnativecommunity.webview;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.reactnativecommunity.webview.events.TopHttpErrorEvent;
import com.reactnativecommunity.webview.events.TopLoadingErrorEvent;
import com.reactnativecommunity.webview.events.TopLoadingFinishEvent;
import com.reactnativecommunity.webview.events.TopLoadingStartEvent;
import com.reactnativecommunity.webview.events.TopShouldStartLoadWithRequestEvent;
import com.reactnativecommunity.webview.jsbridge.BridgeUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.annotation.Nullable;

import static com.reactnativecommunity.webview.RNCWebView.dispatchEvent;

public class RNCWebViewClient extends WebViewClient {
	protected boolean mLastLoadFailed = false;
	protected @Nullable
	ReadableArray mUrlPrefixesForDefaultIntent;
	RNCWebView webView;

	public RNCWebViewClient(RNCWebView webView) {
		this.webView = webView;
	}

	@Override
	public void onPageFinished(WebView webView, String url) {
		RNCWebView reactWebView = (RNCWebView) webView;
		super.onPageFinished(reactWebView, url);

		if (!mLastLoadFailed) {
			reactWebView.callInjectedJavaScript();
			reactWebView.bridgeHelper.onPageFinished();
			emitFinishEvent(reactWebView, url);
		}

	}

	@Override
	public void onPageStarted(WebView webView, String url, Bitmap favicon) {
		super.onPageStarted(webView, url, favicon);
		mLastLoadFailed = false;

		dispatchEvent(webView, new TopLoadingStartEvent(webView.getId(), createWebViewEvent(webView, url)));
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		try {
			String replacedUrl = url.replaceAll("%(?![0-9a-fA-F]{2})", "%25").replaceAll("\\+", "%2B");
			url = URLDecoder.decode(replacedUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {

		}
		if (url.startsWith(BridgeUtil.CUSTOM_RETURN_DATA)) {
			webView.bridgeHelper.handlerReturnData(url);
			return true;
		} else if (url.startsWith(BridgeUtil.CUSTOM_OVERRIDE_SCHEMA)) {
			webView.bridgeHelper.flushMessageQueue();
			return true;
		} else {
			webView.progressChangedFilter.setWaitingForCommandLoadUrl(true);
			dispatchEvent(view, new TopShouldStartLoadWithRequestEvent(view.getId(), createWebViewEvent(view, url)));
		}
		return false;
	}


	@TargetApi(Build.VERSION_CODES.N)
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
		final String url = request.getUrl().toString();
		return this.shouldOverrideUrlLoading(view, url);
	}

	@Override
	public void onReceivedError(
			WebView webView,
			int errorCode,
			String description,
			String failingUrl) {
		super.onReceivedError(webView, errorCode, description, failingUrl);
		mLastLoadFailed = true;

		// In case of an error JS side expect to get a finish event first, and then get an error event
		// Android WebView does it in the opposite way, so we need to simulate that behavior
		emitFinishEvent(webView, failingUrl);

		WritableMap eventData = createWebViewEvent(webView, failingUrl);
		eventData.putDouble("code", errorCode);
		eventData.putString("description", description);

		dispatchEvent(webView, new TopLoadingErrorEvent(webView.getId(), eventData));
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public void onReceivedHttpError(
			WebView webView,
			WebResourceRequest request,
			WebResourceResponse errorResponse) {
		super.onReceivedHttpError(webView, request, errorResponse);

		if (request.isForMainFrame()) {
			WritableMap eventData = createWebViewEvent(webView, request.getUrl().toString());
			eventData.putInt("statusCode", errorResponse.getStatusCode());
			eventData.putString("description", errorResponse.getReasonPhrase());

			dispatchEvent(webView, new TopHttpErrorEvent(webView.getId(), eventData));
		}
	}

	protected void emitFinishEvent(WebView webView, String url) {
		dispatchEvent(webView, new TopLoadingFinishEvent(webView.getId(), createWebViewEvent(webView, url)));
	}

	protected WritableMap createWebViewEvent(WebView webView, String url) {
		WritableMap event = Arguments.createMap();
		event.putDouble("target", webView.getId());
		// Don't use webView.getUrl() here, the URL isn't updated to the new value yet in callbacks
		// like onPageFinished
		event.putString("url", url);
		event.putBoolean("loading", !mLastLoadFailed && webView.getProgress() != 100);
		event.putString("title", webView.getTitle());
		event.putBoolean("canGoBack", webView.canGoBack());
		event.putBoolean("canGoForward", webView.canGoForward());
		return event;
	}

	public void setUrlPrefixesForDefaultIntent(ReadableArray specialUrls) {
		mUrlPrefixesForDefaultIntent = specialUrls;
	}
}