package com.coinrushindia.prototype;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView test = new TextView(this);

        test.setText(
                "🇮🇳 Coin Rush India\n\nAPP START TEST OK"
        );

        test.setTextSize(28);
        test.setTextColor(Color.WHITE);
        test.setGravity(Gravity.CENTER);
        test.setBackgroundColor(
                Color.rgb(12, 18, 28)
        );

        setContentView(test);
    }
}
