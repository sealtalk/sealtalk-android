package cn.rongcloud.im.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.SealCSEvaluateItem;
import io.rong.imlib.CustomServiceConfig;


/**
 * [底部弹出dialog]
 **/
public class BottomEvaluateDialog extends Dialog {
    private RelativeLayout layout_resolve_feedback;
    private ImageView iv_cancel;
    private TextView tv_submit;
    private OnEvaluateDialogBehaviorListener onEvaluateDialogBehaviorListener;
    private List<SealCSEvaluateItem> evaluateList;
    private int mStars;
    private TextView tv_resolved;
    private TextView tv_unresolved;
    private RelativeLayout layout_problem_labels;
    private RelativeLayout layout_problems;
    private TextView problems_title;
    private EditText editTextSuggestion;
    private CSEvaluateScrollView evaluateScrollView;
    private Context mContext;
    private boolean isTagMust = false;
    private boolean isInputMust = false;
    private boolean showSolveStatus = false;

    /**
     * @param context
     */
    public BottomEvaluateDialog(Context context, List<SealCSEvaluateItem> evaluateItems) {
        super(context, R.style.dialogFullscreen);
        evaluateList = evaluateItems;
        mContext = context;
    }

    /**
     * @param context
     * @param theme
     */
    public BottomEvaluateDialog(Context context, int theme) {
        super(context, theme);
    }

    /**
     * @param context
     */
    public BottomEvaluateDialog(Context context, String confirmText, String middleText) {
        super(context, R.style.dialogFullscreen);
    }

    /**
     * @param context
     */
    public BottomEvaluateDialog(Context context, String confirmText, String middleText, String cancelText) {
        super(context, R.style.dialogFullscreen);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_evaluate_dialog);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.5f;
        window.setGravity(Gravity.BOTTOM);
        window.setAttributes(layoutParams);

        window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        initViews();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private void initViews() {
        initTitle();
        initScrollView();
        initResolveFeedback();
        initStarts();
        initProblemLabels();
        initSuggestionView();
        initSubmitView();
    }

    private void initScrollView() {
        evaluateScrollView = (CSEvaluateScrollView) findViewById(R.id.scrollview_evaluate);
    }

    private void initProblemLabels() {
        layout_problems = (RelativeLayout) findViewById(R.id.ll_problems);
        layout_problems.setVisibility(View.GONE);
        problems_title = (TextView) findViewById(R.id.tv_problem_title);
        layout_problem_labels = (RelativeLayout) findViewById(R.id.ll_problem_tables);
        for (int i = 0; i < layout_problem_labels.getChildCount(); i++) {
            View child = layout_problem_labels.getChildAt(i);
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!v.isSelected()) {
                        v.setSelected(true);
                    } else {
                        v.setSelected(false);
                    }
                }
            });
        }
    }

    private void setProblemLabels(List<String> problemLabels) {
        if (problemLabels != null && problemLabels.size() > 0
                && problemLabels.size() <= layout_problem_labels.getChildCount()) {
            layout_problems.setVisibility(View.VISIBLE);
            for (int i = 0; i < layout_problem_labels.getChildCount(); i++) {
                View child = layout_problem_labels.getChildAt(i);
                if (i < problemLabels.size()) {
                    child.setVisibility(View.VISIBLE);
                    child.setSelected(false);
                    ((TextView) child).setText(problemLabels.get(i));
                } else {
                    child.setVisibility(View.GONE);
                }
            }
        }
    }

    private void initSuggestionView() {
        editTextSuggestion = (EditText) findViewById(R.id.edit_suggestion);
        editTextSuggestion.setVisibility(View.GONE);
        editTextSuggestion.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    evaluateScrollView.requestDisallowInterceptTouchEvent(false);
                } else {
                    evaluateScrollView.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });
    }

    private void initStarts() {
        int stars = 5;
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.cs_stars);
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View child = linearLayout.getChildAt(i);
            if (i < stars) {
                child.setSelected(true);
            }
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = linearLayout.indexOfChild(v);
                    int count = linearLayout.getChildCount();
                    mStars = index + 1;
                    if (!v.isSelected()) {
                        while (index >= 0) {
                            linearLayout.getChildAt(index).setSelected(true);
                            index--;
                        }
                    } else {
                        index++;
                        while (index < count) {
                            linearLayout.getChildAt(index).setSelected(false);
                            index++;
                        }
                    }
                    if (mStars < 5 && evaluateList != null && evaluateList.size() > 0) {
                        editTextSuggestion.setVisibility(View.VISIBLE);
                        editTextSuggestion.setHint(evaluateList.get(mStars - 1).getInputLanguage());
                        isTagMust = evaluateList.get(mStars - 1).getTagMust();
                        setProblemTitle(isTagMust);
                        setProblemLabels(evaluateList.get(mStars - 1).getLabelNameList());
                        isInputMust = evaluateList.get(mStars - 1).getInputMust();
                    } else {
                        layout_problems.setVisibility(View.GONE);
                        hintKbTwo();
                        editTextSuggestion.setVisibility(View.GONE);
                        isTagMust = false;
                        isInputMust = false;
                    }
                }
            });
        }
    }

    private void setProblemTitle(boolean isTagMust) {
        String title;
        title = mContext.getResources().getString(R.string.cs_evaluate_problem_title);

        if (isTagMust) {
            problems_title.setText(title + "(" + mContext.getResources().getString(R.string.cs_evaluate_select_must) + ")");
        } else {
            problems_title.setText(title);
        }
    }

    private void initTitle() {
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onEvaluateDialogBehaviorListener != null) {
                    onEvaluateDialogBehaviorListener.onCancel();
                }
            }
        });
    }

    private void initResolveFeedback() {
        layout_resolve_feedback = (RelativeLayout) findViewById(R.id.ll_resolve_feedback);
        if (evaluateList != null) {
            showSolveStatus = evaluateList.get(0).getQuestionFlag();
        }
        if (showSolveStatus) {
            layout_resolve_feedback.setVisibility(View.VISIBLE);
        } else {
            layout_resolve_feedback.setVisibility(View.GONE);
        }
        tv_resolved = (TextView) findViewById(R.id.tv_resolved);
        tv_resolved.setSelected(true);
        tv_resolved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_resolved.setSelected(true);
                tv_unresolved.setSelected(false);
            }
        });
        tv_unresolved = (TextView) findViewById(R.id.tv_unresolved);
        tv_unresolved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_resolved.setSelected(false);
                tv_unresolved.setSelected(true);
            }
        });
    }

    private void initSubmitView() {
        tv_submit = (TextView) findViewById(R.id.tv_submit);
        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomServiceConfig.CSEvaSolveStatus resolveStatus;
                String tablets = "";
                if (tv_resolved.isSelected() && showSolveStatus) {
                    resolveStatus = CustomServiceConfig.CSEvaSolveStatus.RESOLVED;
                } else {
                    resolveStatus = CustomServiceConfig.CSEvaSolveStatus.UNRESOLVED;
                }

                for (int i = 0; i < layout_problem_labels.getChildCount(); i++) {
                    View child = layout_problem_labels.getChildAt(i);
                    if (child.isSelected()) {
                        if (TextUtils.isEmpty(tablets)) {
                            tablets += ((TextView) child).getText().toString();
                        } else {
                            tablets += ("," + ((TextView) child).getText().toString());
                        }
                    }
                }
                if (isTagMust && TextUtils.isEmpty(tablets)) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.cs_evaluate_tag_must), Toast.LENGTH_SHORT).show();
                    return;
                } else if (isInputMust && TextUtils.isEmpty(editTextSuggestion.getText().toString())) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.cs_evaluate_input_must), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (onEvaluateDialogBehaviorListener != null) {
                    onEvaluateDialogBehaviorListener.onSubmit(mStars, tablets, resolveStatus, editTextSuggestion.getText().toString());
                }
            }
        });
    }

    /**
     * 评价对话窗口监听
     */
    public interface OnEvaluateDialogBehaviorListener {
        void onSubmit(int source, String tablets, CustomServiceConfig.CSEvaSolveStatus resolveStatus, String suggestion);

        void onCancel();
    }

    public void setEvaluateDialogBehaviorListener(OnEvaluateDialogBehaviorListener listener) {
        onEvaluateDialogBehaviorListener = listener;
    }

    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (onEvaluateDialogBehaviorListener != null) {
            onEvaluateDialogBehaviorListener.onCancel();
        }
    }
}
