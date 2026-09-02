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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MainActivity extends Activity {

    private TextView scoreText;
    private TextView timerText;
    private TextView messageText;

    private Button coinButton;
    private Button rewardButton;
    private Button restartButton;

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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView makeText(String text, float size, int color) {

        TextView view = new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);
        view.setTypeface(Typeface.DEFAULT);

        return view;
    }

    private void createGameScreen() {

        ScrollView scrollView = new ScrollView(this);

        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(20, 20, 30));

        LinearLayout mainLayout = new LinearLayout(this);

        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setPadding(
                dp(16),
                dp(20),
                dp(16),
                dp(20)
        );

        scrollView.addView(mainLayout);

        // TITLE
        TextView title = makeText(
                "🇮🇳 Coin Rush India",
                30,
                Color.WHITE
        );

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                );

        mainLayout.addView(title, titleParams);

        // COINS
        scoreText = makeText(
                "Coins: 0",
                28,
                Color.YELLOW
        );

        scoreText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams scoreParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                );

        mainLayout.addView(scoreText, scoreParams);

        // TIMER
        timerText = makeText(
                "Time: 30",
                24,
                Color.WHITE
        );

        LinearLayout.LayoutParams timerParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                );

        mainLayout.addView(timerText, timerParams);

        // MESSAGE
        messageText = makeText(
                "Tap the coin as fast as you can!",
                17,
                Color.LTGRAY
        );

        LinearLayout.LayoutParams messageParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                );

        messageParams.setMargins(
                0,
                dp(5),
                0,
                dp(10)
        );

        mainLayout.addView(
                messageText,
                messageParams
        );

        // COIN BUTTON
        coinButton = new Button(this);

        coinButton.setText("🪙\nTAP!");
        coinButton.setTextSize(26);
        coinButton.setTextColor(Color.WHITE);
        coinButton.setGravity(Gravity.CENTER);
        coinButton.setAllCaps(false);
        coinButton.setBackgroundColor(
                Color.rgb(255, 152, 0)
        );

        LinearLayout.LayoutParams coinParams =
                new LinearLayout.LayoutParams(
                        dp(240),
                        dp(150)
                );

        coinParams.gravity = Gravity.CENTER;
        coinParams.setMargins(
                0,
                dp(5),
                0,
                dp(15)
        );

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

        // REWARD BUTTON
        rewardButton = new Button(this);

        rewardButton.setText(
                "🎁  WATCH AD  •  2X COINS"
        );

        rewardButton.setTextSize(15);
        rewardButton.setTextColor(Color.WHITE);
        rewardButton.setAllCaps(false);
        rewardButton.setVisibility(View.GONE);

        LinearLayout.LayoutParams rewardParams =
                new LinearLayout.LayoutParams(
                        dp(280),
                        dp(55)
                );

        rewardParams.gravity = Gravity.CENTER;
        rewardParams.setMargins(
                0,
                dp(5),
                0,
                dp(10)
        );

        mainLayout.addView(
                rewardButton,
                rewardParams
        );

        rewardButton.setOnClickListener(v ->
                showRewardedAd()
        );

        // RESTART BUTTON
        restartButton = new Button(this);

        restartButton.setText(
                "🔄  RESTART GAME"
        );

        restartButton.setTextSize(16);
        restartButton.setAllCaps(false);
        restartButton.setVisibility(View.GONE);

        LinearLayout.LayoutParams restartParams =
                new LinearLayout.LayoutParams(
                        dp(240),
                        dp(55)
                );

        restartParams.gravity = Gravity.CENTER;

        mainLayout.addView(
                restartButton,
                restartParams
        );

        restartButton.setOnClickListener(v ->
                startGame()
        );

        setContentView(scrollView);
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
            public void onTick(
                    long millisUntilFinished) {

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

                rewardButton.setVisibility(
                        View.VISIBLE
                );

                restartButton.setVisibility(
                        View.VISIBLE
                );

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

                        interstitialAd
                                .setFullScreenContentCallback(
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

                        rewardedAd
                                .setFullScreenContentCallback(
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
