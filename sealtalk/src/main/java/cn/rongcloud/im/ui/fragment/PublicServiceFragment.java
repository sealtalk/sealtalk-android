package cn.rongcloud.im.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.PublicServiceAdapter;
import cn.rongcloud.im.ui.adapter.models.ContactModel;
import cn.rongcloud.im.ui.interfaces.PublicServiceClickListener;
import cn.rongcloud.im.viewmodel.PublicServiceViewModel;

public class PublicServiceFragment extends Fragment {
    private RecyclerView recyclerView;
    protected PublicServiceViewModel viewModel;
    private PublicServiceAdapter adapter;

    public void setOnPublicServiceClickListener(PublicServiceClickListener listener){
        adapter = new PublicServiceAdapter(listener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_public_searvice_content, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        viewModel = ViewModelProviders.of(this).get(PublicServiceViewModel.class);
        viewModel.getPublicService().observe(getViewLifecycleOwner(), new Observer<List<ContactModel>>() {
            @Override
            public void onChanged(List<ContactModel> models) {
                adapter.updateData(models);
            }
        });
        onLoad();
        return view;
    }

    protected void onLoad() {
        viewModel.loadPublicServices();
    }
}
