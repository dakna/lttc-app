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


public class AddTransactionDialogFragment extends DialogFragment {
    private static final String TAG = "AddMemberDialogFragment";

    private TextInputEditText textInputEditSubject;
    private TextInputEditText textInputEditAmount;
    private AddTransactionDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle args = getArguments();

        String message = getString(R.string.msg_enter_transaction_details);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View form = inflater.inflate(R.layout.fragment_add_transaction_dialog,null);
        textInputEditSubject = form.findViewById(R.id.subject);
        textInputEditAmount = form.findViewById(R.id.amount);

        textInputEditSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                AlertDialog dialog = (AlertDialog) getDialog();
                if((s.length() > 0) && (textInputEditAmount.getText().length() > 0)) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        textInputEditAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                AlertDialog dialog = (AlertDialog) getDialog();
                if((s.length() > 0) && (textInputEditSubject.getText().length() > 0)) {
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
                .setTitle(getString(R.string.title_add_transaction))
                // Set Dialog Message
                .setMessage(message)

                // positive button
                .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String subject = textInputEditSubject.getText().toString();
                        double amount = Double.valueOf(textInputEditAmount.getText().toString());

                        listener.applyNewTransactionData(subject, amount);
                        Toast.makeText(getActivity(), getString(R.string.msg_adding_new_transaction), Toast.LENGTH_SHORT).show();
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
            listener =  (AddTransactionDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + getString(R.string.implement_AddTransactionDialogListener));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // disable positive button by default
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    public interface AddTransactionDialogListener {
        void applyNewTransactionData(String subject, double amount);
    }

}