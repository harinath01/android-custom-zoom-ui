package us.zoom.sdksample.ui

import android.content.Context
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.Companion.instance
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper.hiddenMeetingWindow
import us.zoom.sdk.ZoomSDK
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper
import android.content.Intent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity
import us.zoom.sdk.InMeetingBOController
import us.zoom.sdk.IBOData
import us.zoom.sdk.IBOMeeting

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

    fun getBoNameUserNameByUserId(
        boController: InMeetingBOController?,
        userId: String?
    ): Array<String> {
        val res = arrayOf("", "")
        if (boController == null || userId == null || userId.length == 0) return res
        val iboData = boController.boDataHelper
        if (iboData != null) {
            val bIds = iboData.boMeetingIDList
            if (bIds != null && bIds.size > 0) {
                for (bId in bIds) {
                    val iboMeeting = iboData.getBOMeetingByID(bId)
                    if (iboMeeting != null) {
                        val users = iboMeeting.boUserList
                        if (users != null && users.contains(userId)) {
                            res[0] = iboMeeting.boName
                            res[1] = iboData.getBOUserName(userId)
                            break
                        }
                    }
                }
            }
        }
        return res
    }
}