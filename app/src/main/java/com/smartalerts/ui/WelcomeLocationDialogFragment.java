package com.smartalerts.ui;

import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.smartalerts.AppEntry;
import com.smartalerts.AsyncResponse;
import com.smartalerts.AsyncTaskToFindLocation;
import com.smartalerts.R;
import com.smartalerts.StaticVariables;
import com.smartalerts.databinding.FragmentGpsInfoDialogBinding;

import java.util.List;

public class WelcomeLocationDialogFragment extends BottomSheetDialogFragment {

    private static final String LOG_TAG = "TAG=>" + WelcomeLocationDialogFragment.class.getSimpleName();

    public static WelcomeLocationDialogFragment newInstance() {
        return new WelcomeLocationDialogFragment();
    }

    private static void saveAddress(Address address) {

        try {
            String area = address.getSubLocality();
            if (area == null) {
                area = address.getLocality();
                if (area == null) {
                    area = address.getSubAdminArea();
                }
            }

            StaticVariables.nearby = area;
            /* StaticVariables.city= address.getSubAdminArea();*/
            Log.d(LOG_TAG, "city is: " + address.getSubAdminArea());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gps_info_dialog, container, false);
        // get the views and attach the listener

        try {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, null));
        } catch (Exception e) {
            Log.v(LOG_TAG, "onCreateView.Error-1 : " + e.getMessage());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {

            FragmentGpsInfoDialogBinding binding = FragmentGpsInfoDialogBinding.bind(view);

            binding.tvTitle.setText("Welcome to Smart Alerts");
            binding.tvDesc.setText("Set your exact landmark to improve your nearby alert finding experience");

            binding.btnSkip.setText("CLOSE");

            binding.btnAction.setText("Change Location");
            binding.btnAction.setVisibility(View.GONE);

            binding.btnSkip.setOnClickListener(view1 -> {

                dismiss();

            });

            getNearbyLocation(binding);

        } catch (Exception e) {
            Log.v(LOG_TAG, "onViewCreated.Error : " + e.getMessage());
        }

    }

    private void getNearbyLocation(FragmentGpsInfoDialogBinding binding) {

        new AsyncTaskToFindLocation(AppEntry.getInstance(), (AsyncResponse) output -> {
            try {

                List<Address> addressList = (List<Address>) output;
                if (addressList == null || addressList.isEmpty()) return;
                Address address = addressList.get(0);
                if (address == null) return;

                saveAddress(address);

                binding.tvTitle.setText("Welcome to " + StaticVariables.nearby + " Region!");
                binding.tvDesc.setText(address.getAddressLine(0));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).execute(new LatLng(StaticVariables.lat, StaticVariables.lng));

    }

}
