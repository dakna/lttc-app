package com.expertsight.app.lttc;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class CheckInMemberDialogFragment extends DialogFragment {

    private RadioGroup radioPayment;
    private CheckBox checkKeepChange;
    private CheckInMemberDialogListener listener;
    private static final float FEE_PER_DAY = 5f;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View form = inflater.inflate(R.layout.fragment_check_in_member_dialog,null);
        radioPayment = form.findViewById(R.id.radioPayment);
        checkKeepChange = form.findViewById(R.id.cbKeepChange);
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setView(form)
                // set dialog icon
                .setIcon(R.drawable.ic_monetization)
                // set Dialog Title
                .setTitle("Club Check-In " + new SimpleDateFormat("MM/dd/yyyy").format(new Date()))
                // Set Dialog Message
                .setMessage("Welcome! The fee for playing today is $" + FEE_PER_DAY)

                // positive button
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //test data
                        listener.applyCheckInData(10f, false);

                        Toast.makeText(getActivity(), "Pressed OK ", Toast.LENGTH_SHORT).show();
                    }
                })
                // negative button
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Club check-in cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = builder.create();
     return alertDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener =  (CheckInMemberDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "mus implement CheckInMemberDialogListener");
        }
    }

    public interface CheckInMemberDialogListener {
        void applyCheckInData(float payment, boolean keepChange);
    }

}