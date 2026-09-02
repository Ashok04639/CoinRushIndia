package com.coinrushindia.prototype;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.animation.ScaleAnimation;
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
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    private GradientDrawable roundedBackground(
            int color,
            int radius
    ) {
        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    private TextView makeText(
            String text,
            float size,
            int color
    ) {
        TextView view = new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);

        return view;
    }

    private void createGameScreen() {

        ScrollView scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);

        scrollView.setBackgroundColor(
                Color.rgb(12, 15, 25)
        );

        LinearLayout mainLayout =
                new LinearLayout(this);

        mainLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        mainLayout.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        mainLayout.setPadding(
                dp(18),
                dp(20),
                dp(18),
                dp(25)
        );

        scrollView.addView(mainLayout);

        // --------------------------------
        // HEADER
        // --------------------------------

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
                        dp(65)
                );

        mainLayout.addView(
                title,
                titleParams
        );

        TextView subtitle = makeText(
                "TAP • COLLECT • RUSH!",
                14,
                Color.LTGRAY
        );

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(35)
                );

        mainLayout.addView(
                subtitle,
                subtitleParams
        );

        // --------------------------------
        // SCORE CARD
        // --------------------------------

        LinearLayout scoreCard =
                new LinearLayout(this);

        scoreCard.setOrientation(
                LinearLayout.VERTICAL
        );

        scoreCard.setGravity(
                Gravity.CENTER
        );

        scoreCard.setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(8)
        );

        scoreCard.setBackground(
                roundedBackground(
                        Color.rgb(28, 32, 48),
                        18
                )
        );

        scoreText = makeText(
                "0",
                38,
                Color.YELLOW
        );

        scoreText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        scoreCard.addView(
                scoreText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        TextView scoreLabel = makeText(
                "COINS",
                13,
                Color.LTGRAY
        );

        scoreCard.addView(
                scoreLabel,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(25)
                )
        );

        LinearLayout.LayoutParams scoreCardParams =
                new LinearLayout.LayoutParams(
                        dp(220),
                        dp(100)
                );

        scoreCardParams.gravity = Gravity.CENTER;

        scoreCardParams.setMargins(
                0,
                dp(10),
                0,
                dp(10)
        );

        mainLayout.addView(
                scoreCard,
                scoreCardParams
        );

        // --------------------------------
        // TIMER
        // --------------------------------

        timerText = makeText(
                "⏱️ 30",
                21,
                Color.WHITE
        );

        timerText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        timerText.setBackground(
                roundedBackground(
                        Color.rgb(40, 44, 62),
                        30
                )
        );

        LinearLayout.LayoutParams timerParams =
                new LinearLayout.LayoutParams(
                        dp(130),
                        dp(45)
                );

        timerParams.gravity = Gravity.CENTER;

        timerParams.setMargins(
                0,
                dp(5),
                0,
                dp(12)
        );

        mainLayout.addView(
                timerText,
                timerParams
        );

        // --------------------------------
        // MESSAGE
        // --------------------------------

        messageText = makeText(
                "Tap the coin as fast as you can!",
                16,
                Color.LTGRAY
        );

        LinearLayout.LayoutParams messageParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                );

        mainLayout.addView(
                messageText,
                messageParams
        );

        // --------------------------------
        // COIN BUTTON
        // --------------------------------

        coinButton = new Button(this);

        coinButton.setText(
                "🪙\nTAP!"
        );

        coinButton.setTextSize(25);
        coinButton.setTextColor(Color.WHITE);
        coinButton.setGravity(Gravity.CENTER);
        coinButton.setAllCaps(false);

        GradientDrawable coinBackground =
                new GradientDrawable();

        coinBackground.setShape(
                GradientDrawable.OVAL
        );

        coinBackground.setColor(
                Color.rgb(255, 152, 0)
        );

        coinButton.setBackground(
                coinBackground
        );

        coinButton.setElevation(
                dp(10)
        );

        LinearLayout.LayoutParams coinParams =
                new LinearLayout.LayoutParams(
                        dp(190),
                        dp(190)
                );

        coinParams.gravity = Gravity.CENTER;

        coinParams.setMargins(
                0,
                dp(8),
                0,
                dp(18)
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

            // TAP animation
            ScaleAnimation animation =
                    new ScaleAnimation(
                            0.90f,
                            1.0f,
                            0.90f,
                            1.0f,
                            ScaleAnimation.RELATIVE_TO_SELF,
                            0.5f,
                            ScaleAnimation.RELATIVE_TO_SELF,
                            0.5f
                    );

            animation.setDuration(100);

            coinButton.startAnimation(
                    animation
            );
        });

        // --------------------------------
        // REWARD BUTTON
        // --------------------------------

        rewardButton = new Button(this);

        rewardButton.setText(
                "🎁  WATCH AD  •  2X COINS"
        );

        rewardButton.setTextSize(15);
        rewardButton.setTextColor(Color.WHITE);
        rewardButton.setAllCaps(false);

        rewardButton.setBackground(
                roundedBackground(
                        Color.rgb(46, 125, 50),
                        18
                )
        );

        rewardButton.setVisibility(
                View.GONE
        );

        LinearLayout.LayoutParams rewardParams =
                new LinearLayout.LayoutParams(
                        dp(290),
                        dp(58)
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

        rewardButton.setOnClickListener(
                v -> showRewardedAd()
        );

        // --------------------------------
        // RESTART BUTTON
        // --------------------------------

        restartButton = new Button(this);

        restartButton.setText(
                "🔄  RESTART GAME"
        );

        restartButton.setTextSize(15);
        restartButton.setTextColor(Color.WHITE);
        restartButton.setAllCaps(false);

        restartButton.setBackground(
                roundedBackground(
                        Color.rgb(55, 65, 85),
                        18
                )
        );

        restartButton.setVisibility(
                View.GONE
        );

        LinearLayout.LayoutParams restartParams =
                new LinearLayout.LayoutParams(
                        dp(250),
                        dp(55)
                );

        restartParams.gravity = Gravity.CENTER;

        mainLayout.addView(
                restartButton,
                restartParams
        );

        restartButton.setOnClickListener(
                v -> startGame()
        );

        // --------------------------------
        // FOOTER
        // --------------------------------

        TextView footer = makeText(
                "Coin Rush India • Have Fun!",
                12,
                Color.GRAY
        );

        LinearLayout.LayoutParams footerParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(35)
                );

        footerParams.setMargins(
                0,
                dp(15),
                0,
                0
        );

        mainLayout.addView(
                footer,
                footerParams
        );

        setContentView(scrollView);
    }

    // --------------------------------
    // START GAME
    // --------------------------------

    private void startGame() {

        score = 0;

        scoreText.setText("0");

        timerText.setText(
                "⏱️ 30"
        );

        messageText.setText(
                "Tap the coin as fast as you can!"
        );

        coinButton.setEnabled(true);
        coinButton.setVisibility(
                View.VISIBLE
        );

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

                        coinButton.setEnabled(
                                false
                        );

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

    // --------------------------------
    // INTERSTITIAL AD
    // --------------------------------

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
                            @NonNull InterstitialAd ad
                    ) {

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
                            @NonNull LoadAdError adError
                    ) {

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

    // --------------------------------
    // REWARDED AD
    // --------------------------------

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
                            @NonNull RewardedAd ad
                    ) {

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
                            @NonNull LoadAdError adError
                    ) {

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
