package com.example.dino;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import java.util.UUID;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Context context;

    // Тепер ми передаємо Context, щоб мати доступ до пам'яті
    public CrashHandler(MainActivity mainActivity) {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.context = context;
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        String errorId = UUID.randomUUID().toString().substring(0, 8);

        GameLogger.log(GameLogger.CRITICAL, "CrashHandler",
                "FATAL CRASH in thread " + thread.getName() + " | ErrorID: " + errorId, throwable);

        // Зберігаємо інформацію про краш, щоб показати при наступному запуску (Вимога для 100%)
        SharedPreferences prefs = context.getSharedPreferences("CrashPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("crashed_last_time", true)
                .putString("crash_error_id", errorId)
                .commit(); // commit() записує синхронно, поки потік не вбили

        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable);
        }
    }
}