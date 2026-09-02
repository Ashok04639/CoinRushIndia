package com.coinrushindia.prototype;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
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
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MainActivity extends Activity {

    // ==============================
    // UI
    // ==============================

    private TextView scoreText;
    private TextView bestText;
    private TextView timerText;
    private TextView messageText;
    private TextView tapFeedback;

    private Button coinButton;
    private Button rewardButton;
    private Button restartButton;

    // ==============================
    // GAME
    // ==============================

    private int score = 0;
    private int bestScore = 0;

    private CountDownTimer countDownTimer;

    private boolean gameRunning = false;

    // ==============================
    // ADS
    // ==============================

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    // Google official TEST IDs
    private static final String INTERSTITIAL_AD_ID =
            "ca-app-pub-3940256099942544/1033173712";

    private static final String REWARDED_AD_ID =
            "ca-app-pub-3940256099942544/5224354917";

    // ==============================
    // STORAGE
    // ==============================

    private static final String PREFS =
            "CoinRushIndiaPrefs";

    private static final String BEST_SCORE =
            "best_score";

    // ==============================
    // COLORS
    // ==============================

    private static final int BG =
            Color.rgb(9, 11, 24);

    private static final int CARD =
            Color.rgb(29, 32, 52);

    private static final int CARD_LIGHT =
            Color.rgb(43, 46, 72);

    private static final int ORANGE =
            Color.rgb(255, 152, 0);

    private static final int ORANGE_LIGHT =
            Color.rgb(255, 193, 50);

    private static final int GREEN =
            Color.rgb(42, 145, 55);

    private static final int BLUE =
            Color.rgb(60, 72, 105);

    // ==============================
    // ON CREATE
    // ==============================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadBestScore();

        createGameScreen();

        // Start AdMob
        MobileAds.initialize(this, initializationStatus -> {
            loadInterstitialAd();
            loadRewardedAd();
        });

        startGame();
    }

    // ==============================
    // DP HELPER
    // ==============================

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
                        + 0.5f
        );
    }

    // ==============================
    // ROUNDED BACKGROUND
    // ==============================

    private GradientDrawable rounded(
            int color,
            int radius
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);

        drawable.setCornerRadius(
                dp(radius)
        );

        return drawable;
    }

    // ==============================
    // CREATE SCREEN
    // ==============================

    private void createGameScreen() {

        LinearLayout main =
                new LinearLayout(this);

        main.setOrientation(
                LinearLayout.VERTICAL
        );

        main.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        main.setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(12)
        );

        main.setBackgroundColor(BG);

        // =================================
        // TITLE
        // =================================

        TextView title =
                new TextView(this);

        title.setText(
                "🇮🇳 Coin Rush India"
        );

        title.setTextSize(31);

        title.setTextColor(
                Color.WHITE
        );

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(
                Gravity.CENTER
        );

        main.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(62)
                )
        );

        // =================================
        // SUBTITLE
        // =================================

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "TAP • COLLECT • RUSH!"
        );

        subtitle.setTextSize(16);

        subtitle.setTextColor(
                Color.rgb(205, 207, 218)
        );

        subtitle.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(38)
                );

        subtitleParams.setMargins(
                0,
                0,
                0,
                dp(8)
        );

        main.addView(
                subtitle,
                subtitleParams
        );

        // =================================
        // SCORE CARD
        // =================================

        LinearLayout scoreCard =
                new LinearLayout(this);

        scoreCard.setOrientation(
                LinearLayout.VERTICAL
        );

        scoreCard.setGravity(
                Gravity.CENTER
        );

        scoreCard.setBackground(
                rounded(CARD, 25)
        );

        scoreText =
                new TextView(this);

        scoreText.setText("0");

        scoreText.setTextSize(48);

        scoreText.setTextColor(
                Color.YELLOW
        );

        scoreText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        scoreText.setGravity(
                Gravity.CENTER
        );

        scoreCard.addView(
                scoreText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(65)
                )
        );

        TextView coins =
                new TextView(this);

        coins.setText("COINS");

        coins.setTextSize(15);

        coins.setTextColor(
                Color.LTGRAY
        );

        coins.setGravity(
                Gravity.CENTER
        );

        scoreCard.addView(
                coins,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(30)
                )
        );

        LinearLayout.LayoutParams scoreParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(110)
                );

        scoreParams.setMargins(
                dp(18),
                0,
                dp(18),
                dp(8)
        );

        main.addView(
                scoreCard,
                scoreParams
        );

        // =================================
        // BEST SCORE
        // =================================

        bestText =
                new TextView(this);

        bestText.setText(
                "🏆 BEST: " + bestScore
        );

        bestText.setTextSize(15);

        bestText.setTextColor(
                Color.rgb(255, 210, 70)
        );

        bestText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        bestText.setGravity(
                Gravity.CENTER
        );

        main.addView(
                bestText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );

        // =================================
        // TIMER
        // =================================

        timerText =
                new TextView(this);

        timerText.setText(
                "⏱️ 30"
        );

        timerText.setTextSize(23);

        timerText.setTextColor(
                Color.WHITE
        );

        timerText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        timerText.setGravity(
                Gravity.CENTER
        );

        timerText.setBackground(
                rounded(CARD_LIGHT, 50)
        );

        LinearLayout.LayoutParams timerParams =
                new LinearLayout.LayoutParams(
                        dp(220),
                        dp(58)
                );

        timerParams.setMargins(
                0,
                dp(5),
                0,
                dp(8)
        );

        main.addView(
                timerText,
                timerParams
        );

        // =================================
        // MESSAGE
        // =================================

        messageText =
                new TextView(this);

        messageText.setText(
                "Tap the coin as fast as you can!"
        );

        messageText.setTextSize(17);

        messageText.setTextColor(
                Color.rgb(210, 212, 220)
        );

        messageText.setGravity(
                Gravity.CENTER
        );

        main.addView(
                messageText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        // =================================
        // TAP FEEDBACK
        // =================================

        tapFeedback =
                new TextView(this);

        tapFeedback.setText("");

        tapFeedback.setTextSize(18);

        tapFeedback.setTextColor(
                Color.YELLOW
        );

        tapFeedback.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        tapFeedback.setGravity(
                Gravity.CENTER
        );

        main.addView(
                tapFeedback,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );

        // =================================
        // COIN BUTTON
        // =================================

        coinButton =
                new Button(this);

        coinButton.setText(
                "🪙\nTAP!"
        );

        coinButton.setTextSize(27);

        coinButton.setTextColor(
                Color.WHITE
        );

        coinButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        coinButton.setGravity(
                Gravity.CENTER
        );

        coinButton.setAllCaps(false);

        coinButton.setBackground(
                createCoinBackground()
        );

        coinButton.setElevation(
                dp(8)
        );

        LinearLayout.LayoutParams coinParams =
                new LinearLayout.LayoutParams(
                        dp(245),
                        dp(245)
                );

        coinParams.setMargins(
                0,
                dp(2),
                0,
                dp(10)
        );

        main.addView(
                coinButton,
                coinParams
        );

        coinButton.setOnClickListener(
                v -> handleCoinTap()
        );

        // =================================
        // REWARD BUTTON
        // =================================

        rewardButton =
                new Button(this);

        rewardButton.setText(
                "🎁  WATCH AD  •  2X COINS"
        );

        rewardButton.setTextSize(17);

        rewardButton.setTextColor(
                Color.WHITE
        );

        rewardButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        rewardButton.setAllCaps(false);

        rewardButton.setBackground(
                rounded(GREEN, 25)
        );

        rewardButton.setVisibility(
                View.GONE
        );

        LinearLayout.LayoutParams rewardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(62)
                );

        rewardParams.setMargins(
                0,
                0,
                0,
                dp(8)
        );

        main.addView(
                rewardButton,
                rewardParams
        );

        rewardButton.setOnClickListener(
                v -> showRewardedAd()
        );

        // =================================
        // RESTART BUTTON
        // =================================

        restartButton =
                new Button(this);

        restartButton.setText(
                "🔄  RESTART GAME"
        );

        restartButton.setTextSize(17);

        restartButton.setTextColor(
                Color.WHITE
        );

        restartButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        restartButton.setAllCaps(false);

        restartButton.setBackground(
                rounded(BLUE, 25)
        );

        restartButton.setVisibility(
                View.GONE
        );

        main.addView(
                restartButton,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(62)
                )
        );

        restartButton.setOnClickListener(
                v -> startGame()
        );

        setContentView(main);
    }

    // ==============================
    // COIN BACKGROUND
    // ==============================

    private GradientDrawable createCoinBackground() {

        GradientDrawable coin =
                new GradientDrawable();

        coin.setShape(
                GradientDrawable.OVAL
        );

        coin.setColor(ORANGE);

        coin.setStroke(
                dp(5),
                ORANGE_LIGHT
        );

        return coin;
    }

    // ==============================
    // COIN TAP
    // ==============================

    private void handleCoinTap() {

        if (!gameRunning) {
            return;
        }

        score++;

        scoreText.setText(
                String.valueOf(score)
        );

        // Save best score
        if (score > bestScore) {

            bestScore = score;

            bestText.setText(
                    "🏆 BEST: " + bestScore
            );

            saveBestScore();
        }

        playTapAnimation();

        showTapFeedback();
    }

    // ==============================
    // TAP ANIMATION
    // ==============================

    private void playTapAnimation() {

        ScaleAnimation animation =
                new ScaleAnimation(
                        1.0f,
                        0.90f,
                        1.0f,
                        0.90f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                );

        animation.setDuration(90);

        animation.setRepeatCount(1);

        animation.setRepeatMode(
                Animation.REVERSE
        );

        coinButton.startAnimation(
                animation
        );
    }

    // ==============================
    // TAP FEEDBACK
    // ==============================

    private void showTapFeedback() {

        tapFeedback.setText("+1 🪙");

        AlphaAnimation fade =
                new AlphaAnimation(
                        1.0f,
                        0.0f
                );

        fade.setDuration(350);

        fade.setFillAfter(false);

        tapFeedback.startAnimation(fade);
    }

    // ==============================
    // START GAME
    // ==============================

    private void startGame() {

        score = 0;

        gameRunning = true;

        scoreText.setText("0");

        timerText.setText(
                "⏱️ 30"
        );

        messageText.setText(
                "Tap the coin as fast as you can!"
        );

        tapFeedback.setText("");

        coinButton.setVisibility(
                View.VISIBLE
        );

        coinButton.setEnabled(true);

        rewardButton.setVisibility(
                View.GONE
        );

        restartButton.setVisibility(
                View.GONE
        );

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
                            long millisUntilFinished
                    ) {

                        long seconds =
                                (millisUntilFinished
                                        + 999) / 1000;

                        timerText.setText(
                                "⏱️ " + seconds
                        );
                    }

                    @Override
                    public void onFinish() {

                        gameRunning = false;

                        timerText.setText(
                                "⏱️ 0"
                        );

                        // IMPORTANT:
                        // Hide TAP button
                        coinButton.setEnabled(
                                false
                        );

                        coinButton.setVisibility(
                                View.GONE
                        );

                        tapFeedback.setText("");

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

    // ==============================
    // LOAD INTERSTITIAL
    // ==============================

    private void loadInterstitialAd() {

        AdRequest request =
                new AdRequest.Builder()
                        .build();

        InterstitialAd.load(
                this,
                INTERSTITIAL_AD_ID,
                request,
                new InterstitialAdLoadCallback() {

                    @Override
                    public void onAdLoaded(
                            @NonNull InterstitialAd ad
                    ) {

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

                                            @Override
                                            public void
                                            onAdFailedToShowFullScreenContent(
                                                    @NonNull com.google.android.gms.ads.AdError adError
                                            ) {

                                                interstitialAd =
                                                        null;

                                                loadInterstitialAd();
                                            }
                                        }
                                );
                    }

                    @Override
                    public void onAdFailedToLoad(
                            @NonNull LoadAdError error
                    ) {

                        interstitialAd = null;
                    }
                }
        );
    }

    // ==============================
    // SHOW INTERSTITIAL
    // ==============================

    private void showInterstitialAd() {

        if (interstitialAd != null) {

            interstitialAd.show(this);

        } else {

            loadInterstitialAd();
        }
    }

    // ==============================
    // LOAD REWARDED
    // ==============================

    private void loadRewardedAd() {

        AdRequest request =
                new AdRequest.Builder()
                        .build();

        RewardedAd.load(
                this,
                REWARDED_AD_ID,
                request,
                new RewardedAdLoadCallback() {

                    @Override
                    public void onAdLoaded(
                            @NonNull RewardedAd ad
                    ) {

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

                                            @Override
                                            public void
                                            onAdFailedToShowFullScreenContent(
                                                    @NonNull com.google.android.gms.ads.AdError adError
                                            ) {

                                                rewardedAd =
                                                        null;

                                                loadRewardedAd();
                                            }
                                        }
                                );
                    }

                    @Override
                    public void onAdFailedToLoad(
                            @NonNull LoadAdError error
                    ) {

                        rewardedAd = null;
                    }
                }
        );
    }

    // ==============================
    // SHOW REWARDED
    // ==============================

    private void showRewardedAd() {

        if (rewardedAd != null) {

            rewardedAd.show(
                    this,
                    rewardItem -> {

                        // Double coins
                        score = score * 2;

                        scoreText.setText(
                                String.valueOf(score)
                        );

                        // Update best score
                        if (score > bestScore) {

                            bestScore = score;

                            bestText.setText(
                                    "🏆 BEST: " + bestScore
                            );

                            saveBestScore();
                        }

                        messageText.setText(
                                "🎉 REWARD!\n"
                                        + "Coins doubled!"
                        );

                        Toast.makeText(
                                MainActivity.this,
                                "🪙 Coins 2X! 🎉",
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

    // ==============================
    // BEST SCORE LOAD
    // ==============================

    private void loadBestScore() {

        android.content.SharedPreferences prefs =
                getSharedPreferences(
                        PREFS,
                        MODE_PRIVATE
                );

        bestScore =
                prefs.getInt(
                        BEST_SCORE,
                        0
                );
    }

    // ==============================
    // BEST SCORE SAVE
    // ==============================

    private void saveBestScore() {

        android.content.SharedPreferences prefs =
                getSharedPreferences(
                        PREFS,
                        MODE_PRIVATE
                );

        prefs.edit()
                .putInt(
                        BEST_SCORE,
                        bestScore
                )
                .apply();
    }

    // ==============================
    // DESTROY
    // ==============================

    @Override
    protected void onDestroy() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        super.onDestroy();
    }
}
