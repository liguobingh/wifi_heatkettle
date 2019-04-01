package com.yunmi.heatkettle.activity;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yunmi.heatkettle.R;
import com.yunmi.heatkettle.callback.RequestCallback;
import com.yunmi.heatkettle.device.UMKettleDevice;
import com.yunmi.heatkettle.utils.UMUtils;
import com.yunmi.heatkettle.utils.log;
import com.yunmi.heatkettle.view.UMExpandLayout;
import com.yunmi.heatkettle.view.UMSwitchButton;

import org.json.JSONObject;

/**
 * 喝水提醒 Activity
 * Created by William on 2017/5/31.
 */

public class UMRemindActivity extends UMBaseActivity implements View.OnClickListener, UMSwitchButton.OnSwitchStateChangeListener {
    private final static String TAG = UMRemindActivity.class.getSimpleName();
    private RequestCallback<JSONObject> mSetRemindCallback;
    private RequestCallback<JSONObject> mSetRemindTimeCallback;
    private UMExpandLayout mUMUmExpandLayout;
    private RelativeLayout mRelativeLayout;
    private LinearLayout mLinearLayout;
    private FrameLayout mFrameLayout;
    private UMSwitchButton mUMSwitchButton;
    private View mView;
    private TextView tvTime, tvText;
    private boolean isFinish = false;
    private int isOpen = 0, remindTime = 0;
    private String oldChose = "";
    private UMKettleDevice mDevice;
    private static final int MSG_SET_REMIND_FAIL = 0;   // 提醒开关设置失败
    private static final int MSG_SET_REMIND_SUCCESS = 1;    // 提醒开关设置成功
    private static final int MSG_SET_REMIND_TIME_FAIL = 2;  // 提醒时间设置失败
    private static final int MSG_SET_REMIND_TIME_SUCCESS = 3;   // 提醒时间设置成功

    @Override
    public void onCreate(Bundle savedInstanceState) {
        layoutId = R.layout.um_activity_remind;
        super.onCreate(savedInstanceState);

        initView();

        initCallBack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
//            mHandler.removeMessages(MSG_SET_REMIND_FAIL);
//            mHandler.removeMessages(MSG_SET_REMIND_SUCCESS);
//            mHandler.removeMessages(MSG_SET_REMIND_TIME_FAIL);
//            mHandler.removeMessages(MSG_SET_REMIND_TIME_SUCCESS);
            mHandler = null;
        }
        mSetRemindCallback = null;
        mSetRemindTimeCallback = null;

        if (mUMUmExpandLayout.getAnimation() != null) mUMUmExpandLayout.clearAnimation();
        if (mUMSwitchButton.getAnimation() != null) mUMSwitchButton.clearAnimation();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!isFinish) {
            isFinish = true;
            mUMUmExpandLayout.initExpand(false);

            int height_1 = mRelativeLayout.getHeight();
            int height_2 = tvText.getHeight();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvText.getLayoutParams();
            params.setMargins(UMUtils.dp2px(24, activity()), UMUtils.dp2px(24, activity()), 0, (height_1 - height_2) + UMUtils.dp2px(24, activity()));

            initData();
        }
    }

    private void initView() {
        mUMSwitchButton = (UMSwitchButton) findViewById(R.id.remind_switch);
        mUMUmExpandLayout = (UMExpandLayout) findViewById(R.id.remind_expand);
        mLinearLayout = (LinearLayout) findViewById(R.id.remind_chose_layout);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.remind_chose);
        mFrameLayout = (FrameLayout) findViewById(R.id.remind_mask);
        mView = findViewById(R.id.remind_line_2);
        tvTime = (TextView) findViewById(R.id.remind_time);
        tvText = (TextView) findViewById(R.id.remind_time_text);

        findViewById(R.id.title_bar_return).setOnClickListener(this);
        mFrameLayout.setOnClickListener(this);
        mRelativeLayout.setOnClickListener(this);

        // 时间选择监听
        View.OnClickListener itemOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUMUmExpandLayout.toggleExpand();
                mFrameLayout.setVisibility(View.GONE);
                String time = ((TextView) v).getText().toString();
                tvTime.setText(time);
                setRemindTime(UMUtils.getIntFromStr(time));
            }
        };
        findViewById(R.id.remind_item1).setOnClickListener(itemOnClickListener);
        findViewById(R.id.remind_item2).setOnClickListener(itemOnClickListener);
        findViewById(R.id.remind_item3).setOnClickListener(itemOnClickListener);
        findViewById(R.id.remind_item4).setOnClickListener(itemOnClickListener);
        findViewById(R.id.remind_item5).setOnClickListener(itemOnClickListener);
        findViewById(R.id.remind_item6).setOnClickListener(itemOnClickListener);
        findViewById(R.id.remind_item7).setOnClickListener(itemOnClickListener);
        findViewById(R.id.remind_item8).setOnClickListener(itemOnClickListener);

        mDevice = UMKettleDevice.getDevice(mDeviceStat);    // 初始化Device
    }

    private void initCallBack() {
        // 设置提醒开关回调
        mSetRemindCallback = new RequestCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                if (mHandler != null)
                    Message.obtain(mHandler, MSG_SET_REMIND_SUCCESS).sendToTarget();
                log.d(TAG, "set_drink_remind_enable success, data=" + data.toString());
            }

            @Override
            public void onFailure(int errorCode, String errorInfo) {
                if (mHandler != null)
                    Message.obtain(mHandler, MSG_SET_REMIND_FAIL).sendToTarget();
                log.e(TAG, "set_drink_remind_enable fail, code = " + errorCode + ",msg=" + errorInfo);
            }
        };
        // 设置提醒时间
        mSetRemindTimeCallback = new RequestCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                if (mHandler != null)
                    Message.obtain(mHandler, MSG_SET_REMIND_TIME_SUCCESS).sendToTarget();
                log.d(TAG, "set_drink_remind_time success, data=" + data.toString());
            }

            @Override
            public void onFailure(int errorCode, String errorInfo) {
                if (mHandler != null)
                    Message.obtain(mHandler, MSG_SET_REMIND_TIME_FAIL).sendToTarget();
                log.e(TAG, "set_drink_remind_time fail, code = " + errorCode + ",msg=" + errorInfo);
            }
        };
    }

    private void initData() {
        int isRemind = getIntent().getIntExtra("isRemind", 0);
//        int count = getIntent().getIntExtra("remindCount", 0);
        if (isRemind == 1) {
            isOpen = 1;
            mUMSwitchButton.setOn(true, true);
            initChoseLayout(true);
            remindTime = getIntent().getIntExtra("remindTime", 10);
            if (remindTime != 1 && remindTime != 2 && remindTime != 3 && remindTime != 4 && remindTime != 6 && remindTime != 8 && remindTime != 10 && remindTime != 12)
                tvTime.setText(activity().getResources().getString(R.string.um_drink_remind_tip_chose));
            else
                tvTime.setText(String.format(activity().getResources().getString(R.string.um_over_time), remindTime));
//                tvTime.setText(activity().getResources().getString(R.string.um_over) + remindTime + activity().getResources().getString(R.string.um_hour));
        } else {
            isOpen = 0;
            remindTime = getIntent().getIntExtra("remindTime", 10);
            initChoseLayout(false);
        }
        mUMSwitchButton.setOnSwitchStateChangeListener(this);
        oldChose = tvTime.getText().toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_bar_return: // 返回
                activity().finish();
                break;
            case R.id.remind_chose:
                mUMUmExpandLayout.toggleExpand();
                if (mUMUmExpandLayout.isExpand()) mFrameLayout.setVisibility(View.VISIBLE);
                else mFrameLayout.setVisibility(View.GONE);
                break;
            case R.id.remind_mask:
                mFrameLayout.setVisibility(View.GONE);
                mUMUmExpandLayout.toggleExpand();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mUMUmExpandLayout.isExpand()) mUMUmExpandLayout.toggleExpand();
        else activity().finish();
    }

    @Override
    public void onSwitchStateChange(boolean isOn) {
        if (isOn) {
            setRemind(1);
            isOpen = 1;
        } else {
            setRemind(0);
            isOpen = 0;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MSG_SET_REMIND_FAIL: // 设置提醒开关失败
                Toast.makeText(activity(), activity().getResources().getString(R.string.um_setting_fail), Toast.LENGTH_SHORT).show();
                mUMSwitchButton.setOn(!mUMSwitchButton.isOn(), true);
                break;
            case MSG_SET_REMIND_SUCCESS: // 设置提醒开关成功
                Toast.makeText(activity(), activity().getResources().getString(R.string.um_setting_success), Toast.LENGTH_SHORT).show();
                if (isOpen == 1) initChoseLayout(true);
                else initChoseLayout(false);
                break;
            case MSG_SET_REMIND_TIME_FAIL: // 设置提醒时间失败:
                Toast.makeText(activity(), activity().getResources().getString(R.string.um_setting_fail), Toast.LENGTH_SHORT).show();
                tvTime.setText(oldChose);
                break;
            case MSG_SET_REMIND_TIME_SUCCESS: // 设置提醒时间成功
                Toast.makeText(activity(), activity().getResources().getString(R.string.um_setting_success), Toast.LENGTH_SHORT).show();
                oldChose = tvTime.getText().toString();
                remindTime = UMUtils.getIntFromStr(oldChose);
                break;
        }
    }

    // 喝水提醒开关设置
    private void setRemind(int isRemind) {
        if (mDevice != null) {
            Object[] params = new Object[1];
            params[0] = isRemind;
            mDevice.setProperty("set_drink_remind_enable", params, mSetRemindCallback);
            if(remindTime<=0){
                remindTime=10;
            }
              tvTime.setText(String.format(activity().getResources().getString(R.string.um_over_time),remindTime ));

        }
    }

    // 喝水提醒时间
    private void setRemindTime(int time) {
        if (mDevice != null) {
            Object[] params = new Object[1];
            params[0] = time;
            mDevice.setProperty("set_drink_remind_time", params, mSetRemindTimeCallback);
        }
    }

    private void initChoseLayout(boolean isOpen) {
        if (!isOpen) {
            if (remindTime != 1 && remindTime != 2 && remindTime != 3 && remindTime != 4 && remindTime != 6 && remindTime != 8 && remindTime != 10 && remindTime != 12)
                tvTime.setText(activity().getResources().getString(R.string.um_drink_remind_tip_chose));
            else
                tvTime.setText(String.format(activity().getResources().getString(R.string.um_over_time), remindTime));
//                tvTime.setText(activity().getResources().getString(R.string.um_over) + remindTime + activity().getResources().getString(R.string.um_hour));
//            tvCount.setVisibility(View.GONE);
            tvText.setVisibility(View.GONE);
            mLinearLayout.setVisibility(View.GONE);
            mUMUmExpandLayout.initExpand(false);
            mView.setVisibility(View.GONE);
        } else {
//            tvCount.setVisibility(View.VISIBLE);
            tvText.setVisibility(View.VISIBLE);
            mLinearLayout.setVisibility(View.VISIBLE);
            mView.setVisibility(View.VISIBLE);
        }
    }
}
