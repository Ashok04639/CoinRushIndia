package com.coinrushindia.prototype;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

    // =========================================================
    // STORAGE
    // =========================================================

    private static final String PREFS =
            "CoinRushIndiaPrefs";

    private static final String KEY_USERNAME =
            "username";

    private static final String KEY_PASSWORD =
            "password";

    private static final String KEY_LOGGED_IN =
            "logged_in";

    private static final String KEY_BEST_SCORE =
            "best_score";

    private SharedPreferences prefs;

    // =========================================================
    // TEST ADS
    // =========================================================

    private static final String INTERSTITIAL_AD_ID =
            "ca-app-pub-3940256099942544/1033173712";

    private static final String REWARDED_AD_ID =
            "ca-app-pub-3940256099942544/5224354917";

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    // =========================================================
    // GAME
    // =========================================================

    private int score = 0;
    private int bestScore = 0;

    private CountDownTimer gameTimer;

    private TextView scoreText;
    private TextView timerText;
    private TextView messageText;
    private TextView bestText;

    private Button coinButton;
    private Button rewardButton;
    private Button restartButton;

    // =========================================================
    // COLORS
    // =========================================================

    private static final int BG =
            Color.rgb(13, 16, 27);

    private static final int CARD =
            Color.rgb(29, 34, 48);

    private static final int INPUT =
            Color.rgb(40, 45, 60);

    private static final int WHITE =
            Color.WHITE;

    private static final int GREY =
            Color.rgb(185, 190, 205);

    private static final int ORANGE =
            Color.rgb(255, 152, 0);

    private static final int GREEN =
            Color.rgb(42, 190, 105);

    private static final int DARK =
            Color.rgb(68, 74, 90);

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(Color.BLACK);

        prefs = getSharedPreferences(
                PREFS,
                MODE_PRIVATE
        );

        bestScore = prefs.getInt(
                KEY_BEST_SCORE,
                0
        );

        MobileAds.initialize(
                this,
                status -> {
                    loadInterstitial();
                    loadRewarded();
                }
        );

        if (prefs.getBoolean(
                KEY_LOGGED_IN,
                false
        )) {
            showGame();
        } else {
            showLogin();
        }
    }

    // =========================================================
    // LOGIN SCREEN
    // =========================================================

    private void showLogin() {

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = root();

        TextView title = text(
                "🇮🇳 Coin Rush India",
                30,
                WHITE
        );

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(Gravity.CENTER);

        root.addView(
                title,
                lp(MATCH, dp(65))
        );

        TextView subtitle = text(
                "LOGIN TO PLAY",
                17,
                GREY
        );

        subtitle.setGravity(Gravity.CENTER);

        root.addView(
                subtitle,
                lp(MATCH, dp(40))
        );

        addSpace(root, 12);

        LinearLayout card = card();

        TextView heading = text(
                "Welcome Back 👋",
                25,
                WHITE
        );

        heading.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        heading.setGravity(Gravity.CENTER);

        card.addView(
                heading,
                lp(MATCH, dp(50))
        );

        addSpace(card, 12);

        EditText username =
                input("Username");

        card.addView(
                username,
                lp(MATCH, dp(52))
        );

        addSpace(card, 12);

        EditText password =
                input("Password");

        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        card.addView(
                password,
                lp(MATCH, dp(52))
        );

        addSpace(card, 18);

        Button login = button(
                "🔐  LOGIN",
                GREEN
        );

        card.addView(
                login,
                lp(MATCH, dp(54))
        );

        addSpace(card, 12);

        Button signup = button(
                "📝  CREATE ACCOUNT",
                ORANGE
        );

        card.addView(
                signup,
                lp(MATCH, dp(54))
        );

        addSpace(card, 12);

        TextView info = text(
                "Account is saved on this device.",
                13,
                GREY
        );

        info.setGravity(Gravity.CENTER);

        card.addView(
                info,
                lp(MATCH, dp(35))
        );

        root.addView(
                card,
                lp(MATCH, WRAP)
        );

        addSpace(root, 20);

        scroll.addView(root);

        login.setOnClickListener(v -> {

            String user =
                    username.getText()
                            .toString()
                            .trim();

            String pass =
                    password.getText()
                            .toString();

            if (user.isEmpty() ||
                    pass.isEmpty()) {

                toast(
                        "Username aur password enter karein"
                );

                return;
            }

            String savedUser =
                    prefs.getString(
                            KEY_USERNAME,
                            ""
                    );

            String savedPass =
                    prefs.getString(
                            KEY_PASSWORD,
                            ""
                    );

            if (user.equals(savedUser)
                    && hash(pass).equals(savedPass)) {

                prefs.edit()
                        .putBoolean(
                                KEY_LOGGED_IN,
                                true
                        )
                        .apply();

                toast(
                        "Login successful! 🎮"
                );

                showGame();

            } else {

                toast(
                        "Username ya password galat hai"
                );
            }
        });

        signup.setOnClickListener(
                v -> showSignup()
        );

        setContentView(scroll);
    }

    // =========================================================
    // SIGNUP SCREEN
    // =========================================================

    private void showSignup() {

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = root();

        TextView title = text(
                "🇮🇳 Coin Rush India",
                30,
                WHITE
        );

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(Gravity.CENTER);

        root.addView(
                title,
                lp(MATCH, dp(65))
        );

        TextView subtitle = text(
                "CREATE YOUR ACCOUNT",
                17,
                GREY
        );

        subtitle.setGravity(Gravity.CENTER);

        root.addView(
                subtitle,
                lp(MATCH, dp(40))
        );

        addSpace(root, 12);

        LinearLayout card = card();

        TextView heading = text(
                "Join Coin Rush 🚀",
                25,
                WHITE
        );

        heading.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        heading.setGravity(Gravity.CENTER);

        card.addView(
                heading,
                lp(MATCH, dp(50))
        );

        addSpace(card, 12);

        EditText username =
                input("Choose Username");

        card.addView(
                username,
                lp(MATCH, dp(52))
        );

        addSpace(card, 12);

        EditText password =
                input("Choose Password");

        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        card.addView(
                password,
                lp(MATCH, dp(52))
        );

        addSpace(card, 12);

        EditText confirm =
                input("Confirm Password");

        confirm.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        card.addView(
                confirm,
                lp(MATCH, dp(52))
        );

        addSpace(card, 18);

        Button create = button(
                "🚀  CREATE ACCOUNT",
                GREEN
        );

        card.addView(
                create,
                lp(MATCH, dp(54))
        );

        addSpace(card, 12);

        Button back = button(
                "←  BACK TO LOGIN",
                DARK
        );

        card.addView(
                back,
                lp(MATCH, dp(52))
        );

        root.addView(
                card,
                lp(MATCH, WRAP)
        );

        addSpace(root, 20);

        scroll.addView(root);

        create.setOnClickListener(v -> {

            String user =
                    username.getText()
                            .toString()
                            .trim();

            String pass =
                    password.getText()
                            .toString();

            String confirmPass =
                    confirm.getText()
                            .toString();

            if (user.length() < 3) {

                toast(
                        "Username kam se kam 3 characters ka ho"
                );

                return;
            }

            if (pass.length() < 4) {

                toast(
                        "Password kam se kam 4 characters ka ho"
                );

                return;
            }

            if (!pass.equals(confirmPass)) {

                toast(
                        "Passwords match nahi kar rahe"
                );

                return;
            }

            if (prefs.contains(KEY_USERNAME)) {

                toast(
                        "Account already bana hua hai"
                );

                return;
            }

            prefs.edit()
                    .putString(
                            KEY_USERNAME,
                            user
                    )
                    .putString(
                            KEY_PASSWORD,
                            hash(pass)
                    )
                    .putBoolean(
                            KEY_LOGGED_IN,
                            true
                    )
                    .putInt(
                            KEY_BEST_SCORE,
                            0
                    )
                    .apply();

            bestScore = 0;

            toast(
                    "Account created! 🎉"
            );

            showGame();
        });

        back.setOnClickListener(
                v -> showLogin()
        );

        setContentView(scroll);
    }

    // =========================================================
    // GAME SCREEN
    // =========================================================

    private void showGame() {

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout root = root();

        // HEADER
        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title = text(
                "🇮🇳 Coin Rush India",
                25,
                WHITE
        );

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setSingleLine(true);

        header.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        Button logout =
                smallButton("Logout");

        header.addView(
                logout,
                new LinearLayout.LayoutParams(
                        dp(82),
                        dp(42)
                )
        );

        root.addView(
                header,
                lp(MATCH, dp(55))
        );

        String username =
                prefs.getString(
                        KEY_USERNAME,
                        "Player"
                );

        TextView player = text(
                "Player: " + username,
                14,
                GREY
        );

        player.setGravity(Gravity.CENTER);

        root.addView(
                player,
                lp(MATCH, dp(30))
        );

        TextView subtitle = text(
                "TAP • COLLECT • RUSH!",
                14,
                GREY
        );

        subtitle.setGravity(Gravity.CENTER);

        root.addView(
                subtitle,
                lp(MATCH, dp(35))
        );

        addSpace(root, 6);

        // SCORE CARD
        LinearLayout scoreCard = card();

        scoreCard.setGravity(
                Gravity.CENTER
        );

        scoreText = text(
                "0 COINS",
                34,
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
                lp(MATCH, dp(55))
        );

        bestText = text(
                "BEST: " + bestScore,
                13,
                GREY
        );

        bestText.setGravity(
                Gravity.CENTER
        );

        scoreCard.addView(
                bestText,
                lp(MATCH, dp(25))
        );

        root.addView(
                scoreCard,
                lp(MATCH, dp(95))
        );

        addSpace(root, 6);

        // TIMER
        timerText = text(
                "⏱  30",
                22,
                WHITE
        );

        timerText.setGravity(
                Gravity.CENTER
        );

        root.addView(
                timerText,
                lp(MATCH, dp(45))
        );

        // MESSAGE
        messageText = text(
                "Tap the coin as fast as you can!",
                15,
                GREY
        );

        messageText.setGravity(
                Gravity.CENTER
        );

        root.addView(
                messageText,
                lp(MATCH, dp(55))
        );

        // COIN BUTTON
        coinButton = new Button(this);

        coinButton.setText(
                "🪙\nTAP!"
        );

        coinButton.setTextSize(26);

        coinButton.setTextColor(WHITE);

        coinButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        coinButton.setGravity(
                Gravity.CENTER
        );

        coinButton.setAllCaps(false);

        coinButton.setPadding(0, 0, 0, 0);

        coinButton.setBackground(
                round(
                        ORANGE,
                        500
                )
        );

        LinearLayout.LayoutParams coinParams =
                new LinearLayout.LayoutParams(
                        dp(205),
                        dp(205)
                );

        coinParams.gravity =
                Gravity.CENTER;

        root.addView(
                coinButton,
                coinParams
        );

        addSpace(root, 10);

        // REWARD
        rewardButton = button(
                "🎁  WATCH AD • 2X COINS",
                GREEN
        );

        root.addView(
                rewardButton,
                lp(MATCH, dp(55))
        );

        addSpace(root, 8);

        // RESTART
        restartButton = button(
                "↪  RESTART GAME",
                DARK
        );

        root.addView(
                restartButton,
                lp(MATCH, dp(52))
        );

        addSpace(root, 12);

        scroll.addView(root);

        // =====================================================
        // BUTTON ACTIONS
        // =====================================================

        coinButton.setOnClickListener(v -> {

            score++;

            updateScore();

            animateCoin();
        });

        rewardButton.setOnClickListener(
                v -> showRewarded()
        );

        restartButton.setOnClickListener(
                v -> startGame()
        );

        logout.setOnClickListener(v -> {

            if (gameTimer != null) {
                gameTimer.cancel();
            }

            prefs.edit()
                    .putBoolean(
                            KEY_LOGGED_IN,
                            false
                    )
                    .apply();

            showLogin();
        });

        setContentView(scroll);

        startGame();
    }

    // =========================================================
    // START GAME
    // =========================================================

    private void startGame() {

        score = 0;

        updateScore();

        timerText.setText(
                "⏱  30"
        );

        messageText.setText(
                "Tap the coin as fast as you can!"
        );

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

        if (gameTimer != null) {
            gameTimer.cancel();
        }

        gameTimer = new CountDownTimer(
                30000,
                1000
        ) {

            @Override
            public void onTick(
                    long millis
            ) {

                long seconds =
                        (millis + 999) / 1000;

                timerText.setText(
                        "⏱  " + seconds
                );
            }

            @Override
            public void onFinish() {

                timerText.setText(
                        "⏱  0"
                );

                coinButton.setEnabled(false);

                // सबसे जरूरी FIX:
                // Game Over पर बड़ा coin button hide
                coinButton.setVisibility(
                        View.GONE
                );

                updateBest();

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

                showInterstitial();
            }
        };

        gameTimer.start();
    }

    // =========================================================
    // SCORE
    // =========================================================

    private void updateScore() {

        if (scoreText != null) {

            scoreText.setText(
                    score + " COINS"
            );
        }
    }

    // =========================================================
    // BEST SCORE
    // =========================================================

    private void updateBest() {

        if (score > bestScore) {

            bestScore = score;

            prefs.edit()
                    .putInt(
                            KEY_BEST_SCORE,
                            bestScore
                    )
                    .apply();
        }

        if (bestText != null) {

            bestText.setText(
                    "BEST: " + bestScore
            );
        }
    }

    // =========================================================
    // COIN ANIMATION
    // =========================================================

    private void animateCoin() {

        ScaleAnimation animation =
                new ScaleAnimation(
                        1f,
                        0.88f,
                        1f,
                        0.88f,
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

        coinButton.startAnimation(
                animation
        );
    }

    // =========================================================
    // INTERSTITIAL LOAD
    // =========================================================

    private void loadInterstitial() {

        AdRequest request =
                new AdRequest.Builder().build();

        InterstitialAd.load(
                this,
                INTERSTITIAL_AD_ID,
                request,
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

                                                loadInterstitial();
                                            }
                                        }
                                );
                    }

                    @Override
                    public void onAdFailedToLoad(
                            LoadAdError error
                    ) {

                        interstitialAd = null;
                    }
                }
        );
    }

    // =========================================================
    // SHOW INTERSTITIAL
    // =========================================================

    private void showInterstitial() {

        if (interstitialAd != null) {

            interstitialAd.show(this);

        } else {

            loadInterstitial();
        }
    }

    // =========================================================
    // REWARDED LOAD
    // =========================================================

    private void loadRewarded() {

        AdRequest request =
                new AdRequest.Builder().build();

        RewardedAd.load(
                this,
                REWARDED_AD_ID,
                request,
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

                                                loadRewarded();
                                            }
                                        }
                                );
                    }

                    @Override
                    public void onAdFailedToLoad(
                            LoadAdError error
                    ) {

                        rewardedAd = null;
                    }
                }
        );
    }

    // =========================================================
    // SHOW REWARDED
    // =========================================================

    private void showRewarded() {

        if (rewardedAd != null) {

            rewardedAd.show(
                    this,
                    rewardItem -> {

                        score =
                                score * 2;

                        updateScore();

                        messageText.setText(
                                "🎉 REWARD!\n" +
                                "Coins doubled!"
                        );

                        toast(
                                "Coins 2X! 🎉"
                        );

                        rewardedAd = null;

                        loadRewarded();
                    }
            );

        } else {

            toast(
                    "Reward ad is loading..."
            );

            loadRewarded();
        }
    }

    // =========================================================
    // ROOT
    // =========================================================

    private LinearLayout root() {

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
                dp(10),
                dp(14),
                dp(10)
        );

        root.setBackgroundColor(BG);

        return root;
    }

    // =========================================================
    // CARD
    // =========================================================

    private LinearLayout card() {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        card.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
        );

        card.setBackground(
                round(
                        CARD,
                        22
                )
        );

        return card;
    }

    // =========================================================
    // TEXT
    // =========================================================

    private TextView text(
            String value,
            float size,
            int color
    ) {

        TextView t =
                new TextView(this);

        t.setText(value);

        t.setTextSize(size);

        t.setTextColor(color);

        t.setGravity(
                Gravity.CENTER_VERTICAL
        );

        return t;
    }

    // =========================================================
    // INPUT
    // =========================================================

    private EditText input(
            String hint
    ) {

        EditText e =
                new EditText(this);

        e.setHint(hint);

        e.setHintTextColor(
                Color.rgb(
                        150,
                        155,
                        170
                )
        );

        e.setTextColor(WHITE);

        e.setTextSize(17);

        e.setSingleLine(true);

        e.setPadding(
                dp(16),
                0,
                dp(16),
                0
        );

        e.setBackground(
                round(
                        INPUT,
                        17
                )
        );

        return e;
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private Button button(
            String value,
            int color
    ) {

        Button b =
                new Button(this);

        b.setText(value);

        b.setTextSize(15);

        b.setTextColor(WHITE);

        b.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        b.setGravity(
                Gravity.CENTER
        );

        b.setAllCaps(false);

        b.setMinHeight(0);

        b.setMinimumHeight(0);

        b.setPadding(
                dp(5),
                0,
                dp(5),
                0
        );

        b.setBackground(
                round(
                        color,
                        18
                )
        );

        return b;
    }

    // =========================================================
    // SMALL BUTTON
    // =========================================================

    private Button smallButton(
            String value
    ) {

        Button b =
                button(
                        value,
                        DARK
                );

        b.setTextSize(12);

        return b;
    }

    // =========================================================
    // BACKGROUND
    // =========================================================

    private GradientDrawable round(
            int color,
            int radius
    ) {

        GradientDrawable d =
                new GradientDrawable();

        d.setColor(color);

        d.setCornerRadius(
                dp(radius)
        );

        return d;
    }

    // =========================================================
    // LAYOUT PARAMS
    // =========================================================

    private static final int MATCH =
            LinearLayout.LayoutParams.MATCH_PARENT;

    private static final int WRAP =
            LinearLayout.LayoutParams.WRAP_CONTENT;

    private LinearLayout.LayoutParams lp(
            int width,
            int height
    ) {

        return new LinearLayout.LayoutParams(
                width,
                height
        );
    }

    // =========================================================
    // SPACE
    // =========================================================

    private void addSpace(
            LinearLayout layout,
            int size
    ) {

        View space =
                new View(this);

        layout.addView(
                space,
                new LinearLayout.LayoutParams(
                        1,
                        dp(size)
                )
        );
    }

    // =========================================================
    // DP CONVERSION
    // =========================================================

    private int dp(int value) {

        return (int) (
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // =========================================================
    // TOAST
    // =========================================================

    private void toast(
            String message
    ) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================================================
    // PASSWORD HASH
    // =========================================================

    private String hash(
            String password
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] bytes =
                    digest.digest(
                            password.getBytes(
                                    "UTF-8"
                            )
                    );

            StringBuilder result =
                    new StringBuilder();

            for (byte b : bytes) {

                String h =
                        Integer.toHexString(
                                b & 0xff
                        );

                if (h.length() == 1) {
                    result.append('0');
                }

                result.append(h);
            }

            return result.toString();

        } catch (Exception e) {

            return password;
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (gameTimer != null) {
            gameTimer.cancel();
        }

        super.onDestroy();
    }
}
