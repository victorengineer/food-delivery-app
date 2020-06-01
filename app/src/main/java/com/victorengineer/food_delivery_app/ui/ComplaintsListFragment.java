package com.victorengineer.food_delivery_app.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.victorengineer.food_delivery_app.R;
import com.victorengineer.food_delivery_app.adapters.RecyclerItemClickListener;
import com.victorengineer.food_delivery_app.adapters.ReportAdapter;
import com.victorengineer.food_delivery_app.models.Order;
import com.victorengineer.food_delivery_app.models.Report;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ComplaintsListFragment extends BaseFragment implements View.OnClickListener, RecyclerItemClickListener.OnItemClickListener {

    public static final String TAG = ComplaintsListFragment.class.getSimpleName();

    private Context context;
    private FirebaseFirestore mDb;


    private RecyclerView rvComplaints;
    private RelativeLayout loadingView;
    private ReportListener listener;

    private List<Order> orders = new ArrayList<>();
    private ReportAdapter adapter;
    private FloatingActionButton btnAddOrder;


    public ComplaintsListFragment() {
    }

    public static ComplaintsListFragment newInstance( ReportListener reportListener) {
        ComplaintsListFragment fragment = new ComplaintsListFragment();
        Bundle args = new Bundle();
        fragment.listener = reportListener;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = FirebaseFirestore.getInstance();

    }

    public void setContext(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_complaints_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

        setViews(view);


    }

    private void setViews(View view){

        rvComplaints = view.findViewById(R.id.rv_complaints_list);
        loadingView = view.findViewById(R.id.relative_loading);
        btnAddOrder = view.findViewById(R.id.btn_add_order);

        rvComplaints.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), this));

        loadingView.setVisibility(View.VISIBLE);


        readData(new ComplaintsListFragment.ReportListCallback() {
            @Override
            public void onReportListCallback(List<Order> orders) {
                rvComplaints.setVisibility(View.VISIBLE);
                rvComplaints.setLayoutManager(new LinearLayoutManager(getContext()));
                Log.w("orders: ", "" + orders.size());
                adapter = new ReportAdapter(orders, getContext());
                rvComplaints.setAdapter(adapter);

                if (loadingView != null){
                    loadingView.setVisibility(View.GONE);
                }

            }

        });

        btnAddOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.onAddOrderAction();
                }
            }
        });


    }


    private void readData(final ReportListCallback reportListCallback){



        CollectionReference complaintsListRef = mDb.collection(getString(R.string.collection_orders));

        complaintsListRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    Log.d("complaints", "complaints successfull");

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String orderId = document.getString("orderId");
                        String food = document.getString("food");
                        int quantity = ((Long) document.get("quantity")).intValue();
                        int estatus = ((Long) document.get("estatus")).intValue();
                        Date timestamp = document.getDate("timestamp");
                        String imgUri = document.getString("imgUri");
                        float totalPrice =  ((Double) document.get("totalPrice")).floatValue();
                        String userId =  document.getString("userId");

                        Order order = new Order();
                        order.setOrderId(orderId);
                        order.setFood(food);
                        order.setQuantity(quantity);
                        order.setEstatus(estatus);
                        order.setTimestamp(timestamp);
                        order.setTotalPrice(totalPrice);
                        order.setUserId(userId);
                        order.setImgUri(imgUri);
                        orders.add(order);

                    }

                    reportListCallback.onReportListCallback(orders);
                } else {
                    Log.d("ToDoList", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Order order = orders.get(position);
        if(listener != null) {
            listener.onReportSelected(order);
        }
    }

    @Override
    public void onClick(View v) {

    }

    private interface ReportListCallback {
        void onReportListCallback(List<Order> orders);
    }

    public interface ReportListener {
        void onReportSelected(Order order);
        void onAddOrderAction();
    }



}
