package com.victorengineer.food_delivery_app;

import android.app.Application;

import com.victorengineer.food_delivery_app.models.User;


public class UserClient extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
