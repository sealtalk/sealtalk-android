package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.utils.DataCenter;
import cn.rongcloud.im.utils.DataCenterImpl;

/** @author gusd @Date 2022/03/29 */
public class SelectDataCenterActivity extends TitleBaseActivity {
    private static final String TAG = "SelectDataCenterActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_data_center);
        RecyclerView dataCenterList = (RecyclerView) findViewById(R.id.rv_data_center_list);
        dataCenterList.setAdapter(
                new RecyclerView.Adapter<DataCenterViewHolder>() {
                    @NonNull
                    @Override
                    public DataCenterViewHolder onCreateViewHolder(
                            @NonNull ViewGroup parent, int viewType) {
                        View view =
                                LayoutInflater.from(SelectDataCenterActivity.this)
                                        .inflate(R.layout.layout_data_center_item, parent, false);
                        return new DataCenterViewHolder(view);
                    }

                    @Override
                    public void onBindViewHolder(
                            @NonNull DataCenterViewHolder holder, int position) {
                        DataCenter value = DataCenterImpl.values()[position];
                        holder.dataCenter.setText(value.getNameId());
                        holder.itemView.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent();
                                        intent.putExtra("code", value.getCode());
                                        setResult(AppCompatActivity.RESULT_OK, intent);
                                        finish();
                                    }
                                });
                    }

                    @Override
                    public int getItemCount() {
                        return DataCenterImpl.values().length;
                    }
                });
    }

    private static class DataCenterViewHolder extends RecyclerView.ViewHolder {

        public final TextView dataCenter;

        public DataCenterViewHolder(@NonNull View itemView) {
            super(itemView);
            dataCenter = itemView.findViewById(R.id.item_data_center);
        }
    }
}
