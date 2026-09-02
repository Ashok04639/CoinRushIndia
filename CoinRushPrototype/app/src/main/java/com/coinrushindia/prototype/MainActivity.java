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

        TextView textView = new TextView(this);
        textView.setText("Coin Rush India");
        textView.setTextSize(28);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);

        setContentView(textView);
    }
}
