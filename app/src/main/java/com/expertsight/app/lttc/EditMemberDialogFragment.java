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
    private TextInputEditText textInputEditTextBalance;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle args = getArguments();

        final String memberId = args.getString("member_id");
        String message = getString(R.string.msg_edit_member_details, memberId);

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
        textInputEditTextBalance = form.findViewById(R.id.balance);


        textInputEditTextFirstName.setText(args.getString("member_firstname"));
        textInputEditTextLastName.setText(args.getString("member_lastname"));
        textInputEditTextEmail.setText(args.getString("member_email"));
        checkBoxMailingList.setChecked(args.getBoolean("member_mailinglist"));
        textInputEditTextSmartcardId.setText(args.getString("member_smartcard_id"));
        checkBoxAdmin.setChecked(args.getBoolean("member_is_admin"));
        textInputEditTextLastCheckIn.setText(args.getString("member_last_check_in"));
        checkBoxActive.setChecked(args.getBoolean("member_is_active"));
        textInputEditTextBalance.setText(String.valueOf(args.getDouble("member_balance")));

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
                .setTitle(getString(R.string.title_edit_member))
                // Set Dialog Message
                .setMessage(message)

                // positive button
                .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String firstName = textInputEditTextFirstName.getText().toString();
                        String lastName = textInputEditTextLastName.getText().toString();
                        String email = textInputEditTextEmail.getText().toString();
                        boolean mailingList = checkBoxMailingList.isChecked();
                        String smartcardId = textInputEditTextSmartcardId.getText().toString();
                        boolean isAdmin = checkBoxAdmin.isChecked();
                        // TODO: 6/4/2018 lastCheckIn should be a date picker and converted to Date
                        String lastCheckIn = textInputEditTextLastCheckIn.getText().toString();
                        boolean isActive = checkBoxActive.isChecked();

                        String balance = textInputEditTextBalance.getText().toString();

                        listener.applyEditMemberData(memberId, firstName, lastName, email, mailingList, smartcardId, isAdmin, lastCheckIn, isActive, balance);

                        Toast.makeText(getActivity(), getString(R.string.msg_editing_member), Toast.LENGTH_SHORT).show();
                    }
                })
                // negative button
                .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), getString(R.string.msg_cancelled), Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = builder.create();

        return alertDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener =  (EditMemberDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + getString(R.string.implement_EditMemberDialogListener));
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
        void applyEditMemberData(String memberId, String firstName, String lastName, String email, boolean mailingList, String smartcardId, boolean isAdmin, String lastCheckIn, boolean isActive, String balance);
    }

}