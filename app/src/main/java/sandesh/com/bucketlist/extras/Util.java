package sandesh.com.bucketlist.extras;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import java.util.List;

/**
 * Created by Sandesh on 2/16/2017.
 */

public class Util {
    public static void showViews(List<View> views){
        for (View view: views)
            view.setVisibility(View.VISIBLE);
    }

    public static void hideViews(List<View> views){
        for (View view: views)
            view.setVisibility(View.GONE);
    }

    public static void setBackground(View mItemView, Drawable drawable) {
        if (Build.VERSION.SDK_INT > 15)
            mItemView.setBackground(drawable);
        else
            mItemView.setBackgroundDrawable(drawable);
    }
}
