package com.victorengineer.food_delivery_app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.victorengineer.food_delivery_app.R;
import com.victorengineer.food_delivery_app.models.Order;
import com.victorengineer.food_delivery_app.models.Report;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Order> mComplaints;
    private Context mContext;

    public ReportAdapter(List<Order> complaintsList, Context context) {
        this.mComplaints = complaintsList;
        this.mContext = context;
    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ReportViewHolder) {
            ReportViewHolder vh = (ReportViewHolder) holder;
            Order order = mComplaints.get(position);

            String totalPrice = "$ " + order.getTotalPrice();
            vh.tvOrder.setText(order.getOrderId());
            vh.tvFood.setText(order.getFood());
            vh.tvQuantity.setText(String.valueOf(order.getQuantity()));
            vh.tvTotalPrice.setText(totalPrice);
        }
    }

    public void updateData(List<Order> orders) {
        this.mComplaints = orders;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mComplaints.size();
    }

    private static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrder;
        TextView tvFood;
        TextView tvQuantity;
        TextView tvTotalPrice;


        ReportViewHolder(View itemView) {
            super(itemView);
            tvOrder = itemView.findViewById(R.id.tv_order);
            tvFood = itemView.findViewById(R.id.tv_food);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
        }

    }

}
