package us.zoom.sdksample.otherfeatures;

import us.zoom.sdksample.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class MyInviteActivity extends Activity {
	public static String EXTRA_SUBJECT = Intent.EXTRA_SUBJECT;
	public static String EXTRA_MEETING_ID = "meetingId";
	public static String EXTRA_MEETING_PSW = "meetingPassword";
	public static String EXTRA_MEETING_RAW_PSW = "meetingRawPassword";
	public static String EXTRA_TEXT = Intent.EXTRA_TEXT;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite_activity);
		
		TextView txtUrl = (TextView)findViewById(R.id.txtUrl);
		TextView txtSubject = (TextView)findViewById(R.id.txtSubject);
		TextView txtMeetingId = (TextView)findViewById(R.id.txtMeetingId);
		TextView txtPassword = (TextView)findViewById(R.id.txtPassword);
		TextView txtRawPassword = (TextView)findViewById(R.id.txtRawPassword);
		EditText edtText = (EditText)findViewById(R.id.edtText);
		
		Intent intent = getIntent();
		Uri uri = intent.getData();
		
		if(uri != null)
			txtUrl.setText("URL:" + uri.toString());
		
		String subject = intent.getStringExtra(EXTRA_SUBJECT);
		if(subject != null)
			txtSubject.setText("Subject: " + subject);
		
		long meetingId = intent.getLongExtra(EXTRA_MEETING_ID, 0);
		if(meetingId > 0)
			txtMeetingId.setText("Meeting ID: " + meetingId);
		
		String meetingPassword = intent.getStringExtra(EXTRA_MEETING_PSW);
		if(meetingPassword != null)
			txtPassword.setText("Password: " + meetingPassword);
		
		String meetingRawPassword = intent.getStringExtra(EXTRA_MEETING_RAW_PSW);
		if(meetingRawPassword != null)
			txtRawPassword.setText("Raw Password: " + meetingRawPassword);
		
		String text = intent.getStringExtra(EXTRA_TEXT);
		if(text != null)
			edtText.setText(text);
	}

}
