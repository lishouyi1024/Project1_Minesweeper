package com.example.project1_minesweeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView resultMessage = findViewById(R.id.result_message);
        TextView timeTaken = findViewById(R.id.time_taken);
        Button restartGame = findViewById(R.id.restart_game);

        boolean hasWon = getIntent().getBooleanExtra("hasWon", false);
        long timeElapsed = getIntent().getLongExtra("timeElapsed", 0);

        resultMessage.setText(hasWon ? "You Won!" : "You Lost!");
        timeTaken.setText("Time taken: " + timeElapsed / 1000 + " seconds");

        restartGame.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}