package com.tiger.plugin;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import static com.tiger.plugin.Content.OPTIMIZE_DEX_DIR;

public class DexFileUtils {


    static void addDexPathList(Context appContext, String[] packages) {
        //data/data/包名/files/odex packages
        String optimizeDir = appContext.getFilesDir().getAbsolutePath() + File.separator + OPTIMIZE_DEX_DIR;// data/data/包名/files/optimize_dex（这个必须是自己程序下的目录）
        File fopt = new File(optimizeDir);
        if (!fopt.exists()) {
            fopt.mkdirs();
        }
        try {
            PathClassLoader classLoader = (PathClassLoader) appContext.getClassLoader();
            for (String packs : packages) {
                ApplicationInfo applicationInfo = appContext.getPackageManager().getApplicationInfo(packs, 0);
                System.out.println("add dexElements of " + applicationInfo.sourceDir);
                DexClassLoader dexLoader = new DexClassLoader(
                        applicationInfo.sourceDir,// 修复好的dex（补丁）所在目录
                        fopt.getAbsolutePath(),// 存放dex的解压目录（用于jar、zip、apk格式的补丁）
                        null,// 加载dex时需要的库
                        classLoader);// 父类加载器
                Object dexPathList = getPathList(dexLoader);
                Object pathPathList = getPathList(classLoader);
                Object leftDexElements = getDexElements(dexPathList);
                Object rightDexElements = getDexElements(pathPathList);
                // 合并完成
                Object dexElements = combineArray(leftDexElements, rightDexElements);
                // 重写给PathList里面的Element[] dexElements;赋值
                Object pathList = getPathList(classLoader);// 一定要重新获取，不要用pathPathList，会报错
                setField(pathList, pathList.getClass(), "dexElements", dexElements);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射得到类加载器中的pathList对象
     */
    private static Object getPathList(Object baseDexClassLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    /**
     * 反射得到对象中的属性值
     */
    private static Object getField(Object obj, Class<?> cl, String field) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    /**
     * 反射得到pathList中的dexElements
     */
    private static Object getDexElements(Object pathList) throws NoSuchFieldException, IllegalAccessException {
        return getField(pathList, pathList.getClass(), "dexElements");
    }

    /**
     * 数组合并
     */
    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> componentType = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);// 得到左数组长度（补丁数组）
        int j = Array.getLength(arrayRhs);// 得到原dex数组长度
        int k = i + j;// 得到总数组长度（补丁数组+原dex数组）
        Object result = Array.newInstance(componentType, k);// 创建一个类型为componentType，长度为k的新数组
        System.arraycopy(arrayLhs, 0, result, 0, i);
        System.arraycopy(arrayRhs, 0, result, i, j);
        return result;
    }

    /**
     * 反射给对象中的属性重新赋值
     */
    private static void setField(Object obj, Class<?> cl, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cl.getDeclaredField(field);
        declaredField.setAccessible(true);
        declaredField.set(obj, value);
    }

}
