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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
    private int totalGames = 0;

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
    private TextView dashboardValueText;
    private TextView dashboardBestText;
    private TextView dashboardGamesText;

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
        totalGames = prefs.getInt("totalGames", 0);

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

        TextView brand = createText("TAP | COLLECT | RUSH", 13, ORANGE, true);
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

        TextView secure = createText("SECURE ACCOUNT | CLOUD BALANCE", 12, GRAY, false);
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
        showDashboard();
        Toast.makeText(this, "Welcome, " + user + "!", Toast.LENGTH_SHORT).show();

        // Login response already contains the authenticated cloud balance.
        // Do not start another /auth/me + legacy reconciliation request here;
        // that extra network round-trip made login feel unnecessarily slow.
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
                            .putBoolean("loggedIn", true).apply(); showDashboard(); });
                } else if (api.code == 401) {
                    mainHandler.post(() -> {
                        prefs.edit().remove("authToken").putBoolean("loggedIn", false).apply();
                        showLoginScreen();
                        Toast.makeText(this, "Session expire ho gayi. Ek baar login karein.", Toast.LENGTH_LONG).show();
                    });
                } else {
                    mainHandler.post(() -> {
                        authReady = true;
                        showDashboard();
                        Toast.makeText(this, "Server temporarily unavailable. Saved account loaded.", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception ex) {
                // Keep the saved session on temporary network/Render cold-start errors.
                // Cached balance remains visible and the same token can be retried later.
                mainHandler.post(() -> {
                    authReady = true;
                    showDashboard();
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

    // =========================================================
    // FINAL BILLIONAIRE DASHBOARD
    // =========================================================

    private void showDashboard() {
        stopTimer();

        totalCoins = prefs.getInt("totalCoins", 0);
        bestScore = prefs.getInt("bestScore", 0);
        totalGames = prefs.getInt("totalGames", 0);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = createRoot();
        root.setPadding(dp(10), dp(10), dp(10), dp(18));

        // Header: menu + brand + logout
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView menu = createText("MENU", 11, WHITE, true);
        menu.setGravity(Gravity.CENTER);
        header.addView(menu, new LinearLayout.LayoutParams(dp(42), dp(52)));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        TextView brandTitle = createText("COIN RUSH INDIA", 20, WHITE, true);
        brandTitle.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        TextView brandSub = createText("INDIA | ELITE REWARDS", 9, ORANGE, true);
        brandSub.setGravity(Gravity.START);
        brandSub.setLetterSpacing(0.10f);
        brand.addView(brandTitle, new LinearLayout.LayoutParams(0, dp(28), 1));
        brand.addView(brandSub, new LinearLayout.LayoutParams(0, dp(18), 1));
        header.addView(brand, new LinearLayout.LayoutParams(0, dp(52), 1));

        logoutButton = createButton("LOGOUT", RED);
        logoutButton.setTextSize(11);
        header.addView(logoutButton, new LinearLayout.LayoutParams(dp(74), dp(38)));
        root.addView(header);

        // Welcome / elite card
        LinearLayout welcome = roundedCard(Color.rgb(17, 27, 39), 14);
        welcome.setGravity(Gravity.CENTER_VERTICAL);
        welcome.setPadding(dp(10), dp(7), dp(8), dp(7));

        TextView crown = createText("STAR", 11, YELLOW, true);
        crown.setGravity(Gravity.CENTER);
        GradientDrawable crownBg = new GradientDrawable();
        crownBg.setColor(Color.rgb(10, 17, 25));
        crownBg.setStroke(dp(1), Color.rgb(255, 190, 45));
        crownBg.setCornerRadius(dp(50));
        crown.setBackground(crownBg);
        welcome.addView(crown, new LinearLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout welcomeText = new LinearLayout(this);
        welcomeText.setOrientation(LinearLayout.VERTICAL);
        welcomeText.setPadding(dp(10), 0, dp(4), 0);
        TextView wb = createText("WELCOME BACK,", 10, GRAY, true);
        wb.setGravity(Gravity.START);
        TextView un = createText(username.isEmpty() ? "PLAYER" : username.toUpperCase(Locale.getDefault()), 20, WHITE, true);
        un.setGravity(Gravity.START);
        String memberSince = prefs.getString("memberSince", "");
        if (memberSince.isEmpty()) {
            memberSince = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
            prefs.edit().putString("memberSince", memberSince).apply();
        }
        TextView ms = createText("Member Since: " + memberSince, 8, GRAY, false);
        ms.setGravity(Gravity.START);
        welcomeText.addView(wb);
        welcomeText.addView(un);
        welcomeText.addView(ms);
        welcome.addView(welcomeText, new LinearLayout.LayoutParams(0, dp(58), 1));

        TextView elite = createText("ELITE", 11, YELLOW, true);
        elite.setGravity(Gravity.CENTER);
        GradientDrawable eliteBg = new GradientDrawable();
        eliteBg.setColor(Color.rgb(13, 24, 34));
        eliteBg.setStroke(dp(1), Color.rgb(255, 190, 45));
        eliteBg.setCornerRadius(dp(10));
        elite.setBackground(eliteBg);
        welcome.addView(elite, new LinearLayout.LayoutParams(dp(54), dp(54)));
        root.addView(welcome, marginParams(0, 4, 0, 7));

        // Total wealth card
        LinearLayout wealth = roundedCard(Color.rgb(13, 27, 39), 12);
        wealth.setOrientation(LinearLayout.HORIZONTAL);
        wealth.setGravity(Gravity.CENTER_VERTICAL);
        wealth.setPadding(dp(12), dp(8), dp(8), dp(8));

        LinearLayout wealthWords = new LinearLayout(this);
        wealthWords.setOrientation(LinearLayout.VERTICAL);
        TextView wl = createText("TOTAL WEALTH", 9, GRAY, true);
        wl.setGravity(Gravity.START);
        wl.setLetterSpacing(0.08f);
        wealthWords.addView(wl);
        wealthBalanceText = createText(totalCoins + "\nCOINS", 31, YELLOW, true);
        wealthBalanceText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        wealthWords.addView(wealthBalanceText);
        dashboardValueText = createText("VALUE: " + String.format(Locale.getDefault(), "%.2f", totalCoins / 100.0), 10, GREEN, true);
        dashboardValueText.setGravity(Gravity.START);
        wealthWords.addView(dashboardValueText);
        wealth.addView(wealthWords, new LinearLayout.LayoutParams(0, dp(118), 1));

        TextView coinsArt = createText("COINS", 28, YELLOW, true);
        coinsArt.setGravity(Gravity.CENTER);
        wealth.addView(coinsArt, new LinearLayout.LayoutParams(dp(115), dp(110)));
        root.addView(wealth, new LinearLayout.LayoutParams(-1, dp(130)));

        // Three stats: best / round / games
        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.setGravity(Gravity.CENTER);
        stats.setPadding(0, dp(6), 0, dp(6));
        LinearLayout bestCard = miniStatCard("BEST SCORE", String.valueOf(bestScore), YELLOW);
        dashboardBestText = (TextView) bestCard.getChildAt(1);
        stats.addView(bestCard, new LinearLayout.LayoutParams(0, dp(66), 1));
        LinearLayout round = miniStatCard("ROUND TIME", "30 SEC", Color.rgb(210, 100, 255));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, dp(66), 1);
        rp.setMargins(dp(5), 0, dp(5), 0);
        stats.addView(round, rp);
        LinearLayout gamesCard = miniStatCard("TOTAL GAMES", String.valueOf(totalGames), GREEN);
        dashboardGamesText = (TextView) gamesCard.getChildAt(1);
        stats.addView(gamesCard, new LinearLayout.LayoutParams(0, dp(66), 1));
        root.addView(stats);

        // Daily bonus
        Button daily = createCompactButton("DAILY BONUS     +100 COINS", GREEN);
        daily.setTextSize(13);
        root.addView(daily);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        if (today.equals(prefs.getString("lastDailyBonus", ""))) {
            daily.setText("DAILY BONUS     CLAIMED");
            daily.setEnabled(false);
        }
        daily.setOnClickListener(v -> claimDailyBonusFromDashboard(daily));

        // Refresh / history
        LinearLayout utility = new LinearLayout(this);
        utility.setOrientation(LinearLayout.HORIZONTAL);
        Button refresh = createCompactButton("REFRESH BALANCE", BLUE);
        Button history = createCompactButton("COIN HISTORY", Color.rgb(110, 55, 150));
        utility.addView(refresh, new LinearLayout.LayoutParams(0, dp(48), 1));
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(0, dp(48), 1);
        hp.setMargins(dp(5), 0, 0, 0);
        utility.addView(history, hp);
        root.addView(utility, marginParams(0, 2, 0, 4));
        refresh.setOnClickListener(v -> {
            refresh.setEnabled(false);
            refresh.setText("SYNCING...");
            syncAccountNow(() -> {
                refresh.setEnabled(true);
                refresh.setText("REFRESH BALANCE");
                updateBalanceUI();
                Toast.makeText(this, "Balance synced: " + totalCoins + " coins", Toast.LENGTH_SHORT).show();
            });
        });
        history.setOnClickListener(v -> showCoinHistory());

        Button withdraw = createCompactButton("WITHDRAW   |   BEP-20", BLUE);
        withdraw.setTextSize(14);
        root.addView(withdraw);
        withdraw.setOnClickListener(v -> showWithdrawalScreen());

        Button watch = createCompactButton("WATCH AD   |   2X REWARD", YELLOW);
        watch.setTextColor(Color.BLACK);
        watch.setTextSize(14);
        root.addView(watch);
        watch.setOnClickListener(v -> {
            if (coins <= 0) {
                Toast.makeText(this, "Pehle ek game complete karein, phir 2X reward le sakte hain.", Toast.LENGTH_LONG).show();
            } else {
                showRewardedAd();
            }
        });

        // Live arena CTA
        LinearLayout arena = roundedCard(Color.rgb(13, 25, 38), 15);
        arena.setOrientation(LinearLayout.VERTICAL);
        arena.setPadding(dp(10), dp(8), dp(10), dp(8));
        TextView at = createText("LIVE TAP ARENA", 16, ORANGE, true);
        arena.addView(at, new LinearLayout.LayoutParams(-1, dp(28)));
        TextView as = createText("TAP | COLLECT | RUSH!", 10, GRAY, true);
        arena.addView(as, new LinearLayout.LayoutParams(-1, dp(20)));
        Button play = createButton("PLAY NOW", ORANGE);
        play.setTextSize(22);
        arena.addView(play, marginParams(0, 6, 0, 0));
        TextView tapStart = createText("TAP TO START", 9, WHITE, true);
        arena.addView(tapStart, new LinearLayout.LayoutParams(-1, dp(20)));
        play.setOnClickListener(v -> showGameScreen());
        root.addView(arena, marginParams(0, 6, 0, 6));

        // Bottom navigation like the reference
        LinearLayout nav = roundedCard(Color.rgb(9, 18, 28), 12);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(0, dp(3), 0, dp(3));
        nav.addView(navItem("HOME", "Home", true), new LinearLayout.LayoutParams(0, dp(58), 1));
        nav.addView(navItem("GAME", "Games", false), new LinearLayout.LayoutParams(0, dp(58), 1));
        nav.addView(navItem("REWARD", "Rewards", false), new LinearLayout.LayoutParams(0, dp(58), 1));
        nav.addView(navItem("PROFILE", "Profile", false), new LinearLayout.LayoutParams(0, dp(58), 1));
        root.addView(nav);

        LinearLayout secure = roundedCard(Color.rgb(10, 22, 32), 12);
        secure.setGravity(Gravity.CENTER_VERTICAL);
        TextView secureIcon = createText("LOCK", 11, YELLOW, true);
        secure.addView(secureIcon, new LinearLayout.LayoutParams(dp(45), dp(48)));
        TextView secureText = createText("SECURE | FAST | REWARDING\nPlay More | Earn More | Be a Billionaire", 10, WHITE, true);
        secureText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        secure.addView(secureText, new LinearLayout.LayoutParams(0, dp(48), 1));
        TextView gold = createText("GOLD", 12, YELLOW, true);
        secure.addView(gold, new LinearLayout.LayoutParams(dp(60), dp(48)));
        root.addView(secure, marginParams(0, 5, 0, 0));

        logoutButton.setOnClickListener(v -> logout());
        menu.setOnClickListener(v -> Toast.makeText(this, "Coin Rush India", Toast.LENGTH_SHORT).show());

        scroll.addView(root);
        setContentView(scroll);
        updateBalanceUI();

        // Balance is already loaded by login/session validation.
        // Manual "REFRESH BALANCE" performs the full cloud reconciliation when needed.

        getWindow().getDecorView().postDelayed(() -> {
            loadInterstitialAd();
            loadRewardedAd();
        }, 500);
    }

    private void claimDailyBonusFromDashboard(Button daily) {
    if (!authReady || prefs.getString("authToken", "").isEmpty()) {
        Toast.makeText(this, "Account server se connect ho raha hai. Thoda wait karein.", Toast.LENGTH_LONG).show();
        syncAccountNow();
        return;
    }

    String currentDate = new SimpleDateFormat(
            "yyyyMMdd",
            Locale.getDefault()
    ).format(new Date());

    if (currentDate.equals(
            prefs.getString("lastDailyBonus", "")
    )) {
        daily.setText("DAILY BONUS     CLAIMED");
        daily.setEnabled(false);
        return;
    }

    daily.setEnabled(false);
    daily.setText("SAVING +100 COINS...");

    final String token = prefs.getString("authToken", "");

    new Thread(() -> {
        try {
            ApiResponse api = postJson(
                    "/bonus/daily",
                    "{}",
                    token
            );

            if (api.code >= 200 && api.code < 300) {

                int serverBalance = parseIntField(
                        api.body,
                        "balance_coins",
                        -1
                );

                // Agar bonus response me balance nahi mila,
                // authenticated account se latest balance lo.
                if (serverBalance < 0) {
                    try {
                        ApiResponse me = postJson(
                                "/auth/me",
                                "{}",
                                token
                        );

                        if (me.code >= 200 && me.code < 300) {
                            serverBalance = parseIntField(
                                    me.body,
                                    "balance_coins",
                                    -1
                            );
                        }
                    } catch (Exception ignored) {
                    }
                }

                final int finalBalance = serverBalance;

                mainHandler.post(() -> {

                    if (finalBalance < 0) {
                        daily.setEnabled(true);
                        daily.setText("DAILY BONUS     +100 COINS");

                        Toast.makeText(
                                this,
                                "Bonus save hua, lekin balance read nahi hua. Refresh Balance dabayein.",
                                Toast.LENGTH_LONG
                        ).show();

                        syncAccountNow();
                        return;
                    }

                    totalCoins = finalBalance;

                    prefs.edit()
                            .putInt("totalCoins", totalCoins)
                            .putString("lastDailyBonus", currentDate)
                            .apply();

                    addCoinHistory("+100 DAILY BONUS");

                    daily.setText(
                            "DAILY BONUS     CLAIMED"
                    );
                    daily.setEnabled(false);

                    updateBalanceUI();

                    Toast.makeText(
                            this,
                            "+100 coins added to total balance!",
                            Toast.LENGTH_SHORT
                    ).show();
                });

            } else {

                mainHandler.post(() -> {
                    daily.setEnabled(true);
                    daily.setText(
                            "DAILY BONUS     +100 COINS"
                    );

                    Toast.makeText(
                            this,
                            friendlyAuthError(api.body),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }

        } catch (Exception ex) {

            mainHandler.post(() -> {
                daily.setEnabled(true);
                daily.setText(
                        "DAILY BONUS     +100 COINS"
                );

                Toast.makeText(
                        this,
                        "Server unavailable. Dobara try karein.",
                        Toast.LENGTH_LONG
                ).show();
            });
        }
    }).start();
    }

    private LinearLayout navItem(String icon, String label, boolean selected) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        TextView i = createText(icon, 21, selected ? ORANGE : GRAY, true);
        TextView l = createText(label, 9, selected ? ORANGE : GRAY, selected);
        item.addView(i, new LinearLayout.LayoutParams(-1, dp(30)));
        item.addView(l, new LinearLayout.LayoutParams(-1, dp(20)));
        item.setOnClickListener(v -> {
            if (label.equals("Games")) showGameScreen();
            else if (label.equals("Rewards")) Toast.makeText(this, "Rewards: daily bonus + game rewards", Toast.LENGTH_SHORT).show();
            else if (label.equals("Profile")) Toast.makeText(this, "Player: " + username, Toast.LENGTH_SHORT).show();
        });
        return item;
    }

    // =========================================================
    // LIVE TAP ARENA
    // =========================================================

    private void showGameScreen() {
        stopTimer();
        totalCoins = prefs.getInt("totalCoins", 0);
        bestScore = prefs.getInt("bestScore", 0);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout root = createRoot();
        root.setPadding(dp(10), dp(10), dp(10), dp(18));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        Button back = createButton("|", Color.TRANSPARENT);
        back.setTextSize(30);
        header.addView(back, new LinearLayout.LayoutParams(dp(52), dp(50)));
        TextView title = createText("LIVE TAP ARENA", 22, ORANGE, true);
        title.setGravity(Gravity.CENTER);
        header.addView(title, new LinearLayout.LayoutParams(0, dp(50), 1));
        TextView sound = createText("SOUND", 10, WHITE, true);
        sound.setGravity(Gravity.CENTER);
        header.addView(sound, new LinearLayout.LayoutParams(dp(52), dp(50)));
        root.addView(header);
        back.setOnClickListener(v -> { stopTimer(); showDashboard(); });

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.HORIZONTAL);
        info.setGravity(Gravity.CENTER);

        LinearLayout scoreCard = roundedCard(Color.rgb(10, 20, 31), 11);
        scoreCard.setOrientation(LinearLayout.VERTICAL);
        scoreCard.setGravity(Gravity.CENTER);
        scoreCard.addView(createText("SCORE", 9, GRAY, true));
        scoreText = createText("0", 22, YELLOW, true);
        scoreCard.addView(scoreText);
        info.addView(scoreCard, new LinearLayout.LayoutParams(0, dp(66), 1));

        LinearLayout timeCard = roundedCard(Color.rgb(10, 20, 31), 11);
        timeCard.setOrientation(LinearLayout.VERTICAL);
        timeCard.setGravity(Gravity.CENTER);
        timeCard.addView(createText("TIME", 9, GRAY, true));
        timerText = createText("30", 22, RED, true);
        timeCard.addView(timerText);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(0, dp(66), 1);
        timeParams.setMargins(dp(5), 0, dp(5), 0);
        info.addView(timeCard, timeParams);

        LinearLayout bestCard = roundedCard(Color.rgb(10, 20, 31), 11);
        bestCard.setOrientation(LinearLayout.VERTICAL);
        bestCard.setGravity(Gravity.CENTER);
        bestCard.addView(createText("BEST", 9, GRAY, true));
        bestText = createText(String.valueOf(bestScore), 22, GREEN, true);
        bestCard.addView(bestText);
        info.addView(bestCard, new LinearLayout.LayoutParams(0, dp(66), 1));
        root.addView(info);

        LinearLayout gameCard = roundedCard(Color.rgb(7, 17, 27), 16);
        gameCard.setOrientation(LinearLayout.VERTICAL);
        gameCard.setPadding(dp(7), dp(8), dp(7), dp(8));

        TextView balance = createText("BALANCE: " + totalCoins, 18, YELLOW, true);
        gameCard.addView(balance, new LinearLayout.LayoutParams(-1, dp(44)));

        FrameLayout arena = new FrameLayout(this);
        arena.setClipChildren(false);
        arena.setClipToPadding(false);
        gameCard.addView(arena, new LinearLayout.LayoutParams(-1, dp(390)));

        tapButton = createRoundButton("TAP!", ORANGE);
        tapButton.setTextSize(34);
        tapButton.setClickable(false);
        tapButton.setFocusable(false);
        FrameLayout.LayoutParams tapParams = new FrameLayout.LayoutParams(dp(185), dp(185));
        tapParams.gravity = Gravity.CENTER;
        arena.addView(tapButton, tapParams);

        arena.setOnTouchListener((view, event) -> {
            if (!gameRunning) return true;
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                coins++;
                updateScore();
                int index = event.getActionIndex();
                showFlyingCoin(arena, event.getX(index), event.getY(index));
                if (event.getPointerCount() >= 4 && messageText != null) messageText.setText("4X MULTI-TOUCH | +1 EACH");
            }
            return true;
        });

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        messageText = createText("TAP | COLLECT | RUSH!", 12, GRAY, true);
        bottom.addView(messageText);
        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        rewardButton = createCompactButton("WATCH AD | 2X", GREEN);
        rewardButton.setVisibility(View.GONE);
        restartButton = createCompactButton("PLAY AGAIN", RED);
        restartButton.setVisibility(View.GONE);
        buttons.addView(rewardButton, new LinearLayout.LayoutParams(0, dp(44), 1));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(0, dp(44), 1); rlp.setMargins(dp(5),0,0,0);
        buttons.addView(restartButton, rlp);
        bottom.addView(buttons);
        gameCard.addView(bottom);

        root.addView(gameCard, marginParams(0, 8, 0, 8));

        rewardButton.setOnClickListener(v -> showRewardedAd());
        restartButton.setOnClickListener(v -> startGame());

        scroll.addView(root);
        setContentView(scroll);
        startGame();
        getWindow().getDecorView().postDelayed(() -> { loadInterstitialAd(); loadRewardedAd(); }, 500);
    }

    private LinearLayout gameMetric(String label, String value, int color) {
        LinearLayout card = roundedCard(Color.rgb(10, 20, 31), 11);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        TextView l = createText(label, 9, GRAY, true);
        TextView v = createText(value, 22, color, true);
        card.addView(l); card.addView(v);
        return card;
    }

    private void logout() {
        stopTimer();
        final String token = prefs.getString("authToken", "");
        if (!token.isEmpty()) {
            new Thread(() -> {
                try { postJson("/auth/logout", "{}", token); } catch (Exception ignored) {}
            }).start();
        }
        prefs.edit()
                .remove("authToken")
                .putBoolean("loggedIn", false)
                .putBoolean("explicitLogout", true)
                .apply();
        authReady = false;
        showLoginScreen();
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
                        bestText.setText(String.valueOf(bestScore));
                    }

                    if (totalText != null) {
                        totalText.setText(
                                "AVAILABLE REWARD BALANCE: " + totalCoins
                        );
                    }

                    if (wealthBalanceText != null) {
                        wealthBalanceText.setText(totalCoins + "\nCOINS");
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

    private void showFlyingCoin(FrameLayout arena, float x, float y) {
        TextView flying = createText("+1", 22, YELLOW, true);
        flying.setGravity(Gravity.CENTER);
        flying.setBackground(roundedBackground(Color.rgb(255, 170, 0), 1000));
        flying.setTextColor(Color.WHITE);
        flying.setPadding(dp(7), dp(2), dp(7), dp(2));
        flying.setElevation(dp(8));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                dp(48), dp(36));
        lp.leftMargin = Math.max(0, (int) x - dp(24));
        lp.topMargin = Math.max(0, (int) y - dp(18));
        arena.addView(flying, lp);

        flying.setScaleX(0.65f);
        flying.setScaleY(0.65f);
        flying.setAlpha(1f);

        flying.animate()
                .translationY(-dp(72))
                .translationX((float) ((Math.random() - 0.5) * dp(44)))
                .alpha(0f)
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(650)
                .withEndAction(() -> arena.removeView(flying))
                .start();
    }

    private void startGame() {

        stopTimer();

        coins = 0;

        gameRunning = true;

        if (scoreText != null) {
            scoreText.setText("0");
        }

        if (timerText != null) {
            timerText.setText("30");
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

                            timerText.setText(String.valueOf(seconds));
                        }
                    }

                    @Override
                    public void onFinish() {

                        gameRunning = false;

                        if (timerText != null) {

                            timerText.setText("0");
                        }

                        gameOver();
                    }
                };

        timer.start();
    }

    private void gameOver() {

        stopTimer();

        gameRunning = false;

        totalGames++;
        prefs.edit().putInt("totalGames", totalGames).apply();

        if (tapButton != null) {

            tapButton.setEnabled(false);

            tapButton.setVisibility(
                    View.GONE
            );
        }

        // Save best score locally immediately and also persist it to the
        // authenticated cloud account. The backend /score endpoint uses
        // GREATEST(), so an old score can never overwrite a newer best.
        if (coins > bestScore) {
            bestScore = coins;
        }

        prefs.edit()
                .putInt("bestScore", bestScore)
                .putInt("totalGames", totalGames)
                .apply();

        if (authReady && !prefs.getString("authToken", "").isEmpty() && coins > 0) {
            saveBestScoreToServer(coins);
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
                if (newBalance < 0) {
                    Toast.makeText(
                            this,
                            "Game reward response me balance nahi mila. Refresh Balance dabayein.",
                            Toast.LENGTH_LONG
                    ).show();
                    syncAccountNow();
                    return;
                }

                // Server already added the reward. Use its returned balance immediately.
                totalCoins = newBalance;
                prefs.edit()
                        .putInt("totalCoins", totalCoins)
                        .putInt("bestScore", bestScore)
                        .putInt("totalGames", totalGames)
                        .apply();

                updateBalanceUI();
                if (bestText != null) bestText.setText(String.valueOf(bestScore));

                addCoinHistory("+" + roundCoins + " GAME REWARD");

                Toast.makeText(
                        this,
                        "+" + roundCoins + " coins added. Balance: " + totalCoins,
                        Toast.LENGTH_SHORT
                ).show();
            });
        }

        updateScore();
        updateBalanceUI();

        if (bestText != null) {

            bestText.setText(String.valueOf(bestScore));
        }

        if (totalText != null) {

            totalText.setText(
                    "AVAILABLE REWARD BALANCE: "
                            + totalCoins
            );
        }

        if (wealthBalanceText != null) {
            wealthBalanceText.setText(totalCoins + "\nCOINS");
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
                if(api.code>=200 && api.code<300){ int newBalance=parseIntField(response,"balance_coins",totalCoins); mainHandler.post(() -> { coins=coins*2; totalCoins=newBalance; if(coins>bestScore)bestScore=coins; prefs.edit().putInt("bestScore",bestScore).putInt("totalCoins",totalCoins).apply(); saveBestScoreToServer(coins); addCoinHistory("+"+bonus+" AD REWARD"); updateScore();updateBalanceUI();if(bestText!=null)bestText.setText(String.valueOf(bestScore));if(messageText!=null)messageText.setText("REWARD! Coins doubled!");Toast.makeText(this,"+"+bonus+" bonus coins!",Toast.LENGTH_SHORT).show(); }); }
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
                v -> showDashboard()
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
                v -> showDashboard()
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

    private interface ServerCoinCallback {
        void onResult(boolean success, int newBalance);
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

    private String readResponse(HttpURLConnection connection) throws Exception {
        InputStream stream = null;
        try {
            int code = connection.getResponseCode();
            stream = (code >= 200 && code < 400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (stream == null) return "";

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            );

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            return result.toString();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private ApiResponse getJson(String path) throws Exception {
        HttpURLConnection c = null;
        try {
            URL url = new URL(API_BASE_URL + path);
            c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(12000);
            c.setReadTimeout(15000);
            c.setUseCaches(false);
            c.setRequestProperty("Accept", "application/json");
            int code = c.getResponseCode();
            return new ApiResponse(code, readResponse(c));
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private ApiResponse postJson(String path, String body, String token) throws Exception {
        HttpURLConnection c=null;
        try {
            URL url=new URL(API_BASE_URL+path); c=(HttpURLConnection)url.openConnection(); c.setRequestMethod("POST"); c.setConnectTimeout(12000); c.setReadTimeout(15000); c.setUseCaches(false); c.setDoOutput(true); c.setRequestProperty("Content-Type","application/json; charset=UTF-8"); c.setRequestProperty("Accept","application/json");
            if(token!=null && !token.isEmpty()) c.setRequestProperty("Authorization","Bearer "+token);
            try(OutputStream os=c.getOutputStream()){os.write(body.getBytes(StandardCharsets.UTF_8));}
            int code=c.getResponseCode(); return new ApiResponse(code, readResponse(c));
        } finally { if(c!=null)c.disconnect(); }
    }

    private void saveBestScoreToServer(int score) {
        final String token = prefs.getString("authToken", "");
        if (token.isEmpty() || score <= 0) return;

        new Thread(() -> {
            try {
                ApiResponse api = postJson(
                        "/score",
                        "{\"score\":" + score + "}",
                        token
                );

                if (api.code >= 200 && api.code < 300) {
                    int serverBest = parseIntField(api.body, "best_score", score);
                    mainHandler.post(() -> {
                        if (serverBest > bestScore) bestScore = serverBest;
                        prefs.edit().putInt("bestScore", bestScore).apply();
                        if (bestText != null) {
                            bestText.setText(String.valueOf(bestScore));
                        }
                    });
                }
            } catch (Exception ignored) {
                // Local best score remains saved if the server is temporarily unavailable.
            }
        }).start();
    }

    private void addCoinsToServer(int amount, String reason, ServerCoinCallback callback) {
    if (amount <= 0) {
        if (callback != null) {
            mainHandler.post(() -> callback.onResult(false, totalCoins));
        }
        return;
    }

    if (!authReady || prefs.getString("authToken", "").isEmpty()) {
        if (callback != null) {
            mainHandler.post(() -> callback.onResult(false, totalCoins));
        }
        syncAccountNow();
        return;
    }

    final String token = prefs.getString("authToken", "");

    new Thread(() -> {
        try {
            ApiResponse api = postJson(
                    "/coins/add",
                    "{\"coins\":" + amount + "}",
                    token
            );

            if (api.code >= 200 && api.code < 300) {

                int serverBalance = parseIntField(
                        api.body,
                        "balance_coins",
                        -1
                );

                if (serverBalance < 0) {
                    try {
                        ApiResponse me = postJson(
                                "/auth/me",
                                "{}",
                                token
                        );

                        if (me.code >= 200 && me.code < 300) {
                            serverBalance = parseIntField(
                                    me.body,
                                    "balance_coins",
                                    -1
                            );
                        }
                    } catch (Exception ignored) {
                    }
                }

                final int finalBalance = serverBalance;

                mainHandler.post(() -> {

                    if (finalBalance < 0) {
                        if (callback != null) {
                            callback.onResult(false, totalCoins);
                        }
                        return;
                    }

                    totalCoins = finalBalance;

                    prefs.edit()
                            .putInt("totalCoins", totalCoins)
                            .apply();

                    updateBalanceUI();

                    if (callback != null) {
                        callback.onResult(true, totalCoins);
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

    private int parseIntField(String json, String field, int fallback) {
        try {
            if (json == null) return fallback;
            String key = "\"" + field + "\"";
            int searchFrom = 0;

            while (searchFrom < json.length()) {
                int k = json.indexOf(key, searchFrom);
                if (k < 0) return fallback;

                int colon = json.indexOf(':', k + key.length());
                if (colon < 0) return fallback;

                int i = colon + 1;
                while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;

                // Accept both JSON numbers and quoted numeric values.
                boolean quoted = i < json.length() && json.charAt(i) == '\"';
                if (quoted) i++;

                int j = i;
                if (j < json.length() && json.charAt(j) == '-') j++;
                int digitStart = j;
                while (j < json.length() && Character.isDigit(json.charAt(j))) j++;

                if (j > digitStart) {
                    return Integer.parseInt(json.substring(i, j));
                }

                searchFrom = k + key.length();
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

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

            scoreText.setText(String.valueOf(coins));
        }
    }

    private void updateBalanceUI() {

        if (balanceButton != null) {
            balanceButton.setText("BALANCE: " + totalCoins + " COINS");
        }
        if (wealthBalanceText != null) {
            wealthBalanceText.setText(totalCoins + "\nCOINS");
        }

        if (totalText != null) {
            totalText.setText("TOTAL COINS: " + totalCoins);
        }
        if (dashboardValueText != null) {
            dashboardValueText.setText(
                    "VALUE: " + String.format(Locale.getDefault(), "%.2f", totalCoins / 100.0)
            );
        }
        if (dashboardBestText != null) {
            dashboardBestText.setText(String.valueOf(bestScore));
        }
        if (dashboardGamesText != null) {
            dashboardGamesText.setText(String.valueOf(totalGames));
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
