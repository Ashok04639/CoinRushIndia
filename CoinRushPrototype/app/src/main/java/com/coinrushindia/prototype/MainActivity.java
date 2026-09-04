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
import java.util.UUID;

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
    private TextView wealthBalanceText;

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    private static final String API_BASE_URL =
            "https://coinrushindia.onrender.com/api/v1";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean authReady = false;
    private long balanceOperationVersion = 0L;

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

        prefs = getSharedPreferences("CoinRushIndia", MODE_PRIVATE);
        username = prefs.getString("username", "");
        totalCoins = prefs.getInt("totalCoins", 0);
        bestScore = prefs.getInt("bestScore", 0);

        String token = prefs.getString("authToken", "");
        if (!token.isEmpty()) {
            showLoadingScreen();
            validateSession(token);
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
        authReady = false;

        LinearLayout root = createRoot();
        root.setPadding(dp(22), dp(30), dp(22), dp(30));

        TextView title = createText("COIN RUSH INDIA", 30, WHITE, true);
        root.addView(title);

        TextView brand = createText("TAP • COLLECT • RUSH", 13, ORANGE, true);
        brand.setLetterSpacing(0.08f);
        root.addView(brand, marginParams(0, 2, 0, 28));

        TextView subtitle = createText("LOGIN TO PLAY", 16, GRAY, true);
        root.addView(subtitle, marginParams(0, 0, 0, 18));

        EditText usernameInput = createInput("Username");
        usernameInput.setText(prefs.getString("username", ""));
        root.addView(usernameInput);

        EditText passwordInput = createInput("Password");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(passwordInput);

        Button loginButton = createButton("LOGIN", GREEN);
        root.addView(loginButton);

        Button signupButton = createButton("CREATE NEW ACCOUNT", ORANGE);
        root.addView(signupButton);

        Button resetButton = createButton("RESET PASSWORD", Color.rgb(80, 90, 105));
        root.addView(resetButton);

        TextView secure = createText("SECURE ACCOUNT • CLOUD BALANCE", 12, GRAY, false);
        root.addView(secure, marginParams(0, 18, 0, 0));

        loginButton.setOnClickListener(v -> {
            String user = usernameInput.getText().toString().trim();
            String pass = passwordInput.getText().toString();
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Username aur password bharo", Toast.LENGTH_SHORT).show();
                return;
            }
            loginButton.setEnabled(false);
            loginButton.setText("CONNECTING...");
            prefs.edit().putString("username", user).apply();
            final String deviceId = getDeviceIdValue();
            new Thread(() -> {
                try {
                    ApiResponse api = postJson("/auth/login",
                            "{\"username\":\"" + jsonEscape(user) + "\",\"password\":\"" + jsonEscape(pass) + "\",\"device_id\":\"" + jsonEscape(deviceId) + "\"}", null);
                    int code = api.code;
                    String response = api.body;
                    if (code >= 200 && code < 300) {
                        String token = parseStringField(response, "token", "");
                        int balance = parseIntField(response, "balance_coins", 0);
                        int best = parseIntField(response, "best_score", 0);
                        boolean claimed = parseBooleanField(response, "daily_bonus_claimed_today", false);
                        mainHandler.post(() -> completeLogin(user, token, balance, best, claimed));
                    } else {
                        mainHandler.post(() -> { loginButton.setEnabled(true); loginButton.setText("LOGIN"); Toast.makeText(this, friendlyAuthError(response), Toast.LENGTH_LONG).show(); });
                    }
                } catch (Exception ex) {
                    mainHandler.post(() -> { loginButton.setEnabled(true); loginButton.setText("LOGIN"); Toast.makeText(this, "Server se connection nahi hua. Internet check karo.", Toast.LENGTH_LONG).show(); });
                }
            }).start();
        });

        signupButton.setOnClickListener(v -> showSignupScreen());
        resetButton.setOnClickListener(v -> showResetPasswordDialog());

        setContentView(wrapScroll(root));
    }

    private void completeLogin(String user, String token, int balance, int best, boolean dailyClaimed) {
        if (token.isEmpty()) {
            showLoginScreen();
            Toast.makeText(this, "Login response invalid hai.", Toast.LENGTH_LONG).show();
            return;
        }
        username = user;
        totalCoins = balance;
        bestScore = best;
        authReady = true;
        prefs.edit().putString("username", user).putString("authToken", token)
                .putBoolean("loggedIn", true).putBoolean("explicitLogout", false)
                .putInt("totalCoins", balance).putInt("bestScore", best)
                .putString("lastDailyBonus", dailyClaimed ? new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) : "")
                .apply();
        showGameScreen();
        Toast.makeText(this, "Welcome, " + user + "!", Toast.LENGTH_SHORT).show();

        // Final authoritative balance reconciliation. This also imports any
        // older device-based balance into the authenticated cloud account.
        syncAccountNow();
    }

    private void showResetPasswordDialog() {
        final EditText usernameInput = createInput("Username");
        String savedUser = prefs.getString("username", "").trim();
        if (!savedUser.isEmpty()) usernameInput.setText(savedUser);

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
        box.addView(usernameInput);
        box.addView(newPassword);
        box.addView(confirmPassword);

        new android.app.AlertDialog.Builder(this)
                .setTitle("RESET PASSWORD")
                .setMessage("Isi device par username verify karke naya password set karein.")
                .setView(box)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("SAVE", (dialog, which) -> {
                    String user = usernameInput.getText().toString().trim();
                    String pass = newPassword.getText().toString();
                    String confirm = confirmPassword.getText().toString();

                    if (user.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Username enter karein.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    if (pass.length() < 4 || !pass.equals(confirm)) {
                        Toast.makeText(
                                this,
                                "Password minimum 4 characters aur same hona chahiye.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    prefs.edit().putString("username", user).apply();
                    final String deviceId = getDeviceIdValue();

                    new Thread(() -> {
                        try {
                            ApiResponse api = postJson(
                                    "/auth/reset",
                                    "{\"username\":\"" + jsonEscape(user)
                                            + "\",\"new_password\":\""
                                            + jsonEscape(pass)
                                            + "\",\"device_id\":\""
                                            + jsonEscape(deviceId)
                                            + "\"}",
                                    null
                            );

                            String response = api.body;

                            if (api.code >= 200 && api.code < 300) {
                                String token = parseStringField(response, "token", "");
                                int balance = parseIntField(response, "balance_coins", 0);
                                int best = parseIntField(response, "best_score", 0);
                                boolean claimed =
                                        parseBooleanField(
                                                response,
                                                "daily_bonus_claimed_today",
                                                false
                                        );

                                mainHandler.post(
                                        () -> completeLogin(
                                                user,
                                                token,
                                                balance,
                                                best,
                                                claimed
                                        )
                                );
                            } else {
                                mainHandler.post(
                                        () -> Toast.makeText(
                                                this,
                                                friendlyAuthError(response),
                                                Toast.LENGTH_LONG
                                        ).show()
                                );
                            }
                        } catch (Exception ex) {
                            mainHandler.post(
                                    () -> Toast.makeText(
                                            this,
                                            "Server se connection nahi hua.",
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                        }
                    }).start();
                })
                .show();
    }

    private void showLoadingScreen() {
        LinearLayout root = createRoot();
        root.setGravity(Gravity.CENTER);
        TextView title = createText("COIN RUSH INDIA", 30, WHITE, true);
        root.addView(title);
        root.addView(createText("SECURELY CONNECTING...", 14, ORANGE, true), marginParams(0, 12, 0, 0));
        setContentView(root);
    }

    private void validateSession(String token) {
        new Thread(() -> {
            try {
                ApiResponse api = postJson("/auth/me", "{}", token);
                String response = api.body;
                if (api.code >= 200 && api.code < 300) {
                    String user = parseStringField(response, "username", "");
                    int balance = parseIntField(response, "balance_coins", 0);
                    int best = parseIntField(response, "best_score", 0);
                    boolean claimed = parseBooleanField(response, "daily_bonus_claimed_today", false);
                    mainHandler.post(() -> { username = user; totalCoins = balance; bestScore = best; authReady = true; prefs.edit().putString("username", user).putInt("totalCoins", balance).putInt("bestScore", best)
                            .putString("lastDailyBonus", claimed ? new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) : "")
                            .putBoolean("loggedIn", true).apply(); showGameScreen(); });
                } else if (api.code == 401) {
                    mainHandler.post(() -> {
                        prefs.edit().remove("authToken").putBoolean("loggedIn", false).apply();
                        showLoginScreen();
                        Toast.makeText(this, "Session expire ho gayi. Ek baar login karein.", Toast.LENGTH_LONG).show();
                    });
                } else {
                    mainHandler.post(() -> {
                        authReady = true;
                        showGameScreen();
                        Toast.makeText(this, "Server temporarily unavailable. Saved account loaded.", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception ex) {
                // Keep the saved session on temporary network/Render cold-start errors.
                // Cached balance remains visible and the same token can be retried later.
                mainHandler.post(() -> {
                    authReady = true;
                    showGameScreen();
                    Toast.makeText(this, "Offline mode: saved account loaded. Server reconnect hote hi sync hoga.", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // =========================================================
    // SIGNUP
    // =========================================================

    private void showSignupScreen() {
        stopTimer();
        LinearLayout root = createRoot();
        TextView title = createText("COIN RUSH INDIA", 30, WHITE, true); root.addView(title);
        root.addView(createText("CREATE YOUR ACCOUNT", 16, ORANGE, true), marginParams(0, 5, 0, 22));
        EditText usernameInput = createInput("Choose Username"); root.addView(usernameInput);
        EditText passwordInput = createInput("Choose Password"); passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD); root.addView(passwordInput);
        EditText confirmInput = createInput("Confirm Password"); confirmInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD); root.addView(confirmInput);
        Button createButton = createButton("CREATE ACCOUNT", GREEN); root.addView(createButton);
        Button backButton = createButton("BACK TO LOGIN", Color.rgb(80,90,105)); root.addView(backButton);
        createButton.setOnClickListener(v -> {
            String user=usernameInput.getText().toString().trim(); String pass=passwordInput.getText().toString(); String confirm=confirmInput.getText().toString();
            if(user.length()<3 || pass.length()<4 || !pass.equals(confirm)){ Toast.makeText(this,"Username 3+ aur password 4+ characters, passwords same hone chahiye.",Toast.LENGTH_LONG).show(); return; }
            createButton.setEnabled(false); createButton.setText("CREATING...");
            final String deviceId=getDeviceIdValue();
            new Thread(() -> {
                try {
                    ApiResponse api=postJson("/auth/register","{\"username\":\""+jsonEscape(user)+"\",\"password\":\""+jsonEscape(pass)+"\",\"device_id\":\""+jsonEscape(deviceId)+"\"}",null);
                    String response=api.body;
                    if(api.code>=200 && api.code<300){ String token=parseStringField(response,"token",""); int balance=parseIntField(response,"balance_coins",0); int best=parseIntField(response,"best_score",0); boolean claimed=parseBooleanField(response,"daily_bonus_claimed_today",false); mainHandler.post(() -> completeLogin(user,token,balance,best,claimed)); }
                    else mainHandler.post(() -> {createButton.setEnabled(true);createButton.setText("CREATE ACCOUNT");Toast.makeText(this,friendlyAuthError(response),Toast.LENGTH_LONG).show();});
                } catch(Exception ex){ mainHandler.post(() -> {createButton.setEnabled(true);createButton.setText("CREATE ACCOUNT");Toast.makeText(this,"Server se connection nahi hua.",Toast.LENGTH_LONG).show();}); }
            }).start();
        });
        backButton.setOnClickListener(v -> showLoginScreen());
        setContentView(wrapScroll(root));
    }

    // =========================================================
    // MAIN GAME SCREEN
    // =========================================================

    private void showGameScreen() {
        stopTimer();

        totalCoins = prefs.getInt("totalCoins", 0);
        bestScore = prefs.getInt("bestScore", 0);

        LinearLayout root = createRoot();
        root.setPadding(dp(10), dp(10), dp(10), dp(12));

        // ---------- PREMIUM HEADER ----------
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout brandBox = new LinearLayout(this);
        brandBox.setOrientation(LinearLayout.VERTICAL);

        TextView title = createText("COIN RUSH", 22, WHITE, true);
        TextView india = createText("INDIA  •  ELITE REWARDS", 9, ORANGE, true);
        india.setLetterSpacing(0.08f);
        brandBox.addView(title, new LinearLayout.LayoutParams(-1, dp(28)));
        brandBox.addView(india, new LinearLayout.LayoutParams(-1, dp(18)));

        header.addView(brandBox, new LinearLayout.LayoutParams(0, dp(46), 1));

        logoutButton = createButton("LOGOUT", RED);
        LinearLayout.LayoutParams logoutParams = new LinearLayout.LayoutParams(dp(82), dp(38));
        header.addView(logoutButton, logoutParams);
        root.addView(header);

        TextView player = createText(
                "WELCOME BACK, " + username.toUpperCase(Locale.getDefault()),
                12, GRAY, true
        );
        root.addView(player, marginParams(0, 0, 0, 5));

        // ---------- WEALTH CARD ----------
        LinearLayout wealthCard = roundedCard(CARD, 16);
        wealthCard.setGravity(Gravity.CENTER);
        wealthCard.setOrientation(LinearLayout.VERTICAL);
        wealthCard.setPadding(dp(10), dp(6), dp(10), dp(6));

        TextView wealthLabel = createText("TOTAL WEALTH", 9, GRAY, true);
        wealthLabel.setLetterSpacing(0.12f);
        wealthCard.addView(wealthLabel);

        wealthBalanceText = createText(totalCoins + " COINS", 27, YELLOW, true);
        wealthCard.addView(wealthBalanceText);

        totalText = createText("AVAILABLE REWARD BALANCE", 9, GRAY, false);
        wealthCard.addView(totalText);

        root.addView(wealthCard, new LinearLayout.LayoutParams(-1, dp(76)));

        // ---------- QUICK STATS ----------
        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.setGravity(Gravity.CENTER);
        stats.setPadding(0, dp(4), 0, dp(4));

        LinearLayout bestCard = miniStatCard("BEST SCORE", String.valueOf(bestScore), YELLOW);
        LinearLayout roundCard = miniStatCard("ROUND", "30 SEC", ORANGE);
        stats.addView(bestCard, new LinearLayout.LayoutParams(0, dp(58), 1));

        LinearLayout.LayoutParams rc = new LinearLayout.LayoutParams(0, dp(58), 1);
        rc.setMargins(dp(6), 0, 0, 0);
        stats.addView(roundCard, rc);
        root.addView(stats);

        // ---------- DAILY BONUS ----------
        Button dailyBonusButton = createCompactButton("DAILY BONUS  +100", ORANGE);
        root.addView(dailyBonusButton);

        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String lastBonusDate = prefs.getString("lastDailyBonus", "");
        if (today.equals(lastBonusDate)) {
            dailyBonusButton.setText("✓  DAILY BONUS CLAIMED");
            dailyBonusButton.setEnabled(false);
        }

        dailyBonusButton.setOnClickListener(v -> {
            if (!authReady || prefs.getString("authToken", "").isEmpty()) {
                Toast.makeText(this, "Account server se connect ho raha hai. Thoda wait karein.", Toast.LENGTH_LONG).show();
                syncAccountNow();
                return;
            }

            String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
            if (currentDate.equals(prefs.getString("lastDailyBonus", ""))) {
                dailyBonusButton.setText("✓  DAILY BONUS CLAIMED");
                dailyBonusButton.setEnabled(false);
                return;
            }

            final long operation = ++balanceOperationVersion;
            dailyBonusButton.setEnabled(false);
            dailyBonusButton.setText("SAVING +100 COINS...");

            new Thread(() -> {
                try {
                    ApiResponse api = postJson("/bonus/daily", "{}", prefs.getString("authToken", ""));
                    String response = api.body;

                    if (api.code >= 200 && api.code < 300) {
                        int newBalance = parseIntField(response, "balance_coins", -1);

                        mainHandler.post(() -> {
                            if (operation != balanceOperationVersion || newBalance < 0) return;

                            totalCoins = newBalance;
                            prefs.edit()
                                    .putInt("totalCoins", totalCoins)
                                    .putString("lastDailyBonus", currentDate)
                                    .apply();

                            updateBalanceUI();
                            addCoinHistory("+100 DAILY BONUS");
                            dailyBonusButton.setText("✓  DAILY BONUS CLAIMED");
                            dailyBonusButton.setEnabled(false);

                            Toast.makeText(
                                    this,
                                    "+100 coins added successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    } else {
                        mainHandler.post(() -> {
                            if (operation != balanceOperationVersion) return;

                            dailyBonusButton.setEnabled(true);
                            dailyBonusButton.setText("DAILY BONUS  +100");
                            Toast.makeText(
                                    this,
                                    friendlyAuthError(response),
                                    Toast.LENGTH_LONG
                            ).show();

                            if (api.code == 401) {
                                syncAccountNow();
                            }
                        });
                    }
                } catch (Exception ex) {
                    mainHandler.post(() -> {
                        if (operation != balanceOperationVersion) return;
                        dailyBonusButton.setEnabled(true);
                        dailyBonusButton.setText("DAILY BONUS  +100");
                        Toast.makeText(
                                this,
                                "Server unavailable. Dobara try karein.",
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }
            }).start();
        });

        // ---------- ACTIONS: TWO COMPACT BUTTONS ----------
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);

        Button refreshButton = createCompactButton("↻  REFRESH", Color.rgb(65, 80, 100));
        Button historyButton = createCompactButton("COIN HISTORY", Color.rgb(75, 88, 108));

        actionRow.addView(refreshButton, new LinearLayout.LayoutParams(0, dp(40), 1));
        LinearLayout.LayoutParams historyParams = new LinearLayout.LayoutParams(0, dp(40), 1);
        historyParams.setMargins(dp(6), 0, 0, 0);
        actionRow.addView(historyButton, historyParams);
        root.addView(actionRow);

        refreshButton.setOnClickListener(v -> {
            refreshButton.setEnabled(false);
            refreshButton.setText("SYNCING...");
            syncAccountNow(() -> {
                refreshButton.setEnabled(true);
                refreshButton.setText("↻  REFRESH");
                Toast.makeText(
                        this,
                        "Balance synced: " + totalCoins + " coins",
                        Toast.LENGTH_SHORT
                ).show();
            });
        });

        historyButton.setOnClickListener(v -> showCoinHistory());

        Button withdrawButton = createCompactButton("WITHDRAW  •  BEP-20", BLUE);
        root.addView(withdrawButton);
        withdrawButton.setOnClickListener(v -> showWithdrawalScreen());

        // ---------- LIVE TAP ARENA ----------
        LinearLayout gameCard = roundedCard(Color.rgb(19, 28, 42), 18);
        gameCard.setOrientation(LinearLayout.VERTICAL);
        gameCard.setGravity(Gravity.CENTER);
        gameCard.setPadding(dp(8), dp(4), dp(8), dp(6));

        TextView gameLabel = createText("LIVE TAP ARENA", 10, ORANGE, true);
        gameLabel.setLetterSpacing(0.12f);
        gameCard.addView(gameLabel, new LinearLayout.LayoutParams(-1, dp(18)));

        LinearLayout gameInfo = new LinearLayout(this);
        gameInfo.setOrientation(LinearLayout.HORIZONTAL);
        gameInfo.setGravity(Gravity.CENTER);

        scoreText = createText("0 COINS", 22, YELLOW, true);
        bestText = createText("BEST: " + bestScore, 11, GRAY, false);
        timerText = createText("TIME: 30", 20, WHITE, true);

        gameInfo.addView(scoreText, new LinearLayout.LayoutParams(0, dp(30), 1));
        gameInfo.addView(bestText, new LinearLayout.LayoutParams(0, dp(30), 1));
        gameInfo.addView(timerText, new LinearLayout.LayoutParams(0, dp(30), 1));
        gameCard.addView(gameInfo);

        messageText = createText("TAP • COLLECT • RUSH", 10, GRAY, true);
        gameCard.addView(messageText, new LinearLayout.LayoutParams(-1, dp(22)));

        tapButton = createRoundButton("TAP!", ORANGE);
        LinearLayout.LayoutParams tapParams =
                new LinearLayout.LayoutParams(dp(115), dp(115));
        tapParams.gravity = Gravity.CENTER;
        gameCard.addView(tapButton, tapParams);

        LinearLayout rewardRow = new LinearLayout(this);
        rewardRow.setOrientation(LinearLayout.HORIZONTAL);

        rewardButton = createCompactButton("WATCH AD  •  2X", GREEN);
        rewardButton.setVisibility(View.GONE);
        rewardRow.addView(rewardButton, new LinearLayout.LayoutParams(0, dp(38), 1));

        restartButton = createCompactButton("PLAY AGAIN", Color.rgb(75, 88, 108));
        restartButton.setVisibility(View.GONE);
        LinearLayout.LayoutParams restartParams =
                new LinearLayout.LayoutParams(0, dp(38), 1);
        restartParams.setMargins(dp(6), 0, 0, 0);
        rewardRow.addView(restartButton, restartParams);

        gameCard.addView(rewardRow);
        rewardButton.setOnClickListener(v -> showRewardedAd());
        restartButton.setOnClickListener(v -> startGame());

        tapButton.setOnClickListener(v -> {
            if (!gameRunning) return;
            coins++;
            updateScore();
        });

        root.addView(gameCard, new LinearLayout.LayoutParams(-1, dp(235)));

        logoutButton.setOnClickListener(v -> {
            stopTimer();
            String token = prefs.getString("authToken", "");

            if (!token.isEmpty()) {
                new Thread(() -> {
                    try {
                        postJson("/auth/logout", "{}", token);
                    } catch (Exception ignored) {}
                }).start();
            }

            prefs.edit()
                    .remove("authToken")
                    .putBoolean("loggedIn", false)
                    .putBoolean("explicitLogout", true)
                    .apply();

            authReady = false;
            showLoginScreen();
        });

        // Dashboard is deliberately compact: no scrolling required on normal phones.
        setContentView(root);
        updateBalanceUI();
        startGame();

        getWindow().getDecorView().postDelayed(() -> {
            loadInterstitialAd();
            loadRewardedAd();
        }, 800);
    }

    private Button createCompactButton(String text, int background) {
        Button button = createButton(text, background);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, dp(40));
        params.setMargins(0, dp(2), 0, dp(2));
        button.setLayoutParams(params);
        button.setTextSize(13);
        return button;
    }


    private LinearLayout roundedCard(int color, int radius) {
        LinearLayout card = new LinearLayout(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(radius));
        card.setBackground(bg);
        return card;
    }

    private LinearLayout miniStatCard(String label, String value, int valueColor) {
        LinearLayout card = roundedCard(CARD, 16);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        TextView l = createText(label, 10, GRAY, true);
        l.setLetterSpacing(0.08f);
        card.addView(l);
        card.addView(createText(value, 20, valueColor, true));
        return card;
    }

    private void syncAccountNow() {
        syncAccountNow(null);
    }

    private void syncAccountNow(Runnable done) {
        final String token = prefs.getString("authToken", "");
        final long operationAtStart = balanceOperationVersion;

        if (token.isEmpty()) {
            authReady = false;
            if (done != null) mainHandler.post(done);
            return;
        }

        new Thread(() -> {
            try {
                ApiResponse accountApi = postJson("/auth/me", "{}", token);

                if (accountApi.code == 401) {
                    authReady = false;
                    mainHandler.post(() -> {
                        prefs.edit()
                                .remove("authToken")
                                .putBoolean("loggedIn", false)
                                .apply();
                        showLoginScreen();
                        Toast.makeText(
                                this,
                                "Session expire ho gayi. Login karein.",
                                Toast.LENGTH_LONG
                        ).show();
                    });
                    return;
                }

                if (accountApi.code < 200 || accountApi.code >= 300) {
                    if (done != null) mainHandler.post(done);
                    return;
                }

                int accountBalance = parseIntField(accountApi.body, "balance_coins", -1);
                int accountBest = parseIntField(accountApi.body, "best_score", -1);
                boolean claimed = parseBooleanField(
                        accountApi.body,
                        "daily_bonus_claimed_today",
                        false
                );

                if (accountBalance < 0 || accountBest < 0) {
                    throw new Exception("Invalid account balance response");
                }

                // Safe legacy reconciliation:
                // only the current device's old balance can be imported,
                // and only the positive difference is added to the account.
                int finalBalance = accountBalance;
                int finalBest = accountBest;

                try {
                    String deviceId = getDeviceIdValue();
                    ApiResponse legacyApi = getJson(
                            "/user/" + java.net.URLEncoder.encode(
                                    deviceId,
                                    "UTF-8"
                            )
                    );

                    if (legacyApi.code >= 200 && legacyApi.code < 300) {
                        int legacyBalance = parseIntField(
                                legacyApi.body,
                                "balance_coins",
                                0
                        );
                        int legacyBest = parseIntField(
                                legacyApi.body,
                                "best_score",
                                0
                        );

                        if (legacyBalance > finalBalance) {
                            int difference = legacyBalance - finalBalance;

                            ApiResponse repairApi = postJson(
                                    "/coins/add",
                                    "{\"coins\":" + difference + "}",
                                    token
                            );

                            if (repairApi.code >= 200 && repairApi.code < 300) {
                                int repairedBalance = parseIntField(
                                        repairApi.body,
                                        "balance_coins",
                                        -1
                                );
                                if (repairedBalance >= 0) {
                                    finalBalance = repairedBalance;
                                }
                            }
                        }

                        if (legacyBest > finalBest) {
                            finalBest = legacyBest;
                        }
                    }
                } catch (Exception ignored) {
                    // Account balance remains authoritative if legacy repair
                    // is unavailable.
                }

                final int shownBalance = finalBalance;
                final int shownBest = finalBest;
                final boolean shownClaimed = claimed;

                mainHandler.post(() -> {
                    // Never let an older refresh overwrite a newer coin operation.
                    if (operationAtStart != balanceOperationVersion) {
                        if (done != null) done.run();
                        return;
                    }

                    totalCoins = shownBalance;
                    bestScore = shownBest;
                    authReady = true;

                    prefs.edit()
                            .putInt("totalCoins", totalCoins)
                            .putInt("bestScore", bestScore)
                            .putString(
                                    "lastDailyBonus",
                                    shownClaimed
                                            ? new SimpleDateFormat(
                                                    "yyyyMMdd",
                                                    Locale.getDefault()
                                            ).format(new Date())
                                            : ""
                            )
                            .apply();

                    updateBalanceUI();

                    if (bestText != null) {
                        bestText.setText("BEST: " + bestScore);
                    }

                    if (totalText != null) {
                        totalText.setText(
                                "AVAILABLE REWARD BALANCE: " + totalCoins
                        );
                    }

                    if (wealthBalanceText != null) {
                        wealthBalanceText.setText(totalCoins + " COINS");
                    }

                    if (done != null) done.run();
                });

            } catch (Exception ex) {
                if (done != null) mainHandler.post(done);
            }
        }).start();
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
                            this,
                            "Game reward server par save nahi hua. Refresh Balance dabayein.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
                totalCoins = newBalance; prefs.edit().putInt("totalCoins", totalCoins).apply(); addCoinHistory("+"+roundCoins+" GAME REWARD"); updateBalanceUI();
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
                    "AVAILABLE REWARD BALANCE: "
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
        if (coins <= 0) { Toast.makeText(this,"Is round me coins nahi mile.",Toast.LENGTH_SHORT).show(); return; }
        if (!authReady) { Toast.makeText(this,"Account server se connected nahi hai.",Toast.LENGTH_LONG).show(); return; }
        final int bonus = coins;
        new Thread(() -> {
            try {
                ApiResponse api=postJson("/coins/add","{\"coins\":"+bonus+"}",prefs.getString("authToken",""));
                String response=api.body;
                if(api.code>=200 && api.code<300){ int newBalance=parseIntField(response,"balance_coins",totalCoins); mainHandler.post(() -> { coins=coins*2; totalCoins=newBalance; if(coins>bestScore)bestScore=coins; prefs.edit().putInt("bestScore",bestScore).putInt("totalCoins",totalCoins).apply(); addCoinHistory("+"+bonus+" AD REWARD"); updateScore();updateBalanceUI();if(bestText!=null)bestText.setText("BEST: "+bestScore);if(messageText!=null)messageText.setText("REWARD! Coins doubled!");Toast.makeText(this,"+"+bonus+" bonus coins!",Toast.LENGTH_SHORT).show(); }); }
                else mainHandler.post(() -> Toast.makeText(this,friendlyAuthError(response),Toast.LENGTH_LONG).show());
            } catch(Exception ex){ mainHandler.post(() -> Toast.makeText(this,"Ad reward save nahi hua.",Toast.LENGTH_LONG).show()); }
        }).start();
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
    // SERVER API
    // =========================================================

    private static class ApiResponse {
        final int code;
        final String body;
        ApiResponse(int code, String body) { this.code = code; this.body = body; }
    }

    private String getDeviceIdValue() {
        String id=Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if(id!=null && id.trim().length()>=6) return id.trim();
        String saved=prefs.getString("stableDeviceId","");
        if(!saved.isEmpty()) return saved;
        String generated="android-"+UUID.randomUUID().toString().replace("-","");
        prefs.edit().putString("stableDeviceId",generated).apply();
        return generated;
    }

    private ApiResponse getJson(String path) throws Exception {
        HttpURLConnection c = null;
        try {
            URL url = new URL(API_BASE_URL + path);
            c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(20000);
            c.setReadTimeout(20000);
            int code = c.getResponseCode();
            return new ApiResponse(code, readResponse(c));
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private ApiResponse postJson(String path, String body, String token) throws Exception {
        HttpURLConnection c=null;
        try {
            URL url=new URL(API_BASE_URL+path); c=(HttpURLConnection)url.openConnection(); c.setRequestMethod("POST"); c.setConnectTimeout(20000); c.setReadTimeout(20000); c.setDoOutput(true); c.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            if(token!=null && !token.isEmpty()) c.setRequestProperty("Authorization","Bearer "+token);
            try(OutputStream os=c.getOutputStream()){os.write(body.getBytes(StandardCharsets.UTF_8));}
            int code=c.getResponseCode(); return new ApiResponse(code, readResponse(c));
        } finally { if(c!=null)c.disconnect(); }
    }

    private void addCoinsToServer(int amount, String reason, ServerCoinCallback callback) {
        if (amount <= 0 || !authReady) {
            if (callback != null) {
                mainHandler.post(() -> callback.onResult(false, totalCoins));
            }
            return;
        }

        final long op = ++balanceOperationVersion;
        final String token = prefs.getString("authToken", "");

        new Thread(() -> {
            try {
                ApiResponse api = postJson(
                        "/coins/add",
                        "{\"coins\":" + amount + "}",
                        token
                );

                String response = api.body;

                if (api.code >= 200 && api.code < 300) {
                    final int balance =
                            parseIntField(response, "balance_coins", -1);

                    if (balance < 0) {
                        mainHandler.post(() -> {
                            if (callback != null) {
                                callback.onResult(false, totalCoins);
                            }
                        });
                        return;
                    }

                    mainHandler.post(() -> {
                        if (op != balanceOperationVersion) return;

                        totalCoins = balance;

                        prefs.edit()
                                .putInt("totalCoins", totalCoins)
                                .apply();

                        updateBalanceUI();

                        if (callback != null) {
                            callback.onResult(true, balance);
                        }
                    });
                } else {
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onResult(false, totalCoins);
                        }

                        if (api.code == 401) {
                            authReady = false;
                            syncAccountNow();
                        }
                    });
                }
            } catch (Exception ex) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onResult(false, totalCoins);
                    }
                });
            }
        }).start();
    }

    private interface ServerCoinCallback { void onResult(boolean success, int newBalance); }

    private String readResponse(HttpURLConnection c) throws Exception {
        InputStream stream=c.getResponseCode()>=400?c.getErrorStream():c.getInputStream(); if(stream==null)return ""; StringBuilder out=new StringBuilder(); try(BufferedReader r=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null)out.append(line);} return out.toString();
    }

    private int parseIntField(String json,String field,int fallback){ try{String key="\""+field+"\"";int k=json.indexOf(key);if(k<0)return fallback;int colon=json.indexOf(':',k+key.length());if(colon<0)return fallback;int i=colon+1;while(i<json.length()&&Character.isWhitespace(json.charAt(i)))i++;int j=i;while(j<json.length()&&(Character.isDigit(json.charAt(j))||json.charAt(j)=='-'))j++;return Integer.parseInt(json.substring(i,j));}catch(Exception e){return fallback;} }

    private String parseStringField(String json,String field,String fallback){ try{String key="\""+field+"\"";int k=json.indexOf(key);if(k<0)return fallback;int colon=json.indexOf(':',k+key.length());if(colon<0)return fallback;int i=colon+1;while(i<json.length()&&Character.isWhitespace(json.charAt(i)))i++;if(i>=json.length()||json.charAt(i)!='\"')return fallback;i++;StringBuilder out=new StringBuilder();boolean esc=false;for(;i<json.length();i++){char ch=json.charAt(i);if(esc){out.append(ch);esc=false;}else if(ch=='\\')esc=true;else if(ch=='\"')break;else out.append(ch);}return out.toString();}catch(Exception e){return fallback;} }

    private boolean parseBooleanField(String json, String field, boolean fallback) {
        try {
            String key = "\"" + field + "\"";
            int k = json.indexOf(key); if (k < 0) return fallback;
            int colon = json.indexOf(':', k + key.length()); if (colon < 0) return fallback;
            String rest = json.substring(colon + 1).trim();
            if (rest.startsWith("true")) return true;
            if (rest.startsWith("false")) return false;
        } catch (Exception ignored) {}
        return fallback;
    }

    private String friendlyAuthError(String response){ if(response==null)return "Request failed."; if(response.contains("Invalid username or password"))return "Username ya password galat hai."; if(response.contains("Username already exists"))return "Username already registered hai. Login karein."; if(response.contains("Account not found"))return "Account nahi mila. Pehle account create karein."; if(response.contains("different device"))return "Ye account kisi aur device se linked hai."; if(response.contains("Daily bonus already claimed"))return "Aaj ka bonus already claim ho chuka hai."; return "Request complete nahi hui. Dobara try karein."; }

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
            balanceButton.setText("BALANCE: " + totalCoins + " COINS");
        }
        if (wealthBalanceText != null) {
            wealthBalanceText.setText(totalCoins + " COINS");
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
                dp(18),
                dp(18),
                dp(18),
                dp(25)
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
    // JSON ESCAPE
    // =========================================================

    private String jsonEscape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
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
