package com.yunmi.heatkettle.activity;

import android.os.Bundle;
import android.view.View;

import com.xiaomi.smarthome.device.api.XmPluginBaseActivity;
import com.yunmi.heatkettle.R;

public class UMBaseActivity extends XmPluginBaseActivity {
    protected int layoutId = R.layout.um_activity_mainscreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);
        View titleBar = findViewById(R.id.title_bar);
        if (titleBar != null)
            mHostActivity.setTitleBarPadding(titleBar); // 设置titleBar在顶部透明显示时的顶部padding
    }
}
