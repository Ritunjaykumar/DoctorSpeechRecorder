package com.softgyan.doctor.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserInfo {

    private static final String DATA_HOLDER = "user_info";
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    private static final String IS_LOGIN = "is_login";
    private final SharedPreferences sharedPreferences;

    private static UserInfo userInfo;

    private UserInfo(Context context) {
        sharedPreferences = context.getSharedPreferences(DATA_HOLDER, Context.MODE_PRIVATE);
    }

    public static UserInfo getInstance(Context context) {
        if (userInfo == null) {
            userInfo = new UserInfo(context);
        }
        return userInfo;
    }

    public void setUserInfo(String userId, String userName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ID, userId);
        editor.putString(USER_NAME, userName);
        editor.putBoolean(IS_LOGIN, true);
        editor.apply();
    }

    public String getUserId(){
        return sharedPreferences.getString(USER_ID, null);
    }

    public String getUserName(){
        return sharedPreferences.getString(USER_NAME, null);
    }

    public boolean isLogin() {
        return sharedPreferences.getBoolean(IS_LOGIN, false);
    }

    public void logOut() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
