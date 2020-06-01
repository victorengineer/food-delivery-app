package com.victorengineer.food_delivery_app.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.victorengineer.food_delivery_app.R;
import com.victorengineer.food_delivery_app.UserClient;
import com.victorengineer.food_delivery_app.models.Food;
import com.victorengineer.food_delivery_app.models.Order;
import com.victorengineer.food_delivery_app.models.TipoResiduo;
import com.victorengineer.food_delivery_app.models.User;
import com.victorengineer.food_delivery_app.models.UserLocation;
import com.victorengineer.food_delivery_app.models.VolumenResiduo;
import com.victorengineer.food_delivery_app.util.Helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


public class AddOrderFragment extends BaseFragment implements View.OnClickListener, TextWatcher {

    public static final String TAG = AddOrderFragment.class.getSimpleName();
    public static final int RESULT_OK = 1;
    private Spinner mFood;
    private Spinner mVolumenResiduo;
    private Button btnDenunciar;
    private TextInputEditText mQuantity;
    private Button btnSelect;
    private ImageView ivImage;
    private TextView tvTotalPrince;
    private Uri filePath = null;
    private FirebaseFirestore mDb;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private UserLocation mUserLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    private int result = 0;
    private ArrayList<Food> foods = new ArrayList<>();
    List<VolumenResiduo> volumenResiduoList;
    String[] arrayTiposResiduo;
    String[] arrayVolumenResiduo;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Uri imgUri;

    private String userChoosenTask;

    private boolean validate = false;
    private float totalPrice;

    private Context context;
    private FrameLayout dateContainer;

    public static AddOrderFragment newInstance(Context context) {
        AddOrderFragment dialog = new AddOrderFragment();
        dialog.setContext(context);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_order, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        setViews(view);
        checkUserLocation();

        validate = true;
    }

    private void setViews(View view){
        mFood = view.findViewById(R.id.food);
        mFood.requestFocus();
        ivImage = view.findViewById(R.id.ivImage);
        mQuantity = view.findViewById(R.id.quantity);
        mQuantity.setFilters(getFilters(50));
        tvTotalPrince = view.findViewById(R.id.total_price);

        btnDenunciar = view.findViewById(R.id.btn_beneficiario_accept);
        btnDenunciar.setOnClickListener(this);

        mQuantity.addTextChangedListener(this);
        mQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
        mQuantity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    Helpers.hideKeyboard(mQuantity);
                    handled = true;
                }
                return handled;
            }
        });

        initSpinners();


        mFood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.w("position: ", "" + position);
                if(position != 0 &&  !mQuantity.getText().toString().equals("")){
                    for(Food food : foods){
                        Log.w("food ", "" + food.getName());
                        Log.w("food selected ", "" + mFood.getSelectedItem().toString().trim());

                        if(mFood.getSelectedItem().toString().trim().equals(food.getName())){
                            Float quantity;
                            if (TextUtils.isEmpty(mQuantity.getText().toString())) {
                                quantity = 0F;
                            } else {
                                quantity = Float.parseFloat(mQuantity.getText().toString().trim());
                            }
                            totalPrice  = food.getPrice() * quantity;
                            String price = "$ " + food.getPrice() * quantity;
                            tvTotalPrince.setText(price);

                        }
                    }


                    downloadImgReport(new AddOrderFragment.DownloadImgFoodCallback() {
                        @Override
                        public void onImgFoodDownloadedCallback(Uri uri) {
                            Glide.with(getActivity())
                                    .load(uri)
                                    .apply(RequestOptions.fitCenterTransform())
                                    .into(ivImage);
                        }
                    });

                }

                if (validate){
                    if (validarFormulario()){
                        btnDenunciar.setBackground(context.getResources().getDrawable(R.drawable.bg_button_enabled));
                        btnDenunciar.setEnabled(true);
                    } else {
                        btnDenunciar.setBackground(context.getResources().getDrawable(R.drawable.bg_button_disabled));
                        btnDenunciar.setEnabled(false);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //empty
            }

        });


        validate = true;
    }

    private void downloadImgReport(final DownloadImgFoodCallback downloadImgFoodCallback){
        if(mFood.getSelectedItem() != null) {
            String tipoResiduo = mFood.getSelectedItem().toString().trim();
            Log.e("food choosed: ", ":" + tipoResiduo);
            String foodUri = tipoResiduo + ".jpg";
            Log.e("food uri: ", ":" + foodUri);


            storageReference = storage.getReference().child("images/").child(foodUri);
            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        downloadImgFoodCallback.onImgFoodDownloadedCallback(task.getResult());

                    }
                    else {
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });



        }

    }

    private void checkUserLocation(){
        if(mUserLocation == null){
            mUserLocation = new UserLocation();
            DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: successfully set the user client.");
                        User user = task.getResult().toObject(User.class);
                        mUserLocation.setUser(user);
                        UserClient userClient = new UserClient();
                        userClient.setUser(user);
                        getLastKnownLocation();
                    }
                }
            });
        }
        else{
            getLastKnownLocation();
        }


    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    }
                }
            });
        }
    }

    private InputFilter[] getFilters(int maxLength){
        InputFilter[] fArray = new InputFilter[2];
        fArray[0] = new InputFilter.AllCaps();
        fArray[1] = new InputFilter.LengthFilter(maxLength);
        return fArray;
    }

    private void initSpinners(){


        readDataTiposResiduo(new TiposResiduoCallback() {
            @Override
            public void onTiposResiduoCallback(ArrayList<Food> foods) {
                Log.d("ToDoList", "read tipos residuo completed");
                fillTiposResiduo(mFood, foods);

            }

        });



    }

    private void readDataTiposResiduo(final TiposResiduoCallback tiposResiduoCallback){
        CollectionReference tiposResiduosRef = mDb.collection(getString(R.string.collection_foods));
        tiposResiduosRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d("ToDoList", "outside");

                if (task.isSuccessful()) {
                    Log.d("ToDoList", "inside successfull");

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Food food = document.toObject(Food.class);
                        foods.add(food);
                    }

                    tiposResiduoCallback.onTiposResiduoCallback(foods);
                } else {
                    Log.d("ToDoList", "Error getting documents: ", task.getException());
                }
            }
        });
    }


    private void fillTiposResiduo(Spinner mTipoResiduo, List<Food> foods) {
        arrayTiposResiduo = getTiposResiduoArray(foods);

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item, arrayTiposResiduo);
        adapterSpinner.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mTipoResiduo.setAdapter(adapterSpinner);
    }


    private String[] getTiposResiduoArray(List<Food> foods){
        String[] tiposResiduoArr = new String[foods.size() + 1];
        tiposResiduoArr[0] = getString(R.string.tipo_residuo_hint);
        for(int counter = 0; counter < foods.size(); counter++){
            tiposResiduoArr[counter+1] = foods.get(counter).getName();
        }
        return tiposResiduoArr;
    }


    @Override
    public void onClick(View v) {
        checkUserLocation();

        saveReport(new ReportUploadCallback() {
            @Override
            public void onReportUploadCallback() {
                Log.d("upload", "orden enviada");
                resetActivity();

            }

        });



    }


    private void saveReport(final ReportUploadCallback reportUploadCallback){

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Ordenando...");
        progressDialog.show();

        DocumentReference newReportRef = mDb
                .collection(getString(R.string.collection_orders)).document();

        String tipoResiduo = mFood.getSelectedItem().toString().trim();
        String description = mQuantity.getText().toString();

        String foodUri = "images/" + tipoResiduo + ".jpg";
        BigDecimal price = new BigDecimal(totalPrice).setScale(2, RoundingMode.HALF_UP);
        float totalPriceFormat = price.floatValue();

        final Order order = new Order();
        order.setFood(tipoResiduo);
        order.setEstatus(1);
        order.setUserId(FirebaseAuth.getInstance().getUid());
        order.setQuantity(Integer.parseInt(description));
        order.setImgUri(foodUri);
        order.setTotalPrice(totalPriceFormat);
        GeoPoint geoPoint = new GeoPoint(mUserLocation.getGeo_point().getLatitude(), mUserLocation.getGeo_point().getLongitude());
        order.setGeo_point(geoPoint);
        order.setOrderId(newReportRef.getId());


        newReportRef.set(order).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), getString(R.string.report_success), Toast.LENGTH_SHORT).show();
                    reportUploadCallback.onReportUploadCallback();

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed, try again", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private void resetActivity(){
        Intent intent = getActivity().getIntent();
        getActivity().finish();
        startActivity(intent);
    }


    private boolean validarFormulario(){

        if(mQuantity.getText().length() < 1){
            return false;
        }

        boolean seleccionoTipoResiduo = validarTipoResiduo(mFood.getSelectedItem().toString());


        return  seleccionoTipoResiduo;
    }


    private boolean validarTipoResiduo(String txtTipoResiduo){
        if(!txtTipoResiduo.equals(getString(R.string.tipo_residuo_hint))) {
            String toEvalute = txtTipoResiduo.toUpperCase();

            if (foods != null && !foods.isEmpty()) {
                for (Food foods : foods) {
                    if (toEvalute.trim().equalsIgnoreCase(foods.getName().trim())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    public void setContext(Context context) {
        this.context = context;
    }



    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //nothing
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.w("food selected ", "" + mFood.getSelectedItem().toString().trim());

        if(!mFood.getSelectedItem().toString().equals("") && !mQuantity.getText().toString().equals("")){
            for(Food food : foods){
                if(mFood.getSelectedItem().toString().trim().equals(food.getName())){
                    Float quantity;
                    if (TextUtils.isEmpty(mQuantity.getText().toString())) {
                        quantity = 0F;
                    } else {
                        quantity = Float.parseFloat(mQuantity.getText().toString().trim());
                    }

                    String price = "$ " + food.getPrice() * quantity;
                    totalPrice  = food.getPrice() * quantity;
                    tvTotalPrince.setText(price);

                }
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (validate){
            if (validarFormulario()){
                btnDenunciar.setBackground(context.getResources().getDrawable(R.drawable.bg_button_enabled));
                btnDenunciar.setEnabled(true);
            } else {
                btnDenunciar.setBackground(context.getResources().getDrawable(R.drawable.bg_button_disabled));
                btnDenunciar.setEnabled(false);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(getView() == null){
            return;
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    // handle back button's click listener
                    resetActivity();
                    return true;
                }
                return false;
            }
        });
    }


    private interface TiposResiduoCallback {
        void onTiposResiduoCallback(ArrayList<Food> foods);
    }


    private interface  ReportUploadCallback {
        void onReportUploadCallback();
    }

    private interface DownloadImgFoodCallback {
        void onImgFoodDownloadedCallback(Uri uri);
    }

}
