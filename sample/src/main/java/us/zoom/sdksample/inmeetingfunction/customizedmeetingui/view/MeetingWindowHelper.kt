package us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view

import us.zoom.sdk.InMeetingShareController.InMeetingShareListener
import us.zoom.sdk.MobileRTCVideoView
import us.zoom.sdk.MobileRTCVideoUnitRenderInfo
import android.app.Activity
import android.content.Context
import us.zoom.sdk.ZoomSDK
import android.os.Build
import android.content.Intent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper
import us.zoom.sdksample.R
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.SingleTapConfirm
import us.zoom.sdk.InMeetingShareController
import android.view.GestureDetector.SimpleOnGestureListener
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity
import android.view.View.OnTouchListener
import android.graphics.PixelFormat
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.*
import us.zoom.sdk.SharingStatus
import us.zoom.sdk.ShareSettingType
import java.lang.Exception
import java.lang.ref.SoftReference

class MeetingWindowHelper private constructor() : InMeetingShareListener {
    private var mbAddedView = false
    var windowView: View? = null
    var lastX = 0
    var lastY = 0
    var gestureDetector: GestureDetector? = null
    var mobileRTCVideoView: MobileRTCVideoView? = null
    var renderInfo: MobileRTCVideoUnitRenderInfo? = null
    private var mWindowManager: WindowManager? = null
    private var refContext: SoftReference<Context?>? = null
    fun showMeetingWindow(context: Activity) {
        ZoomSDK.getInstance().inMeetingService.inMeetingShareController.addListener(this)
        val userList = ZoomSDK.getInstance().inMeetingService.inMeetingUserList
        if (null == userList || userList.size < 2) {
            //only me
            return
        }
        if (mbAddedView) {
            windowView!!.visibility = View.VISIBLE
            addVideoUnit()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.packageName)
            )
            context.startActivityForResult(intent, REQUEST_SYSTEM_ALERT_WINDOW)
        } else {
            showMiniMeetingWindow(context)
        }
    }

    fun onActivityResult(requestCode: Int, context: Context) {
        if (refContext != null && refContext!!.get() != null && refContext!!.get() === context) {
            when (requestCode) {
                REQUEST_SYSTEM_ALERT_WINDOW -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !Settings.canDrawOverlays(
                            context
                        )
                    ) {
                        return
                    }
                    showMiniMeetingWindow(context)
                }
            }
        }
    }

    fun removeOverlayListener() {}
    private fun showMiniMeetingWindow(context: Context) {
        refContext = SoftReference(context)
        if (null == mWindowManager) {
            mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
        if (null == windowView) {
            windowView = LayoutInflater.from(context).inflate(R.layout.layout_meeting_window, null)
            mobileRTCVideoView =
                windowView.findViewById<View>(R.id.active_video_view) as MobileRTCVideoView
            renderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
            renderInfo!!.is_border_visible = true
            gestureDetector = GestureDetector(context, SingleTapConfirm())
            windowView.setOnTouchListener(onTouchListener)
        }
        mWindowManager!!.addView(windowView, getLayoutParams(context))
        mbAddedView = true
        addVideoUnit()
    }

    private fun addVideoUnit() {
        val shareController = ZoomSDK.getInstance().inMeetingService.inMeetingShareController
        mobileRTCVideoView!!.videoViewManager.removeAllVideoUnits()
        val shareUserId: Long = -1
        if (shareUserId > 0 && (shareController.isOtherSharing || shareController.isSharingOut)) {
            mobileRTCVideoView!!.videoViewManager.addShareVideoUnit(shareUserId, renderInfo)
        } else {
            mobileRTCVideoView!!.videoViewManager.addActiveVideoUnit(renderInfo)
        }
        mobileRTCVideoView!!.onResume()
    }

    private inner class SingleTapConfirm : SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            if (null != refContext && null != refContext!!.get()) {
                hiddenMeetingWindow(false)
                val context = refContext!!.get()
                val intent = Intent(context, MyMeetingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                context!!.startActivity(intent)
            }
            return true
        }
    }

    var onTouchListener = OnTouchListener { view, event ->
        if (gestureDetector!!.onTouchEvent(event)) {
            return@OnTouchListener true
        }
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                view.parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val params = view.layoutParams as WindowManager.LayoutParams
                val dx = event.rawX.toInt() - lastX
                val dy = event.rawY.toInt() - lastY
                val left = params.x + dx
                val top = params.y + dy
                params.x = left
                params.y = top
                mWindowManager!!.updateViewLayout(windowView, params)
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
            }
        }
        true
    }

    private fun getLayoutParams(context: Context): WindowManager.LayoutParams {
        val lp = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Settings.canDrawOverlays(context)) {
            lp.type = getSystemAlertWindowType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        } else {
            lp.type = getSystemAlertWindowType(WindowManager.LayoutParams.TYPE_TOAST)
        }
        lp.flags = lp.flags or (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        val width = mWindowManager!!.defaultDisplay.width
        val height = mWindowManager!!.defaultDisplay.height
        lp.format = PixelFormat.RGBA_8888
        windowView!!.measure(-1, -1)
        lp.x = width - windowView!!.measuredWidth - 40
        lp.y = 80
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP or Gravity.LEFT
        return lp
    }

    fun hiddenMeetingWindow(destroy: Boolean) {
        if (null == windowView || null == mWindowManager || null == mobileRTCVideoView) {
            return
        }
        mobileRTCVideoView!!.videoViewManager.removeAllVideoUnits()
        if (!destroy) {
            windowView!!.visibility = View.GONE
        } else {
            try {
                mWindowManager!!.removeView(windowView)
            } catch (e: Exception) {
            }
            mbAddedView = false
            windowView = null
            mobileRTCVideoView = null
        }
        //        ZoomSDK.getInstance().getInMeetingService().getInMeetingShareController().removeListener(this);
    }

    override fun onShareActiveUser(userId: Long) {
        if (mbAddedView && null != mobileRTCVideoView) {
            if (userId < 0) {
                mobileRTCVideoView!!.videoViewManager.removeAllVideoUnits()
                mobileRTCVideoView!!.videoViewManager.addActiveVideoUnit(renderInfo)
            } else {
                mobileRTCVideoView!!.videoViewManager.removeAllVideoUnits()
                val renderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
                mobileRTCVideoView!!.videoViewManager.addShareVideoUnit(userId, renderInfo)
            }
        }
    }

    override fun onShareUserReceivingStatus(userId: Long) {
        Log.d("MeetingWindowHelper", "userId:$userId")
    }

    override fun onSharingStatus(status: SharingStatus, userId: Long) {}
    override fun onShareSettingTypeChanged(type: ShareSettingType) {}
    private fun getSystemAlertWindowType(type: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) //https://developer.android.com/about/versions/oreo/android-8.0-changes#cwt https://developer.android.com/reference/android/view/WindowManager.LayoutParams#TYPE_PHONE
            if (type == WindowManager.LayoutParams.TYPE_PHONE || type == WindowManager.LayoutParams.TYPE_PRIORITY_PHONE || type == WindowManager.LayoutParams.TYPE_SYSTEM_ALERT || type == WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY || type == WindowManager.LayoutParams.TYPE_SYSTEM_ERROR || type == WindowManager.LayoutParams.TYPE_TOAST) return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        return type
    }

    companion object {
        const val REQUEST_SYSTEM_ALERT_WINDOW = 1020
        var instance: MeetingWindowHelper? = null
            get() {
                if (null == field) {
                    synchronized(MeetingWindowHelper::class.java) {
                        if (null == field) {
                            field = MeetingWindowHelper()
                        }
                    }
                }
                return field
            }
            private set
    }
}