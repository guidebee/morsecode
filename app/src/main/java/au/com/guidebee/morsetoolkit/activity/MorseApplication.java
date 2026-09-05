package au.com.guidebee.morsetoolkit.activity;

import android.app.Application;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import au.com.guidebee.morsetoolkit.ConfigInfo;

public class MorseApplication extends Application implements
        Thread.UncaughtExceptionHandler {

    static {
        int maxSampleRate = getValidSampleRates();
        ConfigInfo.sampleRate = maxSampleRate / 2;
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
