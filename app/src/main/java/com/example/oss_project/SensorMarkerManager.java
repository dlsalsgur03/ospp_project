package com.example.oss_project;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.example.oss_project.api.SubmissionResponse;
import android.content.SharedPreferences;
import java.util.Calendar;
import java.util.Random;

public class SensorMarkerManager {

    private final Context context;
    private final KakaoMap kakaoMap;
    private double currentLat;
    private double currentLon;

    public interface SensorCollectListener {
        void onSubmitSensor(int sensorIndex, SubmissionCallback callback);
    }

    public interface SubmissionCallback {
        void onSuccess(SubmissionResponse response);
        void onError(String message);
    }

    private SensorCollectListener collectListener;

    public void setSensorCollectListener(SensorCollectListener listener) {
        this.collectListener = listener;
    }

    public SensorMarkerManager(Context context, KakaoMap kakaoMap) {
        this.context = context;
        this.kakaoMap = kakaoMap;
    }

    private final java.util.Map<String, BleDeviceData> sensorDataMap = new java.util.HashMap<>();

    // BLE 데이터 업데이트 메서드
    public void updateSensorData(java.util.List<BleDeviceData> dataList) {
        for (BleDeviceData data : dataList) {
            sensorDataMap.put(data.deviceAddress.toUpperCase(), data);
        }
    }

    public BleDeviceData getSensorData(String macAddress) {
        if (macAddress == null) return null;
        return sensorDataMap.get(macAddress.toUpperCase());
    }
    public void updateCurrentLocation(double lat, double lon) {
        currentLat = lat;
        currentLon = lon;
    }

    private static final double[][] SENSOR_POSITIONS = {
            {36.6287545, 127.4579699},
            {36.62933,   127.4575},
            {36.62976,   127.4563},
            {36.63083,   127.4549},
            {36.63089,   127.4543}
    };

    public static final String[] SENSOR_NAMES = {
            "센서 1 - 도서관 뒷편",
            "센서 2 - 사회과학대 입구",
            "센서 3 - 이마트24 건너편",
            "센서 4 - 테니스장 앞",
            "센서 5 - 테니스장 건너편"
    };
    public static final String[] SENSOR_MAC_ADDRESSES = {
            "D8:3A:DD:79:8E:BF",
            "B8:27:EB:D3:40:06",
            "88:A2:9E:9B:5E:6A",
            "D8:3A:DD:79:8F:80",
            "D8:3A:DD:C1:88:BD"
    };

    public void addSensorMarkers() {
        Bitmap bitmap = createSensorBitmap();
        LabelStyles styles = kakaoMap.getLabelManager()
                .addLabelStyles(LabelStyles.from(LabelStyle.from(bitmap)));

        for (int i = 0; i < SENSOR_POSITIONS.length; i++) {
            LatLng pos = LatLng.from(SENSOR_POSITIONS[i][0], SENSOR_POSITIONS[i][1]);
            kakaoMap.getLabelManager().getLayer().addLabel(
                    LabelOptions.from(pos)
                            .setStyles(styles)
                            .setTag(i)
            );
        }

        // 마커 클릭 이벤트
        kakaoMap.setOnLabelClickListener((map, layer, label) -> {
            Object tag = label.getTag();
            if (tag instanceof Integer) {
                int index = (Integer) tag;
                showSensorDialog(index);
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
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int)(context.getResources().getDisplayMetrics().widthPixels * 0.88f),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        de.hdodenhof.circleimageview.CircleImageView imgSensor =
                dialog.findViewById(R.id.img_sensor);
        imgSensor.setImageResource(sensorImages[index]);

        dialog.findViewById(R.id.btn_close).setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                HomeFragment fragment = ((MainActivity) context).getHomeFragment();
                if (fragment != null) {
                    fragment.stopBleScan();
                }
            }
            dialog.dismiss();
        });

        Button btnCollect = dialog.findViewById(R.id.btn_collect);
        ProgressBar progressCollect = dialog.findViewById(R.id.progress_collect);
        TextView tvStatus =
                dialog.findViewById(R.id.tv_status);

        Button btnToggleSensorData =
                dialog.findViewById(R.id.btn_toggle_sensor_data);

        LinearLayout layoutSensorData =
                dialog.findViewById(R.id.layout_sensor_data);

        btnToggleSensorData.setOnClickListener(v -> {

            if (layoutSensorData.getVisibility() == View.GONE) {

                layoutSensorData.setVisibility(View.VISIBLE);
                btnToggleSensorData.setText("▲ 환경 정보 접기");

            } else {

                layoutSensorData.setVisibility(View.GONE);
                btnToggleSensorData.setText("▼ 환경 정보 보기");
            }
        });

        TextView tvTempHum =
                dialog.findViewById(R.id.tv_temp_hum);

        TextView tvAirEco2 =
                dialog.findViewById(R.id.tv_air_eco2);

        TextView tvRssi =
                dialog.findViewById(R.id.tv_rssi);

        SharedPreferences prefs =
                context.getSharedPreferences("collect_pref", Context.MODE_PRIVATE);

        long lastCollectedTime =
                prefs.getLong("sensor_time_" + index, 0);

        long now = System.currentTimeMillis();

        Calendar nextHour = Calendar.getInstance();
        nextHour.setTimeInMillis(lastCollectedTime);

        nextHour.set(Calendar.MINUTE, 0);
        nextHour.set(Calendar.SECOND, 0);
        nextHour.set(Calendar.MILLISECOND, 0);

        nextHour.add(Calendar.HOUR_OF_DAY, 1);

// 기본 상태
        btnCollect.setEnabled(true);
        btnCollect.setText("획득");
        btnCollect.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#E91E8C"))
        );

// 1순위 : 시간 제한
        if (lastCollectedTime > 0
                && now < nextHour.getTimeInMillis()) {

            btnCollect.setEnabled(false);
            btnCollect.setText("다음 정각 이후 가능");
            btnCollect.setBackgroundTintList(
                    ColorStateList.valueOf(Color.GRAY)
            );

        } else {

            // 2순위 : 거리 제한
            double distance = calculateDistance(
                    currentLat,
                    currentLon,
                    SENSOR_POSITIONS[index][0],
                    SENSOR_POSITIONS[index][1]
            );

            Log.d("GPS_CHECK",
                    "사용자-센서 거리 = " + distance + "m");

            if (distance > 10.0) {

                btnCollect.setEnabled(false);

                btnCollect.setText("센서에 더 접근하세요");

                btnCollect.setBackgroundTintList(
                        ColorStateList.valueOf(Color.GRAY)
                );
            }
        }

        btnCollect.setOnClickListener(v -> {
            String currentText = btnCollect.getText().toString();

            if (currentText.equals("획득")) {
                if (context instanceof MainActivity) {
                    HomeFragment fragment = ((MainActivity) context).getHomeFragment();
                    if (fragment != null) {
                        fragment.startBleScan();
                    }
                }
                btnCollect.setVisibility(View.GONE);
                progressCollect.setVisibility(View.VISIBLE);
                btnCollect.setEnabled(false);

                if (collectListener != null) {
                    collectListener.onSubmitSensor(index, new SubmissionCallback() {
                        @Override
                        public void onSuccess(SubmissionResponse response) {
                            prefs.edit()
                                    .putLong(
                                            "sensor_time_" + index,
                                            System.currentTimeMillis()
                                    )
                                    .apply();
                            progressCollect.setVisibility(View.GONE);
                            btnCollect.setVisibility(View.VISIBLE);
                            btnCollect.setEnabled(true);
                            btnCollect.setText("수집 완료!");
                            BleDeviceData data =
                                    getSensorData(SENSOR_MAC_ADDRESSES[index]);

                            if (data != null) {

                                tvTempHum.setText(
                                        String.format(
                                                java.util.Locale.KOREA,
                                                "온도 : %.1f℃ / 습도 : %.1f%%",
                                                data.temp,
                                                data.humidity
                                        )
                                );

                                tvAirEco2.setText(
                                        String.format(
                                                java.util.Locale.KOREA,
                                                "공기질 : %d / eCO₂ : %dppm",
                                                data.aqi,
                                                data.eco2
                                        )
                                );
                                tvRssi.setText(
                                        "RSSI : " + data.rssi + " dBm"
                                );

                                btnToggleSensorData.setVisibility(View.VISIBLE);
                            }
                            btnCollect.setBackgroundTintList(ColorStateList.valueOf(
                                    Color.parseColor("#4CAF50")));

                            if (response != null
                                    && Boolean.TRUE.equals(response.characterCollected)
                                    && response.characterReward != null
                                    && response.characterReward.characterId != null) {
                                int characterId = response.characterReward.characterId.intValue();
                                int resId = CharacterManager.getCharacterDrawableId(characterId);
                                imgSensor.setImageBitmap(getScaledBitmapWithPadding(resId, 500, 100));
                                playCelebrateAnimation(dialog, imgSensor);
                                Toast.makeText(context,
                                        "새로운 캐릭터 발견: " + response.characterReward.characterName,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "경험치를 획득했습니다!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String message) {
                            progressCollect.setVisibility(View.GONE);
                            btnCollect.setVisibility(View.VISIBLE);
                            btnCollect.setEnabled(true);
                            btnCollect.setText("다시 시도");
                            btnCollect.setBackgroundTintList(ColorStateList.valueOf(
                                    Color.parseColor("#E91E8C")));
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressCollect.setVisibility(View.GONE);
                    btnCollect.setVisibility(View.VISIBLE);
                    btnCollect.setEnabled(true);
                    Toast.makeText(context, "전송 준비가 되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }

            } else if (currentText.equals("수집 완료!")) {
                if (context instanceof MainActivity) {
                    HomeFragment fragment = ((MainActivity) context).getHomeFragment();
                    if (fragment != null) {
                        fragment.stopBleScan();
                    }
                }
                dialog.dismiss();
            } else if (currentText.equals("다시 시도")) {
                btnCollect.setText("획득");
                btnCollect.performClick();
            }
        });

        dialog.show();
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

        int[] colors = {Color.YELLOW, Color.RED, Color.CYAN, Color.MAGENTA, Color.GREEN, Color.parseColor("#B91858")};
        Random random = new Random();

        for (int i = 0; i < 45; i++) {
            final View particle = new View(context);
            int size = 12 + random.nextInt(15);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
            lp.gravity = android.view.Gravity.CENTER;
            particle.setLayoutParams(lp);

            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            shape.setColor(colors[random.nextInt(colors.length)]);
            particle.setBackground(shape);

            container.addView(particle, 0);

            float angle = (float) (random.nextFloat() * 2 * Math.PI);
            float distance = 350f + random.nextInt(350);
            float tx = (float) (Math.cos(angle) * distance);
            float ty = (float) (Math.sin(angle) * distance);

            particle.animate()
                    .translationX(tx)
                    .translationY(ty)
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
        Bitmap scaled = Bitmap.createScaledBitmap(original, size - padding * 2, size - padding * 2, true);
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(scaled, padding, padding, null);
        return output;
    }
    private double calculateDistance(
            double lat1, double lon1,
            double lat2, double lon2
    ) {
        float[] result = new float[1];

        android.location.Location.distanceBetween(
                lat1,
                lon1,
                lat2,
                lon2,
                result
        );

        return result[0];
    }

}
