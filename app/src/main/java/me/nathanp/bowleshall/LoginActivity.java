package me.nathanp.bowleshall;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashSet;
import java.util.Map;


public class LoginActivity extends AppCompatActivity implements PasswordFragment.PasswordFragmentListener {

    AppCompatAutoCompleteTextView mEmailEdit;
    AppCompatEditText mPassEdit;
    AppCompatButton mLogin;
    AppCompatButton mSignUp;

    ContentLoadingProgressBar mLoadingBar;
    TextView mLoadingText;

    Firebase rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mEmailEdit = (AppCompatAutoCompleteTextView) findViewById(R.id.emailtext);
        mPassEdit = (AppCompatEditText) findViewById(R.id.passwordtext);
        mLogin = (AppCompatButton) findViewById(R.id.login);
        mSignUp = (AppCompatButton) findViewById(R.id.signup);

        mLoadingBar = (ContentLoadingProgressBar) findViewById(R.id.loading_bar);
        mLoadingText = (TextView) findViewById(R.id.loading_text);

        mEmailEdit.setAdapter(getEmailAddressAdapter(getApplicationContext()));
        mEmailEdit.setDropDownBackgroundResource(R.drawable.autocomplete_drop_background);

        rootRef = new Firebase("https://incandescent-heat-3625.firebaseio.com/");

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validEmail()) {
                    hideUI();
                    mLoadingBar.show();
                    login(mEmailEdit.getText().toString(), mPassEdit.getText().toString(), false);
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

    private void hideUI() {
        mEmailEdit.setVisibility(View.INVISIBLE);
        mPassEdit.setVisibility(View.INVISIBLE);
        mLogin.setVisibility(View.INVISIBLE);
        mSignUp.setVisibility(View.INVISIBLE);
    }

    private void showUI() {
        mEmailEdit.setVisibility(View.VISIBLE);
        mPassEdit.setVisibility(View.VISIBLE);
        mLogin.setVisibility(View.VISIBLE);
        mSignUp.setVisibility(View.VISIBLE);
    }

    private void loadingProcess(boolean show, String message) {
        if (show) {
            mLoadingText.setText(message);
            mLoadingBar.show();
            mLoadingText.setVisibility(View.VISIBLE);
        } else {
            mLoadingText.setVisibility(View.GONE);
            mLoadingBar.hide();
        }
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

    private void login(final String email, String password, final boolean newUser) {
        hideUI();
        loadingProcess(true, "Logging you in...");
        rootRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                if (newUser) {
                    loadingProcess(true, "Setting up your account...");
                    Firebase newUserRef = rootRef.child("users").child(authData.getUid());
                    User user = new User(email, null, email);
                    newUserRef.setValue(user);
                }
                Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(mainActivity);
                finish();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                loadingProcess(false, null);
                showUI();
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
                    login(email, confirmedPass, true);
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
