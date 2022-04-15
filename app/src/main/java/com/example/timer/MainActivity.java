package com.example.timer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    TextView textViewTitle;
    TextView textViewRecordTime;
    EditText editTextTaskName;

    ImageButton buttonStart;
    ImageButton buttonPause;
    ImageButton buttonStop;

    SharedPreferences sharedPreferences;

    private static final String SAVED_TIME_KEY = "SAVED_TIME_KEY";
    private static final String SAVED_SUBJECT_KEY = "SAVED_SUBJECT_KEY";
    private static final String SAVED_TIMER_PAUSED_KEY = "SAVED_TIMER_PAUSED_KEY";
    private static final String SAVED_TITLE_KEY = "SAVED_TITLE_KEY";

    Timer timer;
    boolean timerIsPaused = false;
    Integer recordTime = 0;

    String titleString = "You spent 00:00:00 on ... last time.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("com.timer.main", MODE_PRIVATE);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewRecordTime = findViewById(R.id.textViewTimer);
        editTextTaskName = findViewById(R.id.editTextTaskName);

        buttonStart = findViewById(R.id.buttonStart);
        buttonPause = findViewById(R.id.buttonPause);
        buttonStop = findViewById(R.id.buttonStop);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            checkSharedPreferences();
            Integer lastSpentTime = sharedPreferences.getInt(SAVED_TIME_KEY, 0);
            timerIsPaused = sharedPreferences.getBoolean(SAVED_TIMER_PAUSED_KEY, false);

            String title = sharedPreferences.getString(SAVED_TITLE_KEY, "");
            textViewTitle.setText(title);

            if (lastSpentTime > 0) {
                recordTime = lastSpentTime;

                if (timerIsPaused) {
                    String displayRecordTime = convertToTimeInString(recordTime);
                    textViewRecordTime.setText(displayRecordTime);
                    updateButtonsForPauseStatus();
                } else {
                    startTimer();
                    updateButtonsWhenTimerRun();
                }
            }
        } else {
            Log.d(MainActivity.this.toString(), "On Create: The First Time");
            checkSharedPreferences();
            updateStatusForButtonsInTheBeginning();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        destroyTimer();
        saveInfoToPreference();
    }

    public void checkSharedPreferences() {
        Log.d(MainActivity.this.toString(), "Check Shared Preference");
        Integer lastSpentTime = sharedPreferences.getInt(SAVED_TIME_KEY, 0);
        Log.d(MainActivity.this.toString(), "Get last spent time from Preference = " + lastSpentTime);
        String lastSpentTimeInString = convertToTimeInString(lastSpentTime);

        String subjectName = sharedPreferences.getString(SAVED_SUBJECT_KEY, "...");
        Log.d(MainActivity.this.toString(), "Get Subject from Preference = " +subjectName);

        textViewTitle.setText("You spent " + lastSpentTimeInString + " on " + subjectName + " last time.");
    }

    public void updateStatusForButtonsInTheBeginning() {
        // Enable start button

        // Disable pause & stop button
        buttonPause.setEnabled(false);
        buttonPause.setAlpha(0.5F);

        buttonStop.setEnabled(false);
        buttonStop.setAlpha(0.5F);
    }

    public String convertToTimeInString(Integer durationInSecond) {
        String timeInString = "";

        Integer hours = durationInSecond / (60 * 60);
        Integer minutes = (durationInSecond % 3600) / 60;
        Integer seconds = durationInSecond % 60;
        timeInString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return timeInString;
    }

    public void askUserToEnterTaskName() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("");
        alertDialog.setMessage("Please enter your task!");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    public  void startClick(View view) {
        Log.d(MainActivity.this.toString(), "Click Start Button");
        if (TextUtils.isEmpty(editTextTaskName.getText())) {
            askUserToEnterTaskName();
        } else {
            startTimer();
        }
    }

    public void startTimer() {

        timerIsPaused = false;

        if (timer != null) {
            return;
        }

        // Create timer
        timer = new Timer();
        timer.schedule( new TimerTask(){
            public void run(){
                timerChangeState();
            }},0,  1000 );

        updateButtonsWhenTimerRun();
    }

    public void updateButtonsWhenTimerRun() {
        // Disable start button
        buttonStart.setEnabled(false);
        buttonStart.setAlpha(0.5F);

        // Enable pause button
        buttonPause.setEnabled(true);
        buttonPause.setAlpha(1.0F);

        // Enable stop button
        buttonStop.setEnabled(true);
        buttonStop.setAlpha(1.0F);
    }

    public void timerChangeState() {
        Log.d(MainActivity.this.toString(), "Timer Changed State");

        recordTime += 1;
        Log.d(MainActivity.this.toString(), "Record Time = " + recordTime.toString());

        String displayRecordTime = convertToTimeInString(recordTime);
        textViewRecordTime.setText(displayRecordTime);
    }

    public  void pauseClick(View view) {
        destroyTimer();

        timerIsPaused = true;

        updateButtonsForPauseStatus();
    }

    public void updateButtonsForPauseStatus() {
        // Disable pause button
        buttonPause.setEnabled(false);
        buttonPause.setAlpha(0.5F);

        // Enable start button
        buttonStart.setEnabled(true);
        buttonStart.setAlpha(1.0F);

        // Enable stop button
        buttonStop.setEnabled(true);
        buttonStop.setAlpha(1.0F);
    }

    public  void stopClick(View view) {
        destroyTimer();

        timerIsPaused = false;

        // Save record time & subject to preference
        saveInfoToPreference();

        // Disable all buttons
        buttonStart.setEnabled(false);
        buttonStart.setAlpha(0.5F);

        buttonPause.setEnabled(false);
        buttonPause.setAlpha(0.5F);

        buttonStop.setEnabled(false);
        buttonStop.setAlpha(0.5F);

        editTextTaskName.setEnabled(false);
    }

    public void destroyTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void saveInfoToPreference() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SAVED_TIME_KEY, recordTime);
        editor.putString(SAVED_SUBJECT_KEY, editTextTaskName.getText().toString());
        editor.putBoolean(SAVED_TIMER_PAUSED_KEY, timerIsPaused);
        editor.putString(SAVED_TITLE_KEY, textViewTitle.getText().toString());
        editor.apply();
    }
}