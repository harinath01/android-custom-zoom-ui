package us.zoom.sdksample.inmeetingfunction.customizedmeetingui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import us.zoom.sdk.*
import us.zoom.sdksample.R


class CustomChatFragment : Fragment(), InMeetingServiceListener{
    private lateinit var zoomSDK: ZoomSDK
    private lateinit var inMeetingService: InMeetingService
    private lateinit var inMeetingChatController: InMeetingChatController
    private lateinit var inputBox: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        zoomSDK = ZoomSDK.getInstance()
        inMeetingService = zoomSDK.inMeetingService
        inMeetingChatController = inMeetingService.inMeetingChatController
        inMeetingService.addListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_custom_chat, container, false)
        sendButton = view.findViewById(R.id.chat_send)
        inputBox = view.findViewById(R.id.inputBox)
        recyclerView = view.findViewById(R.id.recycler_chat) as RecyclerView
        sendButton.setOnClickListener {
            if(inputBox.text.isNotBlank()){
                inMeetingChatController.sendChatToGroup(InMeetingChatController.MobileRTCChatGroup.MobileRTCChatGroup_All,
                    inputBox.text.toString()
                )
                inputBox.text.clear()
                recyclerView.post { recyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1) }
            }
        }
        return view
    }

    override fun onChatMessageReceived(message: InMeetingChatMessage?) {
        messageAdapter.addMessage(message)
        if (!recyclerView.canScrollVertically(1)) {
            recyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    override fun onMeetingUserJoin(p0: MutableList<Long>?) {
        messageAdapter = MessageListAdapter(inMeetingService.myUserID)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = messageAdapter
    }


    override fun onWebinarNeedRegister(p0: String?) {}
    override fun onJoinWebinarNeedUserNameAndEmail(p0: InMeetingEventHandler?) {}
    override fun onMeetingNeedCloseOtherMeeting(p0: InMeetingEventHandler?) {}
    override fun onMeetingFail(p0: Int, p1: Int) {}
    override fun onMeetingLeaveComplete(p0: Long) {}
    override fun onChatMsgDeleteNotification(p0: String?, p1: ChatMessageDeleteType?) {}
    override fun onSilentModeChanged(p0: Boolean) {}
    override fun onFreeMeetingReminder(p0: Boolean, p1: Boolean, p2: Boolean) {}
    override fun onMeetingActiveVideo(p0: Long) {}
    override fun onSinkAttendeeChatPriviledgeChanged(p0: Int) {}
    override fun onSinkAllowAttendeeChatNotification(p0: Int) {}
    override fun onSinkPanelistChatPrivilegeChanged(p0: InMeetingChatController.MobileRTCWebinarPanelistChatPrivilege?) {}
    override fun onUserNameChanged(p0: Long, p1: String?) {}
    override fun onUserNamesChanged(p0: MutableList<Long>?) {}
    override fun onFreeMeetingNeedToUpgrade(p0: FreeMeetingNeedUpgradeType?, p1: String?) {}
    override fun onFreeMeetingUpgradeToGiftFreeTrialStart() {}
    override fun onFreeMeetingUpgradeToGiftFreeTrialStop() {}
    override fun onFreeMeetingUpgradeToProMeeting() {}
    override fun onClosedCaptionReceived(p0: String?, p1: Long) {}
    override fun onRecordingStatus(p0: InMeetingServiceListener.RecordingStatus?) {}
    override fun onLocalRecordingStatus(p0: Long, p1: InMeetingServiceListener.RecordingStatus?) {}
    override fun onInvalidReclaimHostkey() {}
    override fun onPermissionRequested(p0: Array<out String>?) {}
    override fun onAllHandsLowered() {}
    override fun onLocalVideoOrderUpdated(p0: MutableList<Long>?) {}
    override fun onSpotlightVideoChanged(p0: MutableList<Long>?) {}
    override fun onUserVideoStatusChanged(p0: Long, p1: InMeetingServiceListener.VideoStatus?) {}
    override fun onUserNetworkQualityChanged(p0: Long) {}
    override fun onSinkMeetingVideoQualityChanged(p0: VideoQuality?, p1: Long) {}
    override fun onMicrophoneStatusError(p0: InMeetingAudioController.MobileRTCMicrophoneError?) {}
    override fun onUserAudioStatusChanged(p0: Long, p1: InMeetingServiceListener.AudioStatus?) {}
    override fun onMeetingUserLeave(p0: MutableList<Long>?) {}
    override fun onMeetingUserUpdated(p0: Long) {}
    override fun onMeetingHostChanged(p0: Long) {}
    override fun onMeetingCoHostChanged(p0: Long) {}
    override fun onMeetingCoHostChange(p0: Long, p1: Boolean) {}
    override fun onActiveVideoUserChanged(p0: Long) {}
    override fun onActiveSpeakerVideoUserChanged(p0: Long) {}
    override fun onHostVideoOrderUpdated(p0: MutableList<Long>?) {}
    override fun onFollowHostVideoOrderChanged(p0: Boolean) {}
    override fun onSpotlightVideoChanged(p0: Boolean) {}
    override fun onHostAskUnMute(p0: Long) {}
    override fun onHostAskStartVideo(p0: Long) {}
    override fun onUserAudioTypeChanged(p0: Long) {}
    override fun onMyAudioSourceTypeChanged(p0: Int) {}
    override fun onLowOrRaiseHandStatusChanged(p0: Long, p1: Boolean) {}
    override fun onMeetingNeedPasswordOrDisplayName(
        p0: Boolean,
        p1: Boolean,
        p2: InMeetingEventHandler?
    ) {}


}