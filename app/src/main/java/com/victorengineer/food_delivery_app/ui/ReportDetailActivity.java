package com.victorengineer.food_delivery_app.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.victorengineer.food_delivery_app.BaseActivity;
import com.victorengineer.food_delivery_app.R;
import com.victorengineer.food_delivery_app.models.Order;
import com.victorengineer.food_delivery_app.models.Report;
import com.victorengineer.food_delivery_app.models.Result;
import com.victorengineer.food_delivery_app.util.LocalStorage;
import com.victorengineer.food_delivery_app.util.ResultListener;

import java.util.Date;

public class ReportDetailActivity extends BaseActivity implements BaseFragment.OnChangeListener, ResultListener<Fragment> {

    public static final String ORDER_ID = "orderId";
    public static final String FOOD = "food";
    public static final String QUANTITY = "quantity";
    public static final String STATUS = "status";
    public static final String DATE = "date";
    public static final String IMG_URI = "imgUri";
    public static final String TOTAL_PRICE = "totalPrice";
    public static final String USER_ID = "userId";


    private ReportDetailFragment reportDetailFragment;
    private TextView btnAprove;
    private TextView toolbar;
    private ImageView btnBack;
    String orderId;
    String food;
    int quantity;
    int status;
    Date date;
    float totalPrice;
    String userId;

    String imgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btn_back_action);
        setToolbarTitle(getString(R.string.report_detail));
        setBoldActionBartitle();


        orderId = getIntent().getExtras().getString(ORDER_ID, null);
        food = getIntent().getExtras().getString(FOOD, null);
        quantity = getIntent().getExtras().getInt(QUANTITY, 0);
        status = getIntent().getExtras().getInt(STATUS, 0);
        date = (Date)getIntent().getSerializableExtra(DATE);
        imgUri = getIntent().getExtras().getString(IMG_URI, null);
        totalPrice = getIntent().getExtras().getFloat(TOTAL_PRICE, 0);
        userId = getIntent().getExtras().getString(USER_ID, null);

        Order order = new Order();
        order.setOrderId(orderId);
        order.setFood(food);
        order.setQuantity(quantity);
        order.setEstatus(status);
        order.setTimestamp(date);
        order.setImgUri(imgUri);
        order.setTotalPrice(totalPrice);
        order.setUserId(userId);
        reportDetailFragment = ReportDetailFragment.create(this, order, this);
        addOrReplaceFragment(reportDetailFragment, R.id.fragment_container_detail);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setToolbarTitle(String title) {
        try {
            toolbar.setText(title);;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    public void onDataChanged() {

    }

    @Override
    public void onFragmentAttached() {

    }

    @Override
    public void onStartNewActivity(String stringActivity) {

    }

    @Override
    public void onResult(Result result, Fragment instance) {
        switch (result){
            case OK:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

}
