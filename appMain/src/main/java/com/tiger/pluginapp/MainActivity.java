package com.tiger.pluginapp;


import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;

import com.tiger.plugin.Content;



public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void start(View view) {
        Intent intent = new Intent(this, MainActivity2.class);
        intent.putExtra(Content.GET_ACTIVITY_NAME, "com.tiger.appbranch.MainBranchActivity");
        startActivity(intent);
    }
}