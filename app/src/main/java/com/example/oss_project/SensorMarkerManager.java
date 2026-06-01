package com.example.oss_project;

import android.content.Context;
import android.content.res.ColorStateList;
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

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;

import java.util.Random;

public class SensorMarkerManager {

    private final Context context;
    private final KakaoMap kakaoMap;
    private Integer currentSpawnedId = null;
    private int luckySensorIndex; // 당첨 센서 인덱스 (0~4)

    public interface SensorCollectListener {
        void onStartScan();
        void onStopScanAndUpload(int sensorIndex, Integer characterId);
    }

    private SensorCollectListener collectListener;

    public void setSensorCollectListener(SensorCollectListener listener) {
        this.collectListener = listener;
    }

    public SensorMarkerManager(Context context, KakaoMap kakaoMap) {
        this.context = context;
        this.kakaoMap = kakaoMap;
        this.luckySensorIndex = new java.util.Random().nextInt(5);
        android.util.Log.d("CharacterSpawn", "이번 수집 사이클 당첨 센서: " + (luckySensorIndex + 1) + "번");
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
            "88:A2:9E:9B:6A",
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

        dialog.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());

        Button btnCollect = dialog.findViewById(R.id.btn_collect);
        ProgressBar progressCollect = dialog.findViewById(R.id.progress_collect);

        btnCollect.setOnClickListener(v -> {
            String currentText = btnCollect.getText().toString();

            if (currentText.equals("획득")) {
                btnCollect.setVisibility(View.GONE);
                progressCollect.setVisibility(View.VISIBLE);

                if (collectListener != null) {
                    collectListener.onStartScan();
                }

                // 4초간 로딩 연출
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    progressCollect.setVisibility(View.GONE);
                    btnCollect.setVisibility(View.VISIBLE);
                    btnCollect.setText("수집 완료!");
                    btnCollect.setBackgroundTintList(ColorStateList.valueOf(
                            Color.parseColor("#4CAF50")));

                    // ★ 캐릭터 스폰 로직 (5개 중 1개 당첨 방식)
                    if (index == luckySensorIndex) {
                        currentSpawnedId = CharacterManager.generateRandomSpawn();
                        // 혹시 generateRandomSpawn에서 20% 확률 때문에 null이 나오면 안되므로 
                        // 무조건 나오게 하거나 확률을 높여야 합니다. 
                        // 여기서는 '당첨 센서'이므로 무조건 캐릭터가 나오도록 보정합니다.
                        if (currentSpawnedId == null) {
                            // 다시 굴려서 무조건 하나 선택 (Common 76%, Silver 18%, Gold 6%)
                            double roll = new Random().nextDouble() * 100;
                            int sub = new Random().nextInt(6);
                            if (roll < 76) currentSpawnedId = sub + 1;
                            else if (roll < 94) currentSpawnedId = sub + 7;
                            else currentSpawnedId = sub + 13;
                        }

                        int resId = CharacterManager.getCharacterDrawableId(currentSpawnedId);
                        imgSensor.setImageBitmap(getScaledBitmapWithPadding(resId, 500, 100));
                        playCelebrateAnimation(dialog, imgSensor);
                        Toast.makeText(context, "새로운 캐릭터 발견!", Toast.LENGTH_SHORT).show();
                        
                        // 수집 성공 후 다음 사이클을 위해 당첨 위치 변경
                        luckySensorIndex = new Random().nextInt(5);
                    } else {
                        currentSpawnedId = null;
                        Toast.makeText(context, "경험치를 획득했습니다!", Toast.LENGTH_SHORT).show();
                    }
                }, 4000);

            } else if (currentText.equals("수집 완료!")) {
                if (collectListener != null) {
                    collectListener.onStopScanAndUpload(index, currentSpawnedId);
                }
                dialog.dismiss();
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

    private boolean isSameHour(long time1, long time2) {
        if (time1 == 0) return false;
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal2.setTimeInMillis(time2);
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR) &&
                cal1.get(java.util.Calendar.HOUR_OF_DAY) == cal2.get(java.util.Calendar.HOUR_OF_DAY);
    }
}
