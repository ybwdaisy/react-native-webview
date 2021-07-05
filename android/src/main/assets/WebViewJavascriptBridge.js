(function() {
    if (window.WebViewJavascriptBridge) {
        return;
    }

    var messagingIframe;
    var sendMessageQueue = [];
    var receiveMessageQueue = [];
    var messageHandlers = {};

    var msgQueueIframeHandlers = {};

    var msgQueueIframeId = [];

    var lastCallTime = 0;

    var maxIframeCount = 10;

    var stoId = null;

    var CUSTOM_PROTOCOL_SCHEME = 'genebox';
    var QUEUE_HAS_MESSAGE = '__QUEUE_MESSAGE__/';

    var responseCallbacks = {};
    var uniqueId = 1;

    function _createQueueReadyIframe(doc) {
        messagingIframe = doc.createElement('iframe');
        messagingIframe.style.display = 'none';
        doc.documentElement.appendChild(messagingIframe);
    }

    function init(messageHandler) {
        if (WebViewJavascriptBridge._messageHandler) {
            throw new Error('WebViewJavascriptBridge.init called twice');
        }
        WebViewJavascriptBridge._messageHandler = messageHandler;
        var receivedMessages = receiveMessageQueue;
        receiveMessageQueue = null;
        for (var i = 0; i < receivedMessages.length; i++) {
            _dispatchMessageFromNative(receivedMessages[i]);
        }
    }

    function send(data, responseCallback) {
        _doSend({
            data: data
        }, responseCallback);
    }

    function registerHandler(handlerName, handler) {
        messageHandlers[handlerName] = handler;
    }

    function callHandler(handlerName, data, responseCallback) {
        _doSend({
            handlerName: handlerName,
            data: data
        }, responseCallback);
    }

    function _doSend(message, responseCallback) {

        if (responseCallback) {
            var callbackId = 'cb_' + (uniqueId++) + '_' + new Date().getTime();
            responseCallbacks[callbackId] = responseCallback;
            message.callbackId = callbackId;
        }
        sendMessageQueue.push(message);

        messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://' + QUEUE_HAS_MESSAGE;

    }

    function _fetchQueue() {
        if (sendMessageQueue.length === 0) {
          return;
        }
        var msgQueueLen = sendMessageQueue.length;

        var msgQueueIframeTag = 'iframe_'+ new Date().getTime();

        for(var i=0;i<msgQueueLen;i++){
            var sendMsgItem = sendMessageQueue[i];
            msgQueueIframeHandlers[sendMsgItem.callbackId] = msgQueueIframeTag;
        }

        var messageQueueString = JSON.stringify(sendMessageQueue);
        sendMessageQueue = [];

        var iframe = document.createElement('iframe');
        iframe.id = ""+msgQueueIframeTag;
        iframe.style = "display:none;";
        iframe.src= CUSTOM_PROTOCOL_SCHEME + '://return/_fetchQueue/' + encodeURIComponent(messageQueueString);
        doc.documentElement.appendChild(iframe);

        msgQueueIframeId.push(iframe.id);

         var iframesTest1 = doc.getElementsByTagName("iframe");

         if(iframesTest1.length > maxIframeCount){
            var iframeId = msgQueueIframeId.shift();
            var iframe = doc.getElementById(iframeId);
            iframe.parentNode.removeChild(iframe);
         }
    }

    function _dispatchMessageFromNative(messageJSON) {
        setTimeout(function() {
            var message = JSON.parse(messageJSON);
            var responseCallback;
            if (message.responseId) {
                responseCallback = responseCallbacks[message.responseId];
                if (!responseCallback) {
                    return;
                }
                responseCallback(message.responseData);
                delete responseCallbacks[message.responseId];
                destroyIframe(message.responseId);
            } else {
                if (message.callbackId) {
                    var callbackResponseId = message.callbackId;
                    responseCallback = function(responseData) {
                        _doSend({
                            responseId: callbackResponseId,
                            responseData: responseData
                        });
                    };
                }

                var handler = WebViewJavascriptBridge._messageHandler;
                if (message.handlerName) {
                    handler = messageHandlers[message.handlerName];
                }
                try {
                    handler(message.data, responseCallback);
                } catch (exception) {
                    console.log("exception->"+exception);
                    if (typeof console != 'undefined') {
                        console.log("WebViewJavascriptBridge: WARNING: javascript handler threw.", message, exception);
                    }
                }
            }
        });
    }

    function destroyIframe(responseId){
            var iframeId = msgQueueIframeHandlers[responseId];
            var iframe = doc.getElementById(iframeId);
            iframe.parentNode.removeChild(iframe);
    }

    function _handleMessageFromNative(messageJSON) {
        if (receiveMessageQueue) {
            receiveMessageQueue.push(messageJSON);
        }
        _dispatchMessageFromNative(messageJSON);
    }

    var WebViewJavascriptBridge = window.WebViewJavascriptBridge = {
        init: init,
        send: send,
        registerHandler: registerHandler,
        callHandler: callHandler,
        _fetchQueue: _fetchQueue,
        _handleMessageFromNative: _handleMessageFromNative
    };

    var doc = document;
    _createQueueReadyIframe(doc);
    var readyEvent = doc.createEvent('Events');
    readyEvent.initEvent('WebViewJavascriptBridgeReady');
    readyEvent.bridge = WebViewJavascriptBridge;
    doc.dispatchEvent(readyEvent);
})();