package com.example.convenient.Utils;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.convenient.R;

public class StarRatingUtil {
    public static void setStarRating(View rootView, double rating) {
        int[] stars = { R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5 };
        int fullStars = (int) rating;
        boolean hasHalf = (rating - fullStars) >= 0.25 && (rating - fullStars) < 0.75;

        for (int i = 0; i < 5; i++) {
            ImageView star = rootView.findViewById(stars[i]);
            if (i < fullStars) {
                star.setImageResource(R.drawable.ic_star_full);
            } else if (i == fullStars && hasHalf) {
                star.setImageResource(R.drawable.ic_star_half);
            } else {
                star.setImageResource(R.drawable.ic_star_empty);
            }
        }
    }

}
