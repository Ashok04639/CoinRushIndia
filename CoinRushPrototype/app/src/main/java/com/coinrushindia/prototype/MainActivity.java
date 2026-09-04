package com.coinrushindia.prototype;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

    private static final String API_BASE_URL =
            "https://coinrushindia.onrender.com/api/v1";

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private boolean serverSyncReady = false;

    // Prevent an older startup/login sync from overwriting a newer coin update.
    private long syncRequestVersion = 0L;
    private long balanceUpdateVersion = 0L;

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
    private final int BLUE = Color.rgb(70, 120, 220);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);

        MobileAds.initialize(this, initializationStatus -> {});

        prefs = getSharedPreferences(
                "CoinRushIndia",
                MODE_PRIVATE
        );

        username = prefs.getString("username", "");
        bestScore = prefs.getInt("bestScore", 0);
        totalCoins = prefs.getInt("totalCoins", 0);

        String savedPassword = prefs.getString("password", "");
        boolean explicitlyLoggedOut = prefs.getBoolean("explicitLogout", false);

        // Keep the player logged in across normal app restarts.
        // Only the LOGOUT button should force the login screen.
        if (!username.isEmpty()
                && !savedPassword.isEmpty()
                && !explicitlyLoggedOut) {

            prefs.edit().putBoolean("loggedIn", true).apply();
            showGameScreen();
            syncUserWithServer();

        } else {

            showLoginScreen();
        }
    }

    // =========================================================
    // INTERSTITIAL AD
    // =========================================================

    private void loadInterstitialAd() {

        AdRequest request =
                new AdRequest.Builder().build();

        InterstitialAd.load(
                this,
                INTERSTITIAL_AD_ID,
                request,
                new InterstitialAdLoadCallback() {

                    @Override
                    public void onAdLoaded(InterstitialAd ad) {

                        interstitialAd = ad;

                        interstitialAd.setFullScreenContentCallback(
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

    // =========================================================
    // REWARDED AD
    // =========================================================

    private void loadRewardedAd() {

        AdRequest request =
                new AdRequest.Builder().build();

        RewardedAd.load(
                this,
                REWARDED_AD_ID,
                request,
                new RewardedAdLoadCallback() {

                    @Override
                    public void onAdLoaded(RewardedAd ad) {

                        rewardedAd = ad;

                        rewardedAd.setFullScreenContentCallback(
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

    // =========================================================
    // LOGIN
    // =========================================================

    private void showLoginScreen() {

        stopTimer();

        LinearLayout root = createRoot();

        TextView title = createText(
                "COIN RUSH INDIA",
                30,
                WHITE,
                true
        );

        root.addView(title);

        TextView brandLine = createText(
                "TAP • COLLECT • RUSH",
                12,
                ORANGE,
                true
        );
        brandLine.setLetterSpacing(0.08f);
        root.addView(brandLine);

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

        Button resetButton = createButton(
                "RESET PASSWORD",
                Color.rgb(95, 105, 125)
        );
        root.addView(resetButton);

        resetButton.setOnClickListener(v -> showResetPasswordDialog());

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

            String hashedInput = hashPassword(pass);

            // Support both the current SHA-256 password format and any
            // older local account that may have stored the password directly.
            boolean passwordMatches =
                    savedPassword.equals(hashedInput)
                            || savedPassword.equals(pass);

            if (savedUser.equals(user) && passwordMatches) {

                // Upgrade an older/plain local password to SHA-256 immediately.
                if (!savedPassword.equals(hashedInput)) {
                    prefs.edit()
                            .putString("password", hashedInput)
                            .apply();
                }

                prefs.edit()
                        .putBoolean("loggedIn", true)
                        .putBoolean("explicitLogout", false)
                        .apply();

                username = user;

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

                showGameScreen();
                syncUserWithServer();

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

    private void showResetPasswordDialog() {
        final EditText newPassword = createInput("New Password");
        newPassword.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        final EditText confirmPassword = createInput("Confirm New Password");
        confirmPassword.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(5), dp(20), 0);
        box.addView(newPassword);
        box.addView(confirmPassword);

        new android.app.AlertDialog.Builder(this)
                .setTitle("RESET PASSWORD")
                .setMessage("Set a new password for your saved account.")
                .setView(box)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("SAVE", (dialog, which) -> {
                    String user = prefs.getString("username", "");
                    String pass = newPassword.getText().toString();
                    String confirm = confirmPassword.getText().toString();

                    if (user.isEmpty()) {
                        Toast.makeText(this, "Pehle account create karo.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (pass.length() < 4 || !pass.equals(confirm)) {
                        Toast.makeText(this, "Password minimum 4 characters aur same hona chahiye.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    prefs.edit()
                            .putString("password", hashPassword(pass))
                            .putBoolean("loggedIn", true)
                            .putBoolean("explicitLogout", false)
                            .apply();

                    username = user;
                    showGameScreen();
                    syncUserWithServer();
                    Toast.makeText(this, "Password reset ho gaya.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    // =========================================================
    // SIGNUP
    // =========================================================

    private void showSignupScreen() {

        stopTimer();

        LinearLayout root = createRoot();

        TextView title = createText(
                "COIN RUSH INDIA",
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
                    .putBoolean(
                            "explicitLogout",
                            false
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
            syncUserWithServer();
        });

        backButton.setOnClickListener(
                v -> showLoginScreen()
        );

        setContentView(wrapScroll(root));
    }

    // =========================================================
    // MAIN GAME SCREEN
    // =========================================================

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

        // TOP ROW
        LinearLayout topRow =
                new LinearLayout(this);

        topRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title = createText(
                "COIN RUSH INDIA",
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

        TextView brandLine = createText(
                "TAP • COLLECT • RUSH",
                11,
                ORANGE,
                true
        );
        brandLine.setLetterSpacing(0.08f);
        root.addView(brandLine);

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

        // BALANCE
        balanceButton =
                createButton(
                        "BALANCE: " + totalCoins + " COINS",
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
                    "BALANCE: " + totalCoins + " COINS"
            );

            Toast.makeText(
                    this,
                    "Your Balance: "
                            + totalCoins
                            + " coins",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // DAILY BONUS
        Button dailyBonusButton =
                createButton(
                        "DAILY BONUS +100 COINS",
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
                    "DAILY BONUS CLAIMED"
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

            dailyBonusButton.setEnabled(false);
            dailyBonusButton.setText("SAVING BONUS...");

            addCoinsToServer(100, "DAILY BONUS", (success, newBalance) -> {
                if (!success) {
                    dailyBonusButton.setEnabled(true);
                    dailyBonusButton.setText("DAILY BONUS +100 COINS");
                    Toast.makeText(
                            MainActivity.this,
                            "Bonus save nahi hua. Internet check karo.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                totalCoins = newBalance;

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

                updateBalanceUI();

                addCoinHistory(
                        "+100 DAILY BONUS"
                );

                dailyBonusButton.setText(
                        "DAILY BONUS CLAIMED"
                );

                dailyBonusButton.setEnabled(false);

                Toast.makeText(
                        MainActivity.this,
                        "+100 coins bonus!",
                        Toast.LENGTH_SHORT
                ).show();
            });
        });

        // HISTORY
        Button historyButton =
                createButton(
                        "COIN HISTORY",
                        Color.rgb(80, 90, 105)
                );

        root.addView(historyButton);

        historyButton.setOnClickListener(
                v -> showCoinHistory()
        );

        // WITHDRAWAL
        Button withdrawButton =
                createButton(
                        "WITHDRAW BEP-20",
                        BLUE
                );

        root.addView(withdrawButton);

        withdrawButton.setOnClickListener(
                v -> showWithdrawalScreen()
        );

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
                        "TOTAL COINS: " + totalCoins,
                        15,
                        GRAY,
                        false
                );

        root.addView(
                scoreCard,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(112)
                )
        );

        // TIMER
        timerText =
                createText(
                        "TIME: 30",
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
                        "TAP - COLLECT - RUSH!",
                        17,
                        GRAY,
                        true
                );

        root.addView(messageText);

        // ROUND TAP BUTTON
        tapButton =
                createRoundButton(
                        "TAP!",
                        ORANGE
                );

        LinearLayout.LayoutParams tapParams =
                new LinearLayout.LayoutParams(
                        dp(235),
                        dp(235)
                );

        tapParams.gravity =
                Gravity.CENTER;

        tapParams.setMargins(
                0,
                dp(10),
                0,
                dp(10)
        );

        root.addView(
                tapButton,
                tapParams
        );

        tapButton.setOnClickListener(v -> {

            if (!gameRunning) {
                return;
            }

            coins++;

            updateScore();
        });

        // REWARDED BUTTON
        rewardButton =
                createButton(
                        "WATCH AD - 2X COINS",
                        GREEN
                );

        rewardButton.setVisibility(
                View.GONE
        );

        root.addView(rewardButton);

        rewardButton.setOnClickListener(
                v -> showRewardedAd()
        );

        // RESTART
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

        // LOGOUT
        logoutButton.setOnClickListener(v -> {

            stopTimer();

            prefs.edit()
                    .putBoolean("loggedIn", false)
                    .putBoolean("explicitLogout", true)
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

    // =========================================================
    // GAME
    // =========================================================

    private void startGame() {

        stopTimer();

        coins = 0;

        gameRunning = true;

        if (scoreText != null) {
            scoreText.setText("0 COINS");
        }

        if (timerText != null) {
            timerText.setText("TIME: 30");
        }

        if (messageText != null) {
            messageText.setText(
                    "TAP - COLLECT - RUSH!"
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
                                    "TIME: "
                                            + seconds
                            );
                        }
                    }

                    @Override
                    public void onFinish() {

                        gameRunning = false;

                        if (timerText != null) {

                            timerText.setText(
                                    "TIME: 0"
                            );
                        }

                        gameOver();
                    }
                };

        timer.start();
    }

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

        if (coins > 0) {
            final int roundCoins = coins;

            addCoinsToServer(roundCoins, "GAME REWARD", (success, newBalance) -> {
                if (!success) {
                    Toast.makeText(
                            MainActivity.this,
                            "Game reward save nahi hua. Internet check karo.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                totalCoins = newBalance;

                prefs.edit()
                        .putInt(
                                "totalCoins",
                                totalCoins
                        )
                        .apply();

                addCoinHistory(
                        "+" + roundCoins + " GAME REWARD"
                );

                updateBalanceUI();

                if (totalText != null) {
                    totalText.setText(
                            "TOTAL COINS: " + totalCoins
                    );
                }
            });
        }

        updateScore();
        updateBalanceUI();

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
                    "GAME OVER - "
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

    // =========================================================
    // DOUBLE COINS
    // =========================================================

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

        if (coins > bestScore) {
            bestScore = coins;
        }

        prefs.edit()
                .putInt(
                        "bestScore",
                        bestScore
                )
                .apply();

        addCoinsToServer(bonus, "AD REWARD", (success, newBalance) -> {
            if (!success) {
                Toast.makeText(
                        MainActivity.this,
                        "Ad reward save nahi hua. Internet check karo.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            totalCoins = newBalance;

            prefs.edit()
                    .putInt(
                            "totalCoins",
                            totalCoins
                    )
                    .apply();

            addCoinHistory(
                    "+" + bonus + " AD REWARD"
            );

            updateScore();
            updateBalanceUI();

            if (bestText != null) {
                bestText.setText(
                        "BEST: " + bestScore
                );
            }

            if (totalText != null) {
                totalText.setText(
                        "TOTAL COINS: " + totalCoins
                );
            }

            if (messageText != null) {
                messageText.setText(
                        "REWARD! Coins doubled!"
                );
            }

            Toast.makeText(
                    MainActivity.this,
                    "+" + bonus + " bonus coins!",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // =========================================================
    // COIN HISTORY
    // =========================================================

    private void addCoinHistory(String entry) {

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
                time
                        + " - "
                        + entry;

        if (oldHistory.isEmpty()) {

            oldHistory = newEntry;

        } else {

            oldHistory =
                    newEntry
                            + "\n"
                            + oldHistory;
        }

        prefs.edit()
                .putString(
                        "coinHistory",
                        oldHistory
                )
                .apply();
    }

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
                        "COIN HISTORY",
                        28,
                        WHITE,
                        true
                );

        root.addView(title);

        if (history.isEmpty()) {

            TextView empty =
                    createText(
                            "No coin history yet.",
                            17,
                            GRAY,
                            false
                    );

            root.addView(empty);

        } else {

            TextView historyText =
                    createText(
                            history,
                            16,
                            WHITE,
                            false
                    );

            historyText.setGravity(
                    Gravity.START
            );

            historyText.setPadding(
                    dp(10),
                    dp(15),
                    dp(10),
                    dp(15)
            );

            root.addView(historyText);
        }

        Button backButton =
                createButton(
                        "BACK TO GAME",
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

    // =========================================================
    // BEP-20 WITHDRAWAL
    // =========================================================

    private void showWithdrawalScreen() {

        stopTimer();

        LinearLayout root =
                createRoot();

        TextView title =
                createText(
                        "BEP-20 WITHDRAWAL",
                        28,
                        WHITE,
                        true
                );

        root.addView(title);

        TextView balance =
                createText(
                        "AVAILABLE: "
                                + totalCoins
                                + " COINS",
                        18,
                        YELLOW,
                        true
                );

        balance.setLayoutParams(
                marginParams(
                        0,
                        15,
                        0,
                        20
                )
        );

        root.addView(balance);

        TextView info =
                createText(
                        "Enter your BEP-20 wallet address.\n"
                                + "UPI is not used.",
                        15,
                        GRAY,
                        false
                );

        root.addView(info);

        EditText walletInput =
                createInput(
                        "BEP-20 Wallet Address (0x...)"
                );

        root.addView(walletInput);

        EditText amountInput =
                createInput(
                        "Withdrawal Coins"
                );

        amountInput.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );

        root.addView(amountInput);

        TextView minimum =
                createText(
                        "Minimum withdrawal: 100 coins",
                        14,
                        GRAY,
                        false
                );

        minimum.setLayoutParams(
                marginParams(
                        0,
                        5,
                        0,
                        15
                )
        );

        root.addView(minimum);

        Button withdrawButton =
                createButton(
                        "SUBMIT WITHDRAWAL",
                        GREEN
                );

        root.addView(withdrawButton);

        Button withdrawalHistoryButton =
                createButton(
                        "WITHDRAWAL HISTORY",
                        BLUE
                );

        root.addView(
                withdrawalHistoryButton
        );

        Button backButton =
                createButton(
                        "BACK TO GAME",
                        GRAY
                );

        root.addView(backButton);

        withdrawButton.setOnClickListener(v -> {

            String wallet =
                    walletInput.getText()
                            .toString()
                            .trim();

            String amountText =
                    amountInput.getText()
                            .toString()
                            .trim();

            if (wallet.isEmpty()
                    || amountText.isEmpty()) {

                Toast.makeText(
                        this,
                        "Wallet address aur amount bharo.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (!isValidBep20Address(wallet)) {

                Toast.makeText(
                        this,
                        "Valid BEP-20 address enter karo.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            int amount;

            try {

                amount =
                        Integer.parseInt(
                                amountText
                        );

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "Amount galat hai.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (amount < 100) {

                Toast.makeText(
                        this,
                        "Minimum withdrawal 100 coins hai.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            totalCoins =
                    prefs.getInt(
                            "totalCoins",
                            0
                    );

            if (amount > totalCoins) {

                Toast.makeText(
                        this,
                        "Balance kam hai.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Coins reserve/deduct
            totalCoins -= amount;

            prefs.edit()
                    .putInt(
                            "totalCoins",
                            totalCoins
                    )
                    .apply();

            String time =
                    new SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                    ).format(new Date());

            String request =
                    time
                            + " | "
                            + amount
                            + " coins"
                            + " | "
                            + wallet
                            + " | PENDING";

            addWithdrawalHistory(request);

            addCoinHistory(
                    "-" + amount
                            + " WITHDRAWAL REQUEST"
            );

            Toast.makeText(
                    this,
                    "Withdrawal request submitted.",
                    Toast.LENGTH_LONG
            ).show();

            showWithdrawalScreen();
        });

        withdrawalHistoryButton.setOnClickListener(
                v -> showWithdrawalHistory()
        );

        backButton.setOnClickListener(
                v -> showGameScreen()
        );

        setContentView(
                wrapScroll(root)
        );
    }

    private boolean isValidBep20Address(
            String address) {

        if (address.length() != 42) {
            return false;
        }

        if (!address.startsWith("0x")
                && !address.startsWith("0X")) {

            return false;
        }

        for (int i = 2;
             i < address.length();
             i++) {

            char c =
                    address.charAt(i);

            boolean valid =
                    (c >= '0' && c <= '9')
                            || (c >= 'a' && c <= 'f')
                            || (c >= 'A' && c <= 'F');

            if (!valid) {
                return false;
            }
        }

        return true;
    }

    private void addWithdrawalHistory(
            String entry) {

        String old =
                prefs.getString(
                        "withdrawalHistory",
                        ""
                );

        if (old.isEmpty()) {

            old = entry;

        } else {

            old =
                    entry
                            + "\n"
                            + old;
        }

        prefs.edit()
                .putString(
                        "withdrawalHistory",
                        old
                )
                .apply();
    }

    private void showWithdrawalHistory() {

        LinearLayout root =
                createRoot();

        TextView title =
                createText(
                        "WITHDRAWAL HISTORY",
                        28,
                        WHITE,
                        true
                );

        root.addView(title);

        String history =
                prefs.getString(
                        "withdrawalHistory",
                        ""
                );

        if (history.isEmpty()) {

            TextView empty =
                    createText(
                            "No withdrawal requests yet.",
                            17,
                            GRAY,
                            false
                    );

            root.addView(empty);

        } else {

            TextView historyText =
                    createText(
                            history,
                            15,
                            WHITE,
                            false
                    );

            historyText.setGravity(
                    Gravity.START
            );

            historyText.setPadding(
                    dp(8),
                    dp(15),
                    dp(8),
                    dp(15)
            );

            root.addView(historyText);
        }

        Button backButton =
                createButton(
                        "BACK TO WITHDRAWAL",
                        GRAY
                );

        root.addView(backButton);

        backButton.setOnClickListener(
                v -> showWithdrawalScreen()
        );

        setContentView(
                wrapScroll(root)
        );
    }

    // =========================================================
    // BACKEND SYNC
    // =========================================================

    private String getDeviceIdValue() {
        String id = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (id == null || id.trim().length() < 6) {
            String saved = prefs.getString("stableDeviceId", "");
            if (!saved.isEmpty()) {
                return saved;
            }

            String generated = "android-" +
                    java.util.UUID.randomUUID().toString().replace("-", "");
            prefs.edit().putString("stableDeviceId", generated).apply();
            return generated;
        }

        return id.trim();
    }

    private void syncUserWithServer() {
        final String deviceId = getDeviceIdValue();
        final long requestVersion = ++syncRequestVersion;
        final long balanceVersionAtStart = balanceUpdateVersion;

        serverSyncReady = false;

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(
                        API_BASE_URL + "/user/" + deviceId
                );

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);

                int code = connection.getResponseCode();
                String response = readResponse(connection);

                if (code >= 200 && code < 300) {
                    final int serverBalance =
                            parseIntField(response, "balance_coins", -1);
                    final int serverBest =
                            parseIntField(response, "best_score", -1);

                    if (serverBalance < 0 || serverBest < 0) {
                        throw new Exception("Invalid server balance response");
                    }

                    mainHandler.post(() -> {
                        if (requestVersion != syncRequestVersion
                                || balanceVersionAtStart != balanceUpdateVersion) {
                            return;
                        }

                        serverSyncReady = true;
                        totalCoins = serverBalance;
                        bestScore = serverBest;

                        prefs.edit()
                                .putInt("totalCoins", totalCoins)
                                .putInt("bestScore", bestScore)
                                .apply();

                        updateBalanceUI();

                        if (bestText != null) {
                            bestText.setText("BEST: " + bestScore);
                        }
                        if (totalText != null) {
                            totalText.setText("TOTAL COINS: " + totalCoins);
                        }
                    });
                } else {
                    mainHandler.post(() -> {
                        if (requestVersion == syncRequestVersion) {
                            serverSyncReady = false;
                        }
                    });
                }
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (requestVersion == syncRequestVersion) {
                        serverSyncReady = false;
                    }
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private interface ServerCoinCallback {
        void onResult(boolean success, int newBalance);
    }

    private void addCoinsToServer(int amount, String reason) {
        addCoinsToServer(amount, reason, null);
    }

    private void addCoinsToServer(
            int amount,
            String reason,
            ServerCoinCallback callback) {

        if (amount <= 0) {
            if (callback != null) {
                mainHandler.post(() -> callback.onResult(false, totalCoins));
            }
            return;
        }

        final String deviceId = getDeviceIdValue();
        balanceUpdateVersion++;
        syncRequestVersion++;

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                // 1) Ensure the player row exists.
                URL userUrl = new URL(API_BASE_URL + "/user");
                connection = (HttpURLConnection) userUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                String userBody = "{\"device_id\":\"" + jsonEscape(deviceId) + "\"}";
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(userBody.getBytes(StandardCharsets.UTF_8));
                }

                int userCode = connection.getResponseCode();
                String userResponse = readResponse(connection);
                connection.disconnect();
                connection = null;

                if (userCode < 200 || userCode >= 300) {
                    postCoinResult(callback, false, totalCoins);
                    return;
                }

                // 2) Add the coins atomically on the server.
                URL coinUrl = new URL(API_BASE_URL + "/coins/add");
                connection = (HttpURLConnection) coinUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                String body = "{\"device_id\":\"" + jsonEscape(deviceId)
                        + "\",\"coins\":" + amount + "}";

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int code = connection.getResponseCode();
                String response = readResponse(connection);
                boolean success = code >= 200 && code < 300;
                int newBalance = parseIntField(response, "balance_coins", totalCoins);

                if (success) {
                    final int finalBalance = newBalance;
                    mainHandler.post(() -> {
                        totalCoins = finalBalance;
                        prefs.edit().putInt("totalCoins", totalCoins).apply();
                        updateBalanceUI();
                    });
                }

                postCoinResult(callback, success, newBalance);

            } catch (Exception e) {
                postCoinResult(callback, false, totalCoins);
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void postCoinResult(
            ServerCoinCallback callback,
            boolean success,
            int balance) {
        if (callback != null) {
            mainHandler.post(() -> callback.onResult(success, balance));
        }
    }

    private String readResponse(HttpURLConnection connection) throws Exception {
        InputStream stream;

        if (connection.getResponseCode() >= 400) {
            stream = connection.getErrorStream();
        } else {
            stream = connection.getInputStream();
        }

        if (stream == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    private int parseIntField(String json, String field, int fallback) {
        try {
            String key = "\"" + field + "\"";
            int keyIndex = json.indexOf(key);

            if (keyIndex < 0) {
                return fallback;
            }

            int colon = json.indexOf(':', keyIndex + key.length());

            if (colon < 0) {
                return fallback;
            }

            int start = colon + 1;

            while (start < json.length()
                    && Character.isWhitespace(json.charAt(start))) {
                start++;
            }

            int end = start;

            while (end < json.length()
                    && (Character.isDigit(json.charAt(end))
                    || json.charAt(end) == '-')) {
                end++;
            }

            return Integer.parseInt(
                    json.substring(start, end)
            );
        } catch (Exception e) {
            return fallback;
        }
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private void updateScore() {

        if (scoreText != null) {

            scoreText.setText(
                    coins + " COINS"
            );
        }
    }

    private void updateBalanceUI() {

        if (balanceButton != null) {

            balanceButton.setText(
                    "BALANCE: "
                            + totalCoins
                            + " COINS"
            );
        }

        if (totalText != null) {

            totalText.setText(
                    "TOTAL COINS: "
                            + totalCoins
            );
        }
    }

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
                dp(14),
                dp(12),
                dp(14),
                dp(18)
        );

        root.setBackgroundColor(BG);

        return root;
    }

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
        view.setGravity(Gravity.CENTER);

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

        button.setBackground(
                roundedBackground(
                        background,
                        18
                )
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

    private Button createRoundButton(
            String text,
            int background) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextSize(28);
        button.setTextColor(WHITE);

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setAllCaps(false);

        button.setGravity(
                Gravity.CENTER
        );

        button.setBackground(
                roundedBackground(
                        background,
                        1000
                )
        );

        return button;
    }

    private GradientDrawable roundedBackground(
            int color,
            int radius) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(
                dp(radius)
        );

        return drawable;
    }

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

    private ScrollView wrapScroll(
            LinearLayout root) {

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        scroll.addView(root);

        return scroll;
    }

    private int dp(int value) {

        return (int) (
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // =========================================================
    // PASSWORD HASH
    // =========================================================

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

    // =========================================================
    // TIMER
    // =========================================================

    private void stopTimer() {

        if (timer != null) {

            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onDestroy() {

        stopTimer();

        super.onDestroy();
    }
}
