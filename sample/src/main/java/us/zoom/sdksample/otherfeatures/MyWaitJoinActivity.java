package us.zoom.sdksample.otherfeatures;

import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdksample.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MyWaitJoinActivity extends Activity implements View.OnClickListener {
	public static String EXTRA_TOPIC = "meetingTopic";
	public static String EXTRA_IS_REPEAT = "meetingIsRepeat";
	public static String EXTRA_DATE = "meetingDate";
	public static String EXTRA_TIME = "meetingTime";
	public static String EXTRA_MEETING_ID = "meetingId";

	private final static String TAG = "ZoomSDK";

	private Button mLeave;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.wait_join_activity);
		
		Intent intent = getIntent();
		String topic = intent.getStringExtra(EXTRA_TOPIC);
		long meetingId = intent.getLongExtra(EXTRA_MEETING_ID, 0);
		boolean isRepeat = intent.getBooleanExtra(EXTRA_IS_REPEAT, false);
		String date = intent.getStringExtra(EXTRA_DATE);
		String time = intent.getStringExtra(EXTRA_TIME);
		
		TextView txtTopic = (TextView)findViewById(R.id.txtTopic);	
		if(topic != null)
			txtTopic.setText("Topic: " + topic);
		
		TextView txtMeetingId = (TextView)findViewById(R.id.txtMeetingId);
		if(meetingId > 0)
			txtMeetingId.setText("Meeting ID: " + meetingId);
		
		TextView txtIsRepeat = (TextView)findViewById(R.id.txtIsRepeat);
		txtIsRepeat.setText("Is Repeat Meeting: " + isRepeat);
		
		TextView txtTime = (TextView)findViewById(R.id.txtTime);
		if(time != null)
			txtTime.setText("Time: " + time);
		
		TextView txtDate = (TextView)findViewById(R.id.txtDate);
		
		if(date != null)
			txtDate.setText("Date: " + date);	
		
		mLeave = (Button)findViewById(R.id.btnLeave);
		mLeave.setOnClickListener(this);

		/* init callbacks */
		ZoomSDK.getInstance().getInMeetingService();
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.btnLeave) {			
			onClickLeave();
		}
	}
	
	private void onClickLeave() {
		ZoomSDK zoomSDK = ZoomSDK.getInstance();
		MeetingService meetingService = zoomSDK.getMeetingService();
		if(meetingService != null) {
			meetingService.leaveCurrentMeeting(false);
		}
		finish();
	}
	
	@Override
	public void onBackPressed() {
		onClickLeave();
	}
}
