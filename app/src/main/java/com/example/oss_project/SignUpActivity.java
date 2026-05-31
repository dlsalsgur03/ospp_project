package com.example.oss_project;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oss_project.api.ApiResult;
import com.example.oss_project.api.ApiService;
import com.example.oss_project.api.Response;
import com.example.oss_project.api.RetrofitClient;
import com.example.oss_project.api.SignUpRequest;

import retrofit2.Call;
import retrofit2.Callback;

public class SignUpActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etNickname, etCollege, etDepartment;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 뷰 초기화
        etEmail = findViewById(R.id.et_signup_email);
        etPassword = findViewById(R.id.et_signup_pw);
        etNickname = findViewById(R.id.et_signup_name);
        etCollege = findViewById(R.id.et_signup_college);
        etDepartment = findViewById(R.id.et_signup_department);
        btnSubmit = findViewById(R.id.btn_signup_submit);

        btnSubmit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String nickname = etNickname.getText().toString().trim();
            String college = etCollege.getText().toString().trim();
            String department = etDepartment.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || nickname.isEmpty() || college.isEmpty() || department.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            performSignUp(email, password, nickname, college, department);
        });
    }

    private void performSignUp(String email, String password, String nickname, String college, String department) {
        SignUpRequest request = new SignUpRequest(email, password, nickname, college, department);
        ApiService service = RetrofitClient.getClient().create(ApiService.class);

        service.signUp(request).enqueue(new Callback<ApiResult<Response>>() {
            @Override
            public void onResponse(Call<ApiResult<Response>> call, retrofit2.Response<ApiResult<Response>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SignUpActivity.this, "가입 성공!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "가입 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResult<Response>> call, Throwable t) {
                Log.e("SignUp", "에러: " + t.getMessage());
                Toast.makeText(SignUpActivity.this, "네트워크 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
