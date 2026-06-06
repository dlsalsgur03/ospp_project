package com.example.oss_project;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oss_project.api.ApiResult;
import com.example.oss_project.api.ApiService;
import com.example.oss_project.api.Response;
import com.example.oss_project.api.RetrofitClient;
import com.example.oss_project.api.SignUpRequest;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etNickname;
    private AutoCompleteTextView actCollege, actDepartment;
    private Button btnSubmit;

    private static final Map<String, String[]> COLLEGE_MAP = new LinkedHashMap<>();

    static {
        COLLEGE_MAP.put("인문대학",         new String[]{"국어국문학과","중어중문학과","영어영문학과","독일언어문화학과","프랑스언어문화학과","러시아언어문화학과","철학과","사학과","고고미술사학과"});
        COLLEGE_MAP.put("사회과학대학",      new String[]{"사회학과","심리학과","행정학과","정치외교학과","경제학과"});
        COLLEGE_MAP.put("자연과학대학",      new String[]{"수학과","정보통계학과","물리학과","화학과","생물학과","미생물학과","생화학과","천문우주학과","지구환경과학과"});
        COLLEGE_MAP.put("경영대학",          new String[]{"경영학부","국제경영학과","경영정보학과"});
        COLLEGE_MAP.put("공과대학",          new String[]{"토목공학부","기계공학부","화학공학과","신소재공학과","건축공학과","안전공학과","환경공학과","공업화학과","도시공학과","건축학과","공학자율전공학부"});
        COLLEGE_MAP.put("전자정보대학",      new String[]{"전기공학부","전자공학부","정보통신공학부","컴퓨터공학과","소프트웨어학부","지능로봇공학과"});
        COLLEGE_MAP.put("농업생명환경대학",  new String[]{"식물자원학과","환경생명화학과","식품생명공학과","축산학과","식물의학과","특용식물학과","원예과학과","산림학과","지역건설공학과"});
        COLLEGE_MAP.put("사범대학",          new String[]{"교육학과","국어교육과","영어교육과","역사교육과","지리교육과","사회교육과","윤리교육과","수학교육과","물리교육과","화학교육과","생물교육과","지구과학교육과","체육교육과"});
        COLLEGE_MAP.put("생활과학대학",      new String[]{"식품영양학과","의류학과","아동복지학과","주거환경학과","소비자학과"});
        COLLEGE_MAP.put("의과대학",          new String[]{"의예과","의학과"});
        COLLEGE_MAP.put("간호대학",          new String[]{"간호학과"});
        COLLEGE_MAP.put("창의융합대학",      new String[]{"AI빅데이터학과","바이오헬스학부"});
        COLLEGE_MAP.put("예술학과군",        new String[]{"조형예술학과","디자인학과"});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etEmail = findViewById(R.id.et_signup_email);
        etPassword = findViewById(R.id.et_signup_pw);
        etNickname = findViewById(R.id.et_signup_name);
        actCollege = findViewById(R.id.act_college);
        actDepartment = findViewById(R.id.act_department);
        btnSubmit = findViewById(R.id.btn_signup_submit);

        setupCollegeDropdown();
        setupDepartmentDropdown(null);

        btnSubmit.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            String nickname = etNickname.getText() != null ? etNickname.getText().toString().trim() : "";
            String college = actCollege.getText().toString().trim();
            String department = actDepartment.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()
                    || college.isEmpty() || department.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            performSignUp(email, password, nickname, college, department);
        });
    }

    private void setupCollegeDropdown() {
        List<String> collegeList = new ArrayList<>(COLLEGE_MAP.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown, collegeList);
        actCollege.setAdapter(adapter);
        actCollege.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            actDepartment.setText("");
            setupDepartmentDropdown(COLLEGE_MAP.get(selected));
        });
    }

    private void setupDepartmentDropdown(String[] departments) {
        List<String> deptList = departments != null
                ? Arrays.asList(departments)
                : new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown, deptList);
        actDepartment.setAdapter(adapter);
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
