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


public class AddMemberDialogFragment extends DialogFragment {
    private static final String TAG = "AddMemberDialogFragment";

    private TextInputEditText textInputEditTextFirstName;
    private TextInputEditText textInputEditTextLastName;
    private TextInputEditText textInputEditTextEmail;
    private CheckBox checkBoxMailingList;
    private AddMemberDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle args = getArguments();
        final String smartcardId = args.getString("smartcard_id", null);
        Log.d(TAG, "onCreateDialog: args smartcard " + args.getString("smartcard_id"));

        String message = getString(R.string.msg_enter_member_details);

        if (smartcardId != null) {
            message = message + getString(R.string.msg_addon_smartcard) + smartcardId;
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View form = inflater.inflate(R.layout.fragment_add_member_dialog,null);
        textInputEditTextFirstName = form.findViewById(R.id.firstName);
        textInputEditTextLastName = form.findViewById(R.id.lastName);
        textInputEditTextEmail = form.findViewById(R.id.email);
        checkBoxMailingList = form.findViewById(R.id.mailingList);

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

        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog)
                .setView(form)
                // set dialog icon
                .setIcon(R.drawable.ic_person)
                // set Dialog Title
                .setTitle(getString(R.string.title_add_member))
                // Set Dialog Message
                .setMessage(message)

                // positive button
                .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String firstName = textInputEditTextFirstName.getText().toString();
                        String lastName = textInputEditTextLastName.getText().toString();
                        String email = textInputEditTextEmail.getText().toString();
                        boolean mailingList = checkBoxMailingList.isChecked();

                        listener.applyNewMemberData(firstName, lastName, email, mailingList, smartcardId);

                        Toast.makeText(getActivity(), getString(R.string.msg_adding_new_member), Toast.LENGTH_SHORT).show();
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
            listener =  (AddMemberDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + getString(R.string.implement_AddMemberDialogListener));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // disable positive button by default
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    public interface AddMemberDialogListener {
        void applyNewMemberData(String firstName, String lastName, String email, boolean mailingList, String smartcardId);
    }

}