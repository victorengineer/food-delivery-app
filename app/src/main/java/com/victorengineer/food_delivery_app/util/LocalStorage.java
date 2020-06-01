package com.victorengineer.food_delivery_app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;


import java.io.IOException;
import java.util.Date;
import java.util.List;


public class LocalStorage extends MultiDexApplication {

  public static final String ORDER_ID = "ORDER_ID";
  public static final String QUANTITY = "QUANTITY";
  private static final String DATE = "DATE";
  private static final String STATUS = "STATUS";



  @Override
  public void onCreate() {
    super.onCreate();
  }


  public static synchronized void setOrderId(String value, Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    prefs.edit().putString(ORDER_ID, value).apply();
  }

  public static synchronized String getOrderId(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getString(ORDER_ID, "");
  }

  public static synchronized void setQuantity(int value, Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    prefs.edit().putInt(QUANTITY, value).apply();
  }

  public static synchronized Integer getQuantity(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getInt(QUANTITY, 0);
  }

  public static synchronized void setStatus(int value, Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    prefs.edit().putInt(STATUS, value).apply();
  }

  public static synchronized Integer getStatus(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getInt(STATUS, 0);
  }

  public static synchronized void setDate(Date value, Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    prefs.edit().putLong(DATE, value.getTime()).apply();
  }

  public static synchronized Long getDate(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getLong(DATE, 0);
  }



}