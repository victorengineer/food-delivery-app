package com.victorengineer.food_delivery_app.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.victorengineer.food_delivery_app.BaseActivity;
import com.victorengineer.food_delivery_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.victorengineer.food_delivery_app.models.Order;
import com.victorengineer.food_delivery_app.models.Report;
import com.victorengineer.food_delivery_app.models.User;
import com.victorengineer.food_delivery_app.models.UserLocation;
import com.victorengineer.food_delivery_app.util.LoadingView;
import com.victorengineer.food_delivery_app.util.LocalStorage;
import com.victorengineer.food_delivery_app.util.SessionHandler;

import static com.victorengineer.food_delivery_app.Constants.ERROR_DIALOG_REQUEST;
import static com.victorengineer.food_delivery_app.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.victorengineer.food_delivery_app.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;


public class MainActivity extends BaseActivity implements BaseFragment.OnChangeListener,
        BottomNavigationView.OnNavigationItemSelectedListener,
        ComplaintsListFragment.ReportListener
{

    public static final String TAG = "MainActivity";

    //widgets
    private BottomNavigationView bottomNavigationView;
    private TextView toolbar;

    //vars
    private FirebaseFirestore mDb;
    private AddOrderFragment addOrderFragment;
    private ComplaintsListFragment complaintsListFragment;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;
    private User user;
    private LoadingView mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        mLoadingView = findViewById(R.id.loading_view);

        mDb = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        init();


    }

    private void init(){
        bottomNavigationView = findViewById(R.id.bottom_nav_home);
        setBottomNavView();
        mLoadingView.setLoading(true);
        mLoadingView.setVisibility(View.VISIBLE);
        String idUser = SessionHandler.getIdUser(getApplicationContext());
        DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                .document(idUser);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    user = task.getResult().toObject(User.class);
                                                                /*
                        mLoadingView.setVisibility(View.GONE);
                        setToolbarTitle(getString(R.string.report));
                        setBoldActionBartitle();
                        addOrderFragment = AddOrderFragment.newInstance(getApplicationContext());
                        addOrReplaceFragment(addOrderFragment, R.id.fragment_container);
                        */

                        mLoadingView.setVisibility(View.GONE);
                        setToolbarTitle(getString(R.string.orders));
                        setBoldActionBartitle();
                        inflateFragmentComplaints();

                }else{
                    Log.w(TAG, "Fail get user");
                }
            }
        });
    }

    private void setToolbarTitle(String title) {
        try {
            toolbar.setText(title);
        } catch (NullPointerException e) {
            e.printStackTrace();e.printStackTrace();
        }
    }

    private void inflateFragmentComplaints(){
        complaintsListFragment = ComplaintsListFragment.newInstance(this);
        addOrReplaceFragment(complaintsListFragment, R.id.fragment_container);
    }

    private void getUserLocation(){

        DocumentReference userRef = mDb.collection(getString(R.string.collection_user_locations))
                .document(FirebaseAuth.getInstance().getUid());

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    UserLocation userLocation = task.getResult().toObject(UserLocation.class);
                    if(userLocation == null){
                        Log.d(TAG, "onComplete: successfully set the user client.");
                        mUserLocation = new UserLocation();
                        mUserLocation.setUser(user);
                        getLastKnownLocation();
                    }else{
                        mUserLocation = userLocation;
                        getLastKnownLocation();
                    }

                }
            }
        });

    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mUserLocation.setGeo_point(geoPoint);
                    mUserLocation.setTimestamp(null);
                    saveUserLocation();
                    //startLocationService();
                }
            }
        });

    }

    private void saveUserLocation(){

        if(mUserLocation != null){
            DocumentReference locationRef = mDb
                    .collection(getString(R.string.collection_user_locations))
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                                "\n latitude: " + mUserLocation.getGeo_point().getLatitude() +
                                "\n longitude: " + mUserLocation.getGeo_point().getLongitude());

                        startActivity(new Intent(getApplicationContext(), MapActivity.class));
                        finish();
                    }
                }
            });
        }
    }


    private void setBottomNavView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.getMenu().getItem(0).setChecked(true);
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            final View iconView = menuView.getChildAt(i).findViewById(android.support.design.R.id.icon);
            iconView.setPadding(0,0,0,0);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0,0,0,0);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            iconView.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {

                getLocationPermission();
            }
        }

    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.menu_map) {
            if(checkMapServices()){
                getLocationPermission();
            }

        } else if (id == R.id.menu_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        }
        return false;
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
    public void onReportSelected(Order order) {
        LocalStorage.setQuantity(order.getQuantity(), this);
        LocalStorage.setOrderId(order.getOrderId(), this);
        LocalStorage.setStatus(order.getEstatus(), this);
        LocalStorage.setDate(order.getTimestamp(), this);
        Intent myIntent = new Intent(getApplicationContext(), ReportDetailActivity.class);
        myIntent.putExtra("orderId", order.getOrderId());
        myIntent.putExtra("food", order.getFood());
        myIntent.putExtra("quantity", order.getQuantity());
        myIntent.putExtra("status", order.getEstatus());
        myIntent.putExtra("date", order.getTimestamp());
        myIntent.putExtra("imgUri", order.getImgUri());
        myIntent.putExtra("totalPrice", order.getTotalPrice());
        myIntent.putExtra("userId", order.getUserId());
        startActivity(myIntent);
    }

    @Override
    public void onAddOrderAction() {
        setToolbarTitle("Add an order");
        setBoldActionBartitle();
        addOrderFragment = AddOrderFragment.newInstance(getApplicationContext());
        addOrReplaceFragment(addOrderFragment, R.id.fragment_container);

    }


}
