package com.smartalerts.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.smartalerts.AppEntry;
import com.smartalerts.AsyncResponse;
import com.smartalerts.R;
import com.smartalerts.databinding.FragmentGpsInfoDialogBinding;
import com.smartalerts.utils.GpsUtils;

public class GpsInfoDialogFragment extends BottomSheetDialogFragment {

    private static final String LOG_TAG = "TAG=>" + GpsInfoDialogFragment.class.getSimpleName();
    private static AsyncResponse asyncResponse;

    public GpsInfoDialogFragment() {
    }

    public static GpsInfoDialogFragment newInstance(AsyncResponse asyncResponse) {
        GpsInfoDialogFragment.asyncResponse = asyncResponse;
        return new GpsInfoDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gps_info_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentGpsInfoDialogBinding binding = FragmentGpsInfoDialogBinding.bind(view);

        binding.tvTitle.setText("GPS is OFF");
        binding.tvDesc.setText("Turn on GPS to improve your location finding experience!");

        binding.btnSkip.setOnClickListener(view1 -> {
            GpsUtils.preventLocationCheck = true;
            dismiss();
        });

        binding.btnAction.setVisibility(View.VISIBLE);

        binding.btnAction.setOnClickListener(view1 -> {
            try {
                GpsUtils gpsUtils = new GpsUtils(getActivity(), 1);
                gpsUtils.turnGPSOn(isEnabled -> {
                    try {
                        Log.d(LOG_TAG, "isGPSEnabled: " + isEnabled);
                        if (isEnabled) AsyncResponse.onResponse(asyncResponse, isEnabled);
                        dismiss();
                    } catch (Exception e) {
                        AppEntry.printError(LOG_TAG, e);
                    }
                });
            } catch (Exception e) {
                AppEntry.printError(LOG_TAG, e);
            }
        });
    }
}
