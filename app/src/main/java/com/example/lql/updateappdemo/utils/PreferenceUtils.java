package com.example.lql.updateappdemo.utils;

import android.content.SharedPreferences;

/**
* 类描述：SharedPreferences  工具类
* 作  者：李清林
* 时  间：2016/10/11.
* 修改备注：
* 使用说明：1、需要在Application中声明
 *         2、在PublicStatic 中有静态变量
*/
public class PreferenceUtils {


   public static void setFloat(String key, float value){
       SharedPreferences.Editor editor = PublicStaticData.mySharedPreferences.edit();
       editor.putFloat(key, value);
       editor.commit();
   }


   public static Float getFloat(String key, float devalue){
       return  PublicStaticData.mySharedPreferences.getFloat(key,devalue);
   }
   /**
    * 存String
    * @param key  键
    * @param value  值
    */
   public static void  setString(String key, String value){
       SharedPreferences.Editor editor = PublicStaticData.mySharedPreferences.edit();
       editor.putString(key, value);
       editor.commit();
   }

   /**
    * 取String
    * @param key   键
    * @param devalue  默认值
    * @return
    */

   public static String getString(String key, String devalue){
       return  PublicStaticData.mySharedPreferences.getString(key,devalue);
   }

   /**
    * 取boolean
    * @param key   键
    * @param defValue  默认值
    * @return
    */
   public static boolean getBoolean(String key, boolean defValue) {
       return PublicStaticData.mySharedPreferences.getBoolean(key, defValue);
   }

   /**
    * 存boolean
    * @param key  键
    * @param value  值
    */
   public static void setBoolean(String key, boolean value) {
       SharedPreferences.Editor editor = PublicStaticData.mySharedPreferences.edit();
       editor.putBoolean(key, value);
       editor.commit();
   }

   /**
    * 取int
    * @param key   键
    * @param defValue  默认值
    * @return
    */
   public static int getInt(String key, int defValue) {
       return PublicStaticData.mySharedPreferences.getInt(key, defValue);
   }

   /**
    * 存int
    * @param key  键
    * @param value  值
    */
   public static void setInt(String key, int value) {
       SharedPreferences.Editor editor = PublicStaticData.mySharedPreferences.edit();
       editor.putInt(key, value);
       editor.commit();
   }

   /**
    * 清空一个
    * @param key
    */
   public static void remove(String key) {
       SharedPreferences.Editor editor = PublicStaticData.mySharedPreferences.edit();
       editor.remove(key);
       editor.commit();
   }

   /**
    * 清空所有
    */
   public static void removeAll() {
       SharedPreferences.Editor editor = PublicStaticData.mySharedPreferences.edit();
       editor.clear();
       editor.commit();
   }
}
