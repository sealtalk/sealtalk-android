package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.task.AppTask;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.widget.adapter.ViewHolder;

/** @author gusd */
public class TranslationLanguageListActivity extends TitleBaseActivity {
    private static final String TAG = "TranslationLanguageListActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_language_list);
        RecyclerView rcLanguage = (RecyclerView) findViewById(R.id.rv_language_list);
        final AppTask appTask = new AppTask(this);
        rcLanguage.setAdapter(
                new RecyclerView.Adapter() {
                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(
                            @NonNull ViewGroup parent, int viewType) {
                        return new ViewHolder(
                                TranslationLanguageListActivity.this,
                                LayoutInflater.from(TranslationLanguageListActivity.this)
                                        .inflate(
                                                R.layout.item_translation_language, parent, false));
                    }

                    @Override
                    public void onBindViewHolder(
                            @NonNull RecyclerView.ViewHolder holder, final int position) {
                        TextView tv = holder.itemView.findViewById(R.id.tv_translation_language);
                        final Pair<String, String> languageItem =
                                TranslationSettingActivity.LANGUAGE_LIST.get(position);
                        tv.setText(languageItem.second);
                        holder.itemView.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String type = getIntent().getStringExtra("type");
                                        if ("src".equals(type)) {
                                            appTask.setTranslationSrcLanguage(languageItem.first);
                                            RongConfigCenter.featureConfig()
                                                            .rc_translation_src_language =
                                                    languageItem.first;
                                        } else {
                                            appTask.setTranslationTargetLanguage(
                                                    languageItem.first);
                                            RongConfigCenter.featureConfig()
                                                            .rc_translation_target_language =
                                                    languageItem.first;
                                        }
                                        finish();
                                    }
                                });
                    }

                    @Override
                    public int getItemCount() {
                        return TranslationSettingActivity.LANGUAGE_LIST.size();
                    }
                });
    }
}
