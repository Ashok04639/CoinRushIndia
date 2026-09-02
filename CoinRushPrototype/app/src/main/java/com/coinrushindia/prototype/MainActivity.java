package com.coinrushindia.prototype;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.ColorStateList;

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

    // Google TEST Ad IDs
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
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable roundedBackground(
            int color,
            int radius) {

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    private void createGameScreen() {

        LinearLayout mainLayout = new LinearLayout(this);

        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setPadding(
                dp(18),
                dp(20),
                dp(18),
                dp(20)
        );

        mainLayout.setBackgroundColor(
                Color.rgb(10, 12, 25)
        );

        // =========================
        // TITLE
        // =========================

        TextView title = new TextView(this);

        title.setText("🇮🇳 Coin Rush India");
        title.setTextSize(32);
        title.setTextColor(Color.WHITE);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        title.setGravity(Gravity.CENTER);

        mainLayout.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                )
        );

        // SUBTITLE

        TextView subtitle = new TextView(this);

        subtitle.setText("TAP • COLLECT • RUSH!");
        subtitle.setTextSize(17);
        subtitle.setTextColor(
                Color.rgb(210, 210, 220)
        );
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(45)
                );

        subtitleParams.setMargins(
                0,
                dp(5),
                0,
                dp(12)
        );

        mainLayout.addView(
                subtitle,
                subtitleParams
        );

        // =========================
        // SCORE CARD
        // =========================

        LinearLayout scoreCard =
                new LinearLayout(this);

        scoreCard.setOrientation(
                LinearLayout.VERTICAL
        );

        scoreCard.setGravity(Gravity.CENTER);

        scoreCard.setBackground(
                roundedBackground(
                        Color.rgb(30, 33, 52),
                        28
                )
        );

        scoreText = new TextView(this);

        scoreText.setText("0");
        scoreText.setTextSize(50);
        scoreText.setTextColor(Color.YELLOW);
        scoreText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        scoreText.setGravity(Gravity.CENTER);

        scoreCard.addView(
                scoreText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(75)
                )
        );

        TextView coinsLabel = new TextView(this);

        coinsLabel.setText("COINS");
        coinsLabel.setTextSize(16);
        coinsLabel.setTextColor(
                Color.rgb(220, 220, 225)
        );
        coinsLabel.setGravity(Gravity.CENTER);

        scoreCard.addView(
                coinsLabel,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(40)
                )
        );

        LinearLayout.LayoutParams scoreParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(145)
                );

        scoreParams.setMargins(
                dp(25),
                0,
                dp(25),
                dp(18)
        );

        mainLayout.addView(
                scoreCard,
                scoreParams
        );

        // =========================
        // TIMER
        // =========================

        timerText = new TextView(this);

        timerText.setText("⏱️ 30");
        timerText.setTextSize(25);
        timerText.setTextColor(Color.WHITE);
        timerText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        timerText.setGravity(Gravity.CENTER);

        timerText.setBackground(
                roundedBackground(
                        Color.rgb(42, 45, 70),
                        50
                )
        );

        LinearLayout.LayoutParams timerParams =
                new LinearLayout.LayoutParams(
                        dp(250),
                        dp(65)
                );

        timerParams.setMargins(
                0,
                0,
                0,
                dp(18)
        );

        mainLayout.addView(
                timerText,
                timerParams
        );

        // =========================
        // MESSAGE
        // =========================

        messageText = new TextView(this);

        messageText.setText(
                "Tap the coin as fast as you can!"
        );

        messageText.setTextSize(18);
        messageText.setTextColor(
                Color.rgb(220, 220, 225)
        );
        messageText.setGravity(Gravity.CENTER);

        mainLayout.addView(
                messageText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                )
        );

        // =========================
        // COIN BUTTON
        // =========================

        coinButton = new Button(this);

        coinButton.setText("🪙\nTAP!");
        coinButton.setTextSize(27);
        coinButton.setTextColor(Color.WHITE);
        coinButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        coinButton.setGravity(Gravity.CENTER);

        coinButton.setAllCaps(false);

        coinButton.setBackground(
                createCoinBackground()
        );

        coinButton.setElevation(dp(10));

        LinearLayout.LayoutParams coinParams =
                new LinearLayout.LayoutParams(
                        dp(300),
                        dp(300)
                );

        coinParams.setMargins(
                0,
                dp(5),
                0,
                dp(25)
        );

        mainLayout.addView(
                coinButton,
                coinParams
        );

        coinButton.setOnClickListener(v -> {

            score++;

            scoreText.setText(
                    String.valueOf(score)
            );

            playTapAnimation();

        });

        // =========================
        // REWARD BUTTON
        // =========================

        rewardButton = new Button(this);

        rewardButton.setText(
                "🎁  WATCH AD  •  2X COINS"
        );

        rewardButton.setTextSize(18);
        rewardButton.setTextColor(Color.WHITE);
        rewardButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        rewardButton.setAllCaps(false);

        rewardButton.setBackground(
                roundedBackground(
                        Color.rgb(45, 150, 55),
                        30
                )
        );

        rewardButton.setVisibility(View.GONE);

        LinearLayout.LayoutParams rewardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                );

        rewardParams.setMargins(
                0,
                0,
                0,
                dp(15)
        );

        mainLayout.addView(
                rewardButton,
                rewardParams
        );

        rewardButton.setOnClickListener(
                v -> showRewardedAd()
        );

        // =========================
        // RESTART BUTTON
        // =========================

        restartButton = new Button(this);

        restartButton.setText(
                "🔄  RESTART GAME"
        );

        restartButton.setTextSize(18);
        restartButton.setTextColor(Color.WHITE);
        restartButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        restartButton.setAllCaps(false);

        restartButton.setBackground(
                roundedBackground(
                        Color.rgb(55, 65, 90),
                        30
                )
        );

        restartButton.setVisibility(View.GONE);

        mainLayout.addView(
                restartButton,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                )
        );

        restartButton.setOnClickListener(
                v -> startGame()
        );

        setContentView(mainLayout);
    }

    private GradientDrawable createCoinBackground() {

        GradientDrawable coin =
                new GradientDrawable();

        coin.setShape(
                GradientDrawable.OVAL
        );

        coin.setColor(
                Color.rgb(255, 152, 0)
        );

        coin.setStroke(
                dp(5),
                Color.rgb(255, 190, 40)
        );

        return coin;
    }

    private void playTapAnimation() {

        ScaleAnimation animation =
                new ScaleAnimation(
                        1.0f,
                        0.92f,
                        1.0f,
                        0.92f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                );

        animation.setDuration(80);

        animation.setRepeatCount(1);

        animation.setRepeatMode(
                Animation.REVERSE
        );

        coinButton.startAnimation(animation);
    }

    private void startGame() {

        score = 0;

        scoreText.setText("0");

        timerText.setText("⏱️ 30");

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

        countDownTimer =
                new CountDownTimer(
                        30000,
                        1000
                ) {

                    @Override
                    public void onTick(
                            long millisUntilFinished) {

                        long seconds =
                                (millisUntilFinished + 999)
                                        / 1000;

                        timerText.setText(
                                "⏱️ " + seconds
                        );
                    }

                    @Override
                    public void onFinish() {

                        timerText.setText(
                                "⏱️ 0"
                        );

                        coinButton.setEnabled(false);

                        messageText.setText(
                                "🎉 GAME OVER!\n"
                                        + "You collected "
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

    // =========================
    // INTERSTITIAL AD
    // =========================

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
                                            public void
                                            onAdDismissedFullScreenContent() {

                                                interstitialAd =
                                                        null;

                                                loadInterstitialAd();
                                            }
                                        }
                                );
                    }

                    @Override
                    public void onAdFailedToLoad(
                            @NonNull LoadAdError error) {

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

    // =========================
    // REWARDED AD
    // =========================

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
                                            public void
                                            onAdDismissedFullScreenContent() {

                                                rewardedAd =
                                                        null;

                                                loadRewardedAd();
                                            }
                                        }
                                );
                    }

                    @Override
                    public void onAdFailedToLoad(
                            @NonNull LoadAdError error) {

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
                                String.valueOf(score)
                        );

                        messageText.setText(
                                "🎉 REWARD!\n"
                                        + "Coins doubled!"
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
