package cn.rongcloud.im.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import cn.rongcloud.im.R;


/**
 *
 * Created by bob on 2015/1/28.
 */
public class LoadingDialog extends Dialog {

    private TextView mTextView;

    public LoadingDialog(Context context) {

        super(context, R.style.WinDialog);
        setContentView(R.layout.ui_dialog_loading);
        mTextView = (TextView) findViewById(android.R.id.message);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
    public void setText(String s) {
        if (mTextView != null) {
            mTextView.setText(s);
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    public void setText(int res) {
        if (mTextView != null) {
            mTextView.setText(res);
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return false;
        }
        return super.onTouchEvent(event);
    }
}
