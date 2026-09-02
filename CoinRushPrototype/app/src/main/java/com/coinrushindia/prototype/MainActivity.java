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
    // ACCOUNT STORAGE
    // =========================================================

    private static final String PREFS = "CoinRushIndiaPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_BEST_SCORE = "best_score";

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

    private CountDownTimer timer;

    private TextView scoreText;
    private TextView timerText;
    private TextView messageText;
    private TextView bestText;
    private TextView playerText;

    private Button coinButton;
    private Button rewardButton;
    private Button restartButton;
    private Button logoutButton;

    // =========================================================
    // COLORS
    // =========================================================

    private static final int BG =
            Color.rgb(13, 16, 27);

    private static final int CARD =
            Color.rgb(28, 33, 47);

    private static final int INPUT =
            Color.rgb(39, 44, 59);

    private static final int WHITE =
            Color.WHITE;

    private static final int GREY =
            Color.rgb(185, 190, 205);

    private static final int ORANGE =
            Color.rgb(255, 152, 0);

    private static final int GREEN =
            Color.rgb(42, 190, 105);

    private static final int DARK_BUTTON =
            Color.rgb(65, 70, 85);

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    // ROOT SCREEN
    // =========================================================

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
                18,
                18,
                18,
                18
        );

        root.setBackgroundColor(BG);

        return root;
    }

    // =========================================================
    // SCROLL CONTAINER
    // =========================================================

    private ScrollView createScroll() {

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        scroll.setBackgroundColor(BG);

        return scroll;
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private void showLogin() {

        ScrollView scroll = createScroll();

        LinearLayout root = createRoot();

        // TITLE
        TextView title = text(
                "🇮🇳 Coin Rush India",
                30,
                WHITE
        );

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(
                Gravity.CENTER
        );

        root.addView(
                title,
                params(-1, 70)
        );

        // SUBTITLE
        TextView subtitle = text(
                "LOGIN TO PLAY",
                18,
                GREY
        );

        subtitle.setGravity(
                Gravity.CENTER
        );

        root.addView(
                subtitle,
                params(-1, 45)
        );

        space(root, 10);

        // CARD
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

        heading.setGravity(
                Gravity.CENTER
        );

        card.addView(
                heading,
                params(-1, 55)
        );

        space(card, 10);

        EditText username =
                input("Username");

        card.addView(
                username,
                params(-1, 55)
        );

        space(card, 12);

        EditText password =
                input("Password");

        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        card.addView(
                password,
                params(-1, 55)
        );

        space(card, 18);

        // LOGIN BUTTON
        Button login = button(
                "🔐  LOGIN",
                GREEN
        );

        card.addView(
                login,
                params(-1, 58)
        );

        space(card, 12);

        // SIGNUP BUTTON
        Button signup = button(
                "📝  CREATE ACCOUNT",
                ORANGE
        );

        card.addView(
                signup,
                params(-1, 58)
        );

        space(card, 12);

        TextView info = text(
                "Account is saved on this device.",
                14,
                GREY
        );

        info.setGravity(
                Gravity.CENTER
        );

        card.addView(
                info,
                params(-1, 40)
        );

        root.addView(
                card,
                params(-1, -2)
        );

        space(root, 20);

        scroll.addView(root);

        // LOGIN ACTION
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

            if (user.equals(savedUser) &&
                    hash(pass).equals(savedPass)) {

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

        // SIGNUP
        signup.setOnClickListener(
                v -> showSignup()
        );

        setContentView(scroll);
    }

    // =========================================================
    // SIGNUP
    // =========================================================

    private void showSignup() {

        ScrollView scroll = createScroll();

        LinearLayout root = createRoot();

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
                params(-1, 70)
        );

        TextView subtitle = text(
                "CREATE YOUR ACCOUNT",
                17,
                GREY
        );

        subtitle.setGravity(Gravity.CENTER);

        root.addView(
                subtitle,
                params(-1, 45)
        );

        space(root, 10);

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
                params(-1, 55)
        );

        space(card, 10);

        EditText username =
                input("Choose Username");

        card.addView(
                username,
                params(-1, 55)
        );

        space(card, 12);

        EditText password =
                input("Choose Password");

        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        card.addView(
                password,
                params(-1, 55)
        );

        space(card, 12);

        EditText confirm =
                input("Confirm Password");

        confirm.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        card.addView(
                confirm,
                params(-1, 55)
        );

        space(card, 18);

        Button create = button(
                "🚀  CREATE ACCOUNT",
                GREEN
        );

        card.addView(
                create,
                params(-1, 58)
        );

        space(card, 12);

        Button back = button(
                "←  BACK TO LOGIN",
                DARK_BUTTON
        );

        card.addView(
                back,
                params(-1, 55)
        );

        space(card, 10);

        root.addView(
                card,
                params(-1, -2)
        );

        scroll.addView(root);

        // CREATE ACCOUNT
        create.setOnClickListener(v -> {

            String user =
                    username.getText()
                            .toString()
                            .trim();

            String pass =
                    password.getText()
                            .toString();

            String conf =
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

            if (!pass.equals(conf)) {

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

        ScrollView scroll = createScroll();

        LinearLayout root = createRoot();

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

        header.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        65,
                        1
                )
        );

        logoutButton =
                smallButton("Logout");

        header.addView(
                logoutButton,
                new LinearLayout.LayoutParams(
                        95,
                        48
                )
        );

        root.addView(
                header,
                params(-1, 65)
        );

        String user =
                prefs.getString(
                        KEY_USERNAME,
                        "Player"
                );

        playerText = text(
                "Player: " + user,
                14,
                GREY
        );

        playerText.setGravity(
                Gravity.CENTER
        );

        root.addView(
                playerText,
                params(-1, 35)
        );

        TextView subtitle = text(
                "TAP • COLLECT • RUSH!",
                15,
                GREY
        );

        subtitle.setGravity(
                Gravity.CENTER
        );

        root.addView(
                subtitle,
                params(-1, 40)
        );

        // SCORE CARD
        LinearLayout scoreCard = card();

        scoreCard.setGravity(
                Gravity.CENTER
        );

        scoreText = text(
                "0 COINS",
                36,
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
                params(-1, 65)
        );

        bestText = text(
                "BEST: " + bestScore,
                14,
                GREY
        );

        bestText.setGravity(
                Gravity.CENTER
        );

        scoreCard.addView(
                bestText,
                params(-1, 30)
        );

        root.addView(
                scoreCard,
                params(-1, 110)
        );

        space(root, 8);

        // TIMER
        timerText = text(
                "⏱️  30",
                23,
                WHITE
        );

        timerText.setGravity(
                Gravity.CENTER
        );

        root.addView(
                timerText,
                params(-1, 50)
        );

        // MESSAGE
        messageText = text(
                "Tap the coin as fast as you can!",
                16,
                GREY
        );

        messageText.setGravity(
                Gravity.CENTER
        );

        root.addView(
                messageText,
                params(-1, 55)
        );

        // COIN
        coinButton = new Button(this);

        coinButton.setText(
                "🪙\nTAP!"
        );

        coinButton.setTextSize(27);

        coinButton.setTextColor(WHITE);

        coinButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        coinButton.setGravity(
                Gravity.CENTER
        );

        coinButton.setAllCaps(false);

        coinButton.setPadding(
                0,
                0,
                0,
                0
        );

        coinButton.setBackground(
                round(
                        ORANGE,
                        300
                )
        );

        LinearLayout.LayoutParams coinParams =
                new LinearLayout.LayoutParams(
                        220,
                        220
                );

        coinParams.gravity =
                Gravity.CENTER;

        root.addView(
                coinButton,
                coinParams
        );

        space(root, 8);

        // REWARD
        rewardButton = button(
                "🎁  WATCH AD • 2X COINS",
                GREEN
        );

        root.addView(
                rewardButton,
                params(-1, 58)
        );

        space(root, 8);

        // RESTART
        restartButton = button(
                "↪️  RESTART GAME",
                DARK_BUTTON
        );

        root.addView(
                restartButton,
                params(-1, 55)
        );

        scroll.addView(root);

        // TAP
        coinButton.setOnClickListener(v -> {

            score++;

            updateScore();

            animateCoin();
        });

        // REWARD
        rewardButton.setOnClickListener(
                v -> showRewarded()
        );

        // RESTART
        restartButton.setOnClickListener(
                v -> startGame()
        );

        // LOGOUT
        logoutButton.setOnClickListener(v -> {

            if (timer != null) {
                timer.cancel();
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
                "⏱️  30"
        );

        messageText.setText(
                "Tap the coin as fast as you can!"
        );

        // IMPORTANT
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

        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(
                30000,
                1000
        ) {

            @Override
            public void onTick(
                    long remaining
            ) {

                long seconds =
                        (remaining + 999) / 1000;

                timerText.setText(
                        "⏱️  " + seconds
                );
            }

            @Override
            public void onFinish() {

                timerText.setText(
                        "⏱️  0"
                );

                coinButton.setEnabled(false);

                // IMPORTANT:
                // GAME OVER पर बड़ा TAP button छुपेगा
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

        timer.start();
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
    // TAP ANIMATION
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
                new AdRequest.Builder()
                        .build();

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
                new AdRequest.Builder()
                        .build();

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
    // CARD
    // =========================================================

    private LinearLayout card() {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        layout.setPadding(
                14,
                12,
                14,
                12
        );

        layout.setBackground(
                round(
                        CARD,
                        25
                )
        );

        return layout;
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
                18,
                0,
                18,
                0
        );

        e.setBackground(
                round(
                        INPUT,
                        18
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

        b.setTextColor(WHITE);

        b.setTextSize(16);

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
                8,
                0,
                8,
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
                        DARK_BUTTON
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
                radius
        );

        return d;
    }

    // =========================================================
    // LAYOUT PARAMS
    // =========================================================

    private LinearLayout.LayoutParams params(
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

    private void space(
            LinearLayout layout,
            int height
    ) {

        View v =
                new View(this);

        layout.addView(
                v,
                new LinearLayout.LayoutParams(
                        1,
                        height
                )
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

        if (timer != null) {
            timer.cancel();
        }

        super.onDestroy();
    }
}
