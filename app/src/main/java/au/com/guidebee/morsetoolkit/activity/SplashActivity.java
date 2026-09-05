package au.com.guidebee.morsetoolkit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import au.com.guidebee.morsetoolkit.ui.HomeActivity;


public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }, SPLASH_DELAY_MS);
    }
}
