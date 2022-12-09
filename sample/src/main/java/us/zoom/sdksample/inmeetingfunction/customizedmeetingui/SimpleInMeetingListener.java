package us.zoom.sdksample.inmeetingfunction.customizedmeetingui;

import java.util.List;

import us.zoom.sdk.ChatMessageDeleteType;
import us.zoom.sdk.FreeMeetingNeedUpgradeType;
import us.zoom.sdk.InMeetingAudioController;
import us.zoom.sdk.InMeetingChatController;
import us.zoom.sdk.InMeetingChatMessage;
import us.zoom.sdk.InMeetingEventHandler;
import us.zoom.sdk.InMeetingServiceListener;
import us.zoom.sdk.VideoQuality;

public abstract class SimpleInMeetingListener implements InMeetingServiceListener {

    @Override
    public void onMeetingNeedPasswordOrDisplayName(boolean b, boolean b1, InMeetingEventHandler inMeetingEventHandler) {

    }

    @Override
    public void onWebinarNeedRegister(String registerUrl) {

    }

    @Override
    public void onJoinWebinarNeedUserNameAndEmail(InMeetingEventHandler inMeetingEventHandler) {

    }

    @Override
    public void onMeetingNeedCloseOtherMeeting(InMeetingEventHandler inMeetingEventHandler) {

    }

    @Override
    public void onMeetingFail(int i, int i1) {

    }

    @Override
    public void onMeetingLeaveComplete(long l) {

    }

    @Override
    public void onMeetingUserJoin(List<Long> list) {

    }

    @Override
    public void onMeetingUserLeave(List<Long> list) {

    }

    @Override
    public void onMeetingUserUpdated(long l) {

    }

    @Override
    public void onMeetingHostChanged(long l) {

    }

    @Override
    public void onMeetingCoHostChanged(long l) {

    }

    @Override
    public void onMeetingCoHostChange(long userId, boolean isCoHost) {

    }

    @Override
    public void onActiveVideoUserChanged(long var1) {

    }

    @Override
    public void onActiveSpeakerVideoUserChanged(long var1) {

    }

    @Override
    public void onSpotlightVideoChanged(boolean b) {

    }

    @Override
    public void onSpotlightVideoChanged(List<Long> userList) {

    }

    @Override
    public void onUserVideoStatusChanged(long userId, VideoStatus status) {

    }

    @Override
    public void onMicrophoneStatusError(InMeetingAudioController.MobileRTCMicrophoneError mobileRTCMicrophoneError) {

    }

    @Override
    public void onUserAudioStatusChanged(long userId, AudioStatus audioStatus) {

    }

    @Override
    public void onUserAudioTypeChanged(long l) {

    }

    @Override
    public void onMyAudioSourceTypeChanged(int i) {

    }

    @Override
    public void onLowOrRaiseHandStatusChanged(long l, boolean b) {

    }

    @Override
    public void onChatMessageReceived(InMeetingChatMessage inMeetingChatMessage) {

    }

    @Override
    public void onChatMsgDeleteNotification(String msgID, ChatMessageDeleteType deleteBy) {

    }

    @Override
    public void onUserNetworkQualityChanged(long userId) {

    }

    @Override
    public void onSinkMeetingVideoQualityChanged(VideoQuality videoQuality, long userId) {

    }

    @Override
    public void onHostAskUnMute(long userId) {

    }

    @Override
    public void onHostAskStartVideo(long userId) {

    }

    @Override
    public void onSilentModeChanged(boolean inSilentMode){

    }

    @Override
    public void onFreeMeetingReminder(boolean isOrignalHost, boolean canUpgrade, boolean isFirstGift){

    }

    @Override
    public void onMeetingActiveVideo(long userId) {

    }

    @Override
    public void onSinkAttendeeChatPriviledgeChanged(int privilege) {

    }

    @Override
    public void onSinkAllowAttendeeChatNotification(int privilege) {

    }

    @Override
    public void onSinkPanelistChatPrivilegeChanged(InMeetingChatController.MobileRTCWebinarPanelistChatPrivilege privilege) {

    }

    @Override
    public void onUserNameChanged(long userId, String name) {

    }

    @Override
    public void onUserNamesChanged(List<Long> userList) {

    }

    @Override
    public void onFreeMeetingNeedToUpgrade(FreeMeetingNeedUpgradeType type, String gifUrl) {

    }

    @Override
    public void onFreeMeetingUpgradeToGiftFreeTrialStart() {

    }

    @Override
    public void onFreeMeetingUpgradeToGiftFreeTrialStop() {

    }

    @Override
    public void onFreeMeetingUpgradeToProMeeting() {

    }

    @Override
    public void onClosedCaptionReceived(String message,long senderId) {

    }

    @Override
    public void onRecordingStatus(RecordingStatus status) {

    }

    @Override
    public void onLocalRecordingStatus(long userId,RecordingStatus status) {

    }

    @Override
    public void onInvalidReclaimHostkey() {

    }

    @Override
    public void onHostVideoOrderUpdated(List<Long> orderList) {

    }

    @Override
    public void onFollowHostVideoOrderChanged(boolean bFollow) {

    }

    @Override
    public void onPermissionRequested(String[] permissions) {

    }

    @Override
    public void onAllHandsLowered() {
    }

    @Override
    public void onLocalVideoOrderUpdated(List<Long> localOrderList) {

    }
}
