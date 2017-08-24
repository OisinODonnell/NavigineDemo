package com.navigine.naviginedemo.login.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.navigine.naviginedemo.R;
import com.navigine.naviginedemo.SplashActivity;
import com.navigine.naviginedemo.login.helpers.InputValidation;

import com.navigine.naviginedemo.login.model.Login;
import com.navigine.naviginedemo.login.remote.LoginClient;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
//import com.navigine.naviginedemo.login.sql.DatabaseHelper;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final AppCompatActivity activity = LoginActivity.this;

    private NestedScrollView nestedScrollView;

    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;

    private TextInputEditText textInputEditTextEmail;
    private TextInputEditText textInputEditTextPassword;

    private AppCompatButton appCompatButtonLogin;

    private AppCompatTextView textViewLinkRegister;

    private InputValidation inputValidation;
   // private DatabaseHelper databaseHelper;

    // added in when integrating with the Retrofit call
    private String email = "";
    private String password = "";
    Boolean loginSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        initViews();
        initListeners();
        initObjects();
    }

    /**
     * This method is to initialize views
     */
    private void initViews() {

        nestedScrollView          = (NestedScrollView)  findViewById(R.id.nestedScrollView);

        textInputLayoutEmail      = (TextInputLayout)   findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword   = (TextInputLayout)   findViewById(R.id.textInputLayoutPassword);

        textInputEditTextEmail    = (TextInputEditText) findViewById(R.id.textInputEditTextEmail);
        textInputEditTextPassword = (TextInputEditText) findViewById(R.id.textInputEditTextPassword);

        appCompatButtonLogin      = (AppCompatButton)   findViewById(R.id.appCompatButtonLogin);

        textViewLinkRegister      = (AppCompatTextView) findViewById(R.id.textViewLinkRegister);

    }

    /**
     * This method is to initialize listeners
     */
    private void initListeners() {
        appCompatButtonLogin.setOnClickListener(this);
        textViewLinkRegister.setOnClickListener(this);
    }

    /**
     * This method is to initialize objects to be used
     */
    private void initObjects() {
       // databaseHelper  = new DatabaseHelper(activity);
        inputValidation = new InputValidation(activity);

    }

    /**
     * This implemented method is to listen the click on view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.appCompatButtonLogin:
                verifyFromSQLite();
                break;
            case R.id.textViewLinkRegister:
                // Navigate to RegisterActivity
                Intent intentRegister = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intentRegister);
                break;
        }
    }

    /**
     * This method is to validate the input text fields and verify login credentials from SQLite
     */
    private void verifyFromSQLite() {
        if (!inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        if (!inputValidation.isInputEditTextEmail(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextPassword, textInputLayoutPassword, getString(R.string.error_message_email))) {
            return;
        }

        email = textInputEditTextEmail.getText().toString().trim();
        password = textInputEditTextPassword.getText().toString().trim();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        LoginClient.Factory
                .getInstance()
                .getUser(email, password)
                .enqueue(new Callback<Login>() {
                             @Override
                             public void onResponse(Call<Login> call, Response<Login> response) {
                                 // if http success = 1, login and Call next activity
                                 // note here that there is a difference (of JSON layout) between
                                 // what is sent back by the server for a successful/unsuccessful logins
                                 if((response.body().getSuccess().equals("1"))) {
                                     loginSuccess = true;
                                     Intent navigineIntent = new Intent(activity, SplashActivity.class);
                                     startActivity(navigineIntent);
                                 } else {
                                     loginSuccess = false;
                                 }
                                 // test to see if the httpStatus equals 200 from the above call.
                                 Toast.makeText(getApplication().getBaseContext(), loginSuccess.toString(), Toast.LENGTH_SHORT).show();
                             }

                             @Override
                             public void onFailure(Call<Login> call, Throwable t) {
                                 Log.e("Failed", t.getMessage());
                                 Toast.makeText(getApplicationContext(), "Network Error, Try Again", Toast.LENGTH_SHORT).show();
                                 // textView_points.setText("UserClient call has failed");
                             }
                         });


        if (loginSuccess) {

              Intent navigineIntent = new Intent(activity, SplashActivity.class);
              startActivity(navigineIntent);

        } else {
            // Snack Bar to show success message that record is wrong
            Snackbar.make(nestedScrollView, getString(R.string.error_valid_email_password), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * This method is to empty all input edit text
     */
    private void emptyInputEditText() {
        textInputEditTextEmail.setText(null);
        textInputEditTextPassword.setText(null);
    }


    public void checkUserLogin() {

    }
}
