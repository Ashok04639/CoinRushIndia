package com.coinrushindia.prototype;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.content.Context;
import android.content.SharedPreferences;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.security.MessageDigest;

public class MainActivity extends Activity {

    // ============================================================
    // APP DATA
    // ============================================================

    private static final String PREFS = "CoinRushIndiaPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_BEST_SCORE = "best_score";

    private SharedPreferences prefs;

    // ============================================================
    // GOOGLE TEST ADS
    // IMPORTANT: Keep these for development/testing.
    // ============================================================

    private static final String INTERSTITIAL_AD_ID =
            "ca-app-pub-3940256099942544/1033173712";

    private static final String REWARDED_AD_ID =
            "ca-app-pub-3940256099942544/5224354917";

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    // ============================================================
    // GAME VARIABLES
    // ============================================================

    private int score = 0;
    private int bestScore = 0;

    private CountDownTimer countDownTimer;

    private TextView scoreText;
    private TextView timerText;
    private TextView messageText;
    private TextView bestScoreText;
    private TextView playerText;

    private Button coinButton;
    private Button rewardButton;
    private Button restartButton;
    private Button logoutButton;

    // ============================================================
    // COLORS
    // ============================================================

    private final int BG_COLOR = Color.rgb(15, 18, 28);
    private final int CARD_COLOR = Color.rgb(28, 33, 47);
    private final int WHITE = Color.WHITE;
    private final int LIGHT = Color.rgb(190, 195, 210);
    private final int ORANGE = Color.rgb(255, 152, 0);
    private final int GREEN = Color.rgb(45, 190, 105);
    private final int RED = Color.rgb(220, 70, 70);

    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        bestScore = prefs.getInt(KEY_BEST_SCORE, 0);

        // Initialize AdMob
        MobileAds.initialize(this, initializationStatus -> {
            loadInterstitialAd();
            loadRewardedAd();
        });

        // Check login
        if (isLoggedIn()) {
            showGameScreen();
        } else {
            showLoginScreen();
        }
    }

    // ============================================================
    // LOGIN CHECK
    // ============================================================

    private boolean isLoggedIn() {
        return prefs.contains(KEY_USERNAME);
    }

    // ============================================================
    // LOGIN SCREEN
    // ============================================================

    private void showLoginScreen() {

        LinearLayout root = createRootLayout();

        TextView title = createTitle("🇮🇳 Coin Rush India");

        TextView subtitle = createText(
                "LOGIN TO PLAY",
                18,
                LIGHT
        );

        root.addView(title, matchParams(100));
        root.addView(subtitle, matchParams(55));

        LinearLayout card = createCard();

        TextView heading = createText(
                "Welcome Back 👋",
                25,
                WHITE
        );
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        card.addView(heading, matchParams(65));

        EditText usernameInput = createInput(
                "Username"
        );

        EditText passwordInput = createInput(
                "Password"
        );
        passwordInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        card.addView(usernameInput, matchParams(60));
        addSpace(card, 12);
        card.addView(passwordInput, matchParams(60));
        addSpace(card, 20);

        Button loginButton = createButton(
                "🔐  LOGIN",
                GREEN
        );

        card.addView(loginButton, matchParams(60));

        addSpace(card, 15);

        Button signupButton = createButton(
                "📝  CREATE ACCOUNT",
                ORANGE
        );

        card.addView(signupButton, matchParams(60));

        addSpace(card, 15);

        TextView note = createText(
                "Your account is saved on this device.",
                13,
                LIGHT
        );

        card.addView(note, matchParams(45));

        root.addView(
                card,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        loginButton.setOnClickListener(v -> {

            String username =
                    usernameInput.getText().toString().trim();

            String password =
                    passwordInput.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                        this,
                        "Username aur password enter karein",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String savedUsername =
                    prefs.getString(KEY_USERNAME, "");

            String savedPassword =
                    prefs.getString(KEY_PASSWORD, "");

            if (username.equals(savedUsername)
                    && hashPassword(password).equals(savedPassword)) {

                Toast.makeText(
                        this,
                        "Login successful! 🎮",
                        Toast.LENGTH_SHORT
                ).show();

                showGameScreen();

            } else {

                Toast.makeText(
                        this,
                        "Username ya password galat hai",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        signupButton.setOnClickListener(v ->
                showSignupScreen()
        );

        setContentView(root);
    }

    // ============================================================
    // SIGNUP SCREEN
    // ============================================================

    private void showSignupScreen() {

        LinearLayout root = createRootLayout();

        TextView title = createTitle("🇮🇳 Coin Rush India");

        TextView subtitle = createText(
                "CREATE YOUR ACCOUNT",
                18,
                LIGHT
        );

        root.addView(title, matchParams(100));
        root.addView(subtitle, matchParams(55));

        LinearLayout card = createCard();

        TextView heading = createText(
                "Join Coin Rush 🚀",
                25,
                WHITE
        );
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        card.addView(heading, matchParams(65));

        EditText usernameInput =
                createInput("Choose Username");

        EditText passwordInput =
                createInput("Choose Password");

        passwordInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        EditText confirmInput =
                createInput("Confirm Password");

        confirmInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        card.addView(usernameInput, matchParams(60));

        addSpace(card, 12);

        card.addView(passwordInput, matchParams(60));

        addSpace(card, 12);

        card.addView(confirmInput, matchParams(60));

        addSpace(card, 22);

        Button createButton = createButton(
                "🚀  CREATE ACCOUNT",
                GREEN
        );

        card.addView(createButton, matchParams(60));

        addSpace(card, 15);

        Button backButton = createButton(
                "← BACK TO LOGIN",
                Color.rgb(80, 85, 100)
        );

        card.addView(backButton, matchParams(60));

        addSpace(card, 10);

        TextView note = createText(
                "Account is stored locally on this device.",
                13,
                LIGHT
        );

        card.addView(note, matchParams(45));

        root.addView(
                card,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        createButton.setOnClickListener(v -> {

            String username =
                    usernameInput.getText().toString().trim();

            String password =
                    passwordInput.getText().toString();

            String confirm =
                    confirmInput.getText().toString();

            if (username.length() < 3) {

                Toast.makeText(
                        this,
                        "Username kam se kam 3 characters ka ho",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (password.length() < 4) {

                Toast.makeText(
                        this,
                        "Password kam se kam 4 characters ka ho",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (!password.equals(confirm)) {

                Toast.makeText(
                        this,
                        "Passwords match nahi kar rahe",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (prefs.contains(KEY_USERNAME)) {

                Toast.makeText(
                        this,
                        "Is device par account already bana hua hai",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            prefs.edit()
                    .putString(KEY_USERNAME, username)
                    .putString(
                            KEY_PASSWORD,
                            hashPassword(password)
                    )
                    .putInt(KEY_BEST_SCORE, 0)
                    .apply();

            bestScore = 0;

            Toast.makeText(
                    this,
                    "Account created! 🎉",
                    Toast.LENGTH_SHORT
            ).show();

            showGameScreen();
        });

        backButton.setOnClickListener(v ->
                showLoginScreen()
        );

        setContentView(root);
    }

    // ============================================================
    // GAME SCREEN
    // ============================================================

    private void showGameScreen() {

        LinearLayout root = createRootLayout();

        // ---------------- HEADER ----------------

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams headerParams =
                new LinearLayout.LayoutParams(
                        -1,
                        75
                );

        TextView title = createText(
                "🇮🇳 Coin Rush India",
                25,
                WHITE
        );

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        header.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                )
        );

        logoutButton = createSmallButton(
                "Logout"
        );

        header.addView(
                logoutButton,
                new LinearLayout.LayoutParams(
                        105,
                        50
                )
        );

        root.addView(header, headerParams);

        // ---------------- PLAYER ----------------

        String username =
                prefs.getString(
                        KEY_USERNAME,
                        "Player"
                );

        playerText = createText(
                "Player: " + username,
                15,
                LIGHT
        );

        playerText.setGravity(Gravity.CENTER);

        root.addView(
                playerText,
                matchParams(40)
        );

        // ---------------- SUBTITLE ----------------

        TextView subtitle = createText(
                "TAP • COLLECT • RUSH!",
                15,
                LIGHT
        );

        subtitle.setGravity(Gravity.CENTER);

        root.addView(
                subtitle,
                matchParams(45)
        );

        // ---------------- SCORE CARD ----------------

        LinearLayout scoreCard = createCard();

        scoreCard.setGravity(Gravity.CENTER);

        scoreText = createText(
                "0 COINS",
                38,
                Color.YELLOW
        );

        scoreText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        scoreText.setGravity(Gravity.CENTER);

        scoreCard.addView(
                scoreText,
                matchParams(80)
        );

        bestScoreText = createText(
                "BEST: " + bestScore,
                14,
                LIGHT
        );

        bestScoreText.setGravity(Gravity.CENTER);

        scoreCard.addView(
                bestScoreText,
                matchParams(35)
        );

        root.addView(
                scoreCard,
                new LinearLayout.LayoutParams(
                        -1,
                        125
                )
        );

        addSpace(root, 10);

        // ---------------- TIMER ----------------

        timerText = createText(
                "⏱️  30",
                24,
                WHITE
        );

        timerText.setGravity(Gravity.CENTER);

        root.addView(
                timerText,
                matchParams(55)
        );

        // ---------------- MESSAGE ----------------

        messageText = createText(
                "Tap the coin as fast as you can!",
                16,
                LIGHT
        );

        messageText.setGravity(Gravity.CENTER);

        root.addView(
                messageText,
                matchParams(55)
        );

        // ---------------- COIN BUTTON ----------------

        coinButton = new Button(this);

        coinButton.setText("🪙\nTAP!");
        coinButton.setTextSize(27);
        coinButton.setTextColor(WHITE);
        coinButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        coinButton.setGravity(Gravity.CENTER);

        coinButton.setBackground(
                createRoundBackground(ORANGE, 100)
        );

        LinearLayout.LayoutParams coinParams =
                new LinearLayout.LayoutParams(
                        230,
                        230
                );

        coinParams.gravity = Gravity.CENTER;
        coinParams.setMargins(0, 5, 0, 5);

        root.addView(coinButton, coinParams);

        // ---------------- REWARD ----------------

        rewardButton = createButton(
                "🎁  WATCH AD • 2X COINS",
                GREEN
        );

        LinearLayout.LayoutParams rewardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        60
                );

        rewardParams.setMargins(
                15,
                8,
                15,
                8
        );

        root.addView(
                rewardButton,
                rewardParams
        );

        // ---------------- RESTART ----------------

        restartButton = createButton(
                "↪️  RESTART GAME",
                Color.rgb(75, 80, 95)
        );

        LinearLayout.LayoutParams restartParams =
                new LinearLayout.LayoutParams(
                        -1,
                        55
                );

        restartParams.setMargins(
                15,
                3,
                15,
                10
        );

        root.addView(
                restartButton,
                restartParams
        );

        // ---------------- ACTIONS ----------------

        coinButton.setOnClickListener(v -> {

            score++;

            updateScore();

            animateCoinButton();
        });

        rewardButton.setOnClickListener(v ->
                showRewardedAd()
        );

        restartButton.setOnClickListener(v ->
                startGame()
        );

        logoutButton.setOnClickListener(v -> {

            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            // Only remove login session.
            // Account remains saved.
            prefs.edit()
                    .remove(KEY_USERNAME)
                    .apply();

            interstitialAd = null;
            rewardedAd = null;

            showLoginScreen();
        });

        setContentView(root);

        startGame();
    }

    // ============================================================
    // START GAME
    // ============================================================

    private void startGame() {

        score = 0;

        updateScore();

        timerText.setText("⏱️  30");

        messageText.setText(
                "Tap the coin as fast as you can!"
        );

        coinButton.setVisibility(View.VISIBLE);
        coinButton.setEnabled(true);

        rewardButton.setVisibility(View.GONE);
        restartButton.setVisibility(View.GONE);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer =
                new CountDownTimer(30000, 1000) {

                    @Override
                    public void onTick(
                            long millisUntilFinished
                    ) {

                        long seconds =
                                (millisUntilFinished + 999) / 1000;

                        timerText.setText(
                                "⏱️  " + seconds
                        );
                    }

                    @Override
                    public void onFinish() {

                        timerText.setText("⏱️  0");

                        coinButton.setEnabled(false);

                        // IMPORTANT:
                        // Hide giant TAP button on Game Over
                        coinButton.setVisibility(View.GONE);

                        updateBestScore();

                        messageText.setText(
                                "🎉 GAME OVER!\n" +
                                "You collected " +
                                score +
                                " coins!"
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

    // ============================================================
    // SCORE
    // ============================================================

    private void updateScore() {

        if (scoreText != null) {
            scoreText.setText(
                    score + " COINS"
            );
        }
    }

    // ============================================================
    // BEST SCORE
    // ============================================================

    private void updateBestScore() {

        if (score > bestScore) {

            bestScore = score;

            prefs.edit()
                    .putInt(
                            KEY_BEST_SCORE,
                            bestScore
                    )
                    .apply();
        }

        if (bestScoreText != null) {

            bestScoreText.setText(
                    "BEST: " + bestScore
            );
        }
    }

    // ============================================================
    // COIN BUTTON ANIMATION
    // ============================================================

    private void animateCoinButton() {

        ScaleAnimation animation =
                new ScaleAnimation(
                        1.0f,
                        0.90f,
                        1.0f,
                        0.90f,
                        ScaleAnimation.RELATIVE_TO_SELF,
                        0.5f,
                        ScaleAnimation.RELATIVE_TO_SELF,
                        0.5f
                );

        animation.setDuration(70);

        animation.setRepeatCount(1);

        animation.setRepeatMode(
                ScaleAnimation.REVERSE
        );

        coinButton.startAnimation(animation);
    }

    // ============================================================
    // INTERSTITIAL AD
    // ============================================================

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
                            InterstitialAd ad
                    ) {

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
                                        }
                                );
                    }

                    @Override
                    public void onAdFailedToLoad(
                            LoadAdError adError
                    ) {

                        interstitialAd = null;
                    }
                }
        );
    }

    // ============================================================
    // SHOW INTERSTITIAL
    // ============================================================

    private void showInterstitialAd() {

        if (interstitialAd != null) {

            interstitialAd.show(this);

        } else {

            loadInterstitialAd();
        }
    }

    // ============================================================
    // REWARDED AD
    // ============================================================

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
                            RewardedAd ad
                    ) {

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
                                        }
                                );
                    }

                    @Override
                    public void onAdFailedToLoad(
                            LoadAdError adError
                    ) {

                        rewardedAd = null;
                    }
                }
        );
    }

    // ============================================================
    // SHOW REWARDED AD
    // ============================================================

    private void showRewardedAd() {

        if (rewardedAd != null) {

            rewardedAd.show(
                    this,
                    rewardItem -> {

                        score = score * 2;

                        updateScore();

                        messageText.setText(
                                "🎉 REWARD!\n" +
                                "Coins doubled!"
                        );

                        Toast.makeText(
                                MainActivity.this,
                                "Coins 2X! 🎉",
                                Toast.LENGTH_SHORT
                        ).show();

                        rewardedAd = null;

                        loadRewardedAd();
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

    // ============================================================
    // UI HELPERS
    // ============================================================

    private LinearLayout createRootLayout() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(Gravity.CENTER_HORIZONTAL);

        root.setPadding(
                20,
                15,
                20,
                15
        );

        root.setBackgroundColor(
                BG_COLOR
        );

        return root;
    }

    private LinearLayout createCard() {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        card.setPadding(
                20,
                15,
                20,
                15
        );

        card.setBackground(
                createRoundBackground(
                        CARD_COLOR,
                        25
                )
        );

        return card;
    }

    private TextView createTitle(
            String text
    ) {

        TextView view =
                createText(
                        text,
                        29,
                        WHITE
                );

        view.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        view.setGravity(
                Gravity.CENTER
        );

        return view;
    }

    private TextView createText(
            String text,
            float size,
            int color
    ) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER_VERTICAL);

        return view;
    }

    private EditText createInput(
            String hint
    ) {

        EditText input =
                new EditText(this);

        input.setHint(hint);
        input.setHintTextColor(
                Color.rgb(150, 155, 170)
        );

        input.setTextColor(WHITE);

        input.setTextSize(16);

        input.setSingleLine(true);

        input.setPadding(
                20,
                0,
                20,
                0
        );

        input.setBackground(
                createRoundBackground(
                        Color.rgb(40, 45, 60),
                        18
                )
        );

        return input;
    }

    private Button createButton(
            String text,
            int backgroundColor
    ) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextSize(16);

        button.setTextColor(WHITE);

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setAllCaps(false);

        button.setGravity(Gravity.CENTER);

        button.setBackground(
                createRoundBackground(
                        backgroundColor,
                        20
                )
        );

        return button;
    }

    private Button createSmallButton(
            String text
    ) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextSize(12);
        button.setTextColor(WHITE);
        button.setAllCaps(false);

        button.setBackground(
                createRoundBackground(
                        Color.rgb(70, 75, 90),
                        18
                )
        );

        return button;
    }

    private GradientDrawable createRoundBackground(
            int color,
            int radius
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);

        drawable.setCornerRadius(radius);

        return drawable;
    }

    private LinearLayout.LayoutParams matchParams(
            int height
    ) {

        return new LinearLayout.LayoutParams(
                -1,
                height
        );
    }

    private void addSpace(
            LinearLayout layout,
            int height
    ) {

        View space =
                new View(this);

        layout.addView(
                space,
                new LinearLayout.LayoutParams(
                        1,
                        height
                )
        );
    }

    // ============================================================
    // PASSWORD HASH
    // ============================================================

    private String hashPassword(
            String password
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            password.getBytes(
                                    "UTF-8"
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

    // ============================================================
    // DESTROY
    // ============================================================

    @Override
    protected void onDestroy() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        super.onDestroy();
    }
}
