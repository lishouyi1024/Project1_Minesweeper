package com.example.project1_minesweeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 10;
    private static final int ROW_COUNT = 12;
    private static final int MINE_COUNT = 4;
    private int[][] gridData; // -1 for mine, 0-8 for number of adjacent mines
    private boolean[][] revealed; // to check if a cell is revealed or not
    private boolean flaggingMode = false; // false = digging, true = flagging
    private int flagsPlaced = 0;
    private int clock = 0;
    private boolean running = false; // flag to start or stop the timer
    private boolean gameOver = false;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new ArrayList<TextView>();

        // Add four dynamically created cells
        androidx.gridlayout.widget.GridLayout grid = (androidx.gridlayout.widget.GridLayout) findViewById(R.id.gridLayout01);
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                TextView tv = new TextView(this);
                tv.setHeight(dpToPixel(30));
                tv.setWidth(dpToPixel(30));
                tv.setTextSize(14);
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.GREEN);
                tv.setOnClickListener(this::onClickTV);

                androidx.gridlayout.widget.GridLayout.LayoutParams lp = new androidx.gridlayout.widget.GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(1), dpToPixel(1), dpToPixel(1), dpToPixel(1));
                lp.rowSpec = androidx.gridlayout.widget.GridLayout.spec(i);
                lp.columnSpec = androidx.gridlayout.widget.GridLayout.spec(j);

                grid.addView(tv, lp);
                cell_tvs.add(tv);
            }
        }

        gridData = new int[ROW_COUNT][COLUMN_COUNT];
        revealed = new boolean[ROW_COUNT][COLUMN_COUNT];
        placeRandomMines();
        computeAdjacentMines();

        TextView modeTV = findViewById(R.id.diggingOrFlagging);
        modeTV.setOnClickListener(v -> {
            flaggingMode = !flaggingMode;
            modeTV.setText(flaggingMode ? "🚩" : "⛏");
        });

        runTimer();
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    private void placeRandomMines() {
        Random rand = new Random();
        int minesPlaced = 0;

        while(minesPlaced < MINE_COUNT) {
            int i = rand.nextInt(ROW_COUNT);
            int j = rand.nextInt(COLUMN_COUNT);

            if(gridData[i][j] != -1) {
                gridData[i][j] = -1;
                minesPlaced++;
            }
        }
    }

    private void computeAdjacentMines() {
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                if (gridData[i][j] != -1) {
                    int count = 0;
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            if (i + x >= 0 && i + x < ROW_COUNT && j + y >= 0 && j + y < COLUMN_COUNT && gridData[i + x][j + y] == -1) {
                                count++;
                            }
                        }
                    }
                    gridData[i][j] = count;
                }
            }
        }
    }

    private void revealCell(int i, int j) {
        if (i < 0 || i >= ROW_COUNT || j < 0 || j >= COLUMN_COUNT || revealed[i][j]) return;

        TextView tv = cell_tvs.get(i * COLUMN_COUNT + j);

        revealed[i][j] = true;

        if (gridData[i][j] == 0) {
            tv.setText("");
            tv.setBackgroundColor(Color.LTGRAY);

            // Recursively reveal neighbors
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    revealCell(i + x, j + y);
                }
            }
        }
        else {
            tv.setText(String.valueOf(gridData[i][j]));
            tv.setBackgroundColor(Color.LTGRAY);
        }
    }

    private void revealAllCells() {
        for(int i = 0; i < ROW_COUNT; i++) {
            for(int j = 0; j < COLUMN_COUNT; j++) {
                TextView tv = cell_tvs.get(i * COLUMN_COUNT + j);
                if(gridData[i][j] == -1) {
                    tv.setText("💣");
                    tv.setBackgroundColor(Color.RED);
                }
                else {
                    tv.setText(String.valueOf(gridData[i][j]));
                    tv.setBackgroundColor(Color.LTGRAY);
                    if (gridData[i][j] == 0) {
                        tv.setText("");
                    }
                }
            }
        }
    }

    private void revealBombs() {
        int color;
        if (hasPlayerWon()) {
            color = Color.GREEN;
        }
        else {
            color = Color.RED;
        }
        for(int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                TextView tv = cell_tvs.get(i * COLUMN_COUNT + j);
                if(gridData[i][j] == -1) {
                    tv.setText("💣");
                    tv.setBackgroundColor(color);
                }
            }
        }
    }

    private boolean hasPlayerWon() {
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                if (gridData[i][j] != -1 && !revealed[i][j]) {
                    return false; // Player hasn't revealed all non-mine cells
                }
            }
        }
        return true;
    }

    private void updateFlagCount() {
        TextView tv = (TextView) findViewById(R.id.flagCount);
        String flg = String.format("🚩 %01d", MINE_COUNT - flagsPlaced);
        tv.setText(flg);
    }

    private void showResultPage() {
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        intent.putExtra("timeElapsed", clock);
        intent.putExtra("hasWon", hasPlayerWon());
        startActivity(intent);
    }

    public void onClickTV(View view) {
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int i = n / COLUMN_COUNT;
        int j = n % COLUMN_COUNT;

        if (!running) {
            running = true; // start timer
        }

        if (gameOver) {
            showResultPage();
            return;
        }

        if(flaggingMode) {
            // Handle flag placement
            if(!revealed[i][j]) {
                if(tv.getText().equals("🚩")) {
                    tv.setText(""); // remove flag
                    flagsPlaced--;
                    updateFlagCount();
                }
                else {
                    tv.setText("🚩"); // place flag
                    flagsPlaced++;
                    updateFlagCount();
                }
            }
        }
        else {
            // Handle digging
            if(gridData[i][j] == -1) {
                // Mine. Game over.
                gameOver = true;
                running = false; // stop timer
                revealBombs();
            }
            else {
                revealCell(i, j);
            }
        }

        if (hasPlayerWon()) {
            gameOver = true;
            running = false; // stop timer
            revealBombs();
        }

    }


    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.secondCount);
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                String time = String.format("\uD83D\uDD53 %03d", clock);
                timeView.setText(time);

                if (running) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }



}