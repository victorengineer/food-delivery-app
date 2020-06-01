package com.victorengineer.food_delivery_app.util;

import com.victorengineer.food_delivery_app.models.Result;

public interface ResultListener <I> {

    void onResult(Result result, I instance);

}
