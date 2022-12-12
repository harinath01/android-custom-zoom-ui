package us.zoom.sdksample.inmeetingfunction.customizedmeetingui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.util.List;

import us.zoom.sdk.InMeetingEventHandler;
import us.zoom.sdk.InMeetingService;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.MobileRTCVideoUnitAspectMode;
import us.zoom.sdk.MobileRTCVideoUnitRenderInfo;
import us.zoom.sdk.MobileRTCVideoView;
import us.zoom.sdk.MobileRTCVideoViewManager;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdksample.R;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.audio.MeetingAudioCallback;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.bo.BOEventCallback;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.other.MeetingCommonCallback;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.share.MeetingShareCallback;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.user.MeetingUserCallback;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.video.MeetingVideoCallback;

public class MyMeetingActivity extends FragmentActivity implements MeetingUserCallback.UserEvent, MeetingCommonCallback.CommonEvent{

    private final static String TAG = MyMeetingActivity.class.getSimpleName();


    ZoomSDK zoomSDK;
    MeetingService meetingService;
    InMeetingService inMeetingService;
    FrameLayout meetingVideoView;
    View normalSenceView;
    private MobileRTCVideoView defaultVideoView;
    private MobileRTCVideoViewManager defaultVideoViewMgr;
    private View mWaitJoinView;
    private View mWaitRoomView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        zoomSDK = ZoomSDK.getInstance();
        meetingService = zoomSDK.getMeetingService();
        inMeetingService = zoomSDK.getInMeetingService();

        if (meetingService == null || inMeetingService == null) {
            finish();
            return;
        }
        setContentView(R.layout.my_meeting_layout);
        meetingVideoView = (FrameLayout) findViewById(R.id.meetingVideoView);
        meetingVideoView.setVisibility(View.VISIBLE);
        LayoutInflater inflater = getLayoutInflater();
        normalSenceView = inflater.inflate(R.layout.layout_meeting_content_normal, null);
        defaultVideoView = (MobileRTCVideoView) normalSenceView.findViewById(R.id.videoView);
        mWaitJoinView = (View) findViewById(R.id.waitJoinView);
        mWaitRoomView = (View) findViewById(R.id.waitingRoom);

        meetingVideoView.addView(normalSenceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        defaultVideoViewMgr = defaultVideoView.getVideoViewManager();
        MeetingCommonCallback.getInstance().addListener(this);
        MeetingUserCallback.getInstance().addListener(this);
    }

    @Override
    public void onMeetingUserJoin(List<Long> list) { }

    @Override
    public void onMeetingUserLeave(List<Long> list) {

    }

    @Override
    public void onSilentModeChanged(boolean inSilentMode) {

    }

    @Override
    public void onWebinarNeedRegister(String registerUrl) {

    }

    @Override
    public void onMeetingFail(int errorCode, int internalErrorCode) {}

    @Override
    public void onMeetingLeaveComplete(long ret) {

    }

    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
        checkShowVideoLayout(meetingStatus);
    }

    @Override
    public void onMeetingNeedPasswordOrDisplayName(boolean needPassword, boolean needDisplayName, InMeetingEventHandler handler) {

    }

    @Override
    public void onMeetingNeedColseOtherMeeting(InMeetingEventHandler inMeetingEventHandler) {

    }

    @Override
    public void onJoinWebinarNeedUserNameAndEmail(InMeetingEventHandler inMeetingEventHandler) {

    }

    @Override
    public void onFreeMeetingReminder(boolean isOrignalHost, boolean canUpgrade, boolean isFirstGift) {

    }

    private void checkShowVideoLayout(MeetingStatus meetingStatus) {
        removeExistingViews();
        if (meetingStatus == MeetingStatus.MEETING_STATUS_WAITINGFORHOST) {
            mWaitJoinView.setVisibility(View.VISIBLE);
            meetingVideoView.setVisibility(View.GONE);
        } else if (meetingStatus == MeetingStatus.MEETING_STATUS_IN_WAITING_ROOM) {
            mWaitRoomView.setVisibility(View.VISIBLE);
            meetingVideoView.setVisibility(View.GONE);
        } else if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING){
            if (defaultVideoViewMgr != null) {
                meetingVideoView.setVisibility(View.VISIBLE);
                defaultVideoViewMgr.removeAllVideoUnits();
                MobileRTCVideoUnitRenderInfo renderInfo = new MobileRTCVideoUnitRenderInfo(0, 0, 100, 100);
                defaultVideoViewMgr.addActiveVideoUnit(renderInfo);
            }
        }
    }

    private void removeExistingViews(){
        mWaitJoinView.setVisibility(View.GONE);
        mWaitRoomView.setVisibility(View.GONE);
        meetingVideoView.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(meetingService == null || inMeetingService == null){
            return;
        }
        defaultVideoView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(meetingService == null || inMeetingService == null){
            return;
        }
        clearSubscribe();
    }

    private void clearSubscribe(){
        if(null!=defaultVideoViewMgr)
        {
            defaultVideoViewMgr.removeAllVideoUnits();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterListener();
    }

    private void unRegisterListener() {
        try {
            MeetingUserCallback.getInstance().removeListener(this);
            MeetingCommonCallback.getInstance().removeListener(this);
        }catch (Exception e){
        }
    }
}

