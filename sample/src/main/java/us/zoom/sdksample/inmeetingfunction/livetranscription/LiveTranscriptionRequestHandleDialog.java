package us.zoom.sdksample.inmeetingfunction.livetranscription;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import us.zoom.sdk.InMeetingLiveTranscriptionController;
import us.zoom.sdk.InMeetingService;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdksample.R;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity;

public class LiveTranscriptionRequestHandleDialog extends Dialog implements View.OnClickListener {

    private String mUserName;
    public LiveTranscriptionRequestHandleDialog(@NonNull Context context) {
        super(context);
    }

    public LiveTranscriptionRequestHandleDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected LiveTranscriptionRequestHandleDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static void show(MyMeetingActivity myMeetingActivity, String userName) {

        LiveTranscriptionRequestHandleDialog dialog = new LiveTranscriptionRequestHandleDialog(myMeetingActivity);
        dialog.mUserName = userName;
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_live_transcription_request_handle);
        String request = (mUserName == null ? "some one" : mUserName) + " request to start live transcription";
        ((TextView) findViewById(R.id.requestTv)).setText(request);
        findViewById(R.id.enableBtn).setOnClickListener(this);
        findViewById(R.id.declineBtn).setOnClickListener(this);
        findViewById(R.id.disableBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        InMeetingService inMeetingService = ZoomSDK.getInstance().getInMeetingService();
        if (inMeetingService == null) {
            dismiss();
            return;
        }
        InMeetingLiveTranscriptionController inMeetingLiveTranscriptionController = inMeetingService.getInMeetingLiveTranscriptionController();
        if (v.getId() == R.id.enableBtn) {
            inMeetingLiveTranscriptionController.startLiveTranscription();
        } else if (v.getId() == R.id.declineBtn) {
            /* decline no need do anything */
        } else if (v.getId() == R.id.disableBtn) {
            inMeetingLiveTranscriptionController.enableRequestLiveTranscription(false);
        }
        dismiss();
    }
}
