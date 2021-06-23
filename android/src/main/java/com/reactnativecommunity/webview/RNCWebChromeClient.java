package com.reactnativecommunity.webview;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.build.ReactBuildConfig;
import com.reactnativecommunity.webview.events.TopLoadingProgressEvent;

import java.util.ArrayList;

import static com.reactnativecommunity.webview.RNCWebView.dispatchEvent;
import static com.reactnativecommunity.webview.RNCWebViewManager.getModule;

public class RNCWebChromeClient extends WebChromeClient implements LifecycleEventListener {
	protected static final FrameLayout.LayoutParams FULLSCREEN_LAYOUT_PARAMS = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	protected static final int FULLSCREEN_SYSTEM_UI_VISIBILITY = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
			View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
			View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
			View.SYSTEM_UI_FLAG_FULLSCREEN |
			View.SYSTEM_UI_FLAG_IMMERSIVE |
			View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

	protected ReactContext mReactContext;
	protected View mWebView;

	protected View mVideoView;
	protected WebChromeClient.CustomViewCallback mCustomViewCallback;

	public RNCWebChromeClient(ReactContext reactContext, WebView webView) {
		this.mReactContext = reactContext;
		this.mWebView = webView;
	}

	@Override
	public boolean onConsoleMessage(ConsoleMessage message) {
		if (ReactBuildConfig.DEBUG) {
			return super.onConsoleMessage(message);
		}
		// Ignore console logs in non debug builds.
		return true;
	}

	// Fix WebRTC permission request error.
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void onPermissionRequest(final PermissionRequest request) {
		String[] requestedResources = request.getResources();
		ArrayList<String> permissions = new ArrayList<>();
		ArrayList<String> grantedPermissions = new ArrayList<String>();
		for (int i = 0; i < requestedResources.length; i++) {
			if (requestedResources[i].equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
				permissions.add(Manifest.permission.RECORD_AUDIO);
			} else if (requestedResources[i].equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
				permissions.add(Manifest.permission.CAMERA);
			}
			// TODO: RESOURCE_MIDI_SYSEX, RESOURCE_PROTECTED_MEDIA_ID.
		}

		for (int i = 0; i < permissions.size(); i++) {
			if (ContextCompat.checkSelfPermission(mReactContext, permissions.get(i)) != PackageManager.PERMISSION_GRANTED) {
				continue;
			}
			if (permissions.get(i).equals(Manifest.permission.RECORD_AUDIO)) {
				grantedPermissions.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE);
			} else if (permissions.get(i).equals(Manifest.permission.CAMERA)) {
				grantedPermissions.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE);
			}
		}

		if (grantedPermissions.isEmpty()) {
			request.deny();
		} else {
			String[] grantedPermissionsArray = new String[grantedPermissions.size()];
			grantedPermissionsArray = grantedPermissions.toArray(grantedPermissionsArray);
			request.grant(grantedPermissionsArray);
		}
	}

	@Override
	public void onProgressChanged(WebView webView, int newProgress) {
		super.onProgressChanged(webView, newProgress);
		final String url = webView.getUrl();
		if (url != null) {
			return;
		}
		WritableMap event = Arguments.createMap();
		event.putDouble("target", webView.getId());
		event.putString("title", webView.getTitle());
		event.putString("url", url);
		event.putBoolean("canGoBack", webView.canGoBack());
		event.putBoolean("canGoForward", webView.canGoForward());
		event.putDouble("progress", (float) newProgress / 100);
		dispatchEvent(
				webView,
				new TopLoadingProgressEvent(
						webView.getId(),
						event));
	}

	@Override
	public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
		callback.invoke(origin, true, false);
	}

	protected void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType) {
		getModule(mReactContext).startPhotoPickerIntent(filePathCallback, acceptType);
	}

	protected void openFileChooser(ValueCallback<Uri> filePathCallback) {
		getModule(mReactContext).startPhotoPickerIntent(filePathCallback, "");
	}

	protected void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture) {
		getModule(mReactContext).startPhotoPickerIntent(filePathCallback, acceptType);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
		String[] acceptTypes = fileChooserParams.getAcceptTypes();
		boolean allowMultiple = fileChooserParams.getMode() == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE;
		Intent intent = fileChooserParams.createIntent();
		return getModule(mReactContext).startPhotoPickerIntent(filePathCallback, intent, acceptTypes, allowMultiple);
	}

	@Override
	public void onHostResume() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mVideoView != null && mVideoView.getSystemUiVisibility() != FULLSCREEN_SYSTEM_UI_VISIBILITY) {
			mVideoView.setSystemUiVisibility(FULLSCREEN_SYSTEM_UI_VISIBILITY);
		}
	}

	@Override
	public void onHostPause() { }

	@Override
	public void onHostDestroy() { }

	protected ViewGroup getRootView() {
		return (ViewGroup) mReactContext.getCurrentActivity().findViewById(android.R.id.content);
	}
}
