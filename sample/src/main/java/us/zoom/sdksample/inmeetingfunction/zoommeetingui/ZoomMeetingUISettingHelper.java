package us.zoom.sdksample.inmeetingfunction.zoommeetingui;


import android.content.Context;

import androidx.annotation.NonNull;

import us.zoom.sdk.InstantMeetingOptions;
import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.MeetingOptions;
import us.zoom.sdk.StartMeetingOptions;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.rawdata.VirtualVideoSource;

public class ZoomMeetingUISettingHelper {

    private static JoinMeetingOptions meetingOptions = new JoinMeetingOptions();

    public static boolean useExternalVideoSource=false;

    public static VirtualVideoSource virtualVideoSource;


    public static void changeVideoSource(boolean useVideoSource, @NonNull Context context) {
        if (null == virtualVideoSource) {
            virtualVideoSource = new VirtualVideoSource(context);
        }
        useExternalVideoSource=useVideoSource;
        ZoomSDK.getInstance().getVideoSourceHelper().setExternalVideoSource(useVideoSource ? virtualVideoSource : null);
    }



    public static JoinMeetingOptions getMeetingOptions() {
        return meetingOptions;
    }

    public static StartMeetingOptions getStartMeetingOptions() {
        StartMeetingOptions opts = new StartMeetingOptions();
        fillMeetingOption(opts);
        return opts;
    }

    public static JoinMeetingOptions getJoinMeetingOptions() {
        JoinMeetingOptions opts = new JoinMeetingOptions();
        fillMeetingOption(opts);
        opts.no_audio = meetingOptions.no_audio;
        return opts;
    }

    private static MeetingOptions fillMeetingOption(MeetingOptions opts)
    {
        opts.no_driving_mode = meetingOptions.no_driving_mode;
        opts.no_invite = meetingOptions.no_invite;
        opts.no_meeting_end_message = meetingOptions.no_meeting_end_message;
        opts.no_meeting_error_message = meetingOptions.no_meeting_error_message;
        opts.no_titlebar = meetingOptions.no_titlebar;
        opts.no_bottom_toolbar = meetingOptions.no_bottom_toolbar;
        opts.no_dial_in_via_phone = meetingOptions.no_dial_in_via_phone;
        opts.no_dial_out_to_phone = meetingOptions.no_dial_out_to_phone;
        opts.no_disconnect_audio = meetingOptions.no_disconnect_audio;
        opts.no_record = meetingOptions.no_record;
        opts.no_share = meetingOptions.no_share;
        opts.no_video = meetingOptions.no_video;
        opts.meeting_views_options = meetingOptions.meeting_views_options;
        opts.invite_options = meetingOptions.invite_options;
        opts.customer_key = meetingOptions.customer_key;
        opts.custom_meeting_id = meetingOptions.custom_meeting_id;
        opts.no_unmute_confirm_dialog=meetingOptions.no_unmute_confirm_dialog;
        opts.no_webinar_register_dialog=meetingOptions.no_webinar_register_dialog;
        opts.no_chat_msg_toast = meetingOptions.no_chat_msg_toast;
        return opts;
    }

    public static InstantMeetingOptions getInstantMeetingOptions() {
        InstantMeetingOptions opts = new InstantMeetingOptions();
        fillMeetingOption(opts);
        return opts;
    }

}
