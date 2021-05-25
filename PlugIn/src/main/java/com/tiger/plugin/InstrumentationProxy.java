package com.tiger.plugin;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.text.TextUtils;


public class InstrumentationProxy extends Instrumentation {
    //修改创建Activity的方法
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        String intentName = intent.getStringExtra(Content.GET_ACTIVITY_NAME);
        if (!TextUtils.isEmpty(intentName)) {
            return  super.newActivity(cl, intentName, intent);
        }
        return  super.newActivity(cl, className, intent);
    }
}
