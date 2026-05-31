package com.example.oss_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oss_project.api.ApiResult;
import com.example.oss_project.api.ApiService;
import com.example.oss_project.api.LoginRequest;
import com.example.oss_project.api.LoginResponse;
import com.example.oss_project.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private CheckBox cbAutoLogin;
    private TextView tvGoSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 자동 로그인 체크
        SharedPreferences prefs = getSharedPreferences("auth_pref", MODE_PRIVATE);
        boolean isAutoLogin = prefs.getBoolean("is_auto_login", false);
        String token = prefs.getString("access_token", null);

        if (isAutoLogin && token != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login_submit);
        cbAutoLogin = findViewById(R.id.cb_auto_login);
        tvGoSignUp = findViewById(R.id.tv_goto_signup);

        // 로그인 버튼 클릭 시
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            performLogin(email, password, cbAutoLogin.isChecked());
        });

        // 회원가입 텍스트 클릭 시
        tvGoSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin(String email, String password, boolean isAutoLoginChecked) {
        LoginRequest request = new LoginRequest(email, password);
        ApiService service = RetrofitClient.getClient().create(ApiService.class);

        service.login(request).enqueue(new Callback<ApiResult<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResult<LoginResponse>> call, Response<ApiResult<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 서버 응답의 data 필드에서 진짜 데이터(accessToken 등)를 꺼냄
                    LoginResponse loginData = response.body().data;

                    if (loginData != null) {
                        String token = loginData.accessToken;

                        // 토큰 저장 및 자동 로그인 설정
                        SharedPreferences prefs = getSharedPreferences("auth_pref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("access_token", token);
                        editor.putBoolean("is_auto_login", isAutoLoginChecked);
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                        
                        // 메인 화면으로 이동
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "로그인 실패: 정보를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResult<LoginResponse>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "네트워크 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
