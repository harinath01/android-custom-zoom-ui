package us.zoom.sdkexample2;

import us.zoom.sdk.ChatMessageDeleteType;
import us.zoom.sdk.FreeMeetingNeedUpgradeType;
import us.zoom.sdk.InMeetingAudioController;
import us.zoom.sdk.InMeetingChatController;
import us.zoom.sdk.InMeetingChatMessage;
import us.zoom.sdk.InMeetingEventHandler;
import us.zoom.sdk.InMeetingService;
import us.zoom.sdk.InMeetingServiceListener;
import us.zoom.sdk.MeetingActivity;
import us.zoom.sdk.MeetingParameter;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.VideoQuality;
import us.zoom.sdk.ZoomSDK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import androidx.core.app.ActivityCompat;

import java.util.List;

public class MyMeetingActivity extends MeetingActivity {

	private Button btnLeaveZoomMeeting;
	private Button btnSwitchToNextCamera;
	private Button btnAudio;
	private Button btnParticipants;
	private Button btnShare;
	private Button btnStopShare;
	private Button btnMoreOptions;

	private View viewTabMeeting;
	private Button btnTabWelcome;
	private Button btnTabMeeting;
	private Button btnTabPage2;
	private InMeetingService inMeetingService;

	@Override
	protected int getLayout() {
		return R.layout.my_meeting_layout;
	}

	@Override
	protected boolean isAlwaysFullScreen() {
		return false;
	}

	@Override
	protected boolean isSensorOrientationEnabled() {
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!ZoomSDK.getInstance().isInitialized()) {
			finish();
			return;
		}
		if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(MyMeetingActivity.this, new String[]{android.Manifest.permission.CAMERA}, 1001);
		}
		inMeetingService = ZoomSDK.getInstance().getInMeetingService();
		initListeners();
		disableFullScreenMode();

		setupTabs();

		btnLeaveZoomMeeting = (Button)findViewById(R.id.btnLeaveZoomMeeting);
		btnSwitchToNextCamera = (Button)findViewById(R.id.btnSwitchToNextCamera);
		btnAudio = (Button)findViewById(R.id.btnAudio);
		btnParticipants = (Button)findViewById(R.id.btnParticipants);
		btnShare = (Button)findViewById(R.id.btnShare);
		btnStopShare = (Button)findViewById(R.id.btnStopShare);
		btnMoreOptions = (Button)findViewById(R.id.btnMoreOptions);

		btnLeaveZoomMeeting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ZoomSDK.getInstance().getInMeetingService().leaveCurrentMeeting(true);
			}
		});

		btnSwitchToNextCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				inMeetingService.getInMeetingVideoController().switchToNextCamera();
			}
		});

		btnAudio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InMeetingAudioController inMeetingAudioController= inMeetingService.getInMeetingAudioController();
				if (inMeetingAudioController.isAudioConnected()) {
					inMeetingAudioController.muteMyAudio(!inMeetingAudioController.isMyAudioMuted());
				} else {
					inMeetingAudioController.connectAudioWithVoIP();
				}
			}
		});

		btnParticipants.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				inMeetingService.showZoomParticipantsUI(MyMeetingActivity.this, 1100);
			}
		});

		btnShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});

		btnStopShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopShare();
			}
		});

		btnMoreOptions.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});

	}

	private void initListeners() {
		ZoomSDK.getInstance().getMeetingService().addListener(new MeetingServiceListener() {
			@Override
			public void onMeetingStatusChanged(MeetingStatus meetingStatus, int i, int i1) {
				if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
					updateButtonsStatus();
				}
			}

			@Override
			public void onMeetingParameterNotification(MeetingParameter meetingParameter) {

			}
		});
		ZoomSDK.getInstance().getInMeetingService().addListener(new InMeetingServiceListener() {
			@Override
			public void onMeetingNeedPasswordOrDisplayName(boolean b, boolean b1, InMeetingEventHandler inMeetingEventHandler) {

			}

			@Override
			public void onWebinarNeedRegister(String s) {

			}

			@Override
			public void onJoinWebinarNeedUserNameAndEmail(InMeetingEventHandler inMeetingEventHandler) {

			}

			@Override
			public void onMeetingNeedCloseOtherMeeting(InMeetingEventHandler handler) {

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
			public void onActiveVideoUserChanged(long l) {

			}

			@Override
			public void onActiveSpeakerVideoUserChanged(long l) {

			}

			@Override
			public void onSpotlightVideoChanged(boolean b) {

			}

			@Override
			public void onSpotlightVideoChanged(List<Long> userList) {

			}

			@Override
			public void onUserVideoStatusChanged(long l, VideoStatus videoStatus) {

			}

			@Override
			public void onUserNetworkQualityChanged(long l) {

			}

			@Override
			public void onSinkMeetingVideoQualityChanged(VideoQuality videoQuality, long userId) {
				
			}

			@Override
			public void onMicrophoneStatusError(InMeetingAudioController.MobileRTCMicrophoneError mobileRTCMicrophoneError) {

			}

			@Override
			public void onUserAudioStatusChanged(long l, AudioStatus audioStatus) {

			}

			@Override
			public void onHostAskUnMute(long l) {

			}

			@Override
			public void onHostAskStartVideo(long l) {

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
			public void onSilentModeChanged(boolean b) {
				updateButtonsStatus();
			}

			@Override
			public void onFreeMeetingReminder(boolean b, boolean b1, boolean b2) {

			}

			@Override
			public void onMeetingActiveVideo(long l) {

			}

			@Override
			public void onSinkAttendeeChatPriviledgeChanged(int i) {

			}

			@Override
			public void onSinkAllowAttendeeChatNotification(int i) {

			}

			@Override
			public void onSinkPanelistChatPrivilegeChanged(InMeetingChatController.MobileRTCWebinarPanelistChatPrivilege privilege) {

			}

			@Override
			public void onUserNameChanged(long l, String s) {

			}

			@Override
			public void onUserNamesChanged(List<Long> userList) {

			}

			@Override
			public void onPermissionRequested(String[] permissions) {

			}

			@Override
			public void onFreeMeetingNeedToUpgrade(FreeMeetingNeedUpgradeType freeMeetingNeedUpgradeType, String s) {

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
			public void onClosedCaptionReceived(String message, long senderId) {

			}

			@Override
			public void onRecordingStatus(RecordingStatus recordingStatus) {

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
			public void onAllHandsLowered() {

			}

			@Override
			public void onLocalRecordingStatus(long userId, RecordingStatus status) {

			}

			@Override
			public void onLocalVideoOrderUpdated(List<Long> localOrderList) {

			}
		});
	}

	@Override
	public void onConfigurationChanged (Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		disableFullScreenMode();
	}

	private void disableFullScreenMode() {
		getWindow().setFlags(~WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
	}

	private void setupTabs() {
		viewTabMeeting = findViewById(R.id.viewTabMeeting);
		btnTabWelcome = (Button)findViewById(R.id.btnTabWelcome);
		btnTabMeeting = (Button)findViewById(R.id.btnTabMeeting);
		btnTabPage2 = (Button)findViewById(R.id.btnTabPage2);

		selectTab(MainActivity.TAB_MEETING);

		btnTabMeeting.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				selectTab(MainActivity.TAB_MEETING);
			}
		});

		btnTabWelcome.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				selectTab(MainActivity.TAB_WELCOME);
			}
		});

		btnTabPage2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				selectTab(MainActivity.TAB_PAGE_2);
			}
		});
	}

	private void selectTab(int tabId) {
		if(tabId == MainActivity.TAB_MEETING) {
			btnTabWelcome.setSelected(false);
			btnTabPage2.setSelected(false);
			btnTabMeeting.setSelected(true);
		} else {
			switchToMainActivity(tabId);
		}
	}

	private void switchToMainActivity(int tab) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction(MainActivity.ACTION_RETURN_FROM_MEETING);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.putExtra(MainActivity.EXTRA_TAB_ID, tab);

		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateButtonsStatus();

		// disable animation
		overridePendingTransition(0, 0);
	}

	@Override
	protected void onStartShare() {
		btnShare.setVisibility(View.GONE);
		btnStopShare.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onStopShare() {
		btnShare.setVisibility(View.VISIBLE);
		btnStopShare.setVisibility(View.GONE);
	}

	private void updateButtonsStatus() {

		boolean enabled = (inMeetingService.isMeetingConnected() && !inMeetingService.isInWaitingRoom());

		btnSwitchToNextCamera.setEnabled(enabled);
		btnAudio.setEnabled(enabled);
		btnParticipants.setEnabled(enabled);
		btnShare.setEnabled(enabled);
		btnMoreOptions.setEnabled(enabled);
		if(inMeetingService.getInMeetingShareController().isSharingOut()) {
			btnShare.setVisibility(View.GONE);
			btnStopShare.setVisibility(View.VISIBLE);
		} else {
			btnShare.setVisibility(View.VISIBLE);
			btnStopShare.setVisibility(View.GONE);
		}
	}
}
