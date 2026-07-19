package com.app.sonictaxi;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class RideAdapter extends FirebaseRecyclerAdapter<RideRequest, RideAdapter.RideViewHolder> {

    public RideAdapter(@NonNull FirebaseRecyclerOptions<RideRequest> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RideViewHolder holder, int position, @NonNull RideRequest model) {
        if (model == null) return;

        holder.tvStatus.setText("الحالة: " + (model.getStatus() != null ? model.getStatus() : "قيد المعالجة"));

        double priceToShow = (model.getFinalPrice() > 0) ? model.getFinalPrice() : model.getOfferPrice();
        holder.tvPrice.setText("السعر: " + priceToShow + " شيكل");

        holder.tvRating.setText("★ " + String.format("%.1f", model.getRating()));

        if (model.getRating() >= 4.5) holder.tvRating.setTextColor(Color.parseColor("#388E3C"));
        else if (model.getRating() >= 3.0) holder.tvRating.setTextColor(Color.parseColor("#FBC02D"));
        else holder.tvRating.setTextColor(Color.parseColor("#D32F2F"));

        holder.btnAction.setOnClickListener(v -> {
            String rideId = getRef(position).getKey();
            if (rideId != null) {
                FirebaseDatabase.getInstance().getReference("Rides")
                        .child(rideId).child("status").setValue("suspended");
                Toast.makeText(v.getContext(), "تم إيقاف الطلب", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvPrice, tvRating;
        Button btnAction;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tv_ride_status);
            tvPrice = itemView.findViewById(R.id.tv_ride_price);
            tvRating = itemView.findViewById(R.id.tv_ride_rating);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}