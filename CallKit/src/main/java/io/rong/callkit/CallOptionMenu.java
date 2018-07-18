package io.rong.callkit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * Created by mamingyang on 2018/3/19.
 */

public class CallOptionMenu extends PopupWindow {
    private View.OnClickListener onItemClickListener;
    private LinearLayout layoutAdd;
    private LinearLayout layoutWhiteBoard;
    private LinearLayout layoutHandUp;

    public CallOptionMenu(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.rc_voip_pop_menu, null);
        setContentView(content);
        setWidth(LayoutParams.WRAP_CONTENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        layoutAdd = (LinearLayout) content.findViewById(R.id.voipItemAdd);
        layoutAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) onItemClickListener.onClick(v);
            }
        });
        layoutWhiteBoard = (LinearLayout) content.findViewById(R.id.voipItemWhiteboard);
        layoutWhiteBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) onItemClickListener.onClick(v);
            }
        });

        layoutHandUp = (LinearLayout) content.findViewById(R.id.voipItemHandup);
        layoutHandUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) onItemClickListener.onClick(v);
            }
        });

        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.rc_voip_menu_bg));
        setOutsideTouchable(true);
        setFocusable(true);
    }

    public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setHandUpvisibility(boolean isSeen) {
        if (layoutHandUp != null) {
            if (!isSeen)
                layoutHandUp.setVisibility(View.GONE);
            else {
                layoutHandUp.setVisibility(View.VISIBLE);
            }
        }
    }
}
