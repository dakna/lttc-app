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
import android.widget.Toast;


public class AddMemberDialogFragment extends DialogFragment {

    private TextInputEditText textInputEditTextFirstName;
    private TextInputEditText textInputEditTextLastName;
    private TextInputEditText textInputEditTextEmail;
    private CheckBox checkBoxMailingList;
    private AddMemberDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View form = inflater.inflate(R.layout.fragment_add_member_dialog,null);
        textInputEditTextFirstName = form.findViewById(R.id.firstName);
        textInputEditTextLastName = form.findViewById(R.id.lastName);
        textInputEditTextEmail = form.findViewById(R.id.email);
        checkBoxMailingList = form.findViewById(R.id.mailingList);
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setView(form)
                // set dialog icon
                .setIcon(R.drawable.ic_person)
                // set Dialog Title
                .setTitle("Add a member")
                // Set Dialog Message
                .setMessage("Please enter member details")

                // positive button
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String firstName = textInputEditTextFirstName.getText().toString();
                        String lastName = textInputEditTextLastName.getText().toString();
                        String email = textInputEditTextEmail.getText().toString();
                        Boolean mailingList = checkBoxMailingList.isChecked();

                        listener.applyNewMemberData(firstName, lastName, email, mailingList);

                        Toast.makeText(getActivity(), "Pressed OK ", Toast.LENGTH_SHORT).show();
                    }
                })
                // negative button
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Cancel", Toast.LENGTH_SHORT).show();
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
            listener =  (AddMemberDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "mus implement AddMemberDialogListener");
        }
    }

    public interface AddMemberDialogListener {
        void applyNewMemberData(String firstName, String lastName, String email, Boolean mailingList);
    }

}