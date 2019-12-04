package com.sailflorve.imageviewplus.sample;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.sailflorve.imageviewplus.util.ViewUtil;
import com.sailflorve.imageviewplus.view.ImageViewPlus;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ImageActivity extends AppCompatActivity {

    private ImageViewPlus mImageViewPlus;
    private Button mBtnDraw;
    private Button mBtnBubble;
    private Button mBtnDrawInput;
    private Button mBtnBubbleInput;

    private static final int REQ_PHOTO_PICK = 405;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        setSupportActionBar(findViewById(R.id.toolbar));
        initView();
    }

    protected void initView() {
        mImageViewPlus = findViewById(R.id.cus_iv);

        mImageViewPlus.setImage(
                ViewUtil.getBmpFromDrawable(this, R.drawable.timg));

        mBtnDraw = findViewById(R.id.btn_draw);
        mBtnBubble = findViewById(R.id.btn_add_popup);
        mBtnDrawInput = findViewById(R.id.btn_draw_input);
        mBtnBubbleInput = findViewById(R.id.btn_popup_input);

        mBtnDraw.setOnClickListener(v -> {
            if (!mImageViewPlus.getDrawMode()) {
                setDrawMode(true);
            } else {
                setDrawMode(false);
            }
        });

        mBtnBubble.setOnClickListener(v -> {
            if (!mImageViewPlus.getBubbleMode()) {
                setBubbleMode(true);
            } else {
                setBubbleMode(false);
            }
        });

        mBtnDrawInput.setOnClickListener(v -> getPointFromInput(0));

        mBtnBubbleInput.setOnClickListener(v -> getPointFromInput(1));

        mImageViewPlus.setOnClickListener(v -> finish());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_img) {
            chooseImage();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQ_PHOTO_PICK) {
            if (data == null) {
                Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri imgUri = data.getData();
            if (imgUri != null) {
                Bitmap bitmap = ViewUtil.getBitmapFromUri(this, imgUri);
                mImageViewPlus.setImage(bitmap);
            }
        }
    }

    private void chooseImage() {
        mImageViewPlus.clearMode();
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQ_PHOTO_PICK);
    }

    private void getPointFromInput(int type) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog_coordinate_input, null);

        LinearLayout inputLayout2 = view.findViewById(R.id.ll_co_input_2);
        inputLayout2.setVisibility(type == 1 ? GONE : VISIBLE);

        TextInputLayout layoutX1 = view.findViewById(R.id.til_x_1);
        TextInputLayout layoutY1 = view.findViewById(R.id.til_y_1);
        TextInputLayout layoutX2 = view.findViewById(R.id.til_x_2);
        TextInputLayout layoutY2 = view.findViewById(R.id.til_y_2);

        layoutX1.setHint("X坐标(0 - " + mImageViewPlus.getImageViewWidth() + ")");
        layoutX2.setHint("X坐标(0 - " + mImageViewPlus.getImageViewWidth() + ")");
        layoutY1.setHint("Y坐标(0 - " + mImageViewPlus.getImageViewHeight() + ")");
        layoutY2.setHint("Y坐标(0 - " + mImageViewPlus.getImageViewHeight() + ")");

        new AlertDialog.Builder(this)
                .setTitle("输入坐标")
                .setView(view)
                .setPositiveButton("确定", (dialog, which) -> {
                    try {
                        float x1 = Float.parseFloat(layoutX1.getEditText().getText().toString());
                        float y1 = Float.parseFloat(layoutY1.getEditText().getText().toString());
                        if (type == 0) {
                            float x2 = Float.parseFloat(layoutX2.getEditText().getText().toString());
                            float y2 = Float.parseFloat(layoutY2.getEditText().getText().toString());
                            mImageViewPlus.addLine(new PointF(x1, y1), new PointF(x2, y2));
                        } else {
                            mImageViewPlus.addBubble(x1, y1);
                        }
                    } catch (NumberFormatException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }).show();
    }

    private void setDrawMode(boolean drawMode) {
        if (drawMode) {
            mBtnDraw.setText("停止绘制");
            mBtnBubble.setEnabled(false);
            mImageViewPlus.setDrawMode(true);
        } else {
            mBtnDraw.setText("开始绘制");
            mBtnBubble.setEnabled(true);
            mImageViewPlus.setDrawMode(false);
        }
    }

    private void setBubbleMode(boolean bubbleMode) {
        if (bubbleMode) {
            mBtnBubble.setText("完成");
            mBtnDraw.setEnabled(false);
            mImageViewPlus.setBubbleMode(true);
        } else {
            mBtnBubble.setText("添加气泡");
            mBtnDraw.setEnabled(true);
            mImageViewPlus.setBubbleMode(false);
        }
    }
}
