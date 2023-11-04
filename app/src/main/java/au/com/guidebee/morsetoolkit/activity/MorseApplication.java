package au.com.guidebee.morsetoolkit.activity;

import android.app.Application;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;

import au.com.guidebee.morsetoolkit.ConfigInfo;
import au.com.guidebee.morsetoolkit.component.FontManager;

public class MorseApplication extends Application implements
        Thread.UncaughtExceptionHandler {

    static {
        int maxSampleRate = getValidSampleRates();
        ConfigInfo.sampleRate = maxSampleRate / 2;

        if (ConfigInfo.testAds) {
            ConfigInfo.adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("2F6582E54486D1A78702BB61D701F695")
                    .addTestDevice("F7A852B5CB4C732E62C026A3599644D0")
                    .addTestDevice("21831CCAC4C5CAD735C44DE54AAB6747")
                    .addTestDevice("5A9A2B546A70BA0C8946469C74B55819")
                    .addTestDevice("B571BE84863493C69E77B74A14C8F251")
                    .addTestDevice("6619A68B8B18A1E21152A17996BA9C24")
                    .addTestDevice("2F7452EAC6CA536B2B15E5BF11161F11")
                    .build();
        } else {
            ConfigInfo.adRequest = new AdRequest.Builder()
                    .build();
        }

    }

    public static int getValidSampleRates() {
        for (int rate : new int[]{88200, 44100, 22050, 11025}) {
            // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                return rate;

            }
        }
        return 22050;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FontManager.init(getAssets());
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.i("uncaughtException", ex.getMessage());
        try {

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
