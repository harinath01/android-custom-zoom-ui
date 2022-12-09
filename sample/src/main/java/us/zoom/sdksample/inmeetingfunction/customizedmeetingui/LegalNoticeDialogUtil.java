package us.zoom.sdksample.inmeetingfunction.customizedmeetingui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import us.zoom.sdk.ZoomSDK;

public class LegalNoticeDialogUtil {

    public static void showChatLegalNoticeDialog(Context context) {
        if (!ZoomSDK.getInstance().getInMeetingService().getInMeetingChatController().isMeetingChatLegalNoticeAvailable()) {
            Toast.makeText(context, "Chat legal notice is not available", Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(ZoomSDK.getInstance().getInMeetingService().getInMeetingChatController().getChatLegalNoticesPrompt())
                .setMessage(ZoomSDK.getInstance().getInMeetingService().getInMeetingChatController().getChatLegalNoticesExplained())
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
}
