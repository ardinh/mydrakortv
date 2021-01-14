package com.mydrakortv.beta.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.palette.graphics.Palette;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {

    public static boolean hasPermission(final Context context, final String permission) {
        return PackageManager.PERMISSION_GRANTED == context.getPackageManager().checkPermission(
                permission, context.getPackageName());
    }

    public static int dpToPx(int dp, Context ctx) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PaletteColors getPaletteColors(Palette palette) {
        PaletteColors colors = new PaletteColors();

        //figuring out toolbar palette color in order of preference
        if (palette.getDarkVibrantSwatch() != null) {
            colors.setToolbarBackgroundColor(palette.getDarkVibrantSwatch().getRgb());
            colors.setTextColor(palette.getDarkVibrantSwatch().getBodyTextColor());
            colors.setTitleColor(palette.getDarkVibrantSwatch().getTitleTextColor());
        } else if (palette.getDarkMutedSwatch() != null) {
            colors.setToolbarBackgroundColor(palette.getDarkMutedSwatch().getRgb());
            colors.setTextColor(palette.getDarkMutedSwatch().getBodyTextColor());
            colors.setTitleColor(palette.getDarkMutedSwatch().getTitleTextColor());
        } else if (palette.getVibrantSwatch() != null) {
            colors.setToolbarBackgroundColor(palette.getVibrantSwatch().getRgb());
            colors.setTextColor(palette.getVibrantSwatch().getBodyTextColor());
            colors.setTitleColor(palette.getVibrantSwatch().getTitleTextColor());
        }

        //set the status bar color to be a darker version of the toolbar background Color;
        if (colors.getToolbarBackgroundColor() != 0) {
            float[] hsv = new float[3];
            int color = colors.getToolbarBackgroundColor();
            Color.colorToHSV(color, hsv);
            hsv[2] *= 0.8f; // value component
            colors.setStatusBarColor(Color.HSVToColor(hsv));
        }

        return colors;
    }


}
