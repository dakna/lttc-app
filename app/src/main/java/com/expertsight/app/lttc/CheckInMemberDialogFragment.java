package com.expertsight.app.lttc;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class CheckInMemberDialogFragment extends DialogFragment {

    private TextView tvMemberFullName;
    private TextView tvFee;
    private TextView tvMemberBalance;
    private RadioGroup radioPayment;
    private RadioButton radioButtonZero;
    private CheckBox checkKeepChange;
    private CheckInMemberDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        double balance = args.getDouble("member_balance");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View form = inflater.inflate(R.layout.fragment_check_in_member_dialog,null);
        tvMemberFullName = form.findViewById(R.id.tvMemberFullName);
        tvFee = form.findViewById(R.id.tvFee);
        tvMemberBalance = form.findViewById(R.id.tvMemberBalance);
        radioPayment = form.findViewById(R.id.radioPayment);
        radioButtonZero = form.findViewById(R.id.radioPayZero);
        checkKeepChange = form.findViewById(R.id.cbKeepChange);


        tvMemberFullName.setText(args.getString("member_fullname"));
        tvFee.setText("The fee for playing today is $" + CheckInActivity.FEE_PER_DAY + ".");
        tvMemberBalance.setText("Your balance is $" + balance);

        if (balance < CheckInActivity.FEE_PER_DAY) {
            radioButtonZero.setVisibility(View.GONE);
        }

        checkKeepChange.setEnabled(false);
        // you have to select a payment to continue
        radioPayment.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                AlertDialog dialog = (AlertDialog) getDialog();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                if ((checkedId != R.id.radioPayZero) && (checkedId != R.id.radioPayFive)) {
                    checkKeepChange.setEnabled(true);
                } else {
                    checkKeepChange.setEnabled(false);
                    checkKeepChange.setChecked(false);
                }
            }
        });

        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setView(form)
                // set dialog icon
                .setIcon(R.drawable.ic_monetization)
                // set Dialog Title
                .setTitle("Club Check-In " + new SimpleDateFormat("MM/dd/yyyy").format(new Date()))
                // Set Dialog Message
                //.setMessage("Welcome ")

                // positive button
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //test data
                        int radioButtonId = radioPayment.getCheckedRadioButtonId();
                        double payment = 0f;

                        if(radioButtonId == R.id.radioPayZero) {
                            payment = 0f;
                        } else if(radioButtonId == R.id.radioPayFive) {
                            payment = 5f;
                        } else if (radioButtonId == R.id.radioPayTen) {
                            payment = 10f;
                        } else if (radioButtonId == R.id.radioPayTwenty) {
                            payment = 20f;
                        }

                        boolean keepChange = checkKeepChange.isChecked();

                        listener.applyCheckInData(args.getString("member_id"), payment, keepChange);

                        //Toast.makeText(getActivity(), "Pressed OK ", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onResume() {
        super.onResume();

        // disable positive button by default
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    public interface CheckInMemberDialogListener {
        void applyCheckInData(String memberId, double payment, boolean keepChange);
    }

}