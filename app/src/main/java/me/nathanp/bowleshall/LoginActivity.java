package me.nathanp.bowleshall;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashSet;


public class LoginActivity extends AppCompatActivity implements PasswordFragment.PasswordFragmentListener {

    AppCompatButton mLogin;
    AppCompatButton mSignUp;
    AppCompatAutoCompleteTextView mEmailEdit;
    AppCompatEditText mPassEdit;

    ProgressDialog dialog;

    Firebase rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setIndeterminate(true);
        dialog.setMessage("Logging you in...");

        setContentView(R.layout.activity_login);

        mLogin = (AppCompatButton) findViewById(R.id.login);
        mSignUp = (AppCompatButton) findViewById(R.id.signup);
        mEmailEdit = (AppCompatAutoCompleteTextView) findViewById(R.id.emailtext);
        mPassEdit = (AppCompatEditText) findViewById(R.id.passwordtext);

        rootRef = new Firebase("https://incandescent-heat-3625.firebaseio.com/");

        mEmailEdit.setAdapter(getEmailAddressAdapter(getApplicationContext()));
        mEmailEdit.setDropDownBackgroundResource(R.drawable.autocomplete_drop_background);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validEmail()) {
                    dialog.show();
                    login(mEmailEdit.getText().toString(), mPassEdit.getText().toString());
                }
            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validEmail()) {
                    signup();
                }
            }
        });
    }

    private boolean validEmail() {
        if (!mEmailEdit.getText().toString().contains("@berkeley.edu")) {
            Snackbar.make(mLogin, "Email must be a @berkeley.edu account.", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private ArrayAdapter<String> getEmailAddressAdapter(Context context) {
        Account[] accounts = AccountManager.get(context).getAccounts();
        HashSet<String> emailSet = new HashSet<String>();
        for (Account a : accounts) {
            if (Patterns.EMAIL_ADDRESS.matcher(a.name).matches()) {
                emailSet.add(a.name);
            }
        }
        String[] emailArray = emailSet.toArray(new String[emailSet.size()]);
            return new ArrayAdapter<String>(context, R.layout.autocomplete_list_item, emailArray);
    }

    private void login(String email, String password) {
        dialog.show();
        final Intent mainActivity = new Intent(this, MainActivity.class);
        rootRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                dialog.hide();
                startActivity(mainActivity);
                finish();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                dialog.hide();
                Snackbar.make(mLogin, firebaseError.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void signup() {
        PasswordFragment passVerify = new PasswordFragment();
        passVerify.show(getFragmentManager(), "verifyPassword");
    }

    @Override
    public void onDialogPositiveClick(final String confirmedPass) {
        if (confirmedPass.equals(mPassEdit.getText().toString())) {
            final String email = mEmailEdit.getText().toString();
            rootRef.createUser(email, confirmedPass, new Firebase.ResultHandler() {
                @Override
                public void onSuccess() {
                    login(email, confirmedPass);
                }

                @Override
                public void onError(FirebaseError firebaseError) {
                    Snackbar.make(mSignUp, firebaseError.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        } else {
            Snackbar.make(mSignUp, "Passwords do no match", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}
