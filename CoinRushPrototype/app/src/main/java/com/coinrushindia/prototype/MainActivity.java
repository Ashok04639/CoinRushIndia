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
import android.view.ViewGroup;
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private android.content.SharedPreferences prefs;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String username = "";
    private int coins = 0;
    private int bestScore = 0;
    private int totalCoins = 0;
    private boolean gameRunning = false;

    private CountDownTimer timer;

    private TextView balanceText;
    private TextView wealthText;
    private TextView bestText;
    private TextView totalGamesText;
    private TextView scoreText;
    private TextView timerText;
    private TextView messageText;

    private Button dailyBonusButton;
    private Button tapButton;
    private Button rewardButton;
    private Button restartButton;

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    private static final String API_BASE_URL =
            "https://coinrushindia.onrender.com/api/v1";

    private static final String INTERSTITIAL_AD_ID =
            "ca-app-pub-4590159013838755/9228973931";

    private static final String REWARDED_AD_ID =
            "ca-app-pub-4590159013838755/5227139421";

    private final int BG = Color.rgb(7, 14, 24);
    private final int CARD = Color.rgb(18, 29, 43);
    private final int CARD2 = Color.rgb(24, 37, 53);
    private final int WHITE = Color.WHITE;
    private final int GREEN = Color.rgb(43, 195, 99);
    private final int ORANGE = Color.rgb(255, 145, 35);
    private final int RED = Color.rgb(232, 70, 70);
    private final int GRAY = Color.rgb(145, 157, 174);
    private final int YELLOW = Color.rgb(255, 210, 45);
    private final int BLUE = Color.rgb(55, 111, 224);
    private final int PURPLE = Color.rgb(151, 76, 224);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, initializationStatus -> {});

        prefs = getSharedPreferences("CoinRushIndia", MODE_PRIVATE);

        username = prefs.getString("username", "");
        totalCoins = prefs.getInt("totalCoins", 0);
        bestScore = prefs.getInt("bestScore", 0);

        if (prefs.getBoolean("loggedIn", false) && !username.isEmpty()) {
            showDashboard();
            syncUserWithServer();
        } else {
            showLoginScreen();
        }
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private void showDashboard() {
        stopTimer();

        totalCoins = prefs.getInt("totalCoins", 0);
        bestScore = prefs.getInt("bestScore", 0);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(10), dp(14), dp(20));
        root.setBackgroundColor(BG);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, dp(5), 0, dp(5));

        TextView menu = createText("☰", 28, WHITE, false);
        menu.setGravity(Gravity.CENTER);
        header.addView(menu, new LinearLayout.LayoutParams(dp(45), dp(55)));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setPadding(dp(5), 0, 0, 0);

        TextView brandTitle = createText("COIN RUSH INDIA", 21, WHITE, true);
        brandTitle.setGravity(Gravity.START);
        brand.addView(brandTitle);

        TextView brandSub = createText("INDIA  •  ELITE REWARDS", 10, ORANGE, true);
        brandSub.setGravity(Gravity.START);
        brand.addView(brandSub);

        header.addView(brand, new LinearLayout.LayoutParams(0, dp(55), 1));

        Button logout = smallButton("LOGOUT", RED, 88);
        header.addView(logout);

        root.addView(header);

        // Welcome card
        LinearLayout welcome = cardLayout(CARD2, 16);
        LinearLayout welcomeRow = new LinearLayout(this);
        welcomeRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView crown = createText("♛", 34, YELLOW, true);
        crown.setGravity(Gravity.CENTER);
        crown.setBackground(roundBg(Color.rgb(38, 43, 45), 100));
        welcomeRow.addView(crown, new LinearLayout.LayoutParams(dp(62), dp(62)));

        LinearLayout welcomeInfo = new LinearLayout(this);
        welcomeInfo.setOrientation(LinearLayout.VERTICAL);
        welcomeInfo.setPadding(dp(12), 0, 0, 0);

        TextView wb = createText("WELCOME BACK,", 12, GRAY, true);
        wb.setGravity(Gravity.START);
        welcomeInfo.addView(wb);

        TextView name = createText(username.toUpperCase(Locale.getDefault()), 23, WHITE, true);
        name.setGravity(Gravity.START);
        welcomeInfo.addView(name);

        TextView member = createText("Member • Coin Rush India", 10, GRAY, false);
        member.setGravity(Gravity.START);
        welcomeInfo.addView(member);

        welcomeRow.addView(welcomeInfo, new LinearLayout.LayoutParams(0, dp(70), 1));

        TextView elite = createText("◆\nELITE", 12, YELLOW, true);
        elite.setGravity(Gravity.CENTER);
        elite.setBackground(roundBg(Color.rgb(25, 29, 28), 12));
        welcomeRow.addView(elite, new LinearLayout.LayoutParams(dp(65), dp(62)));

        welcome.addView(welcomeRow);
        root.addView(welcome, marginParams(0, 6, 0, 8));

        // Wealth card
        LinearLayout wealth = cardLayout(Color.rgb(17, 31, 47), 12);
        TextView wt = createText("TOTAL WEALTH", 11, GRAY, true);
        wt.setGravity(Gravity.START);
        wealth.addView(wt);

        wealthText = createText(formatNumber(totalCoins) + "\nCOINS", 31, YELLOW, true);
        wealthText.setGravity(Gravity.START);
        wealthText.setPadding(0, dp(3), 0, 0);
        wealth.addView(wealthText);

        TextView value = createText("≈ ₹" + String.format(Locale.US, "%.2f", totalCoins / 100.0), 12, GREEN, true);
        value.setGravity(Gravity.START);
        wealth.addView(value);

        root.addView(wealth, marginParams(0, 0, 0, 8));

        // Stats row
        LinearLayout stats = new LinearLayout(this);
        stats.setGravity(Gravity.CENTER);
        stats.addView(statCard("🏆", "BEST SCORE", String.valueOf(bestScore), YELLOW),
                new LinearLayout.LayoutParams(0, dp(78), 1));
        stats.addView(statCard("◷", "ROUND TIME", "30 SEC", PURPLE),
                new LinearLayout.LayoutParams(0, dp(78), 1));
        stats.addView(statCard("🎮", "TOTAL GAMES",
                        String.valueOf(prefs.getInt("totalGames", 0)), GREEN),
                new LinearLayout.LayoutParams(0, dp(78), 1));
        root.addView(stats, marginParams(0, 0, 0, 8));

        // Daily bonus
        dailyBonusButton = createButton("🎁   DAILY BONUS        +100 COINS", GREEN);
        dailyBonusButton.setTextSize(14);
        root.addView(dailyBonusButton);

        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String lastBonus = prefs.getString("lastDailyBonus", "");
        if (today.equals(lastBonus)) {
            dailyBonusButton.setText("🎁   DAILY BONUS        CLAIMED ✓");
            dailyBonusButton.setEnabled(false);
        }

        dailyBonusButton.setOnClickListener(v -> claimDailyBonus());

        // Utility row
        LinearLayout utilities = new LinearLayout(this);
        utilities.setGravity(Gravity.CENTER);
        Button refresh = smallButton("⟳  REFRESH BALANCE", BLUE, 0);
        Button history = smallButton("▣  COIN HISTORY", PURPLE, 0);
        utilities.addView(refresh, new LinearLayout.LayoutParams(0, dp(50), 1));
        utilities.addView(history, new LinearLayout.LayoutParams(0, dp(50), 1));

        refresh.setOnClickListener(v -> {
            syncUserWithServer();
            Toast.makeText(this, "Balance refreshing...", Toast.LENGTH_SHORT).show();
        });
        history.setOnClickListener(v -> showCoinHistory());

        root.addView(utilities, marginParams(0, 2, 0, 7));

        // Withdraw
        Button withdraw = createButton("▣   WITHDRAW  •  BEP-20", BLUE);
        withdraw.setTextSize(14);
        root.addView(withdraw);
        withdraw.setOnClickListener(v -> showWithdrawalScreen());

        // Watch ad
        Button watchAd = createButton("▣   WATCH AD  •  2X REWARD", YELLOW);
        watchAd.setTextColor(Color.BLACK);
        watchAd.setTextSize(14);
        root.addView(watchAd);
        watchAd.setOnClickListener(v -> showRewardedAd());

        // Live arena card
        LinearLayout arena = cardLayout(CARD2, 15);

        TextView arenaTitle = createText("•  LIVE TAP ARENA  •", 16, ORANGE, true);
        arenaTitle.setGravity(Gravity.CENTER);
        arena.addView(arenaTitle);

        TextView arenaSub = createText("TAP  •  COLLECT  •  RUSH!", 11, GRAY, true);
        arenaSub.setGravity(Gravity.CENTER);
        arena.addView(arenaSub);

        Button play = createButton("▶   PLAY NOW", ORANGE);
        play.setTextSize(22);
        arena.addView(play, marginParams(0, 10, 0, 2));

        TextView tapStart = createText("TAP TO START", 10, WHITE, true);
        tapStart.setGravity(Gravity.CENTER);
        arena.addView(tapStart);

        play.setOnClickListener(v -> showGameScreen());

        root.addView(arena, marginParams(0, 8, 0, 8));

        // Bottom navigation
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(0, dp(4), 0, dp(4));
        nav.setBackground(roundBg(Color.rgb(13, 23, 35), 14));

        nav.addView(navItem("⌂", "Home", true),
                new LinearLayout.LayoutParams(0, dp(62), 1));
        nav.addView(navItem("🎮", "Games", false),
                new LinearLayout.LayoutParams(0, dp(62), 1));
        nav.addView(navItem("🎁", "Rewards", false),
                new LinearLayout.LayoutParams(0, dp(62), 1));
        nav.addView(navItem("♙", "Profile", false),
                new LinearLayout.LayoutParams(0, dp(62), 1));

        root.addView(nav, marginParams(0, 2, 0, 0));

        logout.setOnClickListener(v -> logout());

        menu.setOnClickListener(v ->
                Toast.makeText(this, "Coin Rush India", Toast.LENGTH_SHORT).show());

        scroll.addView(root);
        setContentView(scroll);
    }

    private LinearLayout statCard(String icon, String label, String value, int valueColor) {
        LinearLayout box = cardLayout(Color.rgb(10, 21, 32), 10);
        box.setGravity(Gravity.CENTER);

        TextView top = createText(icon + "  " + label, 8, GRAY, true);
        top.setGravity(Gravity.CENTER);
        box.addView(top);

        TextView val = createText(value, 15, valueColor, true);
        val.setGravity(Gravity.CENTER);
        box.addView(val);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(78), 1);
        p.setMargins(dp(3), 0, dp(3), 0);
        box.setLayoutParams(p);
        return box;
    }

    private LinearLayout navItem(String icon, String label, boolean active) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);

        TextView i = createText(icon, 22, active ? ORANGE : GRAY, true);
        i.setGravity(Gravity.CENTER);
        item.addView(i);

        TextView l = createText(label, 10, active ? ORANGE : GRAY, true);
        l.setGravity(Gravity.CENTER);
        item.addView(l);

        if (label.equals("Games")) {
            item.setOnClickListener(v -> showGameScreen());
        } else if (label.equals("Rewards")) {
            item.setOnClickListener(v -> showRewardedAd());
        } else if (label.equals("Profile")) {
            item.setOnClickListener(v ->
                    Toast.makeText(this, "Player: " + username, Toast.LENGTH_SHORT).show());
        }

        return item;
    }

    // =========================================================
    // GAME SCREEN
    // =========================================================

    private void showGameScreen() {
        stopTimer();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = createRoot();

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        Button back = smallButton("‹", GRAY, 50);
        top.addView(back);

        TextView title = createText("LIVE TAP ARENA", 22, ORANGE, true);
        top.addView(title, new LinearLayout.LayoutParams(0, dp(55), 1));

        TextView sound = createText("◖", WHITE, false);
        sound.setTextSize(25);
        sound.setGravity(Gravity.CENTER);
        top.addView(sound, new LinearLayout.LayoutParams(dp(50), dp(55)));

        root.addView(top);

        LinearLayout stats = new LinearLayout(this);
        stats.addView(gameStat("SCORE", "0", YELLOW),
                new LinearLayout.LayoutParams(0, dp(68), 1));
        stats.addView(gameStat("TIME", "30", RED),
                new LinearLayout.LayoutParams(0, dp(68), 1));
        stats.addView(gameStat("BEST", String.valueOf(bestScore), GREEN),
                new LinearLayout.LayoutParams(0, dp(68), 1));
        root.addView(stats, marginParams(0, 4, 0, 10));

        scoreText = createText("0 COINS", 24, YELLOW, true);
        scoreText.setBackground(roundBg(Color.rgb(28, 28, 31), 15));
        root.addView(scoreText, marginParams(0, 0, 0, 10));

        timerText = createText("TIME: 30", 28, WHITE, true);
        root.addView(timerText, marginParams(0, 5, 0, 0));

        messageText = createText("TAP • COLLECT • RUSH!", 15, GRAY, true);
        root.addView(messageText);

        tapButton = createRoundButton("TAP!", ORANGE);
        LinearLayout.LayoutParams tapParams =
                new LinearLayout.LayoutParams(dp(280), dp(280));
        tapParams.gravity = Gravity.CENTER;
        tapParams.setMargins(0, dp(18), 0, dp(12));
        root.addView(tapButton, tapParams);

        rewardButton = createButton("WATCH AD • 2X REWARD", GREEN);
        rewardButton.setVisibility(View.GONE);
        root.addView(rewardButton);
        rewardButton.setOnClickListener(v -> showRewardedAd());

        restartButton = createButton("RESTART GAME", BLUE);
        restartButton.setVisibility(View.GONE);
        root.addView(restartButton);
        restartButton.setOnClickListener(v -> startGame());

        Button home = createButton("BACK TO DASHBOARD", GRAY);
        root.addView(home);
        home.setOnClickListener(v -> showDashboard());

        tapButton.setOnClickListener(v -> {
            if (!gameRunning) return;
            coins++;
            updateScore();
        });

        back.setOnClickListener(v -> {
            stopTimer();
            showDashboard();
        });

        scroll.addView(root);
        setContentView(scroll);

        startGame();
    }

    private LinearLayout gameStat(String label, String value, int color) {
        LinearLayout box = cardLayout(Color.rgb(10, 18, 27), 10);
        box.setGravity(Gravity.CENTER);

        TextView a = createText(label, 9, GRAY, true);
        TextView b = createText(value, 19, color, true);
        box.addView(a);
        box.addView(b);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(68), 1);
        p.setMargins(dp(3), 0, dp(3), 0);
        box.setLayoutParams(p);
        return box;
    }

    private void startGame() {
        stopTimer();

        coins = 0;
        gameRunning = true;

        if (scoreText != null) scoreText.setText("0 COINS");
        if (timerText != null) timerText.setText("TIME: 30");
        if (messageText != null) messageText.setText("TAP • COLLECT • RUSH!");

        if (tapButton != null) {
            tapButton.setVisibility(View.VISIBLE);
            tapButton.setEnabled(true);
        }
        if (rewardButton != null) rewardButton.setVisibility(View.GONE);
        if (restartButton != null) restartButton.setVisibility(View.GONE);

        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long ms) {
                int sec = (int) Math.ceil(ms / 1000.0);
                if (timerText != null) timerText.setText("TIME: " + sec);
            }

            @Override
            public void onFinish() {
                gameRunning = false;
                if (timerText != null) timerText.setText("TIME: 0");
                gameOver();
            }
        }.start();
    }

    private void gameOver() {
        stopTimer();
        gameRunning = false;

        if (tapButton != null) {
            tapButton.setEnabled(false);
            tapButton.setVisibility(View.GONE);
        }

        int oldBest = bestScore;
        if (coins > bestScore) bestScore = coins;

        prefs.edit()
                .putInt("bestScore", bestScore)
                .putInt("totalGames", prefs.getInt("totalGames", 0) + 1)
                .apply();

        if (coins > 0) {
            final int reward = coins;
            addCoinsToServer(reward, "GAME REWARD", (success, newBalance) -> {
                if (!success) {
                    Toast.makeText(this,
                            "Game reward save nahi hua. Internet check karo.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                totalCoins = newBalance;
                prefs.edit().putInt("totalCoins", totalCoins).apply();
                addCoinHistory("+" + reward + " GAME REWARD");
                updateDashboardValues();
            });
        }

        if (messageText != null) {
            messageText.setText("GAME OVER • " + coins + " COINS!");
        }
        if (rewardButton != null) rewardButton.setVisibility(View.VISIBLE);
        if (restartButton != null) restartButton.setVisibility(View.VISIBLE);

        if (bestScore > oldBest) {
            Toast.makeText(this, "NEW BEST SCORE!", Toast.LENGTH_SHORT).show();
        }

        showInterstitialAd();
    }

    private void updateScore() {
        if (scoreText != null) scoreText.setText(coins + " COINS");
    }

    // =========================================================
    // DAILY BONUS
    // =========================================================

    private void claimDailyBonus() {
        String currentDate =
                new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        String savedDate = prefs.getString("lastDailyBonus", "");

        if (currentDate.equals(savedDate)) {
            Toast.makeText(this, "Aaj ka bonus already claim ho chuka hai.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        dailyBonusButton.setEnabled(false);

        addCoinsToServer(100, "DAILY BONUS", (success, newBalance) -> {
            if (!success) {
                dailyBonusButton.setEnabled(true);
                Toast.makeText(this,
                        "Bonus save nahi hua. Internet check karo.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            totalCoins = newBalance;
            prefs.edit()
                    .putInt("totalCoins", totalCoins)
                    .putString("lastDailyBonus", currentDate)
                    .apply();

            addCoinHistory("+100 DAILY BONUS");
            dailyBonusButton.setText("🎁   DAILY BONUS        CLAIMED ✓");
            updateDashboardValues();

            Toast.makeText(this, "+100 coins bonus!", Toast.LENGTH_SHORT).show();
        });
    }

    // =========================================================
    // HISTORY
    // =========================================================

    private void addCoinHistory(String entry) {
        String old = prefs.getString("coinHistory", "");
        String time = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        String value = time + " - " + entry;
        String result = old.isEmpty() ? value : value + "\n" + old;

        prefs.edit().putString("coinHistory", result).apply();
    }

    private void showCoinHistory() {
        LinearLayout root = createRoot();

        TextView title = createText("COIN HISTORY", 27, WHITE, true);
        root.addView(title);

        String history = prefs.getString("coinHistory", "");

        TextView body = createText(
                history.isEmpty() ? "No coin history yet." : history,
                15, history.isEmpty() ? GRAY : WHITE, false);
        body.setGravity(Gravity.START);
        body.setPadding(dp(8), dp(18), dp(8), dp(18));
        root.addView(body);

        Button back = createButton("BACK TO DASHBOARD", GRAY);
        root.addView(back);
        back.setOnClickListener(v -> showDashboard());

        setContentView(wrapScroll(root));
    }

    // =========================================================
    // WITHDRAWAL
    // =========================================================

    private void showWithdrawalScreen() {
        stopTimer();

        LinearLayout root = createRoot();

        TextView title = createText("BEP-20 WITHDRAWAL", 27, WHITE, true);
        root.addView(title);

        TextView balance = createText(
                "AVAILABLE: " + totalCoins + " COINS",
                18, YELLOW, true);
        root.addView(balance, marginParams(0, 12, 0, 18));

        TextView info = createText(
                "Enter your BEP-20 wallet address.\nUPI is not used.",
                14, GRAY, false);
        info.setGravity(Gravity.START);
        root.addView(info);

        EditText wallet = createInput("BEP-20 Wallet Address (0x...)");
        root.addView(wallet);

        EditText amount = createInput("Withdrawal Coins");
        amount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        root.addView(amount);

        TextView min = createText("Minimum withdrawal: 100 coins",
                13, GRAY, false);
        min.setGravity(Gravity.START);
        root.addView(min, marginParams(0, 4, 0, 12));

        Button submit = createButton("SUBMIT WITHDRAWAL", GREEN);
        root.addView(submit);

        Button history = createButton("WITHDRAWAL HISTORY", BLUE);
        root.addView(history);

        Button back = createButton("BACK TO DASHBOARD", GRAY);
        root.addView(back);

        submit.setOnClickListener(v -> {
            String address = wallet.getText().toString().trim();
            String amountText = amount.getText().toString().trim();

            if (!isValidBep20Address(address)) {
                Toast.makeText(this, "Valid BEP-20 address enter karo.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            int value;
            try {
                value = Integer.parseInt(amountText);
            } catch (Exception e) {
                Toast.makeText(this, "Amount galat hai.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            totalCoins = prefs.getInt("totalCoins", 0);

            if (value < 100) {
                Toast.makeText(this, "Minimum withdrawal 100 coins hai.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (value > totalCoins) {
                Toast.makeText(this, "Balance kam hai.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            totalCoins -= value;
            prefs.edit().putInt("totalCoins", totalCoins).apply();

            String time = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            addWithdrawalHistory(time + " | " + value + " coins | "
                    + address + " | PENDING");

            addCoinHistory("-" + value + " WITHDRAWAL REQUEST");

            Toast.makeText(this, "Withdrawal request submitted.",
                    Toast.LENGTH_LONG).show();

            showDashboard();
        });

        history.setOnClickListener(v -> showWithdrawalHistory());
        back.setOnClickListener(v -> showDashboard());

        setContentView(wrapScroll(root));
    }

    private boolean isValidBep20Address(String address) {
        if (address.length() != 42) return false;
        if (!address.startsWith("0x") && !address.startsWith("0X")) return false;

        for (int i = 2; i < address.length(); i++) {
            char c = address.charAt(i);
            boolean ok = (c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'f')
                    || (c >= 'A' && c <= 'F');
            if (!ok) return false;
        }
        return true;
    }

    private void addWithdrawalHistory(String entry) {
        String old = prefs.getString("withdrawalHistory", "");
        String value = old.isEmpty() ? entry : entry + "\n" + old;
        prefs.edit().putString("withdrawalHistory", value).apply();
    }

    private void showWithdrawalHistory() {
        LinearLayout root = createRoot();

        TextView title = createText("WITHDRAWAL HISTORY", 27, WHITE, true);
        root.addView(title);

        String history = prefs.getString("withdrawalHistory", "");

        TextView body = createText(
                history.isEmpty() ? "No withdrawal requests yet." : history,
                14, history.isEmpty() ? GRAY : WHITE, false);
        body.setGravity(Gravity.START);
        body.setPadding(dp(8), dp(18), dp(8), dp(18));
        root.addView(body);

        Button back = createButton("BACK TO WITHDRAWAL", GRAY);
        root.addView(back);
        back.setOnClickListener(v -> showWithdrawalScreen());

        setContentView(wrapScroll(root));
    }

    // =========================================================
    // LOGIN / SIGNUP
    // =========================================================

    private void showLoginScreen() {
        stopTimer();

        LinearLayout root = createRoot();

        TextView logo = createText("♛", 48, YELLOW, true);
        root.addView(logo);

        TextView title = createText("COIN RUSH INDIA", 29, WHITE, true);
        root.addView(title);

        TextView sub = createText("LOGIN TO PLAY • EARN • RUSH",
                13, GRAY, true);
        root.addView(sub, marginParams(0, 5, 0, 22));

        EditText user = createInput("Username");
        root.addView(user);

        EditText pass = createInput("Password");
        pass.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(pass);

        Button login = createButton("LOGIN", GREEN);
        root.addView(login);

        Button signup = createButton("CREATE NEW ACCOUNT", ORANGE);
        root.addView(signup);

        login.setOnClickListener(v -> {
            String u = user.getText().toString().trim();
            String p = pass.getText().toString();

            String savedUser = prefs.getString("username", "");
            String savedPass = prefs.getString("password", "");

            if (!u.isEmpty() && !p.isEmpty()
                    && savedUser.equals(u)
                    && savedPass.equals(hashPassword(p))) {

                username = u;
                prefs.edit().putBoolean("loggedIn", true).apply();
                totalCoins = prefs.getInt("totalCoins", 0);
                bestScore = prefs.getInt("bestScore", 0);

                showDashboard();
                syncUserWithServer();
            } else {
                Toast.makeText(this, "Username ya password galat hai.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        signup.setOnClickListener(v -> showSignupScreen());

        setContentView(wrapScroll(root));
    }

    private void showSignupScreen() {
        stopTimer();

        LinearLayout root = createRoot();

        TextView logo = createText("♛", 48, YELLOW, true);
        root.addView(logo);

        TextView title = createText("CREATE ACCOUNT", 28, WHITE, true);
        root.addView(title, marginParams(0, 0, 0, 20));

        EditText user = createInput("Choose Username");
        root.addView(user);

        EditText pass = createInput("Choose Password");
        pass.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(pass);

        EditText confirm = createInput("Confirm Password");
        confirm.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(confirm);

        Button create = createButton("CREATE ACCOUNT", GREEN);
        root.addView(create);

        Button back = createButton("BACK TO LOGIN", GRAY);
        root.addView(back);

        create.setOnClickListener(v -> {
            String u = user.getText().toString().trim();
            String p = pass.getText().toString();
            String c = confirm.getText().toString();

            if (u.length() < 3 || p.length() < 4 || !p.equals(c)) {
                Toast.makeText(this,
                        "Username/password details check karo.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (!prefs.getString("username", "").isEmpty()) {
                Toast.makeText(this, "Account already bana hua hai.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            username = u;
            totalCoins = 0;
            bestScore = 0;

            prefs.edit()
                    .putString("username", u)
                    .putString("password", hashPassword(p))
                    .putBoolean("loggedIn", true)
                    .putInt("totalCoins", 0)
                    .putInt("bestScore", 0)
                    .apply();

            showDashboard();
            syncUserWithServer();
        });

        back.setOnClickListener(v -> showLoginScreen());

        setContentView(wrapScroll(root));
    }

    private void logout() {
        stopTimer();
        prefs.edit().putBoolean("loggedIn", false).apply();
        username = "";
        showLoginScreen();
    }

    // =========================================================
    // ADMOB
    // =========================================================

    private void loadInterstitialAd() {
        InterstitialAd.load(this, INTERSTITIAL_AD_ID,
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        interstitialAd = ad;
                        ad.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        interstitialAd = null;
                                        loadInterstitialAd();
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError error) {
                                        interstitialAd = null;
                                        loadInterstitialAd();
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        interstitialAd = null;
                    }
                });
    }

    private void showInterstitialAd() {
        if (interstitialAd != null) {
            interstitialAd.show(this);
            interstitialAd = null;
        } else {
            loadInterstitialAd();
        }
    }

    private void loadRewardedAd() {
        RewardedAd.load(this, REWARDED_AD_ID,
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd ad) {
                        rewardedAd = ad;
                        ad.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        rewardedAd = null;
                                        loadRewardedAd();
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError error) {
                                        rewardedAd = null;
                                        loadRewardedAd();
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        rewardedAd = null;
                    }
                });
    }

    private void showRewardedAd() {
        if (rewardedAd == null) {
            Toast.makeText(this, "Ad abhi ready nahi hai.",
                    Toast.LENGTH_SHORT).show();
            loadRewardedAd();
            return;
        }

        RewardedAd ad = rewardedAd;
        rewardedAd = null;

        ad.show(this, rewardItem -> {
            // Dashboard ad reward: +100 coins.
            addCoinsToServer(100, "AD REWARD", (success, newBalance) -> {
                if (!success) {
                    Toast.makeText(this,
                            "Ad reward save nahi hua.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                totalCoins = newBalance;
                prefs.edit().putInt("totalCoins", totalCoins).apply();
                addCoinHistory("+100 AD REWARD");
                updateDashboardValues();

                Toast.makeText(this,
                        "+100 ad reward coins!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // =========================================================
    // BACKEND
    // =========================================================

    private String getDeviceIdValue() {
        String id = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        if (id == null || id.trim().length() < 6) {
            return "android-" + android.os.Build.SERIAL;
        }
        return id.trim();
    }

    private void syncUserWithServer() {
        final String deviceId = getDeviceIdValue();

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(API_BASE_URL + "/user");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type", "application/json; charset=UTF-8");

                String body = "{\"device_id\":\""
                        + jsonEscape(deviceId) + "\"}";

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int code = connection.getResponseCode();
                String response = readResponse(connection);

                if (code >= 200 && code < 300) {
                    int serverBalance = parseIntField(
                            response, "balance_coins", totalCoins);
                    int serverBest = parseIntField(
                            response, "best_score", bestScore);

                    mainHandler.post(() -> {
                        totalCoins = serverBalance;
                        bestScore = Math.max(bestScore, serverBest);

                        prefs.edit()
                                .putInt("totalCoins", totalCoins)
                                .putInt("bestScore", bestScore)
                                .apply();

                        updateDashboardValues();
                    });
                }
            } catch (Exception ignored) {
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private interface ServerCoinCallback {
        void onResult(boolean success, int newBalance);
    }

    private void addCoinsToServer(int amount, String reason,
                                  ServerCoinCallback callback) {
        final String deviceId = getDeviceIdValue();

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(API_BASE_URL + "/coins/add");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type", "application/json; charset=UTF-8");

                String body = "{\"device_id\":\""
                        + jsonEscape(deviceId)
                        + "\",\"coins\":" + amount + "}";

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int code = connection.getResponseCode();
                String response = readResponse(connection);

                boolean success = code >= 200 && code < 300;
                int newBalance = parseIntField(
                        response, "balance_coins", totalCoins);

                final boolean ok = success;
                final int balance = newBalance;

                mainHandler.post(() -> callback.onResult(ok, balance));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onResult(false, totalCoins));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private String readResponse(HttpURLConnection connection) throws Exception {
        InputStream stream = connection.getResponseCode() >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();

        if (stream == null) return "";

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
            if (keyIndex < 0) return fallback;

            int colon = json.indexOf(':', keyIndex + key.length());
            if (colon < 0) return fallback;

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

            return Integer.parseInt(json.substring(start, end));
        } catch (Exception e) {
            return fallback;
        }
    }

    private String jsonEscape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private LinearLayout createRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(24));
        root.setBackgroundColor(BG);
        return root;
    }

    private LinearLayout cardLayout(int color, int radius) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setBackground(roundBg(color, radius));
        return card;
    }

    private TextView createText(String text, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);
        view.setFontFeatureSettings("kern");
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private EditText createInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(GRAY);
        input.setTextColor(WHITE);
        input.setTextSize(16);
        input.setSingleLine(true);
        input.setPadding(dp(15), 0, dp(15), 0);

        GradientDrawable bg = roundBg(Color.rgb(20, 30, 43), 12);
        bg.setStroke(dp(1), Color.rgb(55, 70, 88));
        input.setBackground(bg);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(56));
        p.setMargins(0, dp(5), 0, dp(5));
        input.setLayoutParams(p);

        return input;
    }

    private Button createButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setTextColor(WHITE);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setBackground(roundBg(color, 18));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(56));
        p.setMargins(0, dp(4), 0, dp(4));
        button.setLayoutParams(p);

        return button;
    }

    private Button smallButton(String text, int color, int width) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(11);
        button.setTextColor(WHITE);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(4), 0, dp(4), 0);
        button.setBackground(roundBg(color, 14));

        if (width > 0) {
            button.setLayoutParams(new LinearLayout.LayoutParams(dp(width), dp(50)));
        } else {
            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(0, dp(50), 1);
            p.setMargins(dp(3), 0, dp(3), 0);
            button.setLayoutParams(p);
        }

        return button;
    }

    private Button createRoundButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(28);
        button.setTextColor(WHITE);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setBackground(roundBg(color, 1000));
        return button;
    }

    private GradientDrawable roundBg(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private LinearLayout.LayoutParams marginParams(
            int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return p;
    }

    private ScrollView wrapScroll(LinearLayout root) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        scroll.addView(root);
        return scroll;
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density);
    }

    private String formatNumber(int value) {
        return String.format(Locale.US, "%,d", value);
    }

    private void updateDashboardValues() {
        if (balanceText != null) {
            balanceText.setText("BALANCE: " + formatNumber(totalCoins) + " COINS");
        }
        if (wealthText != null) {
            wealthText.setText(formatNumber(totalCoins) + "\nCOINS");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return password;
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        gameRunning = false;
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        super.onDestroy();
    }
}
