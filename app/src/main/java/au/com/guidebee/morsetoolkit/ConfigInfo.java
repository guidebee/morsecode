package au.com.guidebee.morsetoolkit;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.ads.AdRequest;


public class ConfigInfo {

    public final static String preferenceFile = "guidebee.morse";
    public final static int TYPE_LETTER_LETTER = 0x1;
    public final static int TYPE_LETTER_NUMBER = 0x2;
    public final static int TYPE_LETTER_PUNCTUATION = 0x4;
    public final static int TYPE_LETTER = 1;
    public final static int TYPE_WORD = 2;
    public final static int TYPE_FREE_TEXT = 4;
    public static int sampleRate = 44100;
    public static int transmitType;
    public static int transmitLetterType;

    public static int receiveType;
    public static int receiveLetterType;
    public static int morseReceiveWPM;
    public static boolean letterOnFront = true;

    //1 -slow, 2-normal ,4 fast
    public static int morseInputSpeed;
    public static boolean playAudio = true;
    public static int audioVolume = 50;

    //true order ,false random
    public static boolean flashCardType = true;

    public static boolean showAds = false;
    public static boolean testAds = true;
    public static AdRequest adRequest = null;

    public static boolean hasRequestedAudioPermission = false;

    public static void saveConfiguration(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConfigInfo.preferenceFile,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("audioVolume", ConfigInfo.audioVolume);
        editor.putInt("morseInputSpeed", ConfigInfo.morseInputSpeed);
        editor.putInt("morseReceiveWPM", ConfigInfo.morseReceiveWPM);
        editor.putBoolean("playAudio", ConfigInfo.playAudio);
        editor.putInt("receiveLetterType", ConfigInfo.receiveLetterType);
        editor.putInt("receiveType", ConfigInfo.receiveType);
        editor.putInt("transmitLetterType", ConfigInfo.transmitLetterType);
        editor.putInt("transmitType", ConfigInfo.transmitType);
        editor.putBoolean("flashCardType", ConfigInfo.flashCardType);
        editor.apply();
    }

    public static void loadConfiguration(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConfigInfo.preferenceFile,
                Context.MODE_PRIVATE);
        ConfigInfo.audioVolume = sharedPreferences.getInt("audioVolume", 50);
        ConfigInfo.morseInputSpeed = sharedPreferences.getInt("morseInputSpeed", 1);
        ConfigInfo.morseReceiveWPM = sharedPreferences.getInt("morseReceiveWPM", 2);
        ConfigInfo.playAudio = sharedPreferences.getBoolean("playAudio", true);
        ConfigInfo.receiveLetterType = sharedPreferences.getInt("receiveLetterType", 1);
        ConfigInfo.receiveType = sharedPreferences.getInt("receiveType", 1);
        ConfigInfo.transmitLetterType = sharedPreferences.getInt("transmitLetterType", 1);
        ConfigInfo.transmitType = sharedPreferences.getInt("transmitType", 1);
        ConfigInfo.flashCardType = sharedPreferences.getBoolean("flashCardType", true);
    }

}
