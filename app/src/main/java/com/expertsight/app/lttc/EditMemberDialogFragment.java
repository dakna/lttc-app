package com.expertsight.app.lttc;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;


public class EditMemberDialogFragment extends DialogFragment {
    private static final String TAG = "AddMemberDialogFragment";

    private TextInputEditText textInputEditTextFirstName;
    private TextInputEditText textInputEditTextLastName;
    private TextInputEditText textInputEditTextEmail;
    private CheckBox checkBoxMailingList;
    private TextInputEditText textInputEditTextSmartcardId;
    private TextInputEditText textInputEditTextLastCheckIn;
    private EditMemberDialogListener listener;
    private CheckBox checkBoxAdmin;
    private CheckBox checkBoxActive;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle args = getArguments();

        String message = "Please edit member details for ID " + args.getString("member_id");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View form = inflater.inflate(R.layout.fragment_edit_member_dialog,null);
        textInputEditTextFirstName = form.findViewById(R.id.firstName);
        textInputEditTextLastName = form.findViewById(R.id.lastName);
        textInputEditTextEmail = form.findViewById(R.id.email);
        checkBoxMailingList = form.findViewById(R.id.mailingList);
        textInputEditTextSmartcardId = form.findViewById(R.id.smartcardId);
        textInputEditTextLastCheckIn = form.findViewById(R.id.lastCheckIn);
        checkBoxAdmin = form.findViewById(R.id.isAdmin);
        checkBoxActive = form.findViewById(R.id.isActive);


        textInputEditTextFirstName.setText(args.getString("member_firstname"));
        textInputEditTextLastName.setText(args.getString("member_lastname"));
        // TODO: 6/2/2018 change float to double when refactoring
        //args.putFloat("member_balance", member.getBalance());
        textInputEditTextEmail.setText(args.getString("member_email"));
        checkBoxMailingList.setChecked(args.getBoolean("member_mailinglist"));
        textInputEditTextSmartcardId.setText(args.getString("member_smartcard_id"));
        textInputEditTextLastCheckIn.setText(args.getString("member_last_check_in"));
        checkBoxAdmin.setChecked(args.getBoolean("member_is_admin"));
        checkBoxActive.setChecked(args.getBoolean("member_is_active"));





        textInputEditTextFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                AlertDialog dialog = (AlertDialog) getDialog();
                if((s.length() > 1) && (textInputEditTextLastName.getText().length() > 1)) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        textInputEditTextLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                AlertDialog dialog = (AlertDialog) getDialog();
                if((s.length() > 1) && (textInputEditTextFirstName.getText().length() > 1)) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setView(form)
                // set dialog icon
                .setIcon(R.drawable.ic_person)
                // set Dialog Title
                .setTitle("Edit a Member")
                // Set Dialog Message
                .setMessage(message)

                // positive button
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String firstName = textInputEditTextFirstName.getText().toString();
                        String lastName = textInputEditTextLastName.getText().toString();
                        String email = textInputEditTextEmail.getText().toString();
                        boolean mailingList = checkBoxMailingList.isChecked();
                        String smartcardId = textInputEditTextSmartcardId.getText().toString();

                        listener.applyEditMemberData(firstName, lastName, email, mailingList, smartcardId);

                        Toast.makeText(getActivity(), "Adding new member", Toast.LENGTH_SHORT).show();
                    }
                })
                // negative button
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = builder.create();
/*
        alertDialog.show();
        alertDialog.getWindow().setLayout(800, 400);
*/
        return alertDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener =  (EditMemberDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "mus implement EditMemberDialogListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // positive button enabled by default
        //AlertDialog dialog = (AlertDialog) getDialog();
        //dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    public interface EditMemberDialogListener {
        void applyEditMemberData(String firstName, String lastName, String email, boolean mailingList, String smartcardId);
    }

}