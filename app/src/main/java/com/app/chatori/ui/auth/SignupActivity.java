package com.app.chatori.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.app.chatori.R;
import com.app.chatori.model.User;
import com.app.chatori.repository.UserRepository;
import com.app.chatori.ui.home.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Activity for user registration using email/password or Google Sign-In.
 */
public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private MaterialButton btnGoogleSignup;
    private TextView tvLogin;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private UserRepository userRepository;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        Toast.makeText(SignupActivity.this, "Google sign in failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        userRepository = UserRepository.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize views
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignup = findViewById(R.id.btn_signup);
        btnGoogleSignup = findViewById(R.id.btn_google_signup);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);

        // Set click listeners
        btnSignup.setOnClickListener(v -> signupWithEmailPassword());
        btnGoogleSignup.setOnClickListener(v -> signInWithGoogle());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Handles email/password signup
     */
    private void signupWithEmailPassword() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_password_mismatch));
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success, create user in Firestore
                        String userId = auth.getCurrentUser().getUid();
                        User user = new User(userId, name, email);

                        userRepository.createUser(user)
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(SignupActivity.this, "Error creating user profile: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    Log.println(Log.ERROR,e.getMessage(),"Sign Up Error");
                                });
                    } else {
                        // If sign up fails, display a more specific error message
                        progressBar.setVisibility(View.GONE);
                        if (task.getException() != null) {
                            String errorMessage = task.getException().getMessage();
                            if (errorMessage != null && errorMessage.contains("email-already-in-use")) {
                                Toast.makeText(SignupActivity.this, "Email already registered. Please login.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignupActivity.this, "Signup failed: " + errorMessage,
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, getString(R.string.error_signup),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Handles Google Sign-In
     */
    private void signInWithGoogle() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * Authenticates with Firebase using Google credentials
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Sign in success
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                        if (isNewUser) {
                            // Create a new user in Firestore
                            createNewUser(account);
                        } else {
                            // Existing user, go to main activity
                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                            finish();
                        }
                    } else {
                        // If sign in fails, display a message to the user
                        Toast.makeText(SignupActivity.this, getString(R.string.error_signup),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Creates a new user in Firestore after Google Sign-In
     */
    private void createNewUser(GoogleSignInAccount account) {
        String userId = auth.getCurrentUser().getUid();
        String name = account.getDisplayName();
        String email = account.getEmail();
        String profileImageUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "";

        User user = new User(userId, name, email, profileImageUrl);

        userRepository.createUser(user)
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this, "Error creating user profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
