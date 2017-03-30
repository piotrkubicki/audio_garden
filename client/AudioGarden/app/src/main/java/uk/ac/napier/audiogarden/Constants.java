package uk.ac.napier.audiogarden;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Nathan on 30-Mar-17.
 */

public class Constants {
    public interface ACTION {
        String MAIN_ACTION = "uk.ac.napier.audiogarden.action.main";
        String INIT_ACTION = "uk.ac.napier.audiogarden.action.init";
        String STOP_ACTION = "uk.ac.napier.audiogarden.action.stop";
        String PLAY_ACTION = "uk.ac.napier.audiogarden.action.play";
        String PAUSE_ACTION = "uk.ac.napier.audiogarden.action.pause";
        String RESET_ACTION = "uk.ac.napier.audiogarden.action.RESET";
        String STARTFOREGROUND_ACTION = "uk.ac.napier.audiogarden.action.startforeground";
        String STOPFOREGROUND_ACTION = "uk.ac.napier.audiogarden.action.stopforeground";

    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public static Bitmap getDefaultAlbumArt(Context context) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            bm = BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.napier_logo, options);
        } catch (Error ee) {
        } catch (Exception e) {
        }
        return bm;
    }


}
