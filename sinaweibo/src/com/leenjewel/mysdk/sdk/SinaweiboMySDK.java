package com.leenjewel.mysdk.sdk;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.leenjewel.mysdk.callback.IMySDKCallback;
import com.leenjewel.mysdk.exception.MySDKDoNotImplementMethod;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.demo.AccessTokenKeeper;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

public class SinaweiboMySDK implements IMySDK {
	
	final static public String SDK_NAME = "sinaweibo";
	
	private String _appKey = null;
	private String _scope = null;
	private String _redirectUrl = null;
	private AuthInfo _authInfo = null;
	private SsoHandler _ssoHandler = null;
	private Oauth2AccessToken _accessToken = null;
	
	private IWeiboShareAPI _shareSDK = null;
	private HashMap<String, IMySDKCallback> _shareCallbackMap = null;

	@Override
	public void applicationOnCreate(Application application) {
		// TODO Auto-generated method stub
		Bundle metaData = application.getApplicationInfo().metaData;
		_appKey = metaData.getString("sinaweibo_app_key");
		_scope = metaData.getString("sinaweibo_scope");
		_redirectUrl = metaData.getString("sinaweibo_redirect_url");
	}

	@Override
	public void activityOnCreate(Activity activity, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		_authInfo = new AuthInfo(activity, _appKey, _redirectUrl, _scope);
		_ssoHandler = new SsoHandler(activity, _authInfo);
		_accessToken = AccessTokenKeeper.readAccessToken(activity);
		_shareSDK = WeiboShareSDK.createWeiboAPI(activity, _appKey);
		_shareSDK.registerApp();
	}

	@Override
	public void activityOnStart(Activity activity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activityOnRestart(Activity activity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activityOnResume(Activity activity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activityOnPause(Activity activity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activityOnStop(Activity activity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activityOnDestroy(Activity activity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activityOnSaveInstanceState(Activity activity, Bundle outState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activityOnNewIntent(Activity activity, Intent intent) {
		// TODO Auto-generated method stub
		if (null != _shareSDK) {
			_shareSDK.handleWeiboResponse(intent, new IWeiboHandler.Response() {
				
				@Override
				public void onResponse(BaseResponse resp) {
					// TODO Auto-generated method stub
					String handle = resp.transaction;
					if (null != SinaweiboMySDK.this._shareCallbackMap && null != handle) {
						IMySDKCallback callback = SinaweiboMySDK.this._shareCallbackMap.get(handle);
						if (null != callback) {
							switch (resp.errCode) {
							case WBConstants.ErrorCode.ERR_OK:
								callback.onSuccess(SDK_NAME, "share", resp.toString());
								break;
							case WBConstants.ErrorCode.ERR_CANCEL :
								callback.onCancel(SDK_NAME, "share", resp.errMsg);
								break;
							case WBConstants.ErrorCode.ERR_FAIL :
								callback.onFail(SDK_NAME, "share", resp.errCode, resp.errMsg, "");
								break;
							}
							SinaweiboMySDK.this._shareCallbackMap.remove(handle);
						}
					}
				}
			});
		}
	}

	@Override
	public void activityOnActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (null != _ssoHandler) {
			_ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	@Override
	public int applySDKMethodAndReturnInt(String methodName, String params) throws MySDKDoNotImplementMethod {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long applySDKMethodAndReturnLong(String methodName, String params) throws MySDKDoNotImplementMethod {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float applySDKMethodAndReturnFloat(String methodName, String params) throws MySDKDoNotImplementMethod {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double applySDKMethodAndReturnDouble(String methodName, String params) throws MySDKDoNotImplementMethod {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean applySDKMethodAndReturnBoolean(String methodName, String params) throws MySDKDoNotImplementMethod {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String applySDKMethodAndReturnString(String methodName, String params) throws MySDKDoNotImplementMethod {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void sinaweiboAuth(final IMySDKCallback callback) {
		if (null != _accessToken && _accessToken.isSessionValid()) {
			if (null != callback) {
				try {
					JSONObject retJson = new JSONObject();
					retJson.put("token", _accessToken.getToken());
					retJson.put("uid", _accessToken.getUid());
					retJson.put("expire", Long.valueOf(_accessToken.getExpiresTime()));
					callback.onSuccess(SDK_NAME, "auth", retJson.toString());
				} catch(JSONException e) {
					callback.onFail(SDK_NAME, "auth", 0, e.getLocalizedMessage(), "");
				}
			}
		} else {
		_ssoHandler.authorize(new WeiboAuthListener(){

			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				if (null != callback) {
					callback.onCancel(SDK_NAME, "auth", "");
				}
			}

			@Override
			public void onComplete(Bundle arg0) {
				// TODO Auto-generated method stub
				_accessToken = Oauth2AccessToken.parseAccessToken(arg0);
				if (_accessToken.isSessionValid()) {
					AccessTokenKeeper.writeAccessToken(MySDK.getActivity(), _accessToken);
					if (null != callback) {
						try {
							JSONObject retJson = new JSONObject();
							retJson.put("token", _accessToken.getToken());
							retJson.put("uid", _accessToken.getUid());
							retJson.put("expire", Long.valueOf(_accessToken.getExpiresTime()));
							callback.onSuccess(SDK_NAME, "auth", retJson.toString());
						} catch(JSONException e) {
							callback.onFail(SDK_NAME, "auth", 0, e.getLocalizedMessage(), "");
						}
					}
				} else {
					String errCode = arg0.getString("code", "unknow");
					if (null != callback) {
						callback.onFail(SDK_NAME, "auth", 0, errCode, "");
					}
				}
			}

			@Override
			public void onWeiboException(WeiboException err) {
				// TODO Auto-generated method stub
				if (null != callback) {
					callback.onFail(SDK_NAME, "auth", err.hashCode(), err.getLocalizedMessage(), "");
				}
			}});
		}
	}
	
	
	private void sinaweiboUser(final IMySDKCallback callback) {
		long uid = Long.parseLong(_accessToken.getUid());
		UsersAPI userApi = new UsersAPI(MySDK.getActivity(), _appKey, _accessToken);
		userApi.show(uid, new RequestListener(){

			@Override
			public void onComplete(String arg0) {
				// TODO Auto-generated method stub
				if (null != callback) {
					callback.onSuccess(SDK_NAME, "user", arg0);
				}
			}

			@Override
			public void onWeiboException(WeiboException arg0) {
				// TODO Auto-generated method stub
				if (null != callback) {
					callback.onFail(SDK_NAME, "user", arg0.hashCode(), arg0.getLocalizedMessage(), "");
				}
			}});
	}
	

	@Override
	public void applySDKMethodWithCallback(String methodName, String params, final IMySDKCallback callback) {
		// TODO Auto-generated method stub
		
		// **********************************************************
		//
		// Auth
		//
		// **********************************************************
		if (methodName.equalsIgnoreCase("auth")) {
			sinaweiboAuth(callback);
		}
		
		
		// **********************************************************
		//
		// User
		//
		// **********************************************************
		else if (methodName.equalsIgnoreCase("user")) {
			if (null != _accessToken && _accessToken.isSessionValid()) {
				sinaweiboUser(callback);
			} else {
				sinaweiboAuth(new IMySDKCallback(){

					@Override
					public void onSuccess(String sdkName, String methodName, String result) {
						// TODO Auto-generated method stub
						sinaweiboUser(callback);
					}

					@Override
					public void onFail(String sdkName, String methodName, int errorCode, String errorMessage,
							String result) {
						// TODO Auto-generated method stub
						if (null != callback) {
							callback.onFail(sdkName, methodName, errorCode, errorMessage, result);
						}
					}

					@Override
					public void onCancel(String sdkName, String methodName, String result) {
						// TODO Auto-generated method stub
						if (null != callback) {
							callback.onCancel(sdkName, methodName, result);
						}
					}

					@Override
					public void onPayResult(boolean isError, int errorCode, String errorMessage, String sdkName,
							String productID, String orderID, String result) {
						// TODO Auto-generated method stub
						
					}});
			}
		}
		
		
		// **********************************************************
		//
		// Share
		//
		// **********************************************************
		else if (methodName.equalsIgnoreCase("share")) {
			TextObject textObj = new TextObject();
			ImageObject imgObj = null;
			try {
				JSONObject paramJson = new JSONObject(params);
				if (paramJson.has("actionUrl")) {
					textObj.actionUrl = paramJson.getString("actionUrl");
				}
				if (paramJson.has("description")) {
					textObj.description = paramJson.getString("description");
				}
				if (paramJson.has("text")) {
					textObj.text = paramJson.getString("text");
				}
				if (paramJson.has("title")) {
					textObj.title = paramJson.getString("title");
				}
			} catch (JSONException e) {
				textObj.text = params;
			} finally {
				WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
				weiboMessage.textObject = textObj;
				if (null != imgObj) {
					weiboMessage.imageObject = imgObj;
				}
				String handle = "share_"+String.valueOf(System.currentTimeMillis()); 
				if (null != callback) {
					if (null == _shareCallbackMap) {
						_shareCallbackMap = new HashMap<String, IMySDKCallback>();
					}
					_shareCallbackMap.put(handle, callback);
				}
				SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest(); 
				request.transaction = handle;
				request.multiMessage = weiboMessage;
				_shareSDK.sendRequest(MySDK.getActivity(), request);
			}
		}
	}

	@Override
	public void applySDKPay(String productID, String orderID, String params, IMySDKCallback callback) {
		// TODO Auto-generated method stub

	}

}
