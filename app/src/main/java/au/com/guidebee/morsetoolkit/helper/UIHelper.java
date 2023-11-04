package au.com.guidebee.morsetoolkit.helper;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;


public class UIHelper {

    public static int getScreenWidth(Activity context) {
        DisplayMetrics metrics = new DisplayMetrics();

        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int dp = (int) (metrics.widthPixels / metrics.density);
        return dp;
    }

    public static float getScreenDp(Activity context) {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }

    public static float getScreenRatio(Activity context) {
        DisplayMetrics metrics = new DisplayMetrics();

        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float width = Math.min(metrics.widthPixels, metrics.heightPixels);
        float height = Math.max(metrics.widthPixels, metrics.heightPixels);

        return width / height;
    }

    public static void showSnackBar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view,
                message,
                Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(Color.WHITE);
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.BLACK);
        snackbar.show();

    }
}
