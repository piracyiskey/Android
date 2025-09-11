package com.example.convenient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.convenient.R;
import com.example.convenient.Utils.StarRatingUtil;
import com.example.convenient.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private final Context context;
    private final List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.txtName.setText(review.getUserName());
        holder.txtDate.setText(review.getCreatedDate());
        holder.txtReview.setText(review.getBody());
        holder.txtStar.setText(String.valueOf(review.getStar()));
        StarRatingUtil.setStarRating(holder.itemView, review.getStar());

        Glide.with(context)
                .load(review.getProfileURL())
                .placeholder(R.drawable.default_profile_pic)
                .into(holder.imgProfile);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView txtName, txtDate, txtReview, txtStar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtName = itemView.findViewById(R.id.txtName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtReview = itemView.findViewById(R.id.txtReview);
            txtStar = itemView.findViewById(R.id.txtStar);
        }
    }
}
