package us.zoom.sdksample.inmeetingfunction.customizedmeetingui

import androidx.fragment.app.FragmentActivity
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.user.MeetingUserCallback.UserEvent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.share.MeetingShareCallback.ShareEvent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.other.MeetingCommonCallback.CommonEvent
import android.widget.FrameLayout
import android.os.Bundle
import android.view.WindowManager
import us.zoom.sdksample.R
import android.view.View
import android.view.ViewGroup
import us.zoom.sdk.*
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.other.MeetingCommonCallback
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.user.MeetingUserCallback
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.share.MeetingShareCallback
import java.lang.Exception

class MyMeetingActivity : FragmentActivity(), UserEvent, ShareEvent, CommonEvent {
    lateinit var zoomSDK: ZoomSDK;
    lateinit var meetingService: MeetingService;
    lateinit var inMeetingService: InMeetingService;
    lateinit var meetingVideoView: FrameLayout;
    lateinit var normalSenceView: View;
    private var defaultVideoView: MobileRTCVideoView? = null
    private var defaultVideoViewMgr: MobileRTCVideoViewManager? = null
    private var mWaitJoinView: View? = null
    private var mWaitRoomView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        zoomSDK = ZoomSDK.getInstance()
        meetingService = zoomSDK.getMeetingService()
        inMeetingService = zoomSDK.getInMeetingService()
        setContentView(R.layout.my_meeting_layout)
        meetingVideoView = findViewById<View>(R.id.meetingVideoView) as FrameLayout
        meetingVideoView.visibility = View.VISIBLE
        val inflater = layoutInflater
        normalSenceView = inflater.inflate(R.layout.layout_meeting_content_normal, null) as View
        defaultVideoView = normalSenceView.findViewById<View>(R.id.videoView) as MobileRTCVideoView
        mWaitJoinView = findViewById(R.id.waitJoinView) as View
        mWaitRoomView = findViewById(R.id.waitingRoom) as View
        meetingVideoView.addView(
            normalSenceView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        defaultVideoViewMgr = defaultVideoView!!.videoViewManager
        MeetingCommonCallback.getInstance().addListener(this)
        MeetingUserCallback.getInstance().addListener(this)
        MeetingShareCallback.getInstance().addListener(this)
    }

    override fun onMeetingUserJoin(list: List<Long>) {}
    override fun onMeetingUserLeave(list: List<Long>) {}
    override fun onSilentModeChanged(inSilentMode: Boolean) {}
    override fun onWebinarNeedRegister(registerUrl: String) {}
    override fun onMeetingFail(errorCode: Int, internalErrorCode: Int) {}
    override fun onMeetingLeaveComplete(ret: Long) {}
    override fun onMeetingStatusChanged(
        meetingStatus: MeetingStatus,
        errorCode: Int,
        internalErrorCode: Int
    ) {
        checkShowVideoLayout()
    }

    override fun onMeetingNeedPasswordOrDisplayName(
        needPassword: Boolean,
        needDisplayName: Boolean,
        handler: InMeetingEventHandler
    ) {
    }

    override fun onMeetingNeedColseOtherMeeting(inMeetingEventHandler: InMeetingEventHandler) {}
    override fun onJoinWebinarNeedUserNameAndEmail(inMeetingEventHandler: InMeetingEventHandler) {}
    override fun onFreeMeetingReminder(
        isOrignalHost: Boolean,
        canUpgrade: Boolean,
        isFirstGift: Boolean
    ) {
    }

    private fun checkShowVideoLayout() {
        removeExistingViews()
        val meetingStatus = meetingService.meetingStatus
        if (meetingStatus == MeetingStatus.MEETING_STATUS_WAITINGFORHOST) {
            mWaitJoinView!!.visibility = View.VISIBLE
            meetingVideoView.visibility = View.GONE
        } else if (meetingStatus == MeetingStatus.MEETING_STATUS_IN_WAITING_ROOM) {
            mWaitRoomView!!.visibility = View.VISIBLE
            meetingVideoView.visibility = View.GONE
        } else if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
            if (defaultVideoViewMgr != null) {
                meetingVideoView.visibility = View.VISIBLE
                defaultVideoViewMgr!!.removeAllVideoUnits()
                val renderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
                val shareController = inMeetingService.inMeetingShareController
                if (shareController.isOtherSharing) {
                    defaultVideoViewMgr!!.addShareVideoUnit(
                        inMeetingService.activeShareUserID(),
                        renderInfo
                    )
                } else {
                    defaultVideoViewMgr!!.addActiveVideoUnit(renderInfo)
                }
            }
        }
    }

    private fun removeExistingViews() {
        mWaitJoinView!!.visibility = View.GONE
        mWaitRoomView!!.visibility = View.GONE
        meetingVideoView!!.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        defaultVideoView!!.onPause()
    }

    override fun onStop() {
        super.onStop()
        clearSubscribe()
    }

    private fun clearSubscribe() {
        if (null != defaultVideoViewMgr) {
            defaultVideoViewMgr!!.removeAllVideoUnits()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterListener()
    }

    private fun unRegisterListener() {
        try {
            MeetingUserCallback.getInstance().removeListener(this)
            MeetingCommonCallback.getInstance().removeListener(this)
        } catch (_: Exception) {
        }
    }

    override fun onShareActiveUser(userId: Long) {
        checkShowVideoLayout()
    }

    override fun onShareUserReceivingStatus(userId: Long) {}
    override fun onShareSettingTypeChanged(type: ShareSettingType) {}

    companion object {
        private val TAG = MyMeetingActivity::class.java.simpleName
    }
}