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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MainActivity extends Activity {

    private TextView scoreText;
    private TextView timerText;
    private TextView messageText;

    private Button coinButton;
    private Button restartButton;
    private Button rewardButton;

    private int score = 0;

    private CountDownTimer countDownTimer;

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    // Google test ad IDs
    private static final String INTERSTITIAL_AD_ID =
            "ca-app-pub-3940256099942544/1033173712";

    private static final String REWARDED_AD_ID =
            "ca-app-pub-3940256099942544/5224354917";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createGameScreen();

        MobileAds.initialize(this, initializationStatus -> {
            loadInterstitialAd();
            loadRewardedAd();
        });

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

        mainLayout.addView(
                title,
                new LinearLayout.LayoutParams(-1, 100)
        );

        scoreText = new TextView(this);
        scoreText.setText("Coins: 0");
        scoreText.setTextSize(28);
        scoreText.setTextColor(Color.YELLOW);
        scoreText.setGravity(Gravity.CENTER);

        mainLayout.addView(
                scoreText,
                new LinearLayout.LayoutParams(-1, 80)
        );

        timerText = new TextView(this);
        timerText.setText("Time: 30");
        timerText.setTextSize(24);
        timerText.setTextColor(Color.WHITE);
        timerText.setGravity(Gravity.CENTER);

        mainLayout.addView(
                timerText,
                new LinearLayout.LayoutParams(-1, 70)
        );

        messageText = new TextView(this);
        messageText.setText(
                "Tap the coin as fast as you can!"
        );
        messageText.setTextSize(18);
        messageText.setTextColor(Color.LTGRAY);
        messageText.setGravity(Gravity.CENTER);

        mainLayout.addView(
                messageText,
                new LinearLayout.LayoutParams(-1, 80)
        );

        coinButton = new Button(this);
        coinButton.setText("🪙\nTAP!");
        coinButton.setTextSize(28);
        coinButton.setTextColor(Color.WHITE);
        coinButton.setBackgroundColor(
                Color.rgb(255, 152, 0)
        );

        LinearLayout.LayoutParams coinParams =
                new LinearLayout.LayoutParams(300, 220);

        coinParams.gravity = Gravity.CENTER;
        coinParams.setMargins(0, 20, 0, 20);

        mainLayout.addView(
                coinButton,
                coinParams
        );

        coinButton.setOnClickListener(v -> {

            score++;

            scoreText.setText(
                    "Coins: " + score
            );
        });

        rewardButton = new Button(this);
        rewardButton.setText("🎁 WATCH AD → 2X COINS");
        rewardButton.setTextSize(16);
        rewardButton.setVisibility(View.GONE);

        mainLayout.addView(
                rewardButton,
                new LinearLayout.LayoutParams(350, 80)
        );

        rewardButton.setOnClickListener(v ->
                showRewardedAd()
        );

        restartButton = new Button(this);
        restartButton.setText("RESTART GAME");
        restartButton.setTextSize(18);
        restartButton.setVisibility(View.GONE);

        mainLayout.addView(
                restartButton,
                new LinearLayout.LayoutParams(300, 80)
        );

        restartButton.setOnClickListener(v ->
                startGame()
        );

        setContentView(mainLayout);
    }

    private void startGame() {

        score = 0;

        scoreText.setText("Coins: 0");
        timerText.setText("Time: 30");

        messageText.setText(
                "Tap the coin as fast as you can!"
        );

        coinButton.setEnabled(true);
        coinButton.setVisibility(View.VISIBLE);

        rewardButton.setVisibility(View.GONE);
        restartButton.setVisibility(View.GONE);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(
                30000,
                1000
        ) {

            @Override
            public void onTick(long millisUntilFinished) {

                long seconds =
                        (millisUntilFinished + 999) / 1000;

                timerText.setText(
                        "Time: " + seconds
                );
            }

            @Override
            public void onFinish() {

                timerText.setText("Time: 0");

                coinButton.setEnabled(false);

                messageText.setText(
                        "GAME OVER!\nYou collected "
                                + score
                                + " coins!"
                );

                rewardButton.setVisibility(View.VISIBLE);

                restartButton.setVisibility(View.VISIBLE);

                // Show interstitial after game finishes
                showInterstitialAd();
            }
        };

        countDownTimer.start();
    }

    private void loadInterstitialAd() {

        AdRequest adRequest =
                new AdRequest.Builder().build();

        InterstitialAd.load(
                this,
                INTERSTITIAL_AD_ID,
                adRequest,
                new InterstitialAdLoadCallback() {

                    @Override
                    public void onAdLoaded(
                            @NonNull InterstitialAd ad) {

                        interstitialAd = ad;

                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {

                                    @Override
                                    public void onAdDismissedFullScreenContent() {

                                        interstitialAd = null;

                                        loadInterstitialAd();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onAdFailedToLoad(
                            @NonNull LoadAdError adError) {

                        interstitialAd = null;
                    }
                }
        );
    }

    private void showInterstitialAd() {

        if (interstitialAd != null) {

            interstitialAd.show(this);

        } else {

            loadInterstitialAd();
        }
    }

    private void loadRewardedAd() {

        AdRequest adRequest =
                new AdRequest.Builder().build();

        RewardedAd.load(
                this,
                REWARDED_AD_ID,
                adRequest,
                new RewardedAdLoadCallback() {

                    @Override
                    public void onAdLoaded(
                            @NonNull RewardedAd ad) {

                        rewardedAd = ad;

                        rewardedAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {

                                    @Override
                                    public void onAdDismissedFullScreenContent() {

                                        rewardedAd = null;

                                        loadRewardedAd();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onAdFailedToLoad(
                            @NonNull LoadAdError adError) {

                        rewardedAd = null;
                    }
                }
        );
    }

    private void showRewardedAd() {

        if (rewardedAd != null) {

            rewardedAd.show(
                    this,
                    rewardItem -> {

                        // Double the player's coins
                        score = score * 2;

                        scoreText.setText(
                                "Coins: " + score
                        );

                        messageText.setText(
                                "🎉 REWARD!\nCoins doubled!"
                        );

                        Toast.makeText(
                                MainActivity.this,
                                "Coins 2X! 🎉",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
            );

        } else {

            Toast.makeText(
                    this,
                    "Reward ad is loading...",
                    Toast.LENGTH_SHORT
            ).show();

            loadRewardedAd();
        }
    }

    @Override
    protected void onDestroy() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        super.onDestroy();
    }
}
