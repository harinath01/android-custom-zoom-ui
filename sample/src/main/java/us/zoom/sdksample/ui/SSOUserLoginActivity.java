package us.zoom.sdksample.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import us.zoom.sdk.ZoomAuthenticationError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdksample.R;
import us.zoom.sdksample.startjoinmeeting.UserLoginCallback;

public class SSOUserLoginActivity extends Activity implements UserLoginCallback.ZoomDemoAuthenticationListener, View.OnClickListener {

	private final static String TAG = "ZoomSDKExample";
    private EditText mEdtSSOPrefix;
    private Button mBtnConfirm;
	private View mProgressPanel;
	private WebView mWebView;
	private LinearLayout mWebViewLoadingLayout;
	private String mSSOUrl;
	
	private LinearLayout mLoginOperationLayout;
	private LinearLayout mLoginInfoEnterLayout;
	private TextView mUrlGenerateTv;
	private EditText mEdtLoginUrl;
	private Button mBtnLoginConfirm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.sso_login_activity);

        mEdtSSOPrefix = (EditText)findViewById(R.id.edtSSODomainPrefix);
        mBtnConfirm = (Button)findViewById(R.id.btnConfirm);
        mBtnConfirm.setOnClickListener(this);
		mProgressPanel = (View)findViewById(R.id.progressPanel);
		mWebViewLoadingLayout = findViewById(R.id.webViewProgressPanel);
		mWebViewLoadingLayout.setVisibility(View.GONE);
		mLoginOperationLayout = findViewById(R.id.operationLayout);
		mLoginOperationLayout.setVisibility(View.GONE);
		findViewById(R.id.btnLoginBySelf).setOnClickListener(this);
		findViewById(R.id.btnAUtoLogin).setOnClickListener(this);

		initWebView();
		initManualLoginLayout();
		UserLoginCallback.getInstance().addListener(this);
	}

	private void initWebView() {
		mWebView = findViewById(R.id.webview);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if( URLUtil.isNetworkUrl(url) ) {
					return false;
				} else {
					/* get the login url */
					onLogin(url);
				}
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				mWebViewLoadingLayout.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				mWebViewLoadingLayout.setVisibility(View.GONE);
			}


		});
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
	}

	private void initManualLoginLayout() {
		mLoginInfoEnterLayout = findViewById(R.id.enterLoginInfoLayout);
		mLoginInfoEnterLayout.setVisibility(View.GONE);
		mUrlGenerateTv = findViewById(R.id.tvGenerateUrl);
		mEdtLoginUrl = findViewById(R.id.edtLoginUrl);
		mBtnLoginConfirm = findViewById(R.id.btnLoginConfirm);
		mBtnLoginConfirm.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UserLoginCallback.getInstance().removeListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.btnConfirm) {
			onClickBtnGenerateUrl();
        } else if (v.getId() == R.id.btnAUtoLogin) {
			onAutoLogin();
		} else if (v.getId() == R.id.btnLoginBySelf) {
			onLoginBySelf();
		} else if (v.getId() == R.id.btnLoginConfirm) {
			onLoginBySelfConfirm();
		}
	}

	private void onLoginBySelfConfirm() {
		String ssoLoginUrl = mEdtLoginUrl.getText().toString().trim();
		if(ssoLoginUrl.length() == 0) {
			Toast.makeText(this, "You need to enter sso token.", Toast.LENGTH_LONG).show();
			return;
		}
		onLogin(ssoLoginUrl);
	}

	private void onLogin(String ssoLoginUrl) {
		mWebView.setVisibility(View.GONE);
		mEdtSSOPrefix.setVisibility(View.GONE);
		mBtnConfirm.setVisibility(View.GONE);
		mLoginInfoEnterLayout.setVisibility(View.GONE);
		mProgressPanel.setVisibility(View.VISIBLE);
		if(!ZoomSDK.getInstance().handleSSOLoginURIProtocol(ssoLoginUrl)) {
			Toast.makeText(this, "ZoomSDK has not been initialized successfully or sdk is logging in.", Toast.LENGTH_LONG).show();
			onLoginFinish();
		}
	}

	private void onLoginFinish() {
		mProgressPanel.setVisibility(View.GONE);
		mEdtSSOPrefix.setVisibility(View.VISIBLE);
		mBtnConfirm.setVisibility(View.VISIBLE);
	}

	private void onClickBtnGenerateUrl() {
		String ssoDomainPrefix = mEdtSSOPrefix.getText().toString().trim();
		if(ssoDomainPrefix.length() == 0) {
			Toast.makeText(this, "You need to enter sso token.", Toast.LENGTH_LONG).show();
			return;
		}
		String ssoUrl = ZoomSDK.getInstance().generateSSOLoginURL(ssoDomainPrefix);
		if (null == ssoUrl || ssoUrl.isEmpty()) {
			Toast.makeText(this, "generate sso url error.", Toast.LENGTH_LONG).show();
			return;
		}
		mSSOUrl = ssoUrl;
		mEdtSSOPrefix.setVisibility(View.GONE);
		mBtnConfirm.setVisibility(View.GONE);
		mLoginOperationLayout.setVisibility(View.VISIBLE);
	}
	
	private void onAutoLogin() {
		mWebView.setVisibility(View.VISIBLE);
		mWebView.loadUrl(mSSOUrl);
		/* web view Load may Blank page to avoid misunderstanding, add a loading state */
		mWebViewLoadingLayout.setVisibility(View.VISIBLE);
		mLoginOperationLayout.setVisibility(View.GONE);
		mLoginInfoEnterLayout.setVisibility(View.GONE);
	}

	private void onLoginBySelf() {
		mLoginOperationLayout.setVisibility(View.GONE);
		mLoginInfoEnterLayout.setVisibility(View.VISIBLE);
		mWebViewLoadingLayout.setVisibility(View.GONE);
		mWebView.setVisibility(View.GONE);
		mUrlGenerateTv.setText(mSSOUrl);
	}

	@Override
	public void onZoomSDKLoginResult(long result) {
		if(result == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS) {
			Toast.makeText(this, "Login successfully", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, LoginUserStartJoinMeetingActivity.class);
			startActivity(intent);
			finish();
		} else {
			Toast.makeText(this, "Login failed result code = " + result, Toast.LENGTH_SHORT).show();
		}
		onLoginFinish();
	}

	@Override
	public void onZoomSDKLogoutResult(long result) {
		if(result == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS) {
			Toast.makeText(this, "Logout successfully", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Logout failed result code = " + result, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onZoomIdentityExpired() {
		//Zoom identity expired, please re-login;
	}

	@Override
	public void onZoomAuthIdentityExpired() {

	}
}
