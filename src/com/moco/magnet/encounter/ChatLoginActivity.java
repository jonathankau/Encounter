package com.moco.magnet.encounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;

/**
 * Date: 24.10.12
 * Time: 22:16
 */

/**
 * Activity provides interface for user auth.
 *
 * @author <a href="mailto:oleg@quickblox.com">Oleg Soroka</a>
 */
public class ChatLoginActivity extends Activity implements QBCallback, View.OnClickListener {

    private static final String DEFAULT_LOGIN = "";
    private static final String DEFAULT_PASSWORD = "";

    private Button loginButton;
    private Button registerButton;
    private EditText loginEdit;
    private EditText passwordEdit;
    private ProgressDialog progressDialog;

    private String login;
    private String password;
    private QBUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        QBAuth.createSession(new QBCallback() {

			@Override
			public void onComplete(Result result) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onComplete(Result arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}
       	
        });


        // UI stuff
        loginEdit = (EditText) findViewById(R.id.loginEdit);
        passwordEdit = (EditText) findViewById(R.id.passwordEdit);
        loginEdit.setText(DEFAULT_LOGIN);
        passwordEdit.setText(DEFAULT_PASSWORD);
        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
    }

    @Override
    public void onClick(View view) {
        login = loginEdit.getText().toString();
        password = passwordEdit.getText().toString();

        user = new QBUser(login, password);

        progressDialog.show();
        switch (view.getId()) {
            case R.id.loginButton:

                // ================= QuickBlox ===== Step 3 =================
                // Login user into QuickBlox.
                // Pass this activity , because it implements QBCallback interface.
                // Callback result will come into onComplete method below.
                QBUsers.signIn(user, ChatLoginActivity.this);
                break;
            case R.id.registerButton:

                // ================= QuickBlox ===== Step 3 =================
                // Register user in QuickBlox.
                QBUsers.signUpSignInTask(user, ChatLoginActivity.this);
                break;
        }
    }

    @Override
    public void onComplete(Result result) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (result.isSuccess()) {
            Intent intent = new Intent(this, UsersListActivity.class);
            intent.putExtra("myId", user.getId());
            intent.putExtra("myLogin", user.getLogin());
            intent.putExtra("myPassword", user.getPassword());

            startActivity(intent);
            Toast.makeText(this, "You've been successfully logged in application",
                    Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
                    "please. Errors: " + result.getErrors()).create().show();
        }
    }

    @Override
    public void onComplete(Result result, Object context) { }
}