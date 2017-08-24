package com.navigine.naviginedemo.login.activities;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.navigine.naviginedemo.R;
import com.navigine.naviginedemo.SplashActivity;
import com.navigine.naviginedemo.login.model.Login;
import com.navigine.naviginedemo.login.model.Register;
import com.navigine.naviginedemo.login.model.User;
import com.navigine.naviginedemo.login.helpers.InputValidation;
import com.navigine.naviginedemo.login.remote.LoginClient;
import com.navigine.naviginedemo.login.remote.RegisterClient;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// import com.navigine.naviginedemo.login.sql.DatabaseHelper;



public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private final AppCompatActivity activity = RegisterActivity.this;

    private NestedScrollView nestedScrollView;


    //private TextInputLayout textInputLayoutName;

    private TextInputLayout textInputLayoutFirstName;
    private TextInputLayout textInputLayoutSurname;
    private TextInputLayout textInputLayoutPhone;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutConfirmPassword;

    private TextInputEditText textInputEditTextName;
    private TextInputEditText textInputEditTextFirstName;
    private TextInputEditText textInputEditTextSurname;
    private TextInputEditText textInputEditTextPhone;
    private TextInputEditText textInputEditTextEmail;
    private TextInputEditText textInputEditTextPassword;
    private TextInputEditText textInputEditTextConfirmPassword;

    private AppCompatButton appCompatButtonRegister;
    private AppCompatTextView appCompatTextViewLoginLink;

    private InputValidation inputValidation;
 //   private DatabaseHelper databaseHelper;
    private User user;




    // added in when integrating with the Retrofit call
    private String firstName = "";
    private String surname = "";
    private String password = "";
    private String phone = "";
    private String gender = "";
    private String yobString = "";
    private int yob = 0;
    private String emailAddress;
    Boolean registerSuccess = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        initViews();
        initListeners();
        initObjects();

        // Year Spinner.. This may not work because it casts to String
        ArrayList<String> years = new ArrayList<String>();
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 1900; i <= thisYear; i++) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, years);

        Spinner spinYear = (Spinner)findViewById(R.id.yearspin);
        spinYear.setAdapter(adapter);







        // Gender Spinner
        ArrayList<String> genders = new ArrayList<String>();
        String male   = "Male";
        String female = "Female";
        genders.add(male);
        genders.add(female);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, genders);

        Spinner spinGender = (Spinner)findViewById(R.id.genderSpin);
        spinGender.setAdapter(genderAdapter);


    }

    /** This method is to initialize views */
    private void initViews() {
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);

        //textInputLayoutName = (TextInputLayout) findViewById(R.id.textInputLayoutName);

        textInputLayoutFirstName            = (TextInputLayout) findViewById(R.id.textInputLayoutFirstName);
        textInputLayoutSurname              = (TextInputLayout) findViewById(R.id.textInputLayoutSurname);
        textInputLayoutPhone                = (TextInputLayout) findViewById(R.id.textInputLayoutPhone);
        textInputLayoutEmail                = (TextInputLayout) findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword             = (TextInputLayout) findViewById(R.id.textInputLayoutPassword);
        textInputLayoutConfirmPassword      = (TextInputLayout) findViewById(R.id.textInputLayoutConfirmPassword);

        textInputEditTextFirstName          = (TextInputEditText) findViewById(R.id.textInputEditTextFirstName);
        textInputEditTextSurname            = (TextInputEditText) findViewById(R.id.textInputEditTextSurname);
        textInputEditTextPhone              = (TextInputEditText) findViewById(R.id.textInputEditTextPhone);
        textInputEditTextEmail              = (TextInputEditText) findViewById(R.id.textInputEditTextEmail);
        textInputEditTextPassword           = (TextInputEditText) findViewById(R.id.textInputEditTextPassword);
        textInputEditTextConfirmPassword    = (TextInputEditText) findViewById(R.id.textInputEditTextConfirmPassword);

        appCompatButtonRegister             = (AppCompatButton) findViewById(R.id.appCompatButtonRegister);

        appCompatTextViewLoginLink           = (AppCompatTextView) findViewById(R.id.appCompatTextViewLoginLink);

    }

    /**
     * This method is to initialize listeners
     */
    private void initListeners() {
        appCompatButtonRegister.setOnClickListener(this);
        appCompatTextViewLoginLink.setOnClickListener(this);

//        spinYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
//                Object item = parent.getItemAtPosition(pos);
//            }
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });

    }

    /**
     * This method is to initialize objects to be used
     */
    private void initObjects() {
        inputValidation = new InputValidation(activity);
 //       databaseHelper = new DatabaseHelper(activity);
        user = new User();





    }


    /**
     * This implemented method is to listen the click on view
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.appCompatButtonRegister:
                postDataToSQLite();
                break;

            case R.id.appCompatTextViewLoginLink:
                finish();
                break;
        }
    }

    /**
     * This method is to validate the input text fields and post data to SQLite
     */
    private void postDataToSQLite() {
        if (!inputValidation.isInputEditTextFilled(textInputEditTextFirstName, textInputLayoutFirstName, getString(R.string.error_message_first_name))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextSurname, textInputLayoutSurname, getString(R.string.error_message_surname))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextPhone, textInputLayoutPhone, getString(R.string.error_message_phone))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        if (!inputValidation.isInputEditTextEmail(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextPassword, textInputLayoutPassword, getString(R.string.error_message_password))) {
            return;
        }
        if (!inputValidation.isInputEditTextMatches(textInputEditTextPassword, textInputEditTextConfirmPassword,
                textInputLayoutConfirmPassword, getString(R.string.error_password_match))) {
            return;
        }

//        if (!databaseHelper.checkUser(textInputEditTextEmail.getText().toString().trim())) {
//
//            user.setName(textInputEditTextName.getText().toString().trim());
//            user.setEmail(textInputEditTextEmail.getText().toString().trim());
//            user.setPassword(textInputEditTextPassword.getText().toString().trim());
//
//            databaseHelper.addUser(user);
//
//            // Snack Bar to show success message that record saved successfully
//            Snackbar.make(nestedScrollView, getString(R.string.success_message), Snackbar.LENGTH_LONG).show();
//            emptyInputEditText();


        //}




//        // These need to be edited to pull in all the data to populate for the call below...
//        emailAddress = textInputEditTextEmail.getText().toString().trim();
//        password = textInputEditTextPassword.getText().toString().trim();



         firstName    = textInputEditTextFirstName.getText().toString().trim();
         surname      = textInputEditTextSurname.getText().toString().trim();
         password     = textInputEditTextPassword.getText().toString().trim();
         phone        = textInputEditTextPhone.getText().toString().trim();
         emailAddress = textInputEditTextEmail.getText().toString().trim();

         Spinner mySpinner=(Spinner) findViewById(R.id.genderSpin);
         gender = mySpinner.getSelectedItem().toString();

         Spinner mySpinner2=(Spinner) findViewById(R.id.yearspin);
         yobString = mySpinner2.getSelectedItem().toString();

         yob = Integer.parseInt(yobString);

         //yob = 1929;


//        firstName     = "Sherlock";
//        surname       = "Holmes";
//        password      = "root";
//        phone         = "0877777777";
//        gender        = "Male";
//        yob           = 1910;
//        emailAddress  = "sholmes@dd.ie";

        registerSuccess = false;

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        RegisterClient.Factory
                .getInstance()
                .getRegister(firstName, surname, password, phone, gender, yob, emailAddress)
                .enqueue(new Callback<Register>() {
                    @Override
                    public void onResponse(Call<Register> call, Response<Register> response) {
                        // if http success = 1, login and Call next activity
                        // note here that there is a difference (of JSON layout) between
                        // what is sent back by the server for a successful/unsuccessful logins
                        if((response.body().getSuccess().equals("1"))) {
                            registerSuccess = true;
                            Intent loginIntent = new Intent(activity, LoginActivity.class);
                            startActivity(loginIntent);
                            Toast.makeText(getApplication().getBaseContext(), "Registration Successful", Toast.LENGTH_LONG).show();
                        } else {
                            registerSuccess = false;
                            String message = response.body().getMessage();
                            // This shows the error message that returned from Spring as to why the Login was unsuccessful
                            // eg. This email address is already in use.
                            Toast.makeText(getApplication().getBaseContext(), message, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Register> call, Throwable t) {
                        Log.e("Failed", t.getMessage());
                        Toast.makeText(getApplicationContext(), "Network Error, Try Again", Toast.LENGTH_SHORT).show();
                        // textView_points.setText("UserClient call has failed");
                    }
                });


        if (registerSuccess) {

            Intent loginIntent = new Intent(activity, LoginActivity.class);
            startActivity(loginIntent);

        } else {
            // Snack Bar to show error message that record already exists
            Snackbar.make(nestedScrollView, getString(R.string.error_email_exists), Snackbar.LENGTH_LONG).show();
        }


    }

    /**
     * This method is to empty all input edit text
     */
    private void emptyInputEditText() {
        textInputEditTextName.setText(null);
        textInputEditTextEmail.setText(null);
        textInputEditTextPassword.setText(null);
        textInputEditTextConfirmPassword.setText(null);
    }
}
