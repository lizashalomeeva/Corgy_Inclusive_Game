package com.example.dino;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class GameLogger {
    // Рівні логування
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARNING = 5;
    public static final int ERROR = 6;
    public static final int CRITICAL = 7;

    private static final String TAG = "CorgyGameSys";
    private static int currentLogLevel = DEBUG;
    private static SharedPreferences prefs;

    // --- ЗМІННІ ДЛЯ 85% (Файли, Ротація, Контекст) ---
    private static String sessionId; // Контекст: унікальна ігрова сесія
    private static File logFile;
    private static final long MAX_FILE_SIZE = 1024 * 1024; // Максимальний розмір логу: 1 MB

    // Ініціалізація
    public static void init(Context context) {
        prefs = context.getSharedPreferences("LoggingConfig", Context.MODE_PRIVATE);
        currentLogLevel = prefs.getInt("min_log_level", DEBUG);

        // Генеруємо контекст сесії (унікальний ID для кожного запуску гри)
        sessionId = UUID.randomUUID().toString().substring(0, 8);

        // Налаштовуємо файловий обробник (зберігається у внутрішній пам'яті додатку)
        logFile = new File(context.getExternalFilesDir(null), "corgy_game_logs.txt");

        log(INFO, "GameLogger", "System initialized. Level: " + currentLogLevel, null);
    }

    // Головний метод логування
    public static void log(int level, String module, String message, Throwable exception) {
        if (level < currentLogLevel) return;

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String errorId = (level >= ERROR) ? " | ErrID: " + UUID.randomUUID().toString().substring(0, 8) : "";

        // Формат з контекстом (SessionID)
        String formattedMessage = String.format("[%s] [Session:%s] [%s] %s%s", time, sessionId, module, message, errorId);

        // 1. Обробник №1: Консоль (Logcat)
        switch (level) {
            case DEBUG: Log.d(TAG, formattedMessage); break;
            case INFO: Log.i(TAG, formattedMessage); break;
            case WARNING: Log.w(TAG, formattedMessage); break;
            case ERROR: Log.e(TAG, formattedMessage, exception); break;
            case CRITICAL: Log.wtf(TAG, formattedMessage, exception); break;
        }

        // 2. Обробник №2: Файлова система
        writeToFile(formattedMessage, exception);
    }

    // Метод запису у файл із РОТАЦІЄЮ
    private static void writeToFile(String message, Throwable exception) {
        if (logFile == null) return;

        // Реалізація ротації логів (за розміром)
        if (logFile.exists() && logFile.length() > MAX_FILE_SIZE) {
            File oldLog = new File(logFile.getParent(), "corgy_game_logs_old.txt");
            if (oldLog.exists()) oldLog.delete(); // Видаляємо старий архів
            logFile.renameTo(oldLog); // Поточний файл стає архівом
            logFile = new File(logFile.getParent(), "corgy_game_logs.txt"); // Створюється новий чистий файл
        }

        // Запис даних
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.append(message).append("\n");
            if (exception != null) {
                writer.append(Log.getStackTraceString(exception)).append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to write log to file", e);
        }
    }
}