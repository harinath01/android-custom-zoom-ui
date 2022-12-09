package us.zoom.sdksample.inmeetingfunction.customizedmeetingui.share;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import us.zoom.sdk.InMeetingService;
import us.zoom.sdk.InMeetingShareController;
import us.zoom.sdk.MobileRTCSDKError;
import us.zoom.sdk.MobileRTCShareView;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdksample.R;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.AndroidAppUtil;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.LegalNoticeDialogUtil;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.adapter.SimpleMenuAdapter;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.adapter.SimpleMenuItem;
import us.zoom.sdksample.util.FileUtils;

public class MeetingShareHelper {
    public final static int REQUEST_CODE_OPEN_FILE_EXPLORER = 5501;
    private final static String TAG = MeetingShareHelper.class.getSimpleName();

    public final static int MENU_SHARE_SCREEN = 0;
    public final static int MENU_SHARE_IMAGE = 1;
    public final static int MENU_SHARE_WEBVIEW = 2;
    public final static int MENU_WHITE_BOARD = 3;
    public final static int MENU_PDF = 4;
    private int shareType;


    public interface MeetingShareUICallBack {
        void showShareMenu(PopupWindow popupWindow);

        MobileRTCShareView getShareView();

        boolean requestStoragePermission();
    }

    private InMeetingShareController mInMeetingShareController;

    private InMeetingService mInMeetingService;

    private MeetingShareUICallBack callBack;

    private Activity activity;
    private PathAcquireTask pathAcquireTask;

    public MeetingShareHelper(Activity activity, MeetingShareUICallBack callBack) {
        mInMeetingShareController = ZoomSDK.getInstance().getInMeetingService().getInMeetingShareController();
        mInMeetingService = ZoomSDK.getInstance().getInMeetingService();
        this.activity = activity;
        this.callBack = callBack;
    }

    public void onClickShare() {
        if (mInMeetingShareController.isOtherSharing()) {
            showOtherSharingTip();
            return;
        }
        if (!mInMeetingShareController.isSharingOut()) {
            showShareActionPopupWindow();
        } else {
            stopShare();
        }
    }

    public boolean isSenderSupportAnnotation(long userId) {
        return mInMeetingShareController.isSenderSupportAnnotation(userId);
    }

    public boolean isSharingScreen() {
        return mInMeetingShareController.isSharingScreen();
    }

    public boolean isOtherSharing() {
        return mInMeetingShareController.isOtherSharing();
    }

    public boolean isSharingOut() {

        return mInMeetingShareController.isSharingOut();
    }

    public MobileRTCSDKError startShareScreenSession(Intent intent) {
        return mInMeetingShareController.startShareScreenSession(intent);
    }


    public void stopShare() {
        mInMeetingShareController.stopShareScreen();
        if (null != callBack) {
            MobileRTCShareView shareView = callBack.getShareView();
            if (shareView != null) {
                mInMeetingShareController.stopShareView();
                shareView.setVisibility(View.GONE);
            }
        }
    }

    public void showOtherSharingTip() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.alert_other_is_sharing)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();

    }

    public void onShareActiveUser(long currentShareUserId, long userId) {
        if (currentShareUserId > 0 && mInMeetingService.isMyself(currentShareUserId)) {
            if (userId < 0 || !mInMeetingService.isMyself(userId)) { //My share stopped or other start share and stop my share
                mInMeetingShareController.stopShareView();
                mInMeetingShareController.stopShareScreen();
                return;
            }
        }
        if (mInMeetingService.isMyself(userId)) {
            if (mInMeetingShareController.isSharingOut()) {
                if (mInMeetingShareController.isSharingScreen()) {
                    mInMeetingShareController.startShareScreenContent();
                } else {
                    if (null != callBack) {
                        mInMeetingShareController.startShareViewContent(callBack.getShareView());
                    }
                }
            }
        }
    }


    public void showShareActionPopupWindow() {

        final SimpleMenuAdapter menuAdapter = new SimpleMenuAdapter(activity);

        if (Build.VERSION.SDK_INT >= 21) {
            menuAdapter.addItem(new SimpleMenuItem(MENU_SHARE_SCREEN, "Screen"));
        }
        menuAdapter.addItem(new SimpleMenuItem(MENU_SHARE_IMAGE, "Image"));
        menuAdapter.addItem(new SimpleMenuItem(MENU_SHARE_WEBVIEW, "Web url"));
        menuAdapter.addItem(new SimpleMenuItem(MENU_WHITE_BOARD, "WhiteBoard"));
        menuAdapter.addItem(new SimpleMenuItem(MENU_PDF, "PDF"));

        View popupWindowLayout = LayoutInflater.from(activity).inflate(R.layout.popupwindow, null);

        ListView shareActions = (ListView) popupWindowLayout.findViewById(R.id.actionListView);
        final PopupWindow window = new PopupWindow(popupWindowLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.bg_transparent));
        shareActions.setAdapter(menuAdapter);

        shareActions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mInMeetingShareController.isOtherSharing()) {
                    showOtherSharingTip();
                    window.dismiss();
                    return;
                }

                SimpleMenuItem item = (SimpleMenuItem) menuAdapter.getItem(position);
                shareType = item.getAction();
                if (shareType == MENU_SHARE_WEBVIEW) {
                    startShareWebUrl();
                } else if (shareType == MENU_SHARE_IMAGE) {
                    openFileExplorer();
                } else if (shareType == MENU_SHARE_SCREEN) {
                    askScreenSharePermission();
                } else if (shareType == MENU_WHITE_BOARD) {
                    startShareWhiteBoard();
                } else if (shareType == MENU_PDF) {
                    openFileExplorer();
                }

                window.dismiss();
            }
        });

        window.setFocusable(true);
        window.setOutsideTouchable(true);
        window.update();
        if (null != callBack) {
            callBack.showShareMenu(window);
        }
    }

    @SuppressLint("NewApi")
    protected void askScreenSharePermission() {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        if (mInMeetingShareController.isOtherSharing()) {
            showOtherSharingTip();
            return;
        }
        MediaProjectionManager mgr = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mgr != null) {
            Intent intent = mgr.createScreenCaptureIntent();
            if (AndroidAppUtil.hasActivityForIntent(activity, intent)) {
                try {
                    activity.startActivityForResult(mgr.createScreenCaptureIntent(), MyMeetingActivity.REQUEST_SHARE_SCREEN_PERMISSION);
                } catch (Exception e) {
                    Log.e(TAG, "askScreenSharePermission failed");
                }
            }
        }
    }

    private void startShareImage(String path) {
        if (!path.endsWith(".bmp") && !path.endsWith(".jpg") && !path.endsWith(".png") && !path.endsWith(".jpeg")) {
            Toast.makeText(activity, "Unsupported document type, please select an image document!", Toast.LENGTH_LONG).show();
            return;
        }

        if (mInMeetingShareController.isOtherSharing()) {
            showOtherSharingTip();
            return;
        }
        boolean success = (mInMeetingShareController.startShareViewSession() == MobileRTCSDKError.SDKERR_SUCCESS);
        if (!success) {
            Log.i(TAG, "startShare is failed");
            return;
        }
        if (null == callBack) {
            return;
        }
        MobileRTCShareView shareView = callBack.getShareView();
        shareView.setVisibility(View.VISIBLE);
        shareView.setShareImageBitmap(BitmapFactory.decodeFile(path));
    }

    private void startShareWebUrl() {
        if (mInMeetingShareController.isOtherSharing()) {
            showOtherSharingTip();
            return;
        }
        boolean success = (mInMeetingShareController.startShareViewSession() == MobileRTCSDKError.SDKERR_SUCCESS);
        if (!success) {
            Log.i(TAG, "startShare is failed");
            return;
        }
        if (null == callBack) {
            return;
        }
        MobileRTCShareView shareView = callBack.getShareView();
        shareView.setVisibility(View.VISIBLE);
        shareView.setShareWebview("www.zoom.us");
    }

    private void startShareWhiteBoard() {
        if (mInMeetingShareController.isOtherSharing()) {
            showOtherSharingTip();
            return;
        }
        if (null == callBack) {
            return;
        }
        MobileRTCShareView shareView = callBack.getShareView();

        boolean success = (mInMeetingShareController.startShareViewSession() == MobileRTCSDKError.SDKERR_SUCCESS);
        if (!success) {
            Log.i(TAG, "startShare is failed");
            return;
        }
        shareView.setVisibility(View.VISIBLE);
        shareView.setShareWhiteboard();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_OPEN_FILE_EXPLORER) {
            return;
        }
        String path = null;
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            pathAcquireTask = new PathAcquireTask(activity, uri, new PathAcquireTask.Callback() {
                @Override
                public void getPath(String path) {
                    if (TextUtils.isEmpty(path)) {
                        return;
                    }
                    if (shareType == MeetingShareHelper.MENU_SHARE_IMAGE) {
                        startShareImage(path);
                    } else if (shareType == MENU_PDF) {
                        startSharePdf(path);
                    }
                }
            });
            pathAcquireTask.execute();
        }
    }

    public void openFileExplorer() {
        if (callBack == null || !callBack.requestStoragePermission()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(intent, REQUEST_CODE_OPEN_FILE_EXPLORER);
    }

    private void startSharePdf(String path) {
        if (!path.endsWith(".pdf")) {
            Toast.makeText(activity, "Unsupported document type, please select a PDF document!", Toast.LENGTH_LONG).show();
            return;
        }
        if (mInMeetingShareController.isOtherSharing()) {
            showOtherSharingTip();
            return;
        }
        if (callBack == null) {
            return;
        }
        MobileRTCShareView shareView = callBack.getShareView();

        boolean success = (mInMeetingShareController.startShareViewSession() == MobileRTCSDKError.SDKERR_SUCCESS);
        if (!success) {
            Log.i(TAG, "Start share PDF is failed");
            return;
        }
        shareView.setVisibility(View.VISIBLE);
        shareView.setSharePDF(path, "");
    }

    private static class PathAcquireTask extends AsyncTask<Void, Void, String> {
        private Context context;
        private Uri uri;
        private Callback callback;
        public PathAcquireTask(Context context, Uri uri, Callback callback) {
            this.context = context.getApplicationContext();
            this.uri = uri;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (isCancelled()) {
                return null;
            }
            return FileUtils.getReadablePathFromUri(context, uri);
        }

        @Override
        protected void onPostExecute(String path) {
            if (isCancelled()) {
                return;
            }

            if (callback != null) {
                callback.getPath(path);
            }
        }

        public interface Callback {
            void getPath(String path);
        }
    }

}
