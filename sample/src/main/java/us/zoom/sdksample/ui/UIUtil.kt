package us.zoom.sdksample.ui

import android.content.Context
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.Companion.instance
import us.zoom.sdk.ZoomSDK
import android.content.Intent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity

object UIUtil {
    fun returnToMeeting(context: Context?) {
        if (context == null) return
        if (ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            instance!!.hiddenMeetingWindow(true)
            val intent = Intent(context, MyMeetingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            context.startActivity(intent)
        } else {
            ZoomSDK.getInstance().zoomUIService.hideMiniMeetingWindow()
            ZoomSDK.getInstance().meetingService.returnToMeeting(context)
        }
    }
}