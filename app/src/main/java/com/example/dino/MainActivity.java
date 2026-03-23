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
        setContentView(R.layout.activity_main);

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
            // Тема високого контрасту (Чорно-біла для кращого сприйняття)
            bgColor = Color.BLACK;
            textColor = Color.WHITE;
            highScoreColor = Color.LTGRAY;

            ivDino.clearColorFilter();
            ivCactus.clearColorFilter();
            groundLine.setBackgroundColor(Color.WHITE);
        } else {
            // Звичайна колірна тема
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
        isGameRunning = true;
        score = 0;
        cactusSpeed = 15f;
        tvScore.setText("Рахунок: 0");
        tvGameOver.setVisibility(View.GONE);
        gameOverButtons.setVisibility(View.GONE);

        // кактус одразу за правим краєм
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

                    // рекорд
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

        //хітбокси:

        // Для Коргі
        int dinoInsetX = (int) (ivDino.getWidth() * 0.30);
        int dinoInsetY = (int) (ivDino.getHeight() * 0.25);
        dinoRect.inset(dinoInsetX, dinoInsetY);

        // Для Кактуса
        int cactusInsetX = (int) (ivCactus.getWidth() * 0.35);
        int cactusInsetY = (int) (ivCactus.getHeight() * 0.15);
        cactusRect.inset(cactusInsetX, cactusInsetY);

        return Rect.intersects(dinoRect, cactusRect);
    }
}