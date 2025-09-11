package com.example.convenient.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.convenient.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class MomoFragment extends Fragment {

    public MomoFragment() {}

    public static MomoFragment newInstance(String total) {
        MomoFragment fragment = new MomoFragment();
        Bundle args = new Bundle();
        args.putString("finalTotal", total);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);
        ImageView img = view.findViewById(R.id.imgPayment);
        TextView txtTotal = view.findViewById(R.id.txtFinalTotal);

        String finalTotal = getArguments() != null ? getArguments().getString("finalTotal") : "0 VND";
        txtTotal.setText(finalTotal);
        img.setImageResource(R.drawable.momo_qr);

        return view;
    }
}
