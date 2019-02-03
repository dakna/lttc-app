package com.expertsight.app.lttc;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class AdminBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener{
    private static final String TAG = "AdminBottomSheetDialogF";

    private AdminBottomSheetDialogListener listener;
    private Button btnCheckIn;
    private Button btnAdminActivity;
    private String memberId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Bundle args = getArguments();
        memberId = args.getString("member_id");
        View view = inflater.inflate(R.layout.fragment_admin_bottom_sheet_dialog, container, false);
        btnCheckIn = view.findViewById(R.id.btnCheckIn);
        btnCheckIn.setOnClickListener(this);
        btnAdminActivity = view.findViewById(R.id.btnAdmin);
        btnAdminActivity.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: view ID " + v.getId());
        if ((v.getId() == R.id.btnCheckIn) || (v.getId() == R.id.btnAdmin)) {
            listener.applyAdminDialogData(memberId, v.getId());
            AdminBottomSheetDialogFragment.this.dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener =  (AdminBottomSheetDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + getString(R.string.implement_AdminBottomSheetDialogListener));
        }
    }

    public interface AdminBottomSheetDialogListener {
        void applyAdminDialogData(String memberId, int buttonSelection);
    }
}
