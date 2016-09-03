package com.example.jaeseok.airshare;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.widget.Button;

/**
 * Created by swimming on 2016. 6. 5..
 */
public class Popup extends Dialog implements View.OnClickListener {

    private View mDialogView;
    private AnimationSet mModalOutAnim;
    private Animation mOverlayOutAnim;
    private String mConfirmText;
    private Button mConfirmButton;
    private Button mCancelButton;
    private int imageSource;
    private OnSweetClickListener mConfirmClickListener;
    private OnSweetClickListener mCancelClickListener;
    private boolean mCloseFromCancel;


    public static interface OnSweetClickListener {
        public void onClick(Popup sweetAlertDialog);
    }

    public Popup(Context context) {
        super(context, R.style.alert_dialog);
        setCancelable(true);
        setCanceledOnTouchOutside(false);

        mModalOutAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.modal_out);
        mModalOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDialogView.setVisibility(View.GONE);
                mDialogView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCloseFromCancel) {
                            Popup.super.cancel();
                        } else {
                            Popup.super.dismiss();
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // dialog overlay fade out
        mOverlayOutAnim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                WindowManager.LayoutParams wlp = getWindow().getAttributes();
                wlp.alpha = 1 - interpolatedTime;
                getWindow().setAttributes(wlp);
            }
        };
        mOverlayOutAnim.setDuration(120);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_dialog);

        mDialogView = getWindow().getDecorView().findViewById(android.R.id.content);
        mCancelButton = (Button)findViewById(R.id.cancel_button);
        mConfirmButton = (Button)findViewById(R.id.confirm_button);
        mConfirmButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        setConfirmText(mConfirmText);
        changeAlertType(true);

    }

    public Popup setCancelClickListener (OnSweetClickListener listener) {
        mCancelClickListener = listener;
        return this;
    }

    public Popup setConfirmClickListener (OnSweetClickListener listener) {
        mConfirmClickListener = listener;
        return this;
    }

    private void restore () {
        mConfirmButton.setBackgroundResource(R.drawable.blue_button_background);
    }

    private void changeAlertType(boolean fromCreate) {
        // call after created views
        if (mDialogView != null) {
            if (!fromCreate) {
                // restore all of views state before switching alert type
                restore();
            }

        }
    }

    public Popup setConfirmText (String text) {
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null) {
            mConfirmButton.setText(mConfirmText);
        }
        return this;
    }

    /**
     * The real Dialog.cancel() will be invoked async-ly after the animation finishes.
     */
    @Override
    public void cancel() {
        dismissWithAnimation(true);
    }

    /**
     * The real Dialog.dismiss() will be invoked async-ly after the animation finishes.
     */
    public void dismissWithAnimation() {
        dismissWithAnimation(false);
    }

    private void dismissWithAnimation(boolean fromCancel) {
        mCloseFromCancel = fromCancel;
        mConfirmButton.startAnimation(mOverlayOutAnim);
        mDialogView.startAnimation(mModalOutAnim);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirm_button) {
            mConfirmClickListener.onClick(Popup.this);
        } else if(v.getId() == R.id.cancel_button) {
            mCancelClickListener.onClick(Popup.this);
        }
    }
}
