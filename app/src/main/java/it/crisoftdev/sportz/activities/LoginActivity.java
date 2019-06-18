package it.crisoftdev.sportz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import it.crisoftdev.sportz.R;
import it.crisoftdev.sportz.SharedPreferencesManager;
import it.crisoftdev.sportz.obj.User;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1111 ;
    private static final String SING_IN_TAG = "SING_IN";
    private static final String FIREBASE_AUTH_TAG = "FIREBASE_AUTH";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();


        findViewById(R.id.sign_in_button).setOnClickListener(v -> signIn());
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);

    }

    private void signIn() {
        Log.d("CLICK", "true");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void updateUI(GoogleSignInAccount account){
        if(account != null){
            Log.d("account signed", "true");

            FirebaseUser currentUser = mAuth.getCurrentUser();
            Log.d("firebase user", "true");

            if(currentUser != null){
                firebaseAuthWithGoogle(account);
            }

        } else {
            SignInButton signInButton = findViewById(R.id.sign_in_button);
            signInButton.setSize(SignInButton.SIZE_STANDARD);
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        Log.d(FIREBASE_AUTH_TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(FIREBASE_AUTH_TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        SharedPreferencesManager m = SharedPreferencesManager.getInstance(getBaseContext());
                        m.setEmail(Objects.requireNonNull(user).getEmail());
                        m.setName(user.getDisplayName());
                        m.setUid(Objects.requireNonNull(user).getUid());
                        m.setPhotoUrl(Objects.requireNonNull(user.getPhotoUrl()).toString());

                        writeNewUser(new User(m.getUid(), m.getEmail(), m.getName(), m.getPhotoUrl()));

                        Intent i = new Intent(getBaseContext(), MainActivity.class);
                        i.putExtra("uid", user.getUid());
                        startActivity(i);

                    } else {
                        Log.w(FIREBASE_AUTH_TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(getBaseContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }



    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account);
            }
            //updateUI(account);
        } catch (ApiException e) {
            Log.w(SING_IN_TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void writeNewUser(User user) {
        databaseReference.child("users").child(user.getUid()).setValue(user);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("OnResult", "true");
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
}
