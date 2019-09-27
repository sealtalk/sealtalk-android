package cn.rongcloud.im.ui.fragment;

public class PublicServiceSearchFragment extends PublicServiceFragment {

    @Override
    protected void onLoad() {
        //不加载本地资源
    }

    public void search(String match) {
        viewModel.searchPublicServices(match);
    }


}
