package com.example.oss_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.oss_project.api.ApiResult;
import com.example.oss_project.api.ApiService;
import com.example.oss_project.api.CharacterSpawnListResponse;
import com.example.oss_project.api.RetrofitClient;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;

import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;

public class SensorMarkerManager {

    private final Context context;
    private final KakaoMap kakaoMap;

    // /api/characters/spawns 응답의 SpawnItem을 그대로 저장
    private CharacterSpawnListResponse.SpawnItem spawnedCharacter = null;

    // 기본값은 index + 1, 서버 출몰 정보가 있으면 spawn.getSensorId()로 교체됨
    private Long currentSensorId = null;

    public interface SensorCollectListener {
        void onStartScan();
        void onStopScanAndUpload(int sensorIndex, Long sensorId);
    }

    private SensorCollectListener collectListener;

    public void setSensorCollectListener(SensorCollectListener listener) {
        this.collectListener = listener;
    }

    private final java.util.Map<String, BleDeviceData> sensorDataMap = new java.util.HashMap<>();

    public void updateSensorData(java.util.List<BleDeviceData> dataList) {
        for (BleDeviceData data : dataList) {
            if (data != null && data.deviceAddress != null) {
                sensorDataMap.put(data.deviceAddress.toUpperCase(), data);
            }
        }
    }

    public BleDeviceData getSensorData(String macAddress) {
        if (macAddress == null) return null;
        return sensorDataMap.get(macAddress.toUpperCase());
    }

    private static final double[][] SENSOR_POSITIONS = {
            {36.6287545, 127.4579699},
            {36.62933, 127.4575},
            {36.62976, 127.4563},
            {36.63083, 127.4549},
            {36.63089, 127.4543}
    };

    public static final String[] SENSOR_MAC_ADDRESSES = {
            "D8:3A:DD:79:8E:BF",
            "B8:27:EB:D3:40:06",
            "88:A2:9E:9B:6A",
            "D8:3A:DD:79:8F:80",
            "D8:3A:DD:C1:88:BD"
    };

    public SensorMarkerManager(Context context, KakaoMap kakaoMap) {
        this.context = context;
        this.kakaoMap = kakaoMap;
    }

    public void addSensorMarkers() {
        Bitmap bitmap = createSensorBitmap();

        LabelStyles styles = kakaoMap.getLabelManager().addLabelStyles(
                LabelStyles.from(LabelStyle.from(bitmap))
        );

        for (int i = 0; i < SENSOR_POSITIONS.length; i++) {
            LatLng pos = LatLng.from(SENSOR_POSITIONS[i][0], SENSOR_POSITIONS[i][1]);

            kakaoMap.getLabelManager()
                    .getLayer()
                    .addLabel(LabelOptions.from(pos).setStyles(styles).setTag(i));
        }

        kakaoMap.setOnLabelClickListener((map, layer, label) -> {
            Object tag = label.getTag();

            if (tag instanceof Integer) {
                showSensorDialog((Integer) tag);
            }
        });
    }

    private Bitmap createSensorBitmap() {
        int size = 36;

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColor(Color.parseColor("#E53935"));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3f);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, paint);

        return bitmap;
    }

    private void showSensorDialog(int index) {
        int[] sensorImages = {
                R.drawable.sensor_1,
                R.drawable.sensor_2,
                R.drawable.sensor_3,
                R.drawable.sensor_4,
                R.drawable.sensor_5
        };

        android.app.Dialog dialog = new android.app.Dialog(context);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sensor);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
            );

            dialog.getWindow().setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.88f),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        de.hdodenhof.circleimageview.CircleImageView imgSensor =
                dialog.findViewById(R.id.img_sensor);

        imgSensor.setImageResource(sensorImages[index]);

        dialog.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());

        Button btnCollect = dialog.findViewById(R.id.btn_collect);
        ProgressBar progressCollect = dialog.findViewById(R.id.progress_collect);

        btnCollect.setOnClickListener(v -> {
            String buttonText = btnCollect.getText().toString();

            if (buttonText.equals("획득")) {
                btnCollect.setVisibility(View.GONE);
                progressCollect.setVisibility(View.VISIBLE);

                if (collectListener != null) {
                    collectListener.onStartScan();
                }

                // 서버에서 출몰 정보 확인
                spawnedCharacter = null;

                // 기본 센서 ID 매핑.
                // 서버에서 해당 센서 출몰 정보를 찾으면 checkSpawnedCharacter()에서 다시 갱신됨.
                currentSensorId = (long) (index + 1);

                checkSpawnedCharacter(index);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    progressCollect.setVisibility(View.GONE);
                    btnCollect.setVisibility(View.VISIBLE);
                    btnCollect.setText("수집 완료!");
                    btnCollect.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                    );

                    if (spawnedCharacter != null) {
                        Long characterId = spawnedCharacter.getCharacterId();

                        if (characterId != null) {
                            //int resId = CharacterManager.getCharacterDrawableId(characterId.intValue());
                            //imgSensor.setImageBitmap(getScaledBitmapWithPadding(resId, 500, 100));
                        }

                        //playCelebrateAnimation(dialog, imgSensor);

                        String characterName = spawnedCharacter.getCharacterName();
                        if (characterName == null || characterName.trim().isEmpty()) {
                            characterName = "캐릭터";
                        }

                        Toast.makeText(
                                context,
                                characterName + " 발견!",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                context,
                                "경험치를 획득했습니다!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }, 4000);

            } else if (buttonText.equals("수집 완료!")) {
                if (collectListener != null) {
                    collectListener.onStopScanAndUpload(index, currentSensorId);
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void checkSpawnedCharacter(int index) {
        String targetName = "SENSOR-00" + (index + 1);

        ApiService service = RetrofitClient.getClient().create(ApiService.class);

        service.getCurrentSpawns().enqueue(new Callback<ApiResult<CharacterSpawnListResponse>>() {
            @Override
            public void onResponse(
                    Call<ApiResult<CharacterSpawnListResponse>> call,
                    retrofit2.Response<ApiResult<CharacterSpawnListResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    Log.e("SpawnCheck", "출몰 정보 조회 실패 responseCode = " + response.code());

                    try {
                        if (response.errorBody() != null) {
                            Log.e("SpawnCheck", "errorBody = " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e("SpawnCheck", "errorBody 읽기 실패", e);
                    }

                    return;
                }

                if (response.body() == null || response.body().data == null) {
                    Log.e("SpawnCheck", "출몰 정보 응답 body 또는 data가 null입니다.");
                    return;
                }

                List<CharacterSpawnListResponse.SpawnItem> spawns =
                        response.body().data.getSpawns();

                if (spawns == null || spawns.isEmpty()) {
                    Log.d("SpawnCheck", "현재 출몰 중인 캐릭터가 없습니다.");
                    return;
                }

                for (CharacterSpawnListResponse.SpawnItem spawn : spawns) {
                    if (spawn == null) continue;

                    Log.d(
                            "SpawnCheck",
                            "spawn sensorName = " + spawn.getSensorName()
                                    + ", sensorId = " + spawn.getSensorId()
                                    + ", characterId = " + spawn.getCharacterId()
                                    + ", characterName = " + spawn.getCharacterName()
                    );

                    if (targetName.equals(spawn.getSensorName())) {
                        // 기존 spawn.getCharacter()는 없음.
                        // SpawnItem 자체에 characterId, characterName이 들어있으므로 spawn을 그대로 저장.
                        spawnedCharacter = spawn;

                        if (spawn.getSensorId() != null) {
                            currentSensorId = spawn.getSensorId();
                        }

                        Log.d(
                                "SpawnCheck",
                                "해당 센서 출몰 캐릭터 발견: "
                                        + spawn.getCharacterName()
                                        + ", sensorId = "
                                        + currentSensorId
                        );

                        break;
                    }
                }
            }

            @Override
            public void onFailure(
                    Call<ApiResult<CharacterSpawnListResponse>> call,
                    Throwable t
            ) {
                Log.e("SpawnCheck", "서버 통신 실패: " + t.getMessage(), t);
            }
        });
    }

    private void playCelebrateAnimation(android.app.Dialog dialog, View targetView) {
        FrameLayout container = dialog.findViewById(R.id.layout_character_container);
        if (container == null) return;

        targetView.setScaleX(0f);
        targetView.setScaleY(0f);
        targetView.setRotation(-180f);

        targetView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator())
                .start();

        int[] colors = {
                Color.YELLOW,
                Color.RED,
                Color.CYAN,
                Color.MAGENTA,
                Color.GREEN,
                Color.parseColor("#B91858")
        };

        Random random = new Random();

        for (int i = 0; i < 45; i++) {
            final View particle = new View(context);

            int size = 12 + random.nextInt(15);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
            lp.gravity = android.view.Gravity.CENTER;
            particle.setLayoutParams(lp);

            android.graphics.drawable.GradientDrawable shape =
                    new android.graphics.drawable.GradientDrawable();

            shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            shape.setColor(colors[random.nextInt(colors.length)]);
            particle.setBackground(shape);

            container.addView(particle, 0);

            float angle = (float) (random.nextFloat() * 2 * Math.PI);
            float distance = 350f + random.nextInt(350);

            particle.animate()
                    .translationX((float) (Math.cos(angle) * distance))
                    .translationY((float) (Math.sin(angle) * distance))
                    .alpha(0f)
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(1200 + random.nextInt(600))
                    .setStartDelay(random.nextInt(200))
                    .withEndAction(() -> container.removeView(particle))
                    .start();
        }
    }

    private Bitmap getScaledBitmapWithPadding(int resId, int size, int padding) {
        Bitmap original = BitmapFactory.decodeResource(context.getResources(), resId);

        if (original == null) {
            return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }

        Bitmap scaled = Bitmap.createScaledBitmap(
                original,
                size - padding * 2,
                size - padding * 2,
                true
        );

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(scaled, padding, padding, null);

        return output;
    }
}