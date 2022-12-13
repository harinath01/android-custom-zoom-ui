package us.zoom.sdksample.inmeetingfunction.customizedmeetingui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import us.zoom.sdksample.R
import us.zoom.sdk.*

class CustomChatFragment : Fragment() {
    lateinit private var zoomSDK: ZoomSDK
    lateinit private var inMeetingService: InMeetingService
    lateinit private var inMeetingChatController: InMeetingChatController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        zoomSDK = ZoomSDK.getInstance()
        inMeetingService = zoomSDK.inMeetingService
        inMeetingChatController = inMeetingService.inMeetingChatController
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_custom_chat, container, false)
    }
}