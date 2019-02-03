package com.expertsight.app.lttc;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;


public class AddMatchScoreDialogFragment extends DialogFragment {
    private static final String TAG = "AddMatchScoreDialogFrag";

    private TextInputEditText textInputPlayer1Games;
    private TextInputLayout textInputLayoutPlayer1;
    private TextInputEditText textInputPlayer2Games;
    private TextInputLayout textInputLayoutPlayer2;
    private AddMatchScoreDialogListener listener;


    private boolean isValidScore() {
        int player1Games = Integer.parseInt(textInputPlayer1Games.getText().toString());
        int player2Games = Integer.parseInt(textInputPlayer2Games.getText().toString());

        if ((player1Games >3) || (player2Games > 3)) return false;
        if (((player1Games + player2Games) >5) || ((player1Games + player2Games) <3)) return false;

        if ((player1Games != 3) && (player2Games != 3)) return false;


        return true;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle args = getArguments();

        String message = "Please enter score (best 3 out of 5)";

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View form = inflater.inflate(R.layout.fragment_add_match_score_dialog,null);

        Log.d(TAG, "onCreateDialog: player1_fullname " + args.getString("player1_fullname"));

        textInputPlayer1Games = form.findViewById(R.id.player1Games);
        textInputPlayer1Games.setText(String.valueOf(args.getInt("player1_games")));
        textInputLayoutPlayer1 = form.findViewById(R.id.tilPlayer1);
        textInputLayoutPlayer1.setHint(args.getString("player1_fullname"));

        textInputPlayer2Games = form.findViewById(R.id.player2Games);
        textInputPlayer2Games.setText(String.valueOf(args.getInt("player2_games")));
        textInputLayoutPlayer2 = form.findViewById(R.id.tilPlayer2);
        textInputLayoutPlayer2.setHint(args.getString("player2_fullname"));

        textInputPlayer1Games.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                AlertDialog dialog = (AlertDialog) getDialog();
                if((s.length() > 0) && (textInputPlayer2Games.getText().length() > 0) && isValidScore())
                {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        textInputPlayer2Games.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                AlertDialog dialog = (AlertDialog) getDialog();
                if((s.length() > 0) && (textInputPlayer1Games.getText().length() > 0) && isValidScore()) {
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
                .setTitle("Add Match Score")
                // Set Dialog Message
                .setMessage(message)

                // positive button
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        int player1Games = Integer.parseInt(textInputPlayer1Games.getText().toString());
                        int player2Games = Integer.parseInt(textInputPlayer2Games.getText().toString());

                        listener.applyMatchScoreData(args.getString("match_id"), player1Games, player2Games);
                        Toast.makeText(getActivity(), "Adding new match score", Toast.LENGTH_SHORT).show();
                    }
                })
                // negative button
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = builder.create();

        return alertDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener =  (AddMatchScoreDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "mus implement AddMatchScoreDialogListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // disable positive button by default
        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    public interface AddMatchScoreDialogListener {
        void applyMatchScoreData(String matchId, int player1Games, int player2Games);
    }

}