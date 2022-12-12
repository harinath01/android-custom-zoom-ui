package us.zoom.sdksample.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import us.zoom.sdk.CustomizedMiniMeetingViewSize;
import us.zoom.sdk.InMeetingNotificationHandle;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingParameter;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.SimpleZoomUIDelegate;
import us.zoom.sdk.ZoomApiError;
import us.zoom.sdk.ZoomAuthenticationError;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdksample.R;
import us.zoom.sdksample.initsdk.InitAuthSDKCallback;
import us.zoom.sdksample.initsdk.InitAuthSDKHelper;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.RawDataMeetingActivity;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper;
import us.zoom.sdksample.inmeetingfunction.zoommeetingui.ZoomMeetingUISettingHelper;
import us.zoom.sdksample.startjoinmeeting.UserLoginCallback;

public class InitAuthSDKActivity extends Activity implements InitAuthSDKCallback,
        MeetingServiceListener, UserLoginCallback.ZoomDemoAuthenticationListener {

    private final static String TAG = "ZoomSDKExample";
    private View layoutJoin;
    private View mProgressPanel;
    private EditText numberEdit;
    private ZoomSDK mZoomSDK;
    private Button mReturnMeeting;
    private boolean isResumed = false;
    private ZoomSDK zoomSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        zoomSDK = ZoomSDK.getInstance();
        if (zoomSDK.isLoggedIn()) {
            finish();
            showEmailLoginUserStartJoinActivity();
            return;
        }

        setContentView(R.layout.init_auth_sdk);

        mProgressPanel = (View) findViewById(R.id.progressPanel);
        mReturnMeeting = findViewById(R.id.btn_return);
        layoutJoin = findViewById(R.id.layout_join);
        numberEdit = findViewById(R.id.edit_join_number);
        mProgressPanel.setVisibility(View.GONE);

         InitAuthSDKHelper.getInstance().initSDK(this, this);

        if (zoomSDK.isInitialized()) {
            layoutJoin.setVisibility(View.VISIBLE);

            View view = findViewById(R.id.btnSettings);
            if (null != view) {
                view.setVisibility(View.VISIBLE);
            }
            zoomSDK.getMeetingService().addListener(this);
            zoomSDK.getMeetingSettingsHelper().enable720p(false);
            zoomSDK.getMeetingSettingsHelper().setCustomizedMeetingUIEnabled(true);
        } else {
            layoutJoin.setVisibility(View.GONE);
        }
    }

    InMeetingNotificationHandle handle=new InMeetingNotificationHandle() {

        @Override
        public boolean handleReturnToConfNotify(Context context, Intent intent) {
            intent = new Intent(context, MyMeetingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            if(!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.setAction(InMeetingNotificationHandle.ACTION_RETURN_TO_CONF);
            context.startActivity(intent);
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        if (null == ZoomSDK.getInstance().getMeetingService()) {
            super.onBackPressed();
            return;
        }
        MeetingStatus meetingStatus = ZoomSDK.getInstance().getMeetingService().getMeetingStatus();
        if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
        Log.i(TAG, "onZoomSDKInitializeResult, errorCode=" + errorCode + ", internalErrorCode=" + internalErrorCode);

        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
            Toast.makeText(this, "Failed to initialize Zoom SDK. Error: " + errorCode + ", internalErrorCode=" + internalErrorCode, Toast.LENGTH_LONG).show();
        } else {
            zoomSDK.getZoomUIService().enableMinimizeMeeting(true);
            zoomSDK.getZoomUIService().setMiniMeetingViewSize(new CustomizedMiniMeetingViewSize(0, 0, 360, 540));
            setMiniWindows();
            zoomSDK.getMeetingSettingsHelper().enable720p(false);
            zoomSDK.getMeetingSettingsHelper().enableShowMyMeetingElapseTime(true);
            zoomSDK.getMeetingService().addListener(this);
            zoomSDK.getMeetingSettingsHelper().setCustomizedMeetingUIEnabled(true);
            zoomSDK.getMeetingSettingsHelper().setCustomizedNotificationData(null, handle);
            Toast.makeText(this, "Initialize Zoom SDK successfully.", Toast.LENGTH_LONG).show();
            if (zoomSDK.tryAutoLoginZoom() == ZoomApiError.ZOOM_API_ERROR_SUCCESS) {
                UserLoginCallback.getInstance().addListener(this);
                showProgressPanel(true);
            } else {
                showProgressPanel(false);
            }
        }
    }


    @Override
    public void onZoomSDKLoginResult(long result) {
        if ((int) result == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS) {
            showEmailLoginUserStartJoinActivity();
            finish();
        } else {
            showProgressPanel(false);
        }
    }

    @Override
    public void onZoomSDKLogoutResult(long result) {}

    @Override
    public void onZoomIdentityExpired() {
        Log.e(TAG,"onZoomIdentityExpired");
        if (zoomSDK.isLoggedIn()) {
            zoomSDK.logoutZoom();
        }
    }

    @Override
    public void onZoomAuthIdentityExpired() {
        Log.d(TAG,"onZoomAuthIdentityExpired");
    }

    public void onClickJoin(View view) {
        if(!zoomSDK.isInitialized())
        {
            Toast.makeText(this,"Init SDK First",Toast.LENGTH_SHORT).show();
            InitAuthSDKHelper.getInstance().initSDK(this, this);
            return;
        }

        if (ZoomSDK.getInstance().getMeetingSettingsHelper().isCustomizedMeetingUIEnabled()) {
            ZoomSDK.getInstance().getSmsService().enableZoomAuthRealNameMeetingUIShown(false);
        } else {
            ZoomSDK.getInstance().getSmsService().enableZoomAuthRealNameMeetingUIShown(true);
        }
        String number = numberEdit.getText().toString();

        JoinMeetingParams params = new JoinMeetingParams();
        params.meetingNo = (number.isEmpty()) ? "83071465697": number;
        params.displayName = "hari";
        params.password = "110194";
        ZoomSDK.getInstance().getMeetingService().joinMeetingWithParams(this, params,ZoomMeetingUISettingHelper.getJoinMeetingOptions());
    }

    private void showProgressPanel(boolean show) {
        if (show) {
            mReturnMeeting.setVisibility(View.GONE);
            mProgressPanel.setVisibility(View.VISIBLE);
            layoutJoin.setVisibility(View.GONE);
            View view = findViewById(R.id.btnSettings);
            if (null != view) {
                view.setVisibility(View.GONE);
            }
        } else {
            View view = findViewById(R.id.btnSettings);
            if (null != view) {
                view.setVisibility(View.VISIBLE);
            }
            mProgressPanel.setVisibility(View.GONE);
            layoutJoin.setVisibility(View.VISIBLE);
            mReturnMeeting.setVisibility(View.GONE);
        }
    }

    private void showEmailLoginUserStartJoinActivity() {
        Intent intent = new Intent(this, LoginUserStartJoinMeetingActivity.class);
        startActivity(intent);
    }

    public void onClickReturnMeeting(View view) {
        UIUtil.returnToMeeting(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        refreshUI();

        setMiniWindows();
    }

    private void setMiniWindows() {
        if (null != zoomSDK && zoomSDK.isInitialized() && !zoomSDK.getMeetingSettingsHelper().isCustomizedMeetingUIEnabled()) {
            ZoomSDK.getInstance().getZoomUIService().setZoomUIDelegate(new SimpleZoomUIDelegate() {
                @Override
                public void afterMeetingMinimized(Activity activity) {
                    Intent intent = new Intent(activity, InitAuthSDKActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
    }

    private void refreshUI() {
        if(!ZoomSDK.getInstance().isInitialized())
        {
            return;
        }
        MeetingStatus meetingStatus = ZoomSDK.getInstance().getMeetingService().getMeetingStatus();
        if (ZoomSDK.getInstance().getMeetingSettingsHelper().isCustomizedMeetingUIEnabled()) {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING && isResumed) {
                MeetingWindowHelper.getInstance().showMeetingWindow(this);
                showProgressPanel(true);
                mProgressPanel.setVisibility(View.GONE);
                mReturnMeeting.setVisibility(View.VISIBLE);
            } else {
                MeetingWindowHelper.getInstance().hiddenMeetingWindow(true);
                showProgressPanel(false);
            }
        } else {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING && isResumed) {
                showProgressPanel(true);
                mProgressPanel.setVisibility(View.GONE);
                mReturnMeeting.setVisibility(View.VISIBLE);
            } else {
                showProgressPanel(false);
            }
        }
    }


    @Override
    public void onMeetingParameterNotification(MeetingParameter meetingParameter) {
        Log.d(TAG, "onMeetingParameterNotification "+meetingParameter);
    }

    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
        Log.d(TAG,"onMeetingStatusChanged "+meetingStatus+":"+errorCode+":"+internalErrorCode);
        if(!ZoomSDK.getInstance().isInitialized())
        {
            showProgressPanel(false);
            return;
        }
        if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
            if (ZoomMeetingUISettingHelper.useExternalVideoSource) {
                ZoomMeetingUISettingHelper.changeVideoSource(true, InitAuthSDKActivity.this);
            }
        }
        if (ZoomSDK.getInstance().getMeetingSettingsHelper().isCustomizedMeetingUIEnabled()) {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
                showMeetingUi();
            }
        }
        refreshUI();
    }

    private void showMeetingUi() {
        if (ZoomSDK.getInstance().getMeetingSettingsHelper().isCustomizedMeetingUIEnabled()) {
            SharedPreferences sharedPreferences = getSharedPreferences("UI_Setting", Context.MODE_PRIVATE);
            boolean enable = sharedPreferences.getBoolean("enable_rawdata", false);
            Intent intent = null;
            if (!enable) {
                intent = new Intent(this, MyMeetingActivity.class);
                intent.putExtra("from",1);
            } else {
                intent = new Intent(this, RawDataMeetingActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MeetingWindowHelper.getInstance().onActivityResult(requestCode, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UserLoginCallback.getInstance().removeListener(this);

        if(null!= ZoomSDK.getInstance().getMeetingService())
        {
            ZoomSDK.getInstance().getMeetingService().removeListener(this);
        }
        if(null!=ZoomSDK.getInstance().getMeetingSettingsHelper()){
            ZoomSDK.getInstance().getMeetingSettingsHelper().setCustomizedNotificationData(null, null);
        }
        InitAuthSDKHelper.getInstance().reset();
    }
}