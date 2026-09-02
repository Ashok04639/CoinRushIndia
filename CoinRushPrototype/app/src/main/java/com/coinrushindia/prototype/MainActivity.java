package com.coinrushindia.prototype;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MainActivity extends Activity {

    // =========================
    // APP DATA
    // =========================

    private SharedPreferences prefs;

    private String username = "";

    private int coins = 0;
    private int bestScore = 0;
    private int totalCoins = 0;

    private boolean gameRunning = false;

    private CountDownTimer timer;

    // =========================
    // UI
    // =========================

    private TextView scoreText;
    private TextView bestText;
    private TextView totalText;
    private TextView timerText;
    private TextView messageText;

    private Button tapButton;
    private Button rewardButton;
    private Button restartButton;
    private Button logoutButton;

    // =========================
    // ADMOB
    // =========================

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    private boolean adsInitialized = false;

    private static final String INTERSTITIAL_AD_ID =
            "ca-app-pub-459015901387755/9228973931";

    private static final String REWARDED_AD_ID =
            "ca-app-pub-459015901387755/5227139421";

    // =========================
    // COLORS
    // =========================

    private final int BG =
            Color.rgb(12, 18, 28);

    private final int CARD =
            Color.rgb(25, 34, 48);

    private final int WHITE =
            Color.WHITE;

    private final int GREEN =
            Color.rgb(46, 190, 100);

    private final int ORANGE =
            Color.rgb(255, 145, 45);

    private final int RED =
            Color.rgb(230, 70, 70);

    private final int GRAY =
            Color.rgb(150, 160, 175);

    private final int YELLOW =
            Color.rgb(255, 215, 60);

    // =========================
    // ON CREATE
    // =========================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(
                "CoinRushIndia",
                MODE_PRIVATE
        );

        username = prefs.getString(
                "username",
                ""
        );

        bestScore = prefs.getInt(
                "bestScore",
                0
        );

        totalCoins = prefs.getInt(
                "totalCoins",
                0
        );

        boolean loggedIn =
                prefs.getBoolean(
                        "loggedIn",
                        false
                );

        // पहले UI दिखाएँ
        if (loggedIn && !username.isEmpty()) {
            showGameScreen();
        } else {
            showLoginScreen();
        }

        // UI आने के बाद Ads initialize करें
        new Handler(Looper.getMainLooper())
                .postDelayed(
                        this::initializeAdsSafely,
                        1500
                );
    }

    // =========================
    // ADMOB INITIALIZATION
    // =========================

    private void initializeAdsSafely() {

        if (isFinishing()) {
            return;
        }

        try {

            MobileAds.initialize(
                    this,
                    initializationStatus -> {

                        adsInitialized = true;

                        loadInterstitialAd();
                        loadRewardedAd();
                    }
            );

        } catch (Exception e) {

            adsInitialized = false;
        }
    }

    // =========================
    // INTERSTITIAL LOAD
    // =========================

    private void loadInterstitialAd() {

        if (!adsInitialized) {
            return;
        }

        if (isFinishing()) {
            return;
        }

        try {

            AdRequest request =
                    new AdRequest.Builder().build();

            InterstitialAd.load(
                    this,
                    INTERSTITIAL_AD_ID,
                    request,
                    new InterstitialAdLoadCallback() {

                        @Override
                        public void onAdLoaded(
                                InterstitialAd ad) {

                            interstitialAd = ad;

                            interstitialAd
                                    .setFullScreenContentCallback(
                                            new FullScreenContentCallback() {

                                                @Override
                                                public void onAdDismissedFullScreenContent() {

                                                    interstitialAd =
                                                            null;

                                                    loadInterstitialAd();
                                                }

                                                @Override
                                                public void onAdFailedToShowFullScreenContent(
                                                        AdError adError) {

                                                    interstitialAd =
                                                            null;

                                                    loadInterstitialAd();
                                                }
                                            }
                                    );
                        }

                        @Override
                        public void onAdFailedToLoad(
                                LoadAdError error) {

                            interstitialAd = null;
                        }
                    }
            );

        } catch (Exception e) {

            interstitialAd = null;
        }
    }

    // =========================
    // SHOW INTERSTITIAL
    // =========================

    private void showInterstitialAd() {

        if (interstitialAd != null) {

            try {

                interstitialAd.show(this);

            } catch (Exception e) {

                interstitialAd = null;
                loadInterstitialAd();
            }

        } else {

            loadInterstitialAd();
        }
    }

    // =========================
    // REWARDED LOAD
    // =========================

    private void loadRewardedAd() {

        if (!adsInitialized) {
            return;
        }

        if (isFinishing()) {
            return;
        }

        try {

            AdRequest request =
                    new AdRequest.Builder().build();

            RewardedAd.load(
                    this,
                    REWARDED_AD_ID,
                    request,
                    new RewardedAdLoadCallback() {

                        @Override
                        public void onAdLoaded(
                                RewardedAd ad) {

                            rewardedAd = ad;

                            rewardedAd
                                    .setFullScreenContentCallback(
                                            new FullScreenContentCallback() {

                                                @Override
                                                public void onAdDismissedFullScreenContent() {

                                                    rewardedAd =
                                                            null;

                                                    loadRewardedAd();
                                                }

                                                @Override
                                                public void onAdFailedToShowFullScreenContent(
                                                        AdError adError) {

                                                    rewardedAd =
                                                            null;

                                                    loadRewardedAd();
                                                }
                                            }
                                    );
                        }

                        @Override
                        public void onAdFailedToLoad(
                                LoadAdError error) {

                            rewardedAd = null;
                        }
                    }
            );

        } catch (Exception e) {

            rewardedAd = null;
        }
    }

    // =========================
    // SHOW REWARDED
    // =========================

    private void showRewardedAd() {

        if (rewardedAd == null) {

            Toast.makeText(
                    this,
                    "Ad abhi ready nahi hai. Thodi der baad try karo.",
                    Toast.LENGTH_SHORT
            ).show();

            loadRewardedAd();

            return;
        }

        RewardedAd ad =
                rewardedAd;

        rewardedAd = null;

        try {

            ad.show(
                    this,
                    rewardItem -> {

                        giveDoubleCoins();
                    }
            );

        } catch (Exception e) {

            loadRewardedAd();
        }
    }

    // =========================
    // LOGIN SCREEN
    // =========================

    private void showLoginScreen() {

        stopTimer();

        LinearLayout root =
                createRoot();

        TextView title =
                createText(
                        "🇮🇳 Coin Rush India",
                        30,
                        WHITE,
                        true
                );

        root.addView(title);

        TextView subtitle =
                createText(
                        "LOGIN TO PLAY",
                        15,
                        GRAY,
                        true
                );

        subtitle.setLayoutParams(
                marginParams(
                        0,
                        10,
                        0,
                        25
                )
        );

        root.addView(subtitle);

        EditText usernameInput =
                createInput(
                        "Username"
                );

        root.addView(usernameInput);

        EditText passwordInput =
                createInput(
                        "Password"
                );

        passwordInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        root.addView(passwordInput);

        Button loginButton =
                createButton(
                        "LOGIN",
                        GREEN
                );

        root.addView(loginButton);

        Button signupButton =
                createButton(
                        "CREATE NEW ACCOUNT",
                        ORANGE
                );

        root.addView(signupButton);

        loginButton.setOnClickListener(
                v -> {

                    String user =
                            usernameInput
                                    .getText()
                                    .toString()
                                    .trim();

                    String pass =
                            passwordInput
                                    .getText()
                                    .toString();

                    if (user.isEmpty()
                            || pass.isEmpty()) {

                        Toast.makeText(
                                this,
                                "Username aur password bharo",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    String savedUser =
                            prefs.getString(
                                    "username",
                                    ""
                            );

                    String savedPassword =
                            prefs.getString(
                                    "password",
                                    ""
                            );

                    if (savedUser.isEmpty()) {

                        Toast.makeText(
                                this,
                                "Pehle account create karo",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    if (savedUser.equals(user)
                            && savedPassword.equals(
                            hashPassword(pass))) {

                        prefs.edit()
                                .putBoolean(
                                        "loggedIn",
                                        true
                                )
                                .apply();

                        username = user;

                        showGameScreen();

                    } else {

                        Toast.makeText(
                                this,
                                "Username ya password galat hai",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        signupButton.setOnClickListener(
                v -> showSignupScreen()
        );

        setContentView(
                wrapScroll(root)
        );
    }

    // =========================
    // SIGNUP SCREEN
    // =========================

    private void showSignupScreen() {

        stopTimer();

        LinearLayout root =
                createRoot();

        TextView title =
                createText(
                        "🇮🇳 Coin Rush India",
                        30,
                        WHITE,
                        true
                );

        root.addView(title);

        TextView subtitle =
                createText(
                        "CREATE YOUR ACCOUNT",
                        15,
                        GRAY,
                        true
                );

        subtitle.setLayoutParams(
                marginParams(
                        0,
                        10,
                        0,
                        25
                )
        );

        root.addView(subtitle);

        EditText usernameInput =
                createInput(
                        "Choose Username"
                );

        root.addView(usernameInput);

        EditText passwordInput =
                createInput(
                        "Choose Password"
                );

        passwordInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        root.addView(passwordInput);

        EditText confirmInput =
                createInput(
                        "Confirm Password"
                );

        confirmInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        root.addView(confirmInput);

        Button createButton =
                createButton(
                        "CREATE ACCOUNT",
                        GREEN
                );

        root.addView(createButton);

        Button backButton =
                createButton(
                        "BACK TO LOGIN",
                        GRAY
                );

        root.addView(backButton);

        createButton.setOnClickListener(
                v -> {

                    String user =
                            usernameInput
                                    .getText()
                                    .toString()
                                    .trim();

                    String pass =
                            passwordInput
                                    .getText()
                                    .toString();

                    String confirm =
                            confirmInput
                                    .getText()
                                    .toString();

                    if (user.isEmpty()
                            || pass.isEmpty()
                            || confirm.isEmpty()) {

                        Toast.makeText(
                                this,
                                "Sabhi details bharo",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    if (user.length() < 3) {

                        Toast.makeText(
                                this,
                                "Username kam se kam 3 characters ka ho",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    if (pass.length() < 4) {

                        Toast.makeText(
                                this,
                                "Password kam se kam 4 characters ka ho",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    if (!pass.equals(confirm)) {

                        Toast.makeText(
                                this,
                                "Passwords match nahi kar rahe",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    String existingUser =
                            prefs.getString(
                                    "username",
                                    ""
                            );

                    if (!existingUser.isEmpty()) {

                        Toast.makeText(
                                this,
                                "Account already bana hua hai",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    prefs.edit()
                            .putString(
                                    "username",
                                    user
                            )
                            .putString(
                                    "password",
                                    hashPassword(pass)
                            )
                            .putBoolean(
                                    "loggedIn",
                                    true
                            )
                            .putInt(
                                    "bestScore",
                                    0
                            )
                            .putInt(
                                    "totalCoins",
                                    0
                            )
                            .apply();

                    username = user;
                    bestScore = 0;
                    totalCoins = 0;

                    Toast.makeText(
                            this,
                            "Account created!",
                            Toast.LENGTH_SHORT
                    ).show();

                    showGameScreen();
                }
        );

        backButton.setOnClickListener(
                v -> showLoginScreen()
        );

        setContentView(
                wrapScroll(root)
        );
    }

    // =========================
    // GAME SCREEN
    // =========================

    private void showGameScreen() {

        stopTimer();

        LinearLayout root =
                createRoot();

        // TOP ROW
        LinearLayout topRow =
                new LinearLayout(this);

        topRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title =
                createText(
                        "🇮🇳 Coin Rush India",
                        25,
                        WHITE,
                        true
                );

        topRow.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        dp(65),
                        1
                )
        );

        logoutButton =
                createButton(
                        "LOGOUT",
                        RED
                );

        LinearLayout.LayoutParams logoutParams =
                new LinearLayout.LayoutParams(
                        dp(100),
                        dp(50)
                );

        topRow.addView(
                logoutButton,
                logoutParams
        );

        root.addView(topRow);

        // PLAYER
        TextView playerText =
                createText(
                        "Player: " + username,
                        16,
                        GRAY,
                        false
                );

        playerText.setGravity(
                Gravity.CENTER
        );

        playerText.setLayoutParams(
                marginParams(
                        0,
                        5,
                        0,
                        15
                )
        );

        root.addView(playerText);

        // SCORE CARD
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
                dp(15),
                dp(10),
                dp(15)
        );

        scoreCard.setBackgroundColor(
                CARD
        );

        scoreText =
                createText(
                        "0 COINS",
                        34,
                        YELLOW,
                        true
                );

        scoreCard.addView(scoreText);

        bestText =
                createText(
                        "BEST: " + bestScore,
                        15,
                        GRAY,
                        false
                );

        scoreCard.addView(bestText);

        totalText =
                createText(
                        "TOTAL COINS: " + totalCoins,
                        15,
                        GRAY,
                        false
                );

        scoreCard.addView(totalText);

        root.addView(
                scoreCard,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(130)
                )
        );

        // TIMER
        timerText =
                createText(
                        "⏱ 30",
                        28,
                        WHITE,
                        true
                );

        timerText.setGravity(
                Gravity.CENTER
        );

        timerText.setLayoutParams(
                marginParams(
                        0,
                        18,
                        0,
                        5
                )
        );

        root.addView(timerText);

        // MESSAGE
        messageText =
                createText(
                        "TAP • COLLECT • RUSH!",
                        17,
                        GRAY,
                        true
                );

        messageText.setGravity(
                Gravity.CENTER
        );

        root.addView(messageText);

        // TAP BUTTON
        tapButton =
                createButton(
                        "🪙\nTAP!",
                        ORANGE
                );

        tapButton.setTextSize(26);

        LinearLayout.LayoutParams tapParams =
                new LinearLayout.LayoutParams(
                        dp(270),
                        dp(180)
                );

        tapParams.gravity =
                Gravity.CENTER;

        tapParams.setMargins(
                0,
                dp(15),
                0,
                dp(15)
        );

        root.addView(
                tapButton,
                tapParams
        );

        tapButton.setOnClickListener(
                v -> {

                    if (!gameRunning) {
                        return;
                    }

                    coins++;

                    updateScore();
                }
        );

        // REWARD BUTTON
        rewardButton =
                createButton(
                        "🎁 WATCH AD • 2X COINS",
                        GREEN
                );

        rewardButton.setTextSize(15);

        LinearLayout.LayoutParams rewardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                );

        rewardParams.setMargins(
                0,
                dp(5),
                0,
                dp(5)
        );

        root.addView(
                rewardButton,
                rewardParams
        );

        rewardButton.setVisibility(
                View.GONE
        );

        rewardButton.setOnClickListener(
                v -> showRewardedAd()
        );

        // RESTART BUTTON
        restartButton =
                createButton(
                        "RESTART GAME",
                        Color.rgb(
                                80,
                                90,
                                105
                        )
                );

        LinearLayout.LayoutParams restartParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                );

        restartParams.setMargins(
                0,
                dp(5),
                0,
                dp(10)
        );

        root.addView(
                restartButton,
                restartParams
        );

        restartButton.setVisibility(
                View.GONE
        );

        restartButton.setOnClickListener(
                v -> startGame()
        );

        // LOGOUT
        logoutButton.setOnClickListener(
                v -> {

                    stopTimer();

                    prefs.edit()
                            .putBoolean(
                                    "loggedIn",
                                    false
                            )
                            .apply();

                    username = "";

                    showLoginScreen();
                }
        );

        setContentView(
                wrapScroll(root)
        );

        startGame();
    }

    // =========================
    // START GAME
    // =========================

    private void startGame() {

        stopTimer();

        coins = 0;

        gameRunning = true;

        if (scoreText != null) {
            scoreText.setText(
                    "0 COINS"
            );
        }

        if (timerText != null) {
            timerText.setText(
                    "⏱ 30"
            );
        }

        if (messageText != null) {
            messageText.setText(
                    "TAP • COLLECT • RUSH!"
            );
        }

        if (tapButton != null) {
            tapButton.setVisibility(
                    View.VISIBLE
            );

            tapButton.setEnabled(
                    true
            );
        }

        if (rewardButton != null) {
            rewardButton.setVisibility(
                    View.GONE
            );
        }

        if (restartButton != null) {
            restartButton.setVisibility(
                    View.GONE
            );
        }

        timer =
                new CountDownTimer(
                        30000,
                        1000
                ) {

                    @Override
                    public void onTick(
                            long millisUntilFinished) {

                        long seconds =
                                (millisUntilFinished
                                        + 999)
                                        / 1000;

                        if (timerText != null) {

                            timerText.setText(
                                    "⏱ " + seconds
                            );
                        }
                    }

                    @Override
                    public void onFinish() {

                        if (!gameRunning) {
                            return;
                        }

                        gameRunning = false;

                        if (timerText != null) {
                            timerText.setText(
                                    "⏱ 0"
                            );
                        }

                        gameOver();
                    }
                };

        timer.start();
    }

    // =========================
    // GAME OVER
    // =========================

    private void gameOver() {

        stopTimer();

        gameRunning = false;

        if (tapButton != null) {

            tapButton.setEnabled(
                    false
            );

            tapButton.setVisibility(
                    View.GONE
            );
        }

        if (messageText != null) {

            messageText.setText(
                    "🎉 GAME OVER • "
                            + coins
                            + " COINS!"
            );
        }

        // BEST SCORE
        if (coins > bestScore) {

            bestScore = coins;

            prefs.edit()
                    .putInt(
                            "bestScore",
                            bestScore
                    )
                    .apply();
        }

        // TOTAL COINS
        totalCoins += coins;

        prefs.edit()
                .putInt(
                        "totalCoins",
                        totalCoins
                )
                .apply();

        updateScore();

        if (bestText != null) {

            bestText.setText(
                    "BEST: " + bestScore
            );
        }

        if (totalText != null) {

            totalText.setText(
                    "TOTAL COINS: "
                            + totalCoins
            );
        }

        if (rewardButton != null) {

            rewardButton.setVisibility(
                    View.VISIBLE
            );
        }

        if (restartButton != null) {

            restartButton.setVisibility(
                    View.VISIBLE
            );
        }

        // Interstitial
        showInterstitialAd();
    }

    // =========================
    // REWARD
    // =========================

    private void giveDoubleCoins() {

        if (coins <= 0) {

            Toast.makeText(
                    this,
                    "Is round me coins nahi mile.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int bonus =
                coins;

        coins =
                coins * 2;

        totalCoins += bonus;

        if (coins > bestScore) {

            bestScore = coins;
        }

        prefs.edit()
                .putInt(
                        "bestScore",
                        bestScore
                )
                .putInt(
                        "totalCoins",
                        totalCoins
                )
                .apply();

        updateScore();

        if (bestText != null) {

            bestText.setText(
                    "BEST: " + bestScore
            );
        }

        if (totalText != null) {

            totalText.setText(
                    "TOTAL COINS: "
                            + totalCoins
            );
        }

        if (messageText != null) {

            messageText.setText(
                    "🎁 REWARD! Coins doubled!"
            );
        }

        Toast.makeText(
                this,
                "+" + bonus + " bonus coins!",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================
    // UPDATE SCORE
    // =========================

    private void updateScore() {

        if (scoreText != null) {

            scoreText.setText(
                    coins + " COINS"
            );
        }
    }

    // =========================
    // ROOT
    // =========================

    private LinearLayout createRoot() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        root.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(25)
        );

        root.setBackgroundColor(
                BG
        );

        return root;
    }

    // =========================
    // TEXT
    // =========================

    private TextView createText(
            String text,
            int size,
            int color,
            boolean bold) {

        TextView view =
                new TextView(this);

        view.setText(text);

        view.setTextSize(size);

        view.setTextColor(color);

        view.setGravity(
                Gravity.CENTER
        );

        if (bold) {

            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        view.setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
        );

        return view;
    }

    // =========================
    // INPUT
    // =========================

    private EditText createInput(
            String hint) {

        EditText input =
                new EditText(this);

        input.setHint(hint);

        input.setHintTextColor(
                Color.rgb(
                        150,
                        160,
                        170
                )
        );

        input.setTextColor(
                WHITE
        );

        input.setTextSize(16);

        input.setSingleLine(true);

        input.setPadding(
                dp(15),
                dp(5),
                dp(15),
                dp(5)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                );

        params.setMargins(
                0,
                dp(6),
                0,
                dp(6)
        );

        input.setLayoutParams(
                params
        );

        return input;
    }

    // =========================
    // BUTTON
    // =========================

    private Button createButton(
            String text,
            int background) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextSize(15);

        button.setTextColor(
                WHITE
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setAllCaps(false);

        button.setBackgroundColor(
                background
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                );

        params.setMargins(
                0,
                dp(6),
                0,
                dp(6)
        );

        button.setLayoutParams(
                params
        );

        return button;
    }

    // =========================
    // MARGIN
    // =========================

    private LinearLayout.LayoutParams marginParams(
            int left,
            int top,
            int right,
            int bottom) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        params.setMargins(
                dp(left),
                dp(top),
                dp(right),
                dp(bottom)
        );

        return params;
    }

    // =========================
    // SCROLL
    // =========================

    private ScrollView wrapScroll(
            LinearLayout root) {

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(
                true
        );

        scroll.setBackgroundColor(
                BG
        );

        scroll.addView(root);

        return scroll;
    }

    // =========================
    // DP
    // =========================

    private int dp(int value) {

        return (int) (
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // =========================
    // PASSWORD HASH
    // =========================

    private String hashPassword(
            String password) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            password.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hex =
                    new StringBuilder();

            for (byte b : hash) {

                String h =
                        Integer.toHexString(
                                0xff & b
                        );

                if (h.length() == 1) {
                    hex.append('0');
                }

                hex.append(h);
            }

            return hex.toString();

        } catch (Exception e) {

            return password;
        }
    }

    // =========================
    // STOP TIMER
    // =========================

    private void stopTimer() {

        if (timer != null) {

            timer.cancel();

            timer = null;
        }

        gameRunning = false;
    }

    // =========================
    // DESTROY
    // =========================

    @Override
    protected void onDestroy() {

        stopTimer();

        super.onDestroy();
    }
}
