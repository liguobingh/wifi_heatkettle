package com.yunmi.heatkettle.activity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.smarthome.common.ui.dialog.MLAlertDialog;
import com.xiaomi.smarthome.device.api.BaseDevice;
import com.xiaomi.smarthome.device.api.BaseDevice.StateChangedListener;
import com.xiaomi.smarthome.device.api.DeviceUpdateInfo;
import com.yunmi.heatkettle.R;
import com.yunmi.heatkettle.callback.RequestCallback;
import com.yunmi.heatkettle.data.UMDeviceInfo;
import com.yunmi.heatkettle.device.UMKettleDevice;
import com.yunmi.heatkettle.dialog.UMWashDialog;
import com.yunmi.heatkettle.utils.UMGloParam;
import com.yunmi.heatkettle.utils.log;
import com.yunmi.heatkettle.view.UMWaterTempView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("Registered")
public class UMMainScreenActivity extends UMBaseActivity implements StateChangedListener, OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {
    private static final String TAG = UMMainScreenActivity.class.getSimpleName();
    private RequestCallback<DeviceUpdateInfo> mCheckUpdateCallback;   //检查更新回调函数
    private RequestCallback<JSONObject> mSetPropertyCallback;
    private UMKettleDevice mDevice;
    private UMWaterTempView mUmWaterTempView;
    private RelativeLayout mRelativeLayout;
    private ImageView mRemindView;
    private SeekBar mSeekBar;
    private TextView mTempTextView;
    private TextView mTitleView, tvTempTip, tvTDS, tvTDSUnit, tvTDSTip, tvStored, tvStoredUnit, tvStoredTip, tvWash, tvWashUnit, tvWashTip, tvTempSetting, tvMinTemp, tvError, tvMode;
    private ImageView mRedPointView;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private int errorCount = 0, count = 0;// 异常数量, 获取失败计数
    private int minTemp = 40, startTemp = 0;// 最低设置水温，动画开始温度
    private int isRemind = 0, remindTime = 0, remindCount = 0;
    private int oldTemp = 0, oldMinTemp = 0;
    private boolean isSetting = false;// 是否正在设置
    private CharSequence[] error;
    private UMWashDialog mDialog;
    private LocalBroadcastManager mLocalBroadcastManager;
    private ValueAnimator mValueAnimator;
    private static final int MSG_SET_TEMP_FAIL = 4;// 水温设置失败
    private static final int MSG_UPDATE_FIRM = 1;// 检查更新成功
    private static final int MSG_DEVICE_GET_PROP = 2;// GetProp请求
    private static final int MSG_MOVE_BUBBLE = 3;// SeekBar 移动

    @Override
    public void onCreate(Bundle savedInstanceState) {
        layoutId = R.layout.um_activity_mainscreen;
        super.onCreate(savedInstanceState);
        initView();
        initCallback();
    }

    private void initView() {
        mRemindView = (ImageView) findViewById(R.id.remind_setting);
        mTitleView = (TextView) findViewById(R.id.title_bar_title);
        mRedPointView = (ImageView) findViewById(R.id.title_bar_red_point);
        mUmWaterTempView = (UMWaterTempView) findViewById(R.id.water_temp);
        mSeekBar = (SeekBar) findViewById(R.id.temp_setting_control);
        mTempTextView = (TextView) findViewById(R.id.water_temp_text);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.background);
        tvTempTip = (TextView) findViewById(R.id.temp_setting_temp_tip);
        tvTDS = (TextView) findViewById(R.id.water_tds_value);
        tvTDSUnit = (TextView) findViewById(R.id.water_tds_unit);
        tvTDSTip = (TextView) findViewById(R.id.water_tds_tip);
        tvStored = (TextView) findViewById(R.id.stored_remind_value);
        tvStoredUnit = (TextView) findViewById(R.id.stored_remind_unit);
        tvStoredTip = (TextView) findViewById(R.id.stored_remind_tip);
        tvWash = (TextView) findViewById(R.id.wash_remind_value);
        tvWashUnit = (TextView) findViewById(R.id.wash_remind_unit);
        tvWashTip = (TextView) findViewById(R.id.wash_remind_tip);
        tvTempSetting = (TextView) findViewById(R.id.temp_setting_value);
        tvMinTemp = (TextView) findViewById(R.id.temp_setting_min);
        tvError = (TextView) findViewById(R.id.error_status);
        tvMode = (TextView) findViewById(R.id.water_mode);
        TextView tempTipTextView = (TextView) findViewById(R.id.temp_setting_tip);

        tvError.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setOnTouchListener(this);
        mSeekBar.setEnabled(false);
        findViewById(R.id.remind_setting).setOnClickListener(this);
        findViewById(R.id.title_bar_return).setOnClickListener(this);

        mDevice = UMKettleDevice.getDevice(mDeviceStat);// 初始化device
        // 更多
        View moreView = findViewById(R.id.title_bar_more);
        moreView.setOnClickListener(this);
        // 打开分享
        View shareView = findViewById(R.id.title_bar_share);
        if (mDevice.isOwner() || !mDevice.isReadOnlyShared()) {    // 设备拥有者和允许控制的设备分享
            shareView.setVisibility(View.VISIBLE);
            moreView.setVisibility(View.VISIBLE);
            shareView.setOnClickListener(this);
        } else {    // 仅可查看的分享设备
            shareView.setVisibility(View.GONE);
            moreView.setVisibility(View.GONE);
        }
        if (mDeviceStat.model.equals(UMGloParam.MODEL_R2) || mDeviceStat.model.equals(UMGloParam.MODEL_R3)) {
            tempTipTextView.setVisibility(View.INVISIBLE);
        }
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(activity());
    }

    private void initCallback() {
        // 检查更新
        mCheckUpdateCallback = new RequestCallback<DeviceUpdateInfo>() {
            @Override
            public void onSuccess(DeviceUpdateInfo data) {
                if (mHandler != null)
                    Message.obtain(mHandler, MSG_UPDATE_FIRM, data).sendToTarget();
            }

            @Override
            public void onFailure(int errorCode, String errorInfo) {
                log.e(TAG, "checkDeviceUpdateInfo fail,code=" + errorCode + ",msg=" + errorInfo);
            }
        };

        // 设置水温
        mSetPropertyCallback = new RequestCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                isSetting = false;
                Toast.makeText(activity(), activity().getResources().getString(R.string.um_setting_success), Toast.LENGTH_SHORT).show();
                log.d(TAG, data.toString());
            }

            @Override
            public void onFailure(int errorCode, String errorInfo) {
                isSetting = false;
                Toast.makeText(activity(), activity().getResources().getString(R.string.um_setting_fail), Toast.LENGTH_SHORT).show();
                if (mHandler != null) Message.obtain(mHandler, MSG_SET_TEMP_FAIL).sendToTarget();
                log.e(TAG, "set property fail,code=" + errorCode + ",msg=" + errorInfo);
            }
        };
    }

    private void refreshUI() {
        mTitleView.setText(mDevice.getName());
        if (mDevice == null) return;
        UMDeviceInfo info = mDevice.getDeviceInfo();
        // 设备离线
        if (mDevice.getErrorInfo() != null && mDevice.getErrorInfo().equals("device offline")) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setEnabled(false);
            tvError.setText(activity().getResources().getString(R.string.um_device_offline));
            return;
        }
        // 获取数据失败
        else if (mDevice.getErrorInfo() != null) {
            if (count >= 4) {   //？？？啥意思
                tvError.setVisibility(View.VISIBLE);
                tvError.setEnabled(false);
                tvError.setText(activity().getResources().getString(R.string.um_device_network_unavailable));
            } else count++;
            return;
        }

        count = 0;// 复位
        switch (info.work_mode) {
            case 0:
                tvMode.setText(activity().getResources().getString(R.string.um_water_temp_normal));
                break;
            case 1:
                tvMode.setText(activity().getResources().getString(R.string.um_water_temp_warm));
                break;
            case 2:
                tvMode.setText(activity().getResources().getString(R.string.um_water_temp_hot));
                break;
        }
        if (info.statusByte[22] == 1) { // 进水 NTC 异常界面显示
            mUmWaterTempView.setValues(100);
            startTempAnim(0);
            startBackgroundChangeAnim(100);
        } else {
            mUmWaterTempView.setValues(info.setup_tempe);// 当前温度
            startTempAnim(info.setup_tempe);
            startBackgroundChangeAnim(info.setup_tempe);
        }

        if (mDeviceStat.model.equals(UMGloParam.MODEL_R1)) {
            minTemp = info.min_set_tempe;
        }
        if ((90 - minTemp) <= 0) { // 设置温度返回有误
            tvMinTemp.setText(activity().getResources().getString(R.string.um_error_value));
            mSeekBar.setEnabled(false);
        } else {
            tvMinTemp.setText(String.valueOf(minTemp));// 最低加热温度
            mSeekBar.setMax(90 - minTemp);
            mSeekBar.setEnabled(true);
        }

        oldTemp = info.custom_tempe1;
        oldMinTemp = info.min_set_tempe;

        if (!isSetting) {
            if ((90 - minTemp) > 0)
                mSeekBar.setProgress(info.custom_tempe1 - info.min_set_tempe);
            tvTempTip.setText(tempChose(info.custom_tempe1));
            String str = String.valueOf(info.custom_tempe1) + activity().getResources().getString(R.string.um_temp_unit);
            tvTempSetting.setText(str);
        }

        tvTDS.setText(String.valueOf(info.tds));// TDS 值
        tvStored.setText(String.valueOf(info.water_remain_time));// 存水提醒
        tvWash.setText(String.valueOf(info.flush_time / 24));// 清洗提醒
        // 喝水提醒
        isRemind = info.drink_remind;
        remindTime = info.drink_remind_time;
        remindCount = info.drink_time_count;
        if (info.drink_remind == 1)
            mRemindView.setImageResource(R.drawable.icon_drink_remind_active);
        else mRemindView.setImageResource(R.drawable.icon_drink_remind_normal);
        // TDS 提示文案
        if (info.tds <= 40) {
            tvTDS.setTextColor(0xFF24CCDA);
            tvTDSUnit.setTextColor(0xFF24CCDA);
            tvTDSTip.setVisibility(View.VISIBLE);
            tvTDSTip.setText(activity().getResources().getString(R.string.um_water_tds_low));
        } else if (info.tds >= 200) {
            tvTDS.setTextColor(0xFFFF7070);
            tvTDSUnit.setTextColor(0xFFFF7070);
            tvTDSTip.setVisibility(View.VISIBLE);
            tvTDSTip.setText(activity().getResources().getString(R.string.um_water_tds_high));
        } else {
            tvTDS.setTextColor(0xFF24CCDA);
            tvTDSUnit.setTextColor(0xFF24CCDA);
            tvTDSTip.setVisibility(View.GONE);
        }
        // 存水提醒文案
        if (info.water_remain_time >= 0 && info.water_remain_time < 12) {
            tvStored.setTextColor(0xFF24CCDA);
            tvStoredUnit.setTextColor(0xFF24CCDA);
            tvStoredTip.setText(activity().getResources().getString(R.string.um_stored_remind_tip_low));
        } else if (info.water_remain_time >= 12 && info.water_remain_time < 24) {
            tvStored.setTextColor(0xFFE1D900);
            tvStoredUnit.setTextColor(0xFFE1D900);
            tvStoredTip.setText(String.format(activity().getResources().getString(R.string.um_stored_remind_tip), info.water_remain_time));
        } else if (info.water_remain_time >= 24 && info.water_remain_time < 48) {
            tvStored.setTextColor(0xFFF8A937);
            tvStoredUnit.setTextColor(0xFFF8A937);
            tvStoredTip.setText(activity().getResources().getString(R.string.um_stored_remind_middle));
        } else {
            tvStored.setTextColor(0xFFFF7070);
            tvStoredUnit.setTextColor(0xFFFF7070);
            tvStoredTip.setText(activity().getResources().getString(R.string.um_stored_remind_high));
        }
        // 清洗提醒文案
        if ((info.flush_time / 24 >= 0) && (info.flush_time / 24 <= 60)) {
            tvWash.setTextColor(0xFF24CCDA);
            tvWashUnit.setTextColor(0xFF24CCDA);
            tvWashTip.setText(activity().getResources().getString(R.string.um_wash_remind_low));
        } else {
            tvWash.setTextColor(0xFFFF7070);
            tvWashUnit.setTextColor(0xFFFF7070);
            tvWashTip.setText(getResources().getString(R.string.um_wash_remind_high));
        }
        showWashDialog(info.flush_flag);
        showError(info);
    }

    private void startTimer() {
        stopTimer();    // 先停止Timer
        int period = 3 * 1000;  // 轮询周期
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mDevice != null) {
                    if (!mDevice.isUpdateRunning) {
                        if (mHandler != null) {
                            Message.obtain(mHandler, MSG_DEVICE_GET_PROP).sendToTarget();
                        }
                    }
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, period);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    // 背景渐变动画
    private void startBackgroundChangeAnim(int Temp) {
        if (mRelativeLayout.getAnimation() != null) mRelativeLayout.clearAnimation();// 移除上次动画
        Drawable bgDrawable = mRelativeLayout.getBackground();// 获取背景
        Drawable oldDrawable;// 上次渐变完成后 Drawable
        Drawable newDrawable;// 新渐变 Drawable

        if (bgDrawable == null) {
            oldDrawable = ContextCompat.getDrawable(activity(), R.drawable.um_background_low);
        } else if (bgDrawable instanceof TransitionDrawable) {
            oldDrawable = ((TransitionDrawable) bgDrawable).getDrawable(1);
        } else {
            oldDrawable = bgDrawable;
        }

        if (Temp < 50) {
            newDrawable = ContextCompat.getDrawable(activity(), R.drawable.um_background_low);
        } else if (Temp < 70) {
            newDrawable = ContextCompat.getDrawable(activity(), R.drawable.um_background_middle);
        } else {
            newDrawable = ContextCompat.getDrawable(activity(), R.drawable.um_background_high);
        }

        TransitionDrawable drawable = new TransitionDrawable(new Drawable[]{oldDrawable, newDrawable});
        mRelativeLayout.setBackgroundDrawable(drawable);
        drawable.startTransition(2000);
    }

    // 显示清洗动画
    private void showWashDialog(int flush_status) {
        if (flush_status == 1 || flush_status == 2 || flush_status == 3) {
            if (mDialog == null) {
                mDialog = new UMWashDialog(activity(), flush_status);
                mDialog.show();
            } else {
                Intent intent = new Intent();
                intent.setAction("com.yunmi.heatkettle.ACTION_STATUS_CHANGE");
                intent.putExtra("flush_flag", flush_status);
                mLocalBroadcastManager.sendBroadcast(intent);
            }
        } else {
            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        }
    }

    // 显示异常
    private void showError(UMDeviceInfo info) {
        tvError.setEnabled(true);
        errorCount = info.statusCount;
        if (info.statusCount == 0) {
            tvError.setVisibility(View.GONE);
        } else if (info.statusCount == 1) {
            tvError.setVisibility(View.VISIBLE);
            if (info.statusByte[21] == 1) {
                tvError.setText(activity().getResources().getString(R.string.um_device_error21));
            } else if (info.statusByte[22] == 1) {
                tvError.setText(activity().getResources().getString(R.string.um_device_error22));
            } else if (info.statusByte[23] == 1) {
                tvError.setText(activity().getResources().getString(R.string.um_device_error23));
            } else if (info.statusByte[24] == 1) {
                tvError.setText(activity().getResources().getString(R.string.um_device_error24));
            }
        } else {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(String.format(activity().getResources().getString(R.string.um_error_more), info.statusCount));
            ArrayList<String> list = new ArrayList<>();
            if (info.statusByte[21] == 1) {
                list.add(activity().getResources().getString(R.string.um_device_error21));
            }
            if (info.statusByte[22] == 1) {
                list.add(activity().getResources().getString(R.string.um_device_error22));
            }
            if (info.statusByte[23] == 1) {
                list.add(activity().getResources().getString(R.string.um_device_error23));
            }
            if (info.statusByte[24] == 1) {
                list.add(activity().getResources().getString(R.string.um_device_error24));
            }
            error = list.toArray(new CharSequence[list.size()]);
        }
    }

    private void enterDetail(String str) {
        int type = 0;
        if (str.equals(activity().getResources().getString(R.string.um_device_error21))) {
            type = 21;
        } else if (str.equals(activity().getResources().getString(R.string.um_device_error22))) {
            type = 22;
        } else if (str.equals(activity().getResources().getString(R.string.um_device_error23))) {
            type = 23;
        } else if (str.equals(activity().getResources().getString(R.string.um_device_error24))) {
            type = 24;
        }
        Intent intent = new Intent();
        intent.putExtra("error_type", type);
        startActivity(intent, UMErrorDetailActivity.class.getName());
    }

    private void startTempAnim(int setup_tempe) {
        setup_tempe = setup_tempe > 100 ? 100 : setup_tempe;
        if (mValueAnimator != null && mValueAnimator.isRunning())
            mValueAnimator.cancel();
        mValueAnimator = ValueAnimator.ofInt(startTemp, setup_tempe);
        mValueAnimator.setDuration(2000);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                startTemp = (int) valueAnimator.getAnimatedValue();
//                if (startTemp == 100) mTempTextView.setTextSize(60);
//                else mTempTextView.setTextSize(75);
                String temp = startTemp >= 0 && startTemp < 10 ? "0" + startTemp : String.valueOf(startTemp);
                mTempTextView.setText(temp.equals("00") ? "--" : temp);
            }
        });
        mValueAnimator.start();
    }

    // 各温度提示文案
    private String tempChose(int temp) {
        String str;
        switch (temp) {
            case 40:
                str = getResources().getString(R.string.um_temp_tip_40);
                break;
            case 50:
                str = getResources().getString(R.string.um_temp_tip_50);
                break;
            case 60:
                str = getResources().getString(R.string.um_temp_tip_60);
                break;
            case 75:
                str = getResources().getString(R.string.um_temp_tip_75);
                break;
            case 80:
                str = getResources().getString(R.string.um_temp_tip_80);
                break;
            case 85:
                str = getResources().getString(R.string.um_temp_tip_85);
                break;
            case 90:
                str = getResources().getString(R.string.um_temp_tip_90);
                break;
            default:
                str = "";
                break;
        }
        return str;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDevice == null) mDevice = UMKettleDevice.getDevice(mDeviceStat);// 初始化device
        mDevice.checkUpdateInfo(mCheckUpdateCallback);
        mDevice.updateDeviceStatus();
        mDevice.addStateChangedListener(this);// 监听设备数据变化
        ((TextView) findViewById(R.id.title_bar_title)).setText(mDevice.getName());
        startTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 取消监听
        if (mDevice != null)
            mDevice.removeStateChangedListener(this);
        stopTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        mCheckUpdateCallback = null;
        mSetPropertyCallback = null;
        mLocalBroadcastManager = null;

        if (mUmWaterTempView != null && mUmWaterTempView.getAnimation() != null)
            mUmWaterTempView.clearAnimation();
        if (mRelativeLayout.getAnimation() != null)
            mRelativeLayout.clearAnimation();
        if (mValueAnimator != null && mValueAnimator.isRunning()) mValueAnimator.cancel();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_FIRM:   // 刷新固件升级状态
                DeviceUpdateInfo updateInfo = (DeviceUpdateInfo) msg.obj;
                if (mDevice.isOwner()) {
                    log.d(TAG, "update_status:" + updateInfo.mHasNewFirmware);
                    if (updateInfo.mHasNewFirmware) {
                        mRedPointView.setVisibility(View.VISIBLE);
                    } else {
                        mRedPointView.setVisibility(View.GONE);
                    }
                }
                break;
            case MSG_DEVICE_GET_PROP: // getProp
                if (mDevice != null) {
                    mDevice.updateProperty(new String[]{"water_remain_time", "flush_time", "flush_flag",
                            "tds", "time", "curr_tempe", "setup_tempe", "custom_tempe1", "min_set_tempe",
                            "drink_remind", "drink_remind_time", "run_status", "work_mode", "drink_time_count"});
                }
                break;
            case MSG_MOVE_BUBBLE:
                if (tvTempSetting.getVisibility() != View.VISIBLE)
                    tvTempSetting.setVisibility(View.VISIBLE);
                if ((90 - minTemp) > 0) {
                    int tempSet = minTemp + mSeekBar.getProgress();
                    tvTempTip.setText(tempChose(tempSet));
                    String str = String.valueOf(tempSet) + activity().getResources().getString(R.string.um_temp_unit);
                    tvTempSetting.setText(str);
                }
                break;
            case MSG_SET_TEMP_FAIL:// 设置水温失败
                mSeekBar.setProgress(oldTemp - oldMinTemp);
                tvTempTip.setText("");
                break;
            default:
                break;
        }
    }

    @Override
    public void onStateChanged(BaseDevice device) {
        refreshUI();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.title_bar_return: // 返回
                activity().finish();
                break;
            case R.id.title_bar_more: // 更多
                intent = new Intent();
                intent.putExtra("scence_enable", false);
                mHostActivity.openMoreMenu2(null, true, 1, intent);
                break;
            case R.id.title_bar_share: // 分享
                mHostActivity.openShareActivity();
                break;
            case R.id.remind_setting: // 开启或关闭提醒
                intent = new Intent();
                intent.putExtra("isRemind", isRemind);
                intent.putExtra("remindTime", remindTime);
                intent.putExtra("remindCount", remindCount);
                startActivity(intent, UMRemindActivity.class.getName());
                break;
            case R.id.error_status: // 故障信息
                if (errorCount == 1) {
                    enterDetail(tvError.getText().toString());
                } else {
                    new MLAlertDialog.Builder(activity())
                            .setTitle(activity().getResources().getString(R.string.um_error_list_title))
                            .setCancelable(true)
                            .setItems(error, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    enterDetail((String) error[which]);
                                }
                            }).show();
                }
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mHandler != null)
            Message.obtain(mHandler, MSG_MOVE_BUBBLE).sendToTarget();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mDevice != null) {
            Object[] params = new Object[2];
            params[0] = 131434;
            params[1] = seekBar.getProgress() + minTemp;
            mDevice.setProperty("set_tempe_setup", params, mSetPropertyCallback);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                v.performClick();
                isSetting = true;
//                if (tvTempSetting.getVisibility() != View.VISIBLE)
//                    tvTempSetting.setVisibility(View.VISIBLE);
                if (mHandler != null)
                    Message.obtain(mHandler, MSG_MOVE_BUBBLE).sendToTarget();
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                if (tvTempSetting.getVisibility() == View.VISIBLE)
//                    tvTempSetting.setVisibility(View.INVISIBLE);
                return false;
            default:
                return super.onTouchEvent(event);
        }
    }
}