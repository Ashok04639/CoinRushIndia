package com.coinrushindia.prototype;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private android.content.SharedPreferences prefs;

    private String username = "";

    private int coins = 0;
    private int bestScore = 0;
    private int totalCoins = 0;

    private boolean gameRunning = false;

    private CountDownTimer timer;

    private TextView scoreText;
    private TextView bestText;
    private TextView totalText;
    private TextView timerText;
    private TextView messageText;

    private Button tapButton;
    private Button rewardButton;
    private Button restartButton;
    private Button logoutButton;
    private Button balanceButton;

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    private static final String INTERSTITIAL_AD_ID =
            "ca-app-pub-4590159013838755/9228973931";

    private static final String REWARDED_AD_ID =
            "ca-app-pub-4590159013838755/5227139421";

    private final int BG = Color.rgb(12, 18, 28);
    private final int CARD = Color.rgb(25, 34, 48);
    private final int WHITE = Color.WHITE;
    private final int GREEN = Color.rgb(46, 190, 100);
    private final int ORANGE = Color.rgb(255, 145, 45);
    private final int RED = Color.rgb(230, 70, 70);
    private final int GRAY = Color.rgb(150, 160, 175);
    private final int YELLOW = Color.rgb(255, 215, 60);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, initializationStatus -> {});

        prefs = getSharedPreferences(
                "CoinRushIndia",
                MODE_PRIVATE
        );

        username = prefs.getString("username", "");
        bestScore = prefs.getInt("bestScore", 0);
        totalCoins = prefs.getInt("totalCoins", 0);

        if (prefs.getBoolean("loggedIn", false)
                && !username.isEmpty()) {

            showGameScreen();

        } else {

            showLoginScreen();
        }
    }

    // =========================
    // INTERSTITIAL AD
    // =========================

    private void loadInterstitialAd() {

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

                                                interstitialAd = null;
                                                loadInterstitialAd();
                                            }

                                            @Override
                                            public void onAdFailedToShowFullScreenContent(
                                                    AdError error) {

                                                interstitialAd = null;
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
    }

    private void showInterstitialAd() {

        if (interstitialAd != null) {

            interstitialAd.show(this);

            interstitialAd = null;
        }
    }

    // =========================
    // REWARDED AD
    // =========================

    private void loadRewardedAd() {

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

                                                rewardedAd = null;
                                                loadRewardedAd();
                                            }

                                            @Override
                                            public void onAdFailedToShowFullScreenContent(
                                                    AdError error) {

                                                rewardedAd = null;
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
    }

    private void showRewardedAd() {

        if (rewardedAd == null) {

            Toast.makeText(
                    this,
                    "Ad abhi ready nahi hai.",
                    Toast.LENGTH_SHORT
            ).show();

            loadRewardedAd();
            return;
        }

        RewardedAd ad = rewardedAd;

        rewardedAd = null;

        ad.show(
                this,
                rewardItem -> giveDoubleCoins()
        );
    }

    // =========================
    // LOGIN
    // =========================

    private void showLoginScreen() {

        stopTimer();

        LinearLayout root = createRoot();

        TextView title = createText(
                "🇮🇳 Coin Rush India",
                30,
                WHITE,
                true
        );

        root.addView(title);

        TextView subtitle = createText(
                "LOGIN TO PLAY",
                15,
                GRAY,
                true
        );

        subtitle.setLayoutParams(
                marginParams(0, 10, 0, 25)
        );

        root.addView(subtitle);

        EditText usernameInput =
                createInput("Username");

        root.addView(usernameInput);

        EditText passwordInput =
                createInput("Password");

        passwordInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType
                        .TYPE_TEXT_VARIATION_PASSWORD
        );

        root.addView(passwordInput);

        Button loginButton =
                createButton("LOGIN", GREEN);

        root.addView(loginButton);

        Button signupButton =
                createButton(
                        "CREATE NEW ACCOUNT",
                        ORANGE
                );

        root.addView(signupButton);

        loginButton.setOnClickListener(v -> {

            String user =
                    usernameInput.getText()
                            .toString()
                            .trim();

            String pass =
                    passwordInput.getText()
                            .toString();

            if (user.isEmpty() || pass.isEmpty()) {

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

            if (savedUser.equals(user)
                    && savedPassword.equals(
                    hashPassword(pass)
            )) {

                prefs.edit()
                        .putBoolean(
                                "loggedIn",
                                true
                        )
                        .apply();

                username = user;

                bestScore =
                        prefs.getInt(
                                "bestScore",
                                0
                        );

                totalCoins =
                        prefs.getInt(
                                "totalCoins",
                                0
                        );

                showGameScreen();

            } else {

                Toast.makeText(
                        this,
                        "Username ya password galat hai",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        signupButton.setOnClickListener(
                v -> showSignupScreen()
        );

        setContentView(wrapScroll(root));
    }

    // =========================
    // SIGNUP
    // =========================

    private void showSignupScreen() {

        stopTimer();

        LinearLayout root = createRoot();

        TextView title = createText(
                "🇮🇳 Coin Rush India",
                30,
                WHITE,
                true
        );

        root.addView(title);

        TextView subtitle = createText(
                "CREATE YOUR ACCOUNT",
                15,
                GRAY,
                true
        );

        subtitle.setLayoutParams(
                marginParams(0, 10, 0, 25)
        );

        root.addView(subtitle);

        EditText usernameInput =
                createInput("Choose Username");

        root.addView(usernameInput);

        EditText passwordInput =
                createInput("Choose Password");

        passwordInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType
                        .TYPE_TEXT_VARIATION_PASSWORD
        );

        root.addView(passwordInput);

        EditText confirmInput =
                createInput("Confirm Password");

        confirmInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType
                        .TYPE_TEXT_VARIATION_PASSWORD
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

        createButton.setOnClickListener(v -> {

            String user =
                    usernameInput.getText()
                            .toString()
                            .trim();

            String pass =
                    passwordInput.getText()
                            .toString();

            String confirm =
                    confirmInput.getText()
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

            if (!prefs.getString(
                    "username",
                    ""
            ).isEmpty()) {

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

            showGameScreen();
        });

        backButton.setOnClickListener(
                v -> showLoginScreen()
        );

        setContentView(wrapScroll(root));
    }

    // =========================
    // GAME SCREEN
    // =========================

    private void showGameScreen() {

        stopTimer();

        totalCoins =
                prefs.getInt(
                        "totalCoins",
                        0
                );

        bestScore =
                prefs.getInt(
                        "bestScore",
                        0
                );

        LinearLayout root = createRoot();

        LinearLayout topRow =
                new LinearLayout(this);

        topRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title = createText(
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

        topRow.addView(
                logoutButton,
                new LinearLayout.LayoutParams(
                        dp(100),
                        dp(50)
                )
        );

        root.addView(topRow);

        TextView playerText =
                createText(
                        "Player: " + username,
                        16,
                        GRAY,
                        false
                );

        playerText.setLayoutParams(
                marginParams(0, 5, 0, 15)
        );

        root.addView(playerText);

        // =========================
        // BALANCE
        // =========================

        balanceButton =
                createButton(
                        "💰 BALANCE: "
                                + totalCoins
                                + " COINS",
                        GREEN
                );

        root.addView(balanceButton);

        balanceButton.setOnClickListener(v -> {

            totalCoins =
                    prefs.getInt(
                            "totalCoins",
                            0
                    );

            balanceButton.setText(
                    "💰 BALANCE: "
                            + totalCoins
                            + " COINS"
            );

            Toast.makeText(
                    this,
                    "💰 Your Balance: "
                            + totalCoins
                            + " coins",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // =========================
        // DAILY BONUS
        // =========================

        Button dailyBonusButton =
                createButton(
                        "🎁 DAILY BONUS +100 COINS",
                        ORANGE
                );

        root.addView(dailyBonusButton);

        String today =
                new SimpleDateFormat(
                        "yyyyMMdd",
                        Locale.getDefault()
                ).format(new Date());

        String lastBonusDate =
                prefs.getString(
                        "lastDailyBonus",
                        ""
                );

        if (today.equals(lastBonusDate)) {

            dailyBonusButton.setText(
                    "✅ DAILY BONUS CLAIMED"
            );

            dailyBonusButton.setEnabled(false);
        }

        dailyBonusButton.setOnClickListener(v -> {

            String currentDate =
                    new SimpleDateFormat(
                            "yyyyMMdd",
                            Locale.getDefault()
                    ).format(new Date());

            String savedDate =
                    prefs.getString(
                            "lastDailyBonus",
                            ""
                    );

            if (currentDate.equals(savedDate)) {

                Toast.makeText(
                        this,
                        "Aaj ka bonus already claim ho chuka hai.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            totalCoins += 100;

            prefs.edit()
                    .putInt(
                            "totalCoins",
                            totalCoins
                    )
                    .putString(
                            "lastDailyBonus",
                            currentDate
                    )
                    .apply();

            addHistory(
                    "🎁 Daily Bonus: +100 coins"
            );

            balanceButton.setText(
                    "💰 BALANCE: "
                            + totalCoins
                            + " COINS"
            );

            if (totalText != null) {

                totalText.setText(
                        "TOTAL COINS: "
                                + totalCoins
                );
            }

            dailyBonusButton.setText(
                    "✅ DAILY BONUS CLAIMED"
            );

            dailyBonusButton.setEnabled(false);

            Toast.makeText(
                    this,
                    "🎁 +100 coins bonus!",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // =========================
        // HISTORY
        // =========================

        Button historyButton =
                createButton(
                        "📊 COIN HISTORY",
                        Color.rgb(80, 90, 105)
                );

        root.addView(historyButton);

        historyButton.setOnClickListener(
                v -> showCoinHistory()
        );

        // =========================
        // SCORE CARD
        // =========================

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

        scoreCard.setBackgroundColor(CARD);

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
                        "TOTAL COINS: "
                                + totalCoins,
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

        // =========================
        // TIMER
        // =========================

        timerText =
                createText(
                        "⏱ 30",
                        28,
                        WHITE,
                        true
                );

        timerText.setLayoutParams(
                marginParams(0, 18, 0, 5)
        );

        root.addView(timerText);

        messageText =
                createText(
                        "TAP • COLLECT • RUSH!",
                        17,
                        GRAY,
                        true
                );

        root.addView(messageText);

        // =========================
        // ROUND TAP BUTTON
        // =========================

        tapButton =
                createRoundTapButton();

        root.addView(
                tapButton
        );

        tapButton.setOnClickListener(v -> {

            if (!gameRunning) {
                return;
            }

            coins++;

            updateScore();
        });

        // =========================
        // REWARDED BUTTON
        // =========================

        rewardButton =
                createButton(
                        "🎁 WATCH AD • 2X COINS",
                        GREEN
                );

        rewardButton.setVisibility(
                View.GONE
        );

        root.addView(rewardButton);

        rewardButton.setOnClickListener(
                v -> showRewardedAd()
        );

        // =========================
        // RESTART
        // =========================

        restartButton =
                createButton(
                        "RESTART GAME",
                        Color.rgb(80, 90, 105)
                );

        restartButton.setVisibility(
                View.GONE
        );

        root.addView(restartButton);

        restartButton.setOnClickListener(
                v -> startGame()
        );

        // =========================
        // LOGOUT
        // =========================

        logoutButton.setOnClickListener(v -> {

            stopTimer();

            prefs.edit()
                    .putBoolean(
                            "loggedIn",
                            false
                    )
                    .apply();

            username = "";

            showLoginScreen();
        });

        setContentView(
                wrapScroll(root)
        );

        startGame();

        getWindow()
                .getDecorView()
                .postDelayed(() -> {

                    loadInterstitialAd();
                    loadRewardedAd();

                }, 2000);
    }

    // =========================
    // ROUND TAP BUTTON
    // =========================

    private Button createRoundTapButton() {

        Button button =
                new Button(this);

        button.setText("🪙\nTAP!");
        button.setTextSize(26);
        button.setTextColor(WHITE);

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setGravity(
                Gravity.CENTER
        );

        button.setAllCaps(false);

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(ORANGE);

        background.setShape(
                GradientDrawable.OVAL
        );

        button.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        dp(230),
                        dp(230)
                );

        params.gravity = Gravity.CENTER;

        params.setMargins(
                0,
                dp(15),
                0,
                dp(15)
        );

        button.setLayoutParams(params);

        return button;
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

            tapButton.setEnabled(true);
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

                        int seconds =
                                (int) Math.ceil(
                                        millisUntilFinished
                                                / 1000.0
                                );

                        if (timerText != null) {

                            timerText.setText(
                                    "⏱ "
                                            + seconds
                            );
                        }
                    }

                    @Override
                    public void onFinish() {

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

            tapButton.setEnabled(false);

            tapButton.setVisibility(
                    View.GONE
            );
        }

        if (coins > bestScore) {

            bestScore = coins;

            prefs.edit()
                    .putInt(
                            "bestScore",
                            bestScore
                    )
                    .apply();
        }

        totalCoins += coins;

        prefs.edit()
                .putInt(
                        "totalCoins",
                        totalCoins
                )
                .apply();

        if (coins > 0) {

            addHistory(
                    "🎮 Game: +" + coins + " coins"
            );
        }

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

        if (balanceButton != null) {

            balanceButton.setText(
                    "💰 BALANCE: "
                            + totalCoins
                            + " COINS"
            );
        }

        if (messageText != null) {

            messageText.setText(
                    "🎉 GAME OVER • "
                            + coins
                            + " COINS!"
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

        showInterstitialAd();
    }

    // =========================
    // DOUBLE COINS
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

        int bonus = coins;

        coins = coins * 2;

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

        addHistory(
                "🎁 Reward Ad: +" + bonus + " bonus coins"
        );

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

        if (balanceButton != null) {

            balanceButton.setText(
                    "💰 BALANCE: "
                            + totalCoins
                            + " COINS"
            );
        }

        if (messageText != null) {

            messageText.setText(
                    "🎁 REWARD! Coins doubled!"
            );
        }

        Toast.makeText(
                this,
                "+" + bonus
                        + " bonus coins!",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================
    // COIN HISTORY SAVE
    // =========================

    private void addHistory(String entry) {

        String oldHistory =
                prefs.getString(
                        "coinHistory",
                        ""
                );

        String time =
                new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                ).format(new Date());

        String newEntry =
                "• " + entry
                        + " — "
                        + time;

        String newHistory;

        if (oldHistory.isEmpty()) {

            newHistory = newEntry;

        } else {

            newHistory =
                    newEntry
                            + "\n"
                            + oldHistory;
        }

        prefs.edit()
                .putString(
                        "coinHistory",
                        newHistory
                )
                .apply();
    }

    // =========================
    // HISTORY SCREEN
    // =========================

    private void showCoinHistory() {

        String history =
                prefs.getString(
                        "coinHistory",
                        ""
                );

        LinearLayout root =
                createRoot();

        TextView title =
                createText(
                        "📊 COIN HISTORY",
                        28,
                        WHITE,
                        true
                );

        root.addView(title);

        if (history.isEmpty()) {

            TextView empty =
                    createText(
                            "Abhi koi coin history nahi hai.",
                            17,
                            GRAY,
                            false
                    );

            empty.setPadding(
                    dp(10),
                    dp(30),
                    dp(10),
                    dp(30)
            );

            root.addView(empty);

        } else {

            TextView historyText =
                    createText(
                            history,
                            17,
                            WHITE,
                            false
                    );

            historyText.setGravity(
                    Gravity.START
            );

            historyText.setPadding(
                    dp(15),
                    dp(15),
                    dp(15),
                    dp(15)
            );

            root.addView(
                    historyText
            );
        }

        Button backButton =
                createButton(
                        "← BACK TO GAME",
                        GRAY
                );

        root.addView(backButton);

        backButton.setOnClickListener(
                v -> showGameScreen()
        );

        setContentView(
                wrapScroll(root)
        );
    }

    // =========================
    // SCORE
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

        root.setBackgroundColor(BG);

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
        input.setHintTextColor(GRAY);
        input.setTextColor(WHITE);
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

        input.setLayoutParams(params);

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
        button.setTextColor(WHITE);

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

        button.setLayoutParams(params);

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

        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

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
