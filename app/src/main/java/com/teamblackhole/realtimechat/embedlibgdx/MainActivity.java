package com.teamblackhole.realtimechat.embedlibgdx;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.teamblackhole.realtimechat.embedlibgdx.basketball.GameData;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GameData gm = GameData.getInstance();
        gm.setHint(true);
        gm.setBack(true);
    }

    public void startGame(View v) {
        Intent intent = new Intent(this, GameLuncher.class);
        startActivity(intent);
    }
}
