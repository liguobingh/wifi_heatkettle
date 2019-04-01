package com.yunmi.heatkettle.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.yunmi.heatkettle.R;

/**
 * 故障详情描述 Activity
 * Created by William on 2017/7/5.
 */

public class UMErrorDetailActivity extends UMBaseActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        layoutId = R.layout.um_activity_error_detail;
        super.onCreate(savedInstanceState);

        init();
    }

    private void init() {
        int errorType = getIntent().getIntExtra("error_type", 0);
        TextView tvTitle = (TextView) findViewById(R.id.title_bar_title);
        TextView tvFirst = (TextView) findViewById(R.id.error_first);
        TextView tvSecond = (TextView) findViewById(R.id.error_second);
        TextView tvThird = (TextView) findViewById(R.id.error_third);

        findViewById(R.id.title_bar_return).setOnClickListener(this);

        String title = "", first = "", second = "", third = "";
        switch (errorType) {
            case 21:
                title = activity().getResources().getString(R.string.um_device_error21);
                first = activity().getResources().getString(R.string.um_device_error21_one);
                second = activity().getResources().getString(R.string.um_device_error21_two);
                third = activity().getResources().getString(R.string.um_device_error21_three);
                break;
            case 22:
                title = activity().getResources().getString(R.string.um_device_error22);
                first = activity().getResources().getString(R.string.um_device_error22_one);
                second = activity().getResources().getString(R.string.um_device_error22_two);
                third = activity().getResources().getString(R.string.um_device_error22_three);
                break;
            case 23:
                title = activity().getResources().getString(R.string.um_device_error23);
                first = activity().getResources().getString(R.string.um_device_error23_one);
                second = activity().getResources().getString(R.string.um_device_error23_two);
                break;
            case 24:
                title = activity().getResources().getString(R.string.um_device_error24);
                first = activity().getResources().getString(R.string.um_device_error24_one);
                second = activity().getResources().getString(R.string.um_device_error24_two);
                break;
        }
        tvTitle.setText(title);
        tvFirst.setText(first);
        tvSecond.setText(second);
        tvThird.setText(third);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_bar_return:
                activity().finish();
                break;
        }
    }
}
