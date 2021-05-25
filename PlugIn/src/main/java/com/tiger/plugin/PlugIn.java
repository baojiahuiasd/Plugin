package com.tiger.plugin;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.sax.Element;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class PlugIn {
    private static PlugIn plugIn;
    private Resources newResourcesObj;
    private boolean isPlug;
    private AssetManager mAssetManager;

    private PlugIn() {
    }

    public static PlugIn getInstance() {
        if (plugIn == null) {
            plugIn = new PlugIn();
        }
        return plugIn;
    }

    public Resources getResource(Context appContext) {
        return isPlug ? newResourcesObj : appContext.getResources();
    }

    public void init(Context appContext, String... packageName) {
        //修改创建Activity
        if (isPlug) return;
        hookInstrumentation();
        //添加插件的Dex
        if (packageName != null) {
            DexFileUtils.addDexPathList(appContext, packageName);
        }
        //修改资源加载器
        hookAssetManager(appContext, packageName);
        isPlug = true;
        //代理Activity返回的getResources
//        Proxy.newProxyInstance(appContext.getClassLoader(), new Class[]{Context.class}, new InvocationHandler() {
//            @Override
//            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                if (method.getName() == "getResources") {
//                    Log.e("ATTA", "被代理了啊");
//                    return newResourcesObj;
//                }
//                return method.invoke(proxy, args);
//            }
//        });
    }

    private void hookInstrumentation() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field activityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            activityThreadField.setAccessible(true);
            //获取ActivityThread对象sCurrentActivityThread
            Object activityThread = activityThreadField.get(null);

            Field instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            instrumentationField.setAccessible(true);
            //从sCurrentActivityThread中获取成员变量mInstrumentation
//            Instrumentation instrumentation = (Instrumentation) instrumentationField.get(activityThread);
            //创建代理对象InstrumentationProxy
            InstrumentationProxy proxy = new InstrumentationProxy();
            //将sCurrentActivityThread中成员变量mInstrumentation替换成代理类InstrumentationProxy
            instrumentationField.set(activityThread, proxy);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    //创建新的插件资源类
    private void hookAssetManager(Context appContext, String... packageName) {
        try {
            mAssetManager = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            addAssetPath.setAccessible(true);
            for (String path : packageName) {
                ApplicationInfo applicationInfo = appContext.getPackageManager().getApplicationInfo(path, 0);
                addAssetPath.invoke(mAssetManager, applicationInfo.sourceDir);
            }
            Method ensureStringBlocksMethod = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
            ensureStringBlocksMethod.setAccessible(true);
            ensureStringBlocksMethod.invoke(mAssetManager);
            newResourcesObj = new Resources(mAssetManager,
                    appContext.getResources().getDisplayMetrics(),
                    appContext.getResources().getConfiguration());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}
