package com.victorengineer.food_delivery_app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.victorengineer.food_delivery_app.R;
import com.victorengineer.food_delivery_app.models.Order;
import com.victorengineer.food_delivery_app.models.Report;
import com.victorengineer.food_delivery_app.models.Result;
import com.victorengineer.food_delivery_app.util.LoadingView;
import com.victorengineer.food_delivery_app.util.LocalStorage;
import com.victorengineer.food_delivery_app.util.ResultListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ReportDetailFragment extends BaseFragment{

    private TextView tvFood;
    private TextView tvQuantity;
    private TextView tvStatus;
    private TextView tvTotalPrice;
    private TextView tvUserId;
    private ImageView ivImage;
    private TextView tvOrderDate;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private LoadingView mLoadingView;
    private Order order;
    private ResultListener<Fragment> resultListener;
    Context context;

    public static ReportDetailFragment create(Context context, Order order, ResultListener<Fragment> listener) {

        ReportDetailFragment fragment = new ReportDetailFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        fragment.context = context;
        fragment.order = order;
        fragment.setResultListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_report_detail,
                container,
                false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.tvFood = view.findViewById(R.id.tv_food_detail);
        this.tvQuantity = view.findViewById(R.id.tv_quantity_detail);
        this.tvStatus = view.findViewById(R.id.tv_status_detail);
        this.tvTotalPrice = view.findViewById(R.id.tv_total_price_detail);
        this.tvUserId = view.findViewById(R.id.tv_user_id_detail);
        this.ivImage = view.findViewById(R.id.ivImage_detail);
        this.tvOrderDate = view.findViewById(R.id.tv_date_order_detail);
        mLoadingView = view.findViewById(R.id.loading_view);

        //storage = FirebaseStorage.getInstance();
        //String photoName = report.getImgUri() + ".jpg";
        storage = FirebaseStorage.getInstance();
        //storageReference = storage.getReference();
        /*
        mReportRejected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RejectReportDialog dialog = RejectReportDialog.newInstance(order.getOrderId(), resultListener);
                dialog.show(getChildFragmentManager(), RejectReportDialog.class.getSimpleName());
            }
        });

         */

        setDataFragment();

    }



    private void setDataFragment(){
        mLoadingView.setLoading(true);
        mLoadingView.setVisibility(View.VISIBLE);
        int quantity =  LocalStorage.getQuantity(getContext());
        int status =  LocalStorage.getStatus(getContext());
        Long date =  LocalStorage.getDate(getContext());
        Log.w("set views: ", "" + quantity);
        String status_lbl = "";
        if(status == 1){
            status_lbl = "Processing order";
        }else if(status == 2){
            status_lbl = "Preparing a distribution route";
        }else if(status == 3){
            status_lbl = "Order on the way";
        }else if(status == 4){
            status_lbl = "Order delivered";
        }

        tvQuantity.setText(Integer.toString(quantity));
        tvStatus.setText(status_lbl);
        tvFood.setText(order.getFood());
        String total_price = "$ " + order.getTotalPrice();
        tvTotalPrice.setText(total_price);
        tvUserId.setText(order.getUserId());

        Date d = new Date(date * 1000);

        SimpleDateFormat dateFormat = new SimpleDateFormat("d 'de' MMMM 'del' yyyy', ' hh:mm aaa", Locale.getDefault());
        String fechaReportadaFormat = dateFormat.format(d);
        tvOrderDate.setText(fechaReportadaFormat);

        downloadImgReport(new ReportDetailCallback() {
            @Override
            public void onImgDownloadedCallback() {

            }
        });


    }

    private void downloadImgReport(final ReportDetailCallback reportDetailCallback){
        String pathfile =  order.getImgUri();
        Log.w("pathfile: ", "" + pathfile);

        storageReference = storage.getReference().child(pathfile);
        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful())
                {

                    Glide.with(getActivity())
                            .load(task.getResult())
                            .apply(RequestOptions.fitCenterTransform())
                            .into(ivImage);

                    mLoadingView.setVisibility(View.GONE);

                    reportDetailCallback.onImgDownloadedCallback();


                }
                else {
                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void setResultListener(ResultListener<Fragment> resultListener) {
        this.resultListener = resultListener;
    }


    private interface ReportDetailCallback {
        void onImgDownloadedCallback();
    }



}
