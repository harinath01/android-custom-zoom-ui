package us.zoom.sdksample.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;

public class BaseDialogFragment extends DialogFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = (int) (getMetricWidth(getActivity()) * 0.75f);
        params.height = (int)(getMetricHeight(getActivity()) * 0.5f);
        window.setAttributes(params);
    }

    private int getMetricWidth(Context context){
        DisplayMetrics metric = context.getApplicationContext().getResources().getDisplayMetrics();
        return metric.widthPixels;
    }

    private int getMetricHeight(Context context){
        DisplayMetrics metric = context.getApplicationContext().getResources().getDisplayMetrics();
        return metric.heightPixels;
    }
}
