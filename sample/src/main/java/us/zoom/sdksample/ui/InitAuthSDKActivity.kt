package us.zoom.sdksample.ui

import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.Companion.instance
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.showMeetingWindow
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.hiddenMeetingWindow
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.onActivityResult
import android.app.Activity
import us.zoom.sdksample.initsdk.InitAuthSDKCallback
import us.zoom.sdksample.startjoinmeeting.UserLoginCallback.ZoomDemoAuthenticationListener
import android.widget.EditText
import android.os.Bundle
import us.zoom.sdksample.R
import us.zoom.sdksample.initsdk.InitAuthSDKHelper
import android.content.Intent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity
import us.zoom.sdksample.ui.InitAuthSDKActivity
import android.widget.Toast
import us.zoom.sdksample.startjoinmeeting.UserLoginCallback
import us.zoom.sdksample.inmeetingfunction.zoommeetingui.ZoomMeetingUISettingHelper
import us.zoom.sdksample.ui.LoginUserStartJoinMeetingActivity
import us.zoom.sdksample.ui.UIUtil
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.Button
import us.zoom.sdk.*
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.RawDataMeetingActivity

class InitAuthSDKActivity : Activity(), InitAuthSDKCallback, MeetingServiceListener,
    ZoomDemoAuthenticationListener {
    private var layoutJoin: View? = null
    private var mProgressPanel: View? = null
    private var numberEdit: EditText? = null
    private val mZoomSDK: ZoomSDK? = null
    private var mReturnMeeting: Button? = null
    private var isResumed = false
    private var zoomSDK: ZoomSDK? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        zoomSDK = ZoomSDK.getInstance()
        if (zoomSDK.isLoggedIn()) {
            finish()
            showEmailLoginUserStartJoinActivity()
            return
        }
        setContentView(R.layout.init_auth_sdk)
        mProgressPanel = findViewById(R.id.progressPanel) as View
        mReturnMeeting = findViewById(R.id.btn_return)
        layoutJoin = findViewById(R.id.layout_join)
        numberEdit = findViewById(R.id.edit_join_number)
        mProgressPanel!!.visibility = View.GONE
        InitAuthSDKHelper.getInstance().initSDK(this, this)
        if (zoomSDK.isInitialized()) {
            layoutJoin.setVisibility(View.VISIBLE)
            val view = findViewById<View>(R.id.btnSettings)
            if (null != view) {
                view.visibility = View.VISIBLE
            }
            zoomSDK.getMeetingService().addListener(this)
            zoomSDK.getMeetingSettingsHelper().enable720p(false)
            zoomSDK.getMeetingSettingsHelper().isCustomizedMeetingUIEnabled = true
        } else {
            layoutJoin.setVisibility(View.GONE)
        }
    }

    var handle = InMeetingNotificationHandle { context, intent ->
        var intent = intent
        intent = Intent(context, MyMeetingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.action = InMeetingNotificationHandle.ACTION_RETURN_TO_CONF
        context.startActivity(intent)
        true
    }

    override fun onBackPressed() {
        if (null == ZoomSDK.getInstance().meetingService) {
            super.onBackPressed()
            return
        }
        val meetingStatus = ZoomSDK.getInstance().meetingService.meetingStatus
        if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
            moveTaskToBack(true)
        } else {
            super.onBackPressed()
        }
    }

    override fun onZoomSDKInitializeResult(errorCode: Int, internalErrorCode: Int) {
        Log.i(
            TAG,
            "onZoomSDKInitializeResult, errorCode=$errorCode, internalErrorCode=$internalErrorCode"
        )
        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
            Toast.makeText(
                this,
                "Failed to initialize Zoom SDK. Error: $errorCode, internalErrorCode=$internalErrorCode",
                Toast.LENGTH_LONG
            ).show()
        } else {
            zoomSDK!!.zoomUIService.enableMinimizeMeeting(true)
            zoomSDK!!.zoomUIService.setMiniMeetingViewSize(
                CustomizedMiniMeetingViewSize(
                    0,
                    0,
                    360,
                    540
                )
            )
            setMiniWindows()
            zoomSDK!!.meetingSettingsHelper.enable720p(false)
            zoomSDK!!.meetingSettingsHelper.enableShowMyMeetingElapseTime(true)
            zoomSDK!!.meetingService.addListener(this)
            zoomSDK!!.meetingSettingsHelper.isCustomizedMeetingUIEnabled = true
            zoomSDK!!.meetingSettingsHelper.setCustomizedNotificationData(null, handle)
            Toast.makeText(this, "Initialize Zoom SDK successfully.", Toast.LENGTH_LONG).show()
            if (zoomSDK!!.tryAutoLoginZoom() == ZoomApiError.ZOOM_API_ERROR_SUCCESS) {
                UserLoginCallback.getInstance().addListener(this)
                showProgressPanel(true)
            } else {
                showProgressPanel(false)
            }
        }
    }

    override fun onZoomSDKLoginResult(result: Long) {
        if (result.toInt() == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS) {
            showEmailLoginUserStartJoinActivity()
            finish()
        } else {
            showProgressPanel(false)
        }
    }

    override fun onZoomSDKLogoutResult(result: Long) {}
    override fun onZoomIdentityExpired() {
        Log.e(TAG, "onZoomIdentityExpired")
        if (zoomSDK!!.isLoggedIn) {
            zoomSDK!!.logoutZoom()
        }
    }

    override fun onZoomAuthIdentityExpired() {
        Log.d(TAG, "onZoomAuthIdentityExpired")
    }

    fun onClickJoin(view: View?) {
        if (!zoomSDK!!.isInitialized) {
            Toast.makeText(this, "Init SDK First", Toast.LENGTH_SHORT).show()
            InitAuthSDKHelper.getInstance().initSDK(this, this)
            return
        }
        if (ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            ZoomSDK.getInstance().smsService.enableZoomAuthRealNameMeetingUIShown(false)
        } else {
            ZoomSDK.getInstance().smsService.enableZoomAuthRealNameMeetingUIShown(true)
        }
        val number = numberEdit!!.text.toString()
        val params = JoinMeetingParams()
        params.meetingNo = if (number.isEmpty()) "83071465697" else number
        params.displayName = "hari"
        params.password = "110194"
        ZoomSDK.getInstance().meetingService.joinMeetingWithParams(
            this,
            params,
            ZoomMeetingUISettingHelper.getJoinMeetingOptions()
        )
    }

    private fun showProgressPanel(show: Boolean) {
        if (show) {
            mReturnMeeting!!.visibility = View.GONE
            mProgressPanel!!.visibility = View.VISIBLE
            layoutJoin!!.visibility = View.GONE
            val view = findViewById<View>(R.id.btnSettings)
            if (null != view) {
                view.visibility = View.GONE
            }
        } else {
            val view = findViewById<View>(R.id.btnSettings)
            if (null != view) {
                view.visibility = View.VISIBLE
            }
            mProgressPanel!!.visibility = View.GONE
            layoutJoin!!.visibility = View.VISIBLE
            mReturnMeeting!!.visibility = View.GONE
        }
    }

    private fun showEmailLoginUserStartJoinActivity() {
        val intent = Intent(this, LoginUserStartJoinMeetingActivity::class.java)
        startActivity(intent)
    }

    fun onClickReturnMeeting(view: View?) {
        UIUtil.returnToMeeting(this)
    }

    override fun onResume() {
        super.onResume()
        isResumed = true
        refreshUI()
        setMiniWindows()
    }

    private fun setMiniWindows() {
        if (null != zoomSDK && zoomSDK!!.isInitialized && !zoomSDK!!.meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            ZoomSDK.getInstance().zoomUIService.setZoomUIDelegate(object : SimpleZoomUIDelegate() {
                override fun afterMeetingMinimized(activity: Activity) {
                    val intent = Intent(activity, InitAuthSDKActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    activity.startActivity(intent)
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        isResumed = false
    }

    private fun refreshUI() {
        if (!ZoomSDK.getInstance().isInitialized) {
            return
        }
        val meetingStatus = ZoomSDK.getInstance().meetingService.meetingStatus
        if (ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING && isResumed) {
                instance!!.showMeetingWindow(this)
                showProgressPanel(true)
                mProgressPanel!!.visibility = View.GONE
                mReturnMeeting!!.visibility = View.VISIBLE
            } else {
                instance!!.hiddenMeetingWindow(true)
                showProgressPanel(false)
            }
        } else {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING && isResumed) {
                showProgressPanel(true)
                mProgressPanel!!.visibility = View.GONE
                mReturnMeeting!!.visibility = View.VISIBLE
            } else {
                showProgressPanel(false)
            }
        }
    }

    override fun onMeetingParameterNotification(meetingParameter: MeetingParameter) {
        Log.d(TAG, "onMeetingParameterNotification $meetingParameter")
    }

    override fun onMeetingStatusChanged(
        meetingStatus: MeetingStatus,
        errorCode: Int,
        internalErrorCode: Int
    ) {
        Log.d(TAG, "onMeetingStatusChanged $meetingStatus:$errorCode:$internalErrorCode")
        if (!ZoomSDK.getInstance().isInitialized) {
            showProgressPanel(false)
            return
        }
        if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
            if (ZoomMeetingUISettingHelper.useExternalVideoSource) {
                ZoomMeetingUISettingHelper.changeVideoSource(true, this@InitAuthSDKActivity)
            }
        }
        if (ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
                showMeetingUi()
            }
        }
        refreshUI()
    }

    private fun showMeetingUi() {
        if (ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            val sharedPreferences = getSharedPreferences("UI_Setting", MODE_PRIVATE)
            val enable = sharedPreferences.getBoolean("enable_rawdata", false)
            var intent: Intent? = null
            if (!enable) {
                intent = Intent(this, MyMeetingActivity::class.java)
                intent.putExtra("from", 1)
            } else {
                intent = Intent(this, RawDataMeetingActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            this.startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        instance!!.onActivityResult(requestCode, this)
    }

    public override fun onDestroy() {
        super.onDestroy()
        UserLoginCallback.getInstance().removeListener(this)
        if (null != ZoomSDK.getInstance().meetingService) {
            ZoomSDK.getInstance().meetingService.removeListener(this)
        }
        if (null != ZoomSDK.getInstance().meetingSettingsHelper) {
            ZoomSDK.getInstance().meetingSettingsHelper.setCustomizedNotificationData(null, null)
        }
        InitAuthSDKHelper.getInstance().reset()
    }

    companion object {
        private const val TAG = "ZoomSDKExample"
    }
}