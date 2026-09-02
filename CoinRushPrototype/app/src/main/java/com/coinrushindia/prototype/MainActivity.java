package com.coinrushindia.prototype;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView scoreText;
    private TextView timerText;
    private TextView messageText;
    private Button coinButton;
    private Button restartButton;

    private int score = 0;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createGameScreen();
        startGame();
    }

    private void createGameScreen() {

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setPadding(25, 25, 25, 25);
        mainLayout.setBackgroundColor(Color.rgb(20, 20, 30));

        TextView title = new TextView(this);
        title.setText("🇮🇳 Coin Rush India");
        title.setTextSize(30);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        mainLayout.addView(title,
                new LinearLayout.LayoutParams(
                        -1, 100));

        scoreText = new TextView(this);
        scoreText.setText("Coins: 0");
        scoreText.setTextSize(28);
        scoreText.setTextColor(Color.YELLOW);
        scoreText.setGravity(Gravity.CENTER);

        mainLayout.addView(scoreText,
                new LinearLayout.LayoutParams(
                        -1, 80));

        timerText = new TextView(this);
        timerText.setText("Time: 30");
        timerText.setTextSize(24);
        timerText.setTextColor(Color.WHITE);
        timerText.setGravity(Gravity.CENTER);

        mainLayout.addView(timerText,
                new LinearLayout.LayoutParams(
                        -1, 70));

        messageText = new TextView(this);
        messageText.setText("Tap the coin as fast as you can!");
        messageText.setTextSize(18);
        messageText.setTextColor(Color.LTGRAY);
        messageText.setGravity(Gravity.CENTER);

        mainLayout.addView(messageText,
                new LinearLayout.LayoutParams(
                        -1, 80));

        coinButton = new Button(this);
        coinButton.setText("🪙\nTAP!");
        coinButton.setTextSize(28);
        coinButton.setTextColor(Color.WHITE);
        coinButton.setBackgroundColor(Color.rgb(255, 152, 0));

        LinearLayout.LayoutParams coinParams =
                new LinearLayout.LayoutParams(300, 220);
        coinParams.gravity = Gravity.CENTER;
        coinParams.setMargins(0, 30, 0, 30);

        mainLayout.addView(coinButton, coinParams);

        coinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score++;
                scoreText.setText("Coins: " + score);
            }
        });

        restartButton = new Button(this);
        restartButton.setText("RESTART GAME");
        restartButton.setTextSize(18);
        restartButton.setVisibility(View.GONE);

        mainLayout.addView(restartButton,
                new LinearLayout.LayoutParams(
                        300, 80));

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });

        setContentView(mainLayout);
    }

    private void startGame() {

        score = 0;

        scoreText.setText("Coins: 0");
        timerText.setText("Time: 30");
        messageText.setText("Tap the coin as fast as you can!");

        coinButton.setEnabled(true);
        coinButton.setVisibility(View.VISIBLE);
        restartButton.setVisibility(View.GONE);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                long seconds =
                        (millisUntilFinished + 999) / 1000;

                timerText.setText("Time: " + seconds);
            }

            @Override
            public void onFinish() {

                timerText.setText("Time: 0");
                coinButton.setEnabled(false);

                messageText.setText(
                        "GAME OVER!\nYou collected " + score + " coins!");

                restartButton.setVisibility(View.VISIBLE);
            }
        };

        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        super.onDestroy();
    }
}
