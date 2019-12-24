package com.expertsight.app.lttc;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
    private TextView tvAmount;
    private RadioGroup radioPayment;
    private RadioButton radioPayZero;
    private RadioButton radioPayFive;
    private RadioButton radioPayTen;
    private RadioButton radioPayTwenty;
    private Button btnChangeAmount;
    private CheckBox checkKeepChange;
    private CheckInMemberDialogListener listener;
    private double payment = 5f;
    private ConstraintLayout differentAmountLayout;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        double balance = args.getDouble("member_balance");



        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View form = inflater.inflate(R.layout.fragment_check_in_member_dialog,null);
        tvMemberFullName = form.findViewById(R.id.tvMemberFullName);
        tvFee = form.findViewById(R.id.tvFee);
        tvMemberBalance = form.findViewById(R.id.tvMemberBalance);
        tvAmount = form.findViewById(R.id.tvAmount);
        radioPayment = form.findViewById(R.id.radioPayment);
        radioPayZero = form.findViewById(R.id.radioPayZero);
        radioPayFive = form.findViewById(R.id.radioPayFive);
        radioPayTen = form.findViewById(R.id.radioPayTen);
        radioPayTwenty = form.findViewById(R.id.radioPayTwenty);
        checkKeepChange = form.findViewById(R.id.cbKeepChange);
        btnChangeAmount = form.findViewById(R.id.btnChangeAmount);
        differentAmountLayout = form.findViewById(R.id.differentAmount);


        tvMemberFullName.setText(args.getString("member_fullname"));
        tvFee.setText(getString(R.string.dollar, String.valueOf(HomeActivity.FEE_PER_DAY)));
        tvMemberBalance.setText(getString(R.string.dollar,String.valueOf(balance)));
        tvAmount.setText(radioPayFive.getText());

        checkKeepChange.setEnabled(false);

        btnChangeAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("click", "onClick: ");
                differentAmountLayout.setVisibility(View.VISIBLE);
                view.setVisibility(View.GONE);
            }
        });

        radioPayment.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                AlertDialog dialog = (AlertDialog) getDialog();
                //dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                RadioButton button = form.findViewById(checkedId);
                tvAmount.setText(button.getText());

                if ((checkedId != R.id.radioPayZero) && (checkedId != R.id.radioPayFive)) {
                    checkKeepChange.setEnabled(true);
                } else {
                    checkKeepChange.setEnabled(false);
                    checkKeepChange.setChecked(false);
                }
            }
        });

        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog)
                .setView(form)
                // set dialog icon
                .setIcon(R.drawable.ic_monetization)
                // set Dialog Title
                .setTitle(getString(R.string.title_club_checkin, new SimpleDateFormat("MM/dd/yyyy").format(new Date())))
                // Set Dialog Message
                //.setMessage("Welcome ")

                // positive button
                .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //test data
                        int radioButtonId = radioPayment.getCheckedRadioButtonId();

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

                    }
                })
                // negative button
                .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), getString(R.string.msg_club_checkin_cancelled), Toast.LENGTH_SHORT).show();
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
            throw new ClassCastException(context.toString() + getString(R.string.implement_CheckInMemberDialogListener));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // disable positive button by default
        AlertDialog dialog = (AlertDialog) getDialog();
        //dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    public interface CheckInMemberDialogListener {
        void applyCheckInData(String memberId, double payment, boolean keepChange);
    }

}