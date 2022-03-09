package com.reactnativecommunity.webview;

public class ProgressChangedFilter {
	private boolean waitingForCommandLoadUrl = false;

	public void setWaitingForCommandLoadUrl(boolean isWaiting) {
		waitingForCommandLoadUrl = isWaiting;
	}

	public boolean isWaitingForCommandLoadUrl() {
		return waitingForCommandLoadUrl;
	}
}
