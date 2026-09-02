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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.SharedPreferences;

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
    private TextView balanceText;
    private TextView accountText;

    private Button coinButton;
    private Button rewardButton;
    private Button restartButton;
    private Button signupButton;

    private int score = 0;
    private int totalBalance = 0;

    private CountDownTimer countDownTimer;

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    private SharedPreferences prefs;

    // Google TEST Ad IDs
    private static final String INTERSTITIAL_AD_ID =
            "ca-app-pub-3940256099942544/1033173712";

    private static final String REWARDED_AD_ID =
            "ca-app-pub-3940256099942544/5224354917";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(
                "CoinRushData",
                MODE_PRIVATE
        );

        totalBalance = prefs.getInt(
                "total_balance",
                0
        );

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
                        .getDisplayMetrics()
                        .density + 0.5f
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

    private GradientDrawable coinBackground() {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.OVAL
        );

        drawable.setColor(
                Color.rgb(255, 152, 0)
        );

        drawable.setStroke(
                dp(5),
                Color.rgb(255, 195, 50)
        );

        return drawable;
    }

    private void createGameScreen() {

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
                dp(15),
                dp(18),
                dp(15)
        );

        mainLayout.setBackgroundColor(
                Color.rgb(10, 12, 25)
        );

        // =========================
        // TITLE
        // =========================

        TextView title =
                new TextView(this);

        title.setText(
                "🇮🇳 Coin Rush India"
        );

        title.setTextSize(30);

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

        mainLayout.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                )
        );

        // =========================
        // ACCOUNT ROW
        // =========================

        LinearLayout accountRow =
                new LinearLayout(this);

        accountRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        accountRow.setPadding(
                dp(12),
                0,
                dp(12),
                0
        );

        accountRow.setBackground(
                roundedBackground(
                        Color.rgb(35, 38, 58),
                        25
                )
        );

        accountText =
                new TextView(this);

        accountText.setText(
                "👤 Guest"
        );

        accountText.setTextSize(16);

        accountText.setTextColor(
                Color.WHITE
        );

        accountRow.addView(
                accountText,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        signupButton =
                new Button(this);

        signupButton.setText(
                "SIGN UP"
        );

        signupButton.setTextSize(14);

        signupButton.setTextColor(
                Color.WHITE
        );

        signupButton.setAllCaps(false);

        signupButton.setBackground(
                roundedBackground(
                        Color.rgb(70, 90, 180),
                        25
                )
        );

        accountRow.addView(
                signupButton,
                new LinearLayout.LayoutParams(
                        dp(110),
                        dp(48)
                )
        );

        LinearLayout.LayoutParams accountParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                );

        accountParams.setMargins(
                0,
                0,
                0,
                dp(10)
        );

        mainLayout.addView(
                accountRow,
                accountParams
        );

        signupButton.setOnClickListener(
                v -> showSignupDialog()
        );

        // =========================
        // TOTAL BALANCE
        // =========================

        LinearLayout balanceCard =
                new LinearLayout(this);

        balanceCard.setOrientation(
                LinearLayout.VERTICAL
        );

        balanceCard.setGravity(
                Gravity.CENTER
        );

        balanceCard.setBackground(
                roundedBackground(
                        Color.rgb(30, 33, 52),
                        25
                )
        );

        TextView balanceLabel =
                new TextView(this);

        balanceLabel.setText(
                "TOTAL BALANCE"
        );

        balanceLabel.setTextSize(15);

        balanceLabel.setTextColor(
                Color.LTGRAY
        );

        balanceLabel.setGravity(
                Gravity.CENTER
        );

        balanceCard.addView(
                balanceLabel,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(30)
                )
        );

        balanceText =
                new TextView(this);

        balanceText.setText(
                "🪙 " + totalBalance
        );

        balanceText.setTextSize(30);

        balanceText.setTextColor(
                Color.rgb(255, 215, 0)
        );

        balanceText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        balanceText.setGravity(
                Gravity.CENTER
        );

        balanceCard.addView(
                balanceText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                )
        );

        LinearLayout.LayoutParams balanceParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(90)
                );

        balanceParams.setMargins(
                dp(15),
                0,
                dp(15),
                dp(10)
        );

        mainLayout.addView(
                balanceCard,
                balanceParams
        );

        // =========================
        // SUBTITLE
        // =========================

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "TAP • COLLECT • RUSH!"
        );

        subtitle.setTextSize(17);

        subtitle.setTextColor(
                Color.LTGRAY
        );

        subtitle.setGravity(
                Gravity.CENTER
        );

        mainLayout.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        dp
