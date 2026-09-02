package com.coinrushindia.prototype;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MainActivity extends Activity {

    // =========================
    // ACCOUNT DATA
    // =========================

    private SharedPreferences prefs;

    private String username = "";

    // =========================
    // GAME DATA
    // =========================

    private int coins = 0;
    private int bestScore = 0;
    private int totalCoins = 0;
    private int timeLeft = 30;

    private CountDownTimer timer;

    // =========================
    // GAME VIEWS
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
    // COLORS
    // =========================

    private final int BG = Color.rgb(12, 18, 28);
    private final int CARD = Color.rgb(25, 34, 48);
    private final int WHITE = Color.WHITE;
    private final int GREEN = Color.rgb(45, 190, 100);
    private final int ORANGE = Color.rgb(255, 145, 45);
    private final int RED = Color.rgb(230, 70, 70);
    private final int GRAY = Color.rgb(150, 160, 175);


    // =========================
    // ON CREATE
    // =========================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("CoinRushIndia", MODE_PRIVATE);

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


    // =========================================================
    // LOGIN SCREEN
    // =========================================================

    private void showLoginScreen() {

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

        LinearLayout.LayoutParams subParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        subParams.setMargins(0, 10, 0, 30);
        subtitle.setLayoutParams(subParams);

        root.addView(subtitle);


        EditText usernameInput = createInput("Username");

        root.addView(usernameInput);


        EditText passwordInput = createInput("Password");
        passwordInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        root.addView(passwordInput);


        Button loginButton = createButton(
                "LOGIN",
                GREEN
        );

        root.addView(loginButton);


        Button signupButton = createButton(
                "CREATE NEW ACCOUNT",
                ORANGE
        );

        root.addView(signupButton);


        loginButton.setOnClickListener(v -> {

            String user =
                    usernameInput.getText().toString().trim();

            String pass =
                    passwordInput.getText().toString();

            if (user.isEmpty() || pass.isEmpty()) {

                Toast.makeText(
                        this,
                        "Username aur password bharo",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            String savedUser =
                    prefs.getString("username", "");

            String savedPassword =
                    prefs.getString("password", "");


            if (savedUser.isEmpty()) {

                Toast.makeText(
                        this,
                        "Pehle account create karo",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            if (savedUser.equals(user)
                    && savedPassword.equals(hashPassword(pass))) {

                prefs.edit()
                        .putBoolean("loggedIn", true)
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
        });


        signupButton.setOnClickListener(v ->
                showSignupScreen()
        );


        setContentView(wrapScroll(root));
    }


    // =========================================================
    // SIGNUP SCREEN
    // =========================================================

    private void showSignupScreen() {

        LinearLayout root = createRoot();

        TextView title = createText(
                "🇮🇳 Create Account",
                28,
                WHITE,
                true
        );

        root.addView(title);


        TextView info = createText(
                "Create your Coin Rush India account",
                15,
                GRAY,
                false
        );

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        infoParams.setMargins(0, 10, 0, 30);

        info.setLayoutParams(infoParams);

        root.addView(info);


        EditText usernameInput =
                createInput("Choose Username");

        root.addView(usernameInput);


        EditText passwordInput =
                createInput("Choose Password");

        passwordInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        root.addView(passwordInput);


        EditText confirmInput =
                createInput("Confirm Password");

        confirmInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
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
                        CARD
                );

        root.addView(backButton);


        createButton.setOnClickListener(v -> {

            String user =
                    usernameInput.getText().toString().trim();

            String pass =
                    passwordInput.getText().toString();

            String confirm =
                    confirmInput.getText().toString();


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


            prefs.edit()
                    .putString("username", user)
                    .putString("password", hashPassword(pass))
                    .putInt("bestScore", 0)
                    .putInt("totalCoins", 0)
                    .putBoolean("loggedIn", true)
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
        });


        backButton.setOnClickListener(v ->
                showLoginScreen()
        );


        setContentView(wrapScroll(root));
    }


    // =========================================================
    // GAME SCREEN
    // =========================================================

    private void showGameScreen() {

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


        TextView title =
                createText(
                        "🇮🇳 Coin Rush India",
                        24,
                        WHITE,
                        true
                );


        header.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );


        logoutButton =
                createButton(
                        "Logout",
                        RED
                );

        logoutButton.setTextSize(12);

        header.addView(
                logoutButton,
                new LinearLayout.LayoutParams(
                        dp(85),
                        dp(45)
                )
        );


        root.addView(header);


        logoutButton.setOnClickListener(v -> {

            stopTimer();

            prefs.edit()
                    .putBoolean("loggedIn", false)
                    .apply();

            showLoginScreen();
        });


        // PLAYER
        TextView playerText =
                createText(
                        "Player: " + username,
                        16,
                        GRAY,
                        false
                );

        LinearLayout.LayoutParams playerParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        playerParams.setMargins(
                0,
                15,
                0,
                5
        );

        playerText.setLayoutParams(playerParams);

        root.addView(playerText);


        TextView subtitle =
                createText(
                        "TAP • COLLECT • RUSH!",
                        15,
                        GRAY,
                        true
                );

        root.addView(subtitle);


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
                dp(20),
                dp(10),
                dp(20)
        );

        scoreCard.setBackgroundColor(CARD);


        scoreText =
                createText(
                        "0 COINS",
                        38,
                        WHITE,
                        true
                );

        scoreCard.addView(scoreText);


        LinearLayout.LayoutParams scoreParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        scoreParams.setMargins(
                0,
                20,
                0,
                15
        );

        scoreCard.setLayoutParams(scoreParams);

        root.addView(scoreCard);


        // STATS
        LinearLayout stats =
                new LinearLayout(this);

        stats.setOrientation(
                LinearLayout.HORIZONTAL
        );

        stats.setGravity(
                Gravity.CENTER
        );


        bestText =
                createText(
                        "BEST: " + bestScore,
                        15,
                        GRAY,
                        true
                );


        totalText =
                createText(
                        "TOTAL: " + totalCoins,
                        15,
                        GRAY,
                        true
                );


        stats.addView(
                bestText,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );


        stats.addView(
                totalText,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );


        root.addView(stats);


        // TIMER
        timerText =
                createText(
                        "TIME: 30",
                        18,
                        ORANGE,
                        true
                );

        LinearLayout.LayoutParams timerParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        timerParams.setMargins(
                0,
                20,
                0,
                10
        );

        timerText.setLayoutParams(timerParams);

        root.addView(timerText);


        // MESSAGE
        messageText =
                createText(
                        "Tap the button!",
                        16,
                        WHITE,
                        true
                );

        root.addView(messageText);


        // TAP BUTTON
        tapButton =
                createButton(
                        "TAP!",
                        ORANGE
                );

        tapButton.setTextSize(30);

        LinearLayout.LayoutParams tapParams =
                new LinearLayout.LayoutParams(
                        dp(180),
                        dp(180)
                );

        tapParams.gravity =
                Gravity.CENTER_HORIZONTAL;

        tapParams.setMargins(
                0,
                20,
                0,
                20
        );

        tapButton.setLayoutParams(tapParams);

        root.addView(tapButton);


        // REWARD BUTTON
        rewardButton =
                createButton(
                        "🎁 WATCH AD • 2X COINS",
                        GREEN
                );

        root.addView(rewardButton);


        // RESTART BUTTON
        restartButton =
                createButton(
                        "↪ RESTART GAME",
                        CARD
                );

        root.addView(restartButton);


        // TAP ACTION
        tapButton.setOnClickListener(v -> {

            if (timeLeft <= 0) {
                return;
            }

            coins++;

            totalCoins++;

            updateScore();

            // small feedback
            tapButton.setScaleX(0.94f);
            tapButton.setScaleY(0.94f);

            tapButton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .start();
        });


        // REWARD
        rewardButton.setOnClickListener(v -> {

            if (coins <= 0) {

                Toast.makeText(
                        this,
                        "Pehle coins collect karo",
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


            saveGameData();

            updateScore();


            messageText.setText(
                    "🎉 REWARD! Coins doubled!"
            );


            rewardButton.setVisibility(
                    View.GONE
            );
        });


        // RESTART
        restartButton.setOnClickListener(v ->
                startGame()
        );


        setContentView(
                wrapScroll(root)
        );


        startGame();
    }


    // =========================================================
    // START GAME
    // =========================================================

    private void startGame() {

        stopTimer();


        coins = 0;

        timeLeft = 30;


        if (tapButton != null) {
            tapButton.setVisibility(
                    View.VISIBLE
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


        if (messageText != null) {

            messageText.setText(
                    "Tap the button!"
            );
        }


        updateScore();


        timer =
                new CountDownTimer(
                        30000,
                        1000
                ) {

                    @Override
                    public void onTick(
                            long millisUntilFinished
                    ) {

                        timeLeft =
                                (int)
                                        (millisUntilFinished / 1000);

                        updateTimer();
                    }


                    @Override
                    public void onFinish() {

                        timeLeft = 0;

                        updateTimer();

                        gameOver();
                    }

                };


        timer.start();
    }


    // =========================================================
    // GAME OVER
    // =========================================================

    private void gameOver() {

        stopTimer();


        if (coins > bestScore) {

            bestScore = coins;

            saveGameData();
        }


        updateScore();


        if (tapButton != null) {

            tapButton.setVisibility(
                    View.GONE
            );
        }


        if (messageText != null) {

            messageText.setText(
                    "🏁 GAME OVER! Score: " + coins
            );
        }


        Toast.makeText(
                this,
                "Game Over! Coins: " + coins,
                Toast.LENGTH_SHORT
        ).show();
    }


    // =========================================================
    // UPDATE SCORE
    // =========================================================

    private void updateScore() {

        if (scoreText != null) {

            scoreText.setText(
                    coins + " COINS"
            );
        }


        if (bestText != null) {

            bestText.setText(
                    "BEST: " + bestScore
            );
        }


        if (totalText != null) {

            totalText.setText(
                    "TOTAL: " + totalCoins
            );
        }


        saveGameData();
    }


    // =========================================================
    // UPDATE TIMER
    // =========================================================

    private void updateTimer() {

        if (timerText != null) {

            timerText.setText(
                    "TIME: " + timeLeft
            );
        }
    }


    // =========================================================
    // SAVE DATA
    // =========================================================

    private void saveGameData() {

        prefs.edit()
                .putInt("bestScore", bestScore)
                .putInt("totalCoins", totalCoins)
                .apply();
    }


    // =========================================================
    // STOP TIMER
    // =========================================================

    private void stopTimer() {

        if (timer != null) {

            timer.cancel();

            timer = null;
        }
    }


    // =========================================================
    // ROOT LAYOUT
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
                dp(20),
                dp(25),
                dp(20),
                dp(30)
        );

        root.setBackgroundColor(BG);

        return root;
    }


    // =========================================================
    // SCROLL VIEW
    // =========================================================

    private ScrollView wrapScroll(
            LinearLayout root
    ) {

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        scroll.setBackgroundColor(BG);

        scroll.addView(root);

        return scroll;
    }


    // =========================================================
    // TEXT VIEW
    // =========================================================

    private TextView createText(
            String text,
            float size,
            int color,
            boolean bold
    ) {

        TextView tv =
                new TextView(this);

        tv.setText(text);

        tv.setTextSize(size);

        tv.setTextColor(color);

        tv.setGravity(
                Gravity.CENTER
        );

        if (bold) {

            tv.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        tv.setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
        );

        return tv;
    }


    // =========================================================
    // INPUT
    // =========================================================

    private EditText createInput(
            String hint
    ) {

        EditText input =
                new EditText(this);

        input.setHint(hint);

        input.setTextColor(WHITE);

        input.setHintTextColor(GRAY);

        input.setTextSize(16);

        input.setSingleLine(true);

        input.setPadding(
                dp(15),
                dp(12),
                dp(15),
                dp(12)
        );


        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                );

        params.setMargins(
                0,
                0,
                0,
                12
        );

        input.setLayoutParams(params);

        return input;
    }


    // =========================================================
    // BUTTON
    // =========================================================

    private Button createButton(
            String text,
            int background
    ) {

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
                8,
                0,
                8
        );

        button.setLayoutParams(params);

        return button;
    }


    // =========================================================
    // DP HELPER
    // =========================================================

    private int dp(int value) {

        return (int)
                (
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
    // ACTIVITY CLOSE
    // =========================================================

    @Override
    protected void onDestroy() {

        stopTimer();

        super.onDestroy();
    }
}
