package us.zoom.sdksample.ui

import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.Companion.instance
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.removeOverlayListener
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.showMeetingWindow
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.hiddenMeetingWindow
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.onActivityResult
import android.app.Activity
import android.app.AlertDialog
import us.zoom.sdksample.initsdk.AuthConstants
import android.widget.EditText
import android.os.Bundle
import us.zoom.sdksample.R
import android.content.Intent
import us.zoom.sdksample.ui.LoginUserStartJoinMeetingActivity
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper
import android.widget.Toast
import us.zoom.sdksample.startjoinmeeting.joinmeetingonly.JoinMeetingHelper
import android.content.DialogInterface
import us.zoom.sdksample.startjoinmeeting.LoginUserStartMeetingHelper
import us.zoom.sdksample.ui.UIUtil
import android.text.TextUtils
import us.zoom.sdksample.ui.MeetingSettingActivity
import us.zoom.sdksample.inmeetingfunction.zoommeetingui.ZoomMeetingUISettingHelper
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.Button
import us.zoom.sdk.*
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.RawDataMeetingActivity
import java.lang.Exception
import java.lang.NumberFormatException
import java.util.*

class LoginUserStartJoinMeetingActivity() : Activity(), AuthConstants, MeetingServiceListener,
    ZoomSDKAuthenticationListener {
    private var mEdtMeetingNo: EditText? = null
    private var mEdtMeetingPassword: EditText? = null
    private var mEdtVanityId: EditText? = null
    private var mBtnStartInstantMeeting: Button? = null
    private var mBtnLoginOut: Button? = null
    private var mBtnSettings: Button? = null
    private var mReturnMeeting: Button? = null
    private var mBtnDirectShare: Button? = null
    private var mbPendingStartMeeting = false
    private var isResumed = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_user_start_join)
        mEdtMeetingNo = findViewById<View>(R.id.edtMeetingNo) as EditText
        mEdtVanityId = findViewById<View>(R.id.edtVanityUrl) as EditText
        mEdtMeetingPassword = findViewById<View>(R.id.edtMeetingPassword) as EditText
        mBtnStartInstantMeeting = findViewById<View>(R.id.btnLoginUserStartInstant) as Button
        mBtnLoginOut = findViewById<View>(R.id.btnLogout) as Button
        mBtnSettings = findViewById(R.id.btn_settings)
        mReturnMeeting = findViewById(R.id.btn_return)
        mBtnDirectShare = findViewById(R.id.btnShare)
        registerListener()
    }

    private fun registerListener() {
        val zoomSDK = ZoomSDK.getInstance()
        zoomSDK.addAuthenticationListener(this)
        val meetingService = zoomSDK.meetingService
        meetingService?.addListener(this)
    }

    override fun onResume() {
        super.onResume()
        isResumed = true
        refreshUI()
        ZoomSDK.getInstance().zoomUIService.setZoomUIDelegate(object : SimpleZoomUIDelegate() {
            override fun afterMeetingMinimized(activity: Activity) {
                val intent = Intent(activity, LoginUserStartJoinMeetingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                activity.startActivity(intent)
            }
        })
        val preMeetingService = ZoomSDK.getInstance().preMeetingService
        if (null == preMeetingService || !preMeetingService.directShareService.canStartDirectShare()) {
            mBtnDirectShare!!.visibility = View.GONE
        } else {
            mBtnDirectShare!!.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        isResumed = false
    }

    override fun onDestroy() {
        val zoomSDK = ZoomSDK.getInstance()
        zoomSDK.removeAuthenticationListener(this) //unregister ZoomSDKAuthenticationListener
        if (zoomSDK.isInitialized) {
            val meetingService = zoomSDK.meetingService
            meetingService.removeListener(this) //unregister meetingServiceListener
        }
        instance!!.removeOverlayListener()
        super.onDestroy()
    }

    fun onClickBtnJoinMeeting(view: View?) {
        val meetingNo = mEdtMeetingNo!!.text.toString().trim { it <= ' ' }
        val meetingPassword = mEdtMeetingPassword!!.text.toString().trim { it <= ' ' }
        val vanityId = mEdtVanityId!!.text.toString().trim { it <= ' ' }
        if (meetingNo.length == 0 && vanityId.length == 0) {
            Toast.makeText(
                this,
                "You need to enter a meeting number/ vanity id which you want to join.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (meetingNo.length != 0 && vanityId.length != 0) {
            Toast.makeText(
                this,
                "Both meeting number and vanity id have value,  just set one of them",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val zoomSDK = ZoomSDK.getInstance()
        if (!zoomSDK.isInitialized) {
            Toast.makeText(this, "ZoomSDK has not been initialized successfully", Toast.LENGTH_LONG)
                .show()
            return
        }
        val meetingService = zoomSDK.meetingService
        var ret = -1
        if (vanityId.length != 0) {
            ret = JoinMeetingHelper.getInstance()
                .joinMeetingWithVanityId(this, vanityId, meetingPassword)
        } else {
            ret = JoinMeetingHelper.getInstance()
                .joinMeetingWithNumber(this, meetingNo, meetingPassword)
        }
        Log.i(TAG, "onClickBtnJoinMeeting, ret=$ret")
    }

    fun onClickBtnStartMeeting(view: View?) {
        val meetingNo = mEdtMeetingNo!!.text.toString().trim { it <= ' ' }
        val vanityId = mEdtVanityId!!.text.toString().trim { it <= ' ' }
        if (meetingNo.length == 0 && vanityId.length == 0) {
            Toast.makeText(
                this,
                "You need to enter a meeting number/ vanity  which you want to join.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (meetingNo.length != 0 && vanityId.length != 0) {
            Toast.makeText(
                this,
                "Both meeting number and vanity  have value,  just set one of them",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val zoomSDK = ZoomSDK.getInstance()
        if (!zoomSDK.isInitialized) {
            Toast.makeText(this, "ZoomSDK has not been initialized successfully", Toast.LENGTH_LONG)
                .show()
            return
        }
        val meetingService = zoomSDK.meetingService
        if (meetingService.meetingStatus != MeetingStatus.MEETING_STATUS_IDLE) {
            var lMeetingNo: Long = 0
            try {
                lMeetingNo = meetingNo.toLong()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid meeting number: $meetingNo", Toast.LENGTH_LONG).show()
                return
            }
            if (meetingService.currentRtcMeetingNumber == lMeetingNo) {
                meetingService.returnToMeeting(this)
                return
            }
            AlertDialog.Builder(this)
                .setMessage("Do you want to leave current meeting and start another?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    mbPendingStartMeeting = true
                    meetingService.leaveCurrentMeeting(false)
                })
                .setNegativeButton("No", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {}
                })
                .show()
            return
        }
        var ret = -1
        if (vanityId.length != 0) {
            ret = LoginUserStartMeetingHelper.getInstance().startMeetingWithVanityId(this, vanityId)
        } else {
            ret = LoginUserStartMeetingHelper.getInstance().startMeetingWithNumber(this, meetingNo)
        }
        Log.i(TAG, "onClickBtnStartMeeting, ret=$ret")
    }

    fun onClickBtnLoginUserStartInstant(view: View?) {
        val zoomSDK = ZoomSDK.getInstance()
        if (!zoomSDK.isInitialized) {
            Toast.makeText(this, "ZoomSDK has not been initialized successfully", Toast.LENGTH_LONG)
                .show()
            return
        }
        val ret = LoginUserStartMeetingHelper.getInstance().startInstanceMeeting(this)
        Log.i(TAG, "onClickBtnLoginUserStartInstant, ret=$ret")
    }

    fun onClickReturnMeeting(view: View?) {
        UIUtil.returnToMeeting(this)
    }

    fun onClickDirectShare(view: View?) {
        val shareServiceHelper = ZoomSDK.getInstance().preMeetingService.directShareService
        if (shareServiceHelper.isDirectShareInProgress) {
            Toast.makeText(baseContext, "isDirectShareInProgress", Toast.LENGTH_SHORT).show()
            return
        }
        val meetingNumber = mEdtMeetingNo!!.text.toString()
        val pairCode = mEdtVanityId!!.text.toString()
        if (TextUtils.isEmpty(meetingNumber) && TextUtils.isEmpty(pairCode)) {
            Toast.makeText(baseContext, "meetingNumber or pairCode is null", Toast.LENGTH_SHORT)
                .show()
            return
        }
        var number: Long = 0
        if (TextUtils.isEmpty(pairCode)) {
            try {
                number = meetingNumber.toLong()
            } catch (e: Exception) {
                return
            }
        }
        val tempNumber = number
        shareServiceHelper.setEvent(object : IDirectShareServiceHelperEvent {
            override fun onDirectShareStatusUpdate(
                status: DirectShareStatus,
                handler: IDirectShareViaMeetingIDOrPairingCodeHandler
            ) {
                if (null != handler) {
                    if (status == DirectShareStatus.DirectShare_Need_MeetingID_Or_PairingCode) {
                        if (tempNumber != 0L) {
                            handler.tryWithMeetingNumber(tempNumber)
                        } else {
                            val code = pairCode.uppercase(Locale.getDefault())
                            Toast.makeText(baseContext, "Code:$code", Toast.LENGTH_SHORT).show()
                            handler.tryWithPairingCode(code)
                        }
                    } else if (status == DirectShareStatus.DirectShare_WrongMeetingID_Or_SharingKey) {
                        Toast.makeText(baseContext, "Wrong Id or Key:", Toast.LENGTH_SHORT).show()
                        handler.cancel()
                    } else if (status == DirectShareStatus.DirectShare_Other_Error || status == DirectShareStatus.DirectShare_NetWork_Error) {
                        Toast.makeText(baseContext, "Error:$status", Toast.LENGTH_SHORT).show()
                        handler.cancel()
                    }
                }
                Log.d(TAG, "status:" + status + " :" + shareServiceHelper.isDirectShareInProgress)
            }
        })
        shareServiceHelper.startDirectShare()
    }

    fun onClickBtnLogout(view: View?) {
        val zoomSDK = ZoomSDK.getInstance()
        if (!zoomSDK.logoutZoom()) {
            Toast.makeText(this, "ZoomSDK has not been initialized successfully", Toast.LENGTH_LONG)
                .show()
        }
    }

    fun onClickSettings(view: View?) {
        val intent = Intent(this, MeetingSettingActivity::class.java)
        startActivity(intent)
    }

    override fun onMeetingParameterNotification(meetingParameter: MeetingParameter) {}
    override fun onMeetingStatusChanged(
        meetingStatus: MeetingStatus, errorCode: Int,
        internalErrorCode: Int
    ) {
        Log.i(
            TAG,
            ("onMeetingStatusChanged, meetingStatus=" + meetingStatus + ", errorCode=" + errorCode
                    + ", internalErrorCode=" + internalErrorCode)
        )
        if (meetingStatus == MeetingStatus.MEETING_STATUS_FAILED && errorCode == MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE) {
            Toast.makeText(this, "Version of ZoomSDK is too low!", Toast.LENGTH_LONG).show()
        }
        if (mbPendingStartMeeting && meetingStatus == MeetingStatus.MEETING_STATUS_IDLE) {
            mbPendingStartMeeting = false
            onClickBtnStartMeeting(null)
        }
        if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
            if (ZoomMeetingUISettingHelper.useExternalVideoSource) {
                ZoomMeetingUISettingHelper.changeVideoSource(
                    true,
                    this@LoginUserStartJoinMeetingActivity
                )
            }
            showMeetingUi()
        }
        refreshUI()
    }

    private fun refreshUI() {
        if (null == ZoomSDK.getInstance().meetingService) {
            return
        }
        val meetingStatus = ZoomSDK.getInstance().meetingService.meetingStatus
        if ((meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) || (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING
                    ) || (meetingStatus == MeetingStatus.MEETING_STATUS_RECONNECTING)
        ) {
            mBtnSettings!!.visibility = View.GONE
            mReturnMeeting!!.visibility = View.VISIBLE
        } else {
            mBtnSettings!!.visibility = View.VISIBLE
            mReturnMeeting!!.visibility = View.GONE
        }
        if (ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING && isResumed) {
                instance!!.showMeetingWindow(this)
            } else {
                instance!!.hiddenMeetingWindow(true)
            }
        }
    }

    private fun showMeetingUi() {
        if (ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            val sharedPreferences = getSharedPreferences("UI_Setting", MODE_PRIVATE)
            val enable = sharedPreferences.getBoolean("enable_rawdata", false)
            var intent: Intent? = null
            if (!enable) {
                intent = Intent(this, MyMeetingActivity::class.java)
            } else {
                intent = Intent(this, RawDataMeetingActivity::class.java)
            }
            intent.putExtra("from", 3)
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            this.startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if (ZoomSDK.getInstance().isLoggedIn) {
            moveTaskToBack(true)
        } else {
            super.onBackPressed()
        }
    }

    override fun onZoomSDKLoginResult(l: Long) {}
    override fun onZoomSDKLogoutResult(result: Long) {
        if (result == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS.toLong()) {
            Toast.makeText(this, "Logout successfully", Toast.LENGTH_SHORT).show()
            showLoginView()
        } else {
            Toast.makeText(this, "Logout failed result code = $result", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onZoomIdentityExpired() {
        ZoomSDK.getInstance().logoutZoom()
    }

    override fun onZoomAuthIdentityExpired() {}
    private fun showLoginView() {
        mEdtMeetingPassword!!.postDelayed(object : Runnable {
            override fun run() {
                if (ZoomSDK.getInstance().isInitialized) {
                }
                finish()
            }
        }, 500)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        instance!!.onActivityResult(requestCode, this)
    }

    companion object {
        private val TAG = "ZoomSDKExample"
        private val DISPLAY_NAME = "ZoomUS SDK"
    }
}