package com.tiger.appbranch;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

import com.tiger.plugin.PlugIn;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class MainBranchActivity extends Activity {
    //插件通过PlugIn来获取资源文件
    @Override
    public Resources getResources() {
        return PlugIn.getInstance().getResource(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mains);

    }
}
