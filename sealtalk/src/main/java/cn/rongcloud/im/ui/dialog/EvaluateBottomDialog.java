package cn.rongcloud.im.ui.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.EvaluateInfo;
import cn.rongcloud.im.ui.adapter.LablesAdapter;
import cn.rongcloud.im.ui.view.StarsView;
import cn.rongcloud.im.ui.widget.AntGridView;
import cn.rongcloud.im.viewmodel.ConversationViewModel;
import io.rong.imlib.cs.CustomServiceConfig;

public class EvaluateBottomDialog extends BaseBottomDialog implements View.OnClickListener {


    private LinearLayout resolveFeedbackLl;
    private RadioGroup resolveRg;
    private StarsView starsSv;
    private LinearLayout problemsLl;
    private TextView problemTitleTv;
    private AntGridView lablesGv;
    private EditText suggestionEt;
    private Button submitBtn;
    private ConversationViewModel conversationViewModel;
    private LablesAdapter lablesAdapter;
    private EvaluateInfo currentEvaluate;
    private String targetId;
    private String dialogId;
    private OnEvaluateListener onEvaluateListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        targetId = getArguments().getString("targetId");
        dialogId = getArguments().getString("dialogId");
        View view = inflater.inflate(R.layout.dialog_bottom_evaluate, null);
        // 关闭dialog
        view.findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onEvaluateListener != null){
                    onEvaluateListener.onCancel();
                }
                dismiss();
            }
        });

        // 问题是否解决布局
        resolveFeedbackLl = (LinearLayout) view.findViewById(R.id.ll_resolve_feedback);
        resolveRg = (RadioGroup) view.findViewById(R.id.rg_resolve);

        // 评星
        starsSv = (StarsView) view.findViewById(R.id.sv_stars);
        starsSv.init(5);
        starsSv.setOnSelectStatusListener(new StarsView.OnSelectStatusListener() {
            @Override
            public void onSelectStatus(View view, int stars) {
                // 根据选择去改变 lables和问题标签
                if (conversationViewModel != null) {
                    List<EvaluateInfo> values = conversationViewModel.getEvaluateList().getValue();
                    if (stars > 0 && values != null && values.size() > 0) {
                        controlView(stars, values.get(stars - 1));
                    }
                }
            }
        });

        // 问题的标签
        problemsLl = (LinearLayout) view.findViewById(R.id.ll_problems);
        problemTitleTv = (TextView) view.findViewById(R.id.tv_problem_title);
        lablesGv = (AntGridView) view.findViewById(R.id.gv_problem_tables);
        lablesGv.setNumColumns(2);
        lablesAdapter = new LablesAdapter();
        lablesGv.setAdapter(lablesAdapter);

        // 建议
        suggestionEt = (EditText) view.findViewById(R.id.et_suggestion);

        //提交
        view.findViewById(R.id.btn_submit).setOnClickListener(this);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        conversationViewModel = ViewModelProviders.of(getActivity()).get(ConversationViewModel.class);
        conversationViewModel.getEvaluateList().observe(this, new Observer<List<EvaluateInfo>>() {
            @Override
            public void onChanged(List<EvaluateInfo> evaluateInfos) {
                if (evaluateInfos == null || evaluateInfos.size() <= 0) {
                    return;
                }
                controlView(starsSv.getStars(), evaluateInfos.get(0));
            }
        });
    }

    public void setOnEvaluateListener(OnEvaluateListener listener){
        this.onEvaluateListener = listener;
    }

    /**
     * 加载显示标签
     *
     * @param evaluateInfo
     */
    private void controlView(int stars, EvaluateInfo evaluateInfo) {
        this.currentEvaluate = evaluateInfo;
        // 是否显示问题
        boolean showSolveStatus = evaluateInfo.getQuestionFlag();
        if (showSolveStatus) {
            resolveFeedbackLl.setVisibility(View.VISIBLE);
        } else {
            resolveFeedbackLl.setVisibility(View.GONE);
        }

        suggestionEt.setVisibility(View.VISIBLE);

        if (stars < starsSv.getMaxStar()) {
            suggestionEt.setHint(evaluateInfo.getInputLanguage());
            boolean isTagMust = evaluateInfo.getTagMust();
            if (isTagMust) {
                problemTitleTv.setText(getResources().getString(R.string.seal_evaluate_problem_title));
            } else {
                problemTitleTv.setText(getResources().getString(R.string.seal_evaluate_title_select_must));
            }
            lablesGv.setVisibility(View.VISIBLE);
            setProblemLabels(evaluateInfo.getLabelNameList());
        } else {
            lablesGv.setVisibility(View.GONE);
        }

    }

    /**
     * 更具不同的对象，加载不同的 lables
     *
     * @param labelNameList
     */
    private void setProblemLabels(List<String> labelNameList) {
        lablesAdapter.updateList(labelNameList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                if (currentEvaluate == null) {
                    return;
                }
                boolean showSolveStatus = currentEvaluate.getQuestionFlag();
                CustomServiceConfig.CSEvaSolveStatus resolveStatus = CustomServiceConfig.CSEvaSolveStatus.UNRESOLVED;
                if (showSolveStatus && resolveRg.isSelected() && resolveRg.getCheckedRadioButtonId() == R.id.rb_resolved) {
                    resolveStatus = CustomServiceConfig.CSEvaSolveStatus.RESOLVED;
                }

                String seletedLables = lablesAdapter.getSeletedLablesString();
                boolean tagMust = currentEvaluate.getTagMust();
                boolean isInputMust = currentEvaluate.getInputMust();
                if (starsSv.getStars() >= 5) {
                    tagMust = false;
                    isInputMust = false;
                }

                if (tagMust && TextUtils.isEmpty(seletedLables)) {
                    showToast(R.string.seal_evaluate_lable_must);
                    return;
                } else if (isInputMust && TextUtils.isEmpty(suggestionEt.getText().toString())) {
                    showToast(R.string.seal_evaluate_input_must);
                    return;
                }

                submit(targetId, starsSv.getStars(), seletedLables, resolveStatus, suggestionEt.getText().toString(), dialogId);
                break;
            default:
                //Do nothing
                break;
        }
    }

    /**
     * 提交评价
     *
     * @param targetId
     * @param stars
     * @param seletedLables
     * @param resolveStatus
     * @param suggestion
     */
    private void submit(String targetId, int stars, String seletedLables, CustomServiceConfig.CSEvaSolveStatus resolveStatus, String suggestion, String dialogId) {
        if (conversationViewModel != null) {
            conversationViewModel.submitEvaluate(targetId, stars, seletedLables, resolveStatus, suggestion, dialogId);
        }
        if(onEvaluateListener != null){
            onEvaluateListener.onSubmitted();
        }
    }


    public static class Builder {
        String targetId;
        String dialogId;

        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }

        public void setDialogId(String dialogId) {
            this.dialogId = dialogId;
        }

        public EvaluateBottomDialog build() {
            EvaluateBottomDialog dialog = getCurrentDialog();
            Bundle bundle = new Bundle();
            bundle.putString("targetId", targetId);
            bundle.putString("dialogId", dialogId);
            dialog.setArguments(bundle);
            return dialog;
        }


        protected EvaluateBottomDialog getCurrentDialog() {
            return new EvaluateBottomDialog();
        }
    }


    private void showToast(int resId) {
        showToast(getString(resId));
    }

    private void showToast(String str) {
        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
    }

    public interface OnEvaluateListener {
        void onCancel();

        void onSubmitted();
    }
}
