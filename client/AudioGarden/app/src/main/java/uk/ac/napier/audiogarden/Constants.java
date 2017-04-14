package uk.ac.napier.audiogarden;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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
        String RESET_ACTION = "uk.ac.napier.audiogarden.action.reset";
        String REPLAY_ACTION = "uk.ac.napier.audiogarden.action.replay";
        String BLANK_ACTION = "uk.ac.napier.audiogarden.action.blank";
        String SEND_STOP_ACTION = "uk.ac.napier.audiogarden.action.sendstop";
        String SEND_PLAY_ACTION = "uk.ac.napier.audiogarden.action.sendplay";
        String SEND_PAUSE_ACTION = "uk.ac.napier.audiogarden.action.sendpause";
        String SEND_RESET_ACTION = "uk.ac.napier.audiogarden.action.sendreset";
        String SEND_REPLAY_ACTION = "uk.ac.napier.audiogarden.action.sendreplay";
        String DISABLE_PLAY_PAUSE = "uk.ac.napier.audiogarden.action.disableplaypause";
        String DISABLE_STOP_REPLAY = "uk.ac.napier.audiogarden.action.disablestopreplay";
        String ENABLE_PLAY_PAUSE = "uk.ac.napier.audiogarden.action.enableplaypause";
        String ENABLE_STOP_REPLAY = "uk.ac.napier.audiogarden.action.enablestopreplay";
        String STARTFOREGROUND_ACTION = "uk.ac.napier.audiogarden.action.startforeground";
        String STOPFOREGROUND_ACTION = "uk.ac.napier.audiogarden.action.stopforeground";

    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public static void setUserGuideStatus(Context context, String screenName, Boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.user_guide_settings), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(screenName, value);
        editor.commit();

        setUserGuidePage(context, screenName, 0);
    }

    public static boolean getUserGuideStatus(Context context, String screenName) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.user_guide_settings), Context.MODE_PRIVATE);

        return prefs.getBoolean(screenName, true);
    }

    public static void setUserGuidePage(Context context, String screenName, int page) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.user_guide_settings), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(screenName + "_page", page - 1);
        editor.commit();
    }

    public static int getUserGuidePage(Context context, String screenName) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.user_guide_settings), Context.MODE_PRIVATE);

        return prefs.getInt(screenName + "_page", -1);
    }

}
