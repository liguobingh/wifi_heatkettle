package com.yunmi.heatkettle.dialog;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import com.yunmi.heatkettle.R;
import com.yunmi.heatkettle.utils.log;

/**
 * 清洗动画 Dialog
 * Created by William on 2017/6/29.
 */

public class UMWashDialog extends Dialog {
    private static final String TAG = UMWashDialog.class.getSimpleName();
    private ProgressBar mProgressBar;
    private int flush_status;
    private LocalBroadcastManager mLocalBroadcastManager;
    private ValueAnimator mValueAnimator;

    public UMWashDialog(@NonNull Context context, int flush_status) {
        super(context);
        this.flush_status = flush_status;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.um_dialog_wash);

        Window window = this.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
            window.setBackgroundDrawable(null);
            window.setGravity(Gravity.CENTER);
        }
        setCancelable(false);

        initView();

        registerReceiver();
    }

    private void initView() {
        mProgressBar = (ProgressBar) findViewById(R.id.wash_flash_progress);

        progressChange(flush_status);
    }

    private void progressChange(int flush_status) {
        log.d(TAG, "status change");
        if (mValueAnimator != null && mValueAnimator.isRunning()) mValueAnimator.cancel();
        if (flush_status == 1) {
            mValueAnimator = ValueAnimator.ofInt(mProgressBar.getProgress(), 33);
        } else if (flush_status == 2) {
            mValueAnimator = ValueAnimator.ofInt(mProgressBar.getProgress(), 66);
        } else {
            mValueAnimator = ValueAnimator.ofInt(mProgressBar.getProgress(), 100);
        }
        mValueAnimator.setDuration(1000);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mProgressBar.setProgress((int) valueAnimator.getAnimatedValue());
            }
        });
        mValueAnimator.start();
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.yunmi.heatkettle.ACTION_STATUS_CHANGE");
        mLocalBroadcastManager.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
        if (mValueAnimator != null && mValueAnimator.isRunning()) mValueAnimator.cancel();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null)
                if (intent.getAction().equals("com.yunmi.heatkettle.ACTION_STATUS_CHANGE")) {
                    log.d(TAG, "get com.yunmi.heatkettle.ACTION_STATUS_CHANGE success");
                    int status = intent.getIntExtra("flush_flag", 0);
                    if (flush_status != status) {
                        flush_status = status;
                        progressChange(status);
                    }
            }
        }
    };
}
