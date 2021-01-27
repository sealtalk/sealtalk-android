package cn.rongcloud.im.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import cn.rongcloud.im.R;

public class SelectCleanTimeDialog extends DialogFragment implements View.OnClickListener {

    private OnDialogButtonClickListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_clean_time, null);
        Button cleanSixtySixHour = view.findViewById(R.id.btn_clean_thirty_six_hour);
        Button cleanThreeDay = view.findViewById(R.id.btn_clean_three_day);
        Button cleanSevenDay = view.findViewById(R.id.btn_clean_seven_day);
        Button cleanNot = view.findViewById(R.id.btn_not_clean);
        cleanSixtySixHour.setOnClickListener(this);
        cleanThreeDay.setOnClickListener(this);
        cleanSevenDay.setOnClickListener(this);
        cleanNot.setOnClickListener(this);

        Dialog dialog = getDialog();
        if(dialog != null){
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            switch (v.getId()) {
                case R.id.btn_clean_thirty_six_hour:
                    mListener.onThirtySixHourClick();
                    break;
                case R.id.btn_clean_three_day:
                    mListener.onThreeDayClick();
                    break;
                case R.id.btn_clean_seven_day:
                    mListener.onSevenDayClick();
                    break;
                case R.id.btn_not_clean:
                    mListener.onNotCleanClick();
                    break;
            }
            SelectCleanTimeDialog.this.dismiss();
        }

    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener listener) {
        this.mListener = listener;
    }

    public interface OnDialogButtonClickListener {
        void onThirtySixHourClick();

        void onThreeDayClick();

        void onSevenDayClick();

        void onNotCleanClick();
    }
}
