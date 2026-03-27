package com.example.dino;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;

/**
 * Головний клас-контролер гри Corgy Inclusive Game.
 * @author Liza Shalomeeva
 * @version 1.0
 */
public class MainActivity extends Activity {

    // UI Елементи (Меню та Налаштування)
    private LinearLayout menuLayout, settingsLayout, gameOverButtons;
    private Button btnMenuStart, btnMenuSettings, btnMenuExit, btnSettingsBack, btnBackToMenu;
    private Switch switchHaptics, switchContrast;
    private TextView tvSettingsTitle;

    // UI Елементи (Гра)
    private FrameLayout rootLayout;
    private RelativeLayout gameArea;
    private ImageView ivDino, ivCactus;
    private TextView tvScore, tvGameOver, tvHighScore;
    private Button btnReset;
    private View groundLine;

    // Ігрові змінні
    private int score = 0;
    private int highScore = 0;
    private boolean isGameRunning = false;
    private int screenWidth;

    // для нормальної фізики
    private boolean isDinoJumping = false;
    private float dinoVelocityY = 0f;
    private float gravity = 2.5f;
    private float jumpForce = -44f;
    private float currentDinoY = 0f;
    private float cactusSpeed = 15f;

    // Інклюзивні налаштування
    private boolean hapticsEnabled = true;
    private boolean highContrastEnabled = false;

    // Об'єкти для ігрового циклу
    private Handler gameHandler = new Handler(Looper.getMainLooper());
    private Runnable gameRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- СИСТЕМА ЛОГУВАННЯ ТА ОБРОБКИ ПОМИЛОК ---
        GameLogger.init(this);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        GameLogger.log(GameLogger.INFO, "MainActivity", "Game Application Started", null);
        // ---------------------------------------------

        setContentView(R.layout.activity_main);
        checkForPreviousCrash();

        initViews();
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        setupGameInputs();

        // Початкове налаштування теми
        applyTheme();
    }

    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        gameArea = findViewById(R.id.gameArea);
        ivDino = findViewById(R.id.ivDino);
        ivCactus = findViewById(R.id.ivCactus);
        tvScore = findViewById(R.id.tvScore);
        tvHighScore = findViewById(R.id.tvHighScore);
        tvGameOver = findViewById(R.id.tvGameOver);
        btnReset = findViewById(R.id.btnReset);
        groundLine = findViewById(R.id.groundLine);

        menuLayout = findViewById(R.id.menuLayout);
        settingsLayout = findViewById(R.id.settingsLayout);
        gameOverButtons = findViewById(R.id.gameOverButtons);
        btnMenuStart = findViewById(R.id.btnMenuStart);
        btnMenuSettings = findViewById(R.id.btnMenuSettings);
        btnMenuExit = findViewById(R.id.btnMenuExit);
        btnSettingsBack = findViewById(R.id.btnSettingsBack);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
        switchHaptics = findViewById(R.id.switchHaptics);
        switchContrast = findViewById(R.id.switchContrast);
        tvSettingsTitle = findViewById(R.id.tvSettingsTitle);
    }

    private void setupGameInputs() {
        btnMenuStart.setOnClickListener(v -> {
            menuLayout.setVisibility(View.GONE);
            gameArea.setVisibility(View.VISIBLE);
            startGame();
        });

        btnMenuSettings.setOnClickListener(v -> {
            menuLayout.setVisibility(View.GONE);
            settingsLayout.setVisibility(View.VISIBLE);
        });

        btnMenuExit.setOnClickListener(v -> finish());

        btnSettingsBack.setOnClickListener(v -> {
            settingsLayout.setVisibility(View.GONE);
            menuLayout.setVisibility(View.VISIBLE);
        });

        switchHaptics.setOnCheckedChangeListener((btn, isChecked) -> hapticsEnabled = isChecked);
        switchContrast.setOnCheckedChangeListener((btn, isChecked) -> {
            highContrastEnabled = isChecked;
            applyTheme();
        });

        gameArea.setOnClickListener(v -> {
            if (isGameRunning && !isDinoJumping) {
                dinoJump();
            }
        });

        btnReset.setOnClickListener(v -> startGame());

        btnBackToMenu.setOnClickListener(v -> {
            gameArea.setVisibility(View.GONE);
            menuLayout.setVisibility(View.VISIBLE);
            resetGame();
        });
    }

    private void applyTheme() {
        int bgColor, textColor, highScoreColor;

        if (highContrastEnabled) {
            GameLogger.log(GameLogger.DEBUG, "Settings", "High Contrast mode enabled", null);
            bgColor = Color.BLACK;
            textColor = Color.WHITE;
            highScoreColor = Color.LTGRAY;

            ivDino.clearColorFilter();
            ivCactus.clearColorFilter();
            groundLine.setBackgroundColor(Color.WHITE);
        } else {
            bgColor = Color.WHITE;
            textColor = Color.parseColor("#333333");
            highScoreColor = Color.parseColor("#888888");

            ivDino.clearColorFilter();
            ivCactus.clearColorFilter();
            groundLine.setBackgroundColor(Color.parseColor("#555555"));
        }

        rootLayout.setBackgroundColor(bgColor);
        tvScore.setTextColor(textColor);
        tvHighScore.setTextColor(highScoreColor);
        tvSettingsTitle.setTextColor(textColor);
        switchHaptics.setTextColor(textColor);
        switchContrast.setTextColor(textColor);
    }

    private void startGame() {
        GameLogger.log(GameLogger.DEBUG, "GameEngine", "New game session started", null);
        isGameRunning = true;
        score = 0;
        cactusSpeed = 15f;
        tvScore.setText("Рахунок: 0");
        tvGameOver.setVisibility(View.GONE);
        gameOverButtons.setVisibility(View.GONE);

        ivCactus.setTranslationX(200f);
        currentDinoY = 0f;
        dinoVelocityY = 0f;
        ivDino.setTranslationY(0f);
        isDinoJumping = false;

        gameHandler.removeCallbacks(gameRunnable);
        startGameLoop();
    }

    private void resetGame() {
        isGameRunning = false;
        tvGameOver.setVisibility(View.GONE);
        gameOverButtons.setVisibility(View.GONE);
        ivCactus.setTranslationX(screenWidth + 500f);
        gameHandler.removeCallbacks(gameRunnable);
    }

    private void gameOver() {
        GameLogger.log(GameLogger.INFO, "GameEngine", "Game Over. Final Score: " + score, null);
        isGameRunning = false;
        tvGameOver.setVisibility(View.VISIBLE);
        gameOverButtons.setVisibility(View.VISIBLE);
        gameHandler.removeCallbacks(gameRunnable);

        if (hapticsEnabled) {
            gameArea.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
    }

    private void dinoJump() {
        isDinoJumping = true;
        dinoVelocityY = jumpForce;
    }

    private void startGameLoop() {
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isGameRunning) return;

                if (isDinoJumping) {
                    dinoVelocityY += gravity;
                    currentDinoY += dinoVelocityY;

                    if (currentDinoY >= 0f) {
                        currentDinoY = 0f;
                        isDinoJumping = false;
                        dinoVelocityY = 0f;
                    }
                    ivDino.setTranslationY(currentDinoY);
                }

                float currentCactusX = ivCactus.getTranslationX();
                currentCactusX -= cactusSpeed;

                if (currentCactusX < -screenWidth) {
                    currentCactusX = 200f;
                    score++;
                    tvScore.setText("Рахунок: " + score);

                    if (score > highScore) {
                        highScore = score;
                        tvHighScore.setText("Рекорд: " + highScore);
                    }

                    cactusSpeed += 0.3f;
                }
                ivCactus.setTranslationX(currentCactusX);

                if (checkCollision()) {
                    gameOver();
                } else {
                    gameHandler.postDelayed(this, 20);
                }
            }
        };
        gameHandler.post(gameRunnable);
    }

    private boolean checkCollision() {
        Rect dinoRect = new Rect();
        ivDino.getHitRect(dinoRect);

        Rect cactusRect = new Rect();
        ivCactus.getHitRect(cactusRect);

        int dinoInsetX = (int) (ivDino.getWidth() * 0.30);
        int dinoInsetY = (int) (ivDino.getHeight() * 0.25);
        dinoRect.inset(dinoInsetX, dinoInsetY);

        int cactusInsetX = (int) (ivCactus.getWidth() * 0.35);
        int cactusInsetY = (int) (ivCactus.getHeight() * 0.15);
        cactusRect.inset(cactusInsetX, cactusInsetY);

        return Rect.intersects(dinoRect, cactusRect);
    }
    private void checkForPreviousCrash() {
        SharedPreferences prefs = getSharedPreferences("CrashPrefs", Context.MODE_PRIVATE);
        if (prefs.getBoolean("crashed_last_time", false)) {
            String errorId = prefs.getString("crash_error_id", "UNKNOWN");

            // Очищаємо прапорець, щоб не показувати вікно вічно
            prefs.edit().putBoolean("crashed_last_time", false).apply();

            // Створюємо зрозуміле повідомлення без технічних деталей
            new AlertDialog.Builder(this)
                    .setTitle("Ой, Коргі спіткнувся! 🐶")
                    .setMessage("Минулого разу гра несподівано закрилася.\n\n" +
                            "Що робити: Переконайтеся, що на пристрої достатньо пам'яті, або спробуйте перезавантажити гру.\n\n" +
                            "Код помилки для підтримки: " + errorId)
                    .setPositiveButton("Повідомити про проблему", (dialog, which) -> {
                        // Можливість надіслати лог розробникам
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("text/plain");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@corgygame.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Crash Report: " + errorId);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "Будь ласка, опишіть, що ви робили перед вильотом гри:\n\n...");
                        startActivity(Intent.createChooser(emailIntent, "Надіслати звіт розробнику..."));
                    })
                    .setNegativeButton("Закрити", null)
                    .setCancelable(false)
                    .show();
        }
    }
}