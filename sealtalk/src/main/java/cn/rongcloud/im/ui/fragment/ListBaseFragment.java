package cn.rongcloud.im.ui.fragment;

import androidx.lifecycle.Observer;

import java.util.List;

import cn.rongcloud.im.ui.adapter.ListWithSideBarBaseAdapter;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.viewmodel.CommonListBaseViewModel;

/**
 *  此 Fragment 是列表展示的半成品。 继承于 {@link ListWithSidebarBaseFragment}。
 *  在 ListWithSidebarBaseFragment 基础上添加了 ViewModel 获取数据并刷新列表的逻辑。
 *
 *  Fragment 中有两个抽象方法。 {@link #getListAdapter()} 是需要子类返回的 Adapter （ ListWithSideBarBaseAdapter 或
 *  继承 ListWithSideBarBaseAdapter）。 另一个方法为 {@link #createViewModel()} . 子类需要实现此方法并返回
 *  {@link CommonListBaseViewModel} 的子类。
 *
 *  可通过继承Fragment 类并实现其抽象方法， 来进行自定义数据获取的逻辑部分和 Adapter 展示部分。
 *
 * @see ListWithSideBarBaseAdapter
 * @see CommonListBaseViewModel
 *
 */
public abstract class ListBaseFragment extends ListWithSidebarBaseFragment {

    private CommonListBaseViewModel viewModel;
    private ListWithSideBarBaseAdapter adapter;

    @Override
    protected void onInitViewModel() {
        viewModel = createViewModel();
        viewModel.getConversationLiveData().observe(this, new Observer<List<ListItemModel>>() {
            @Override
            public void onChanged(List<ListItemModel> contactModels) {
                if (adapter != null) {
                    adapter.updateData(contactModels);
                }
            }
        });
        viewModel.loadData();
    }

    @Override
    protected boolean isUseSideBar() {
        return false;
    }

    @Override
    protected ListWithSideBarBaseAdapter getAdapter() {

        if (adapter == null) {
            adapter = getListAdapter();
        }
        return adapter;
    }

    /**
     * 调用此方法可重新加载数据
     */
    public void reloadData() {
        if (viewModel != null) {
            viewModel.loadData();
        }
    }

    /**
     * 实现数据的Adapter。 可根据自己的需求去实现自己的 adapter
     * @return
     */
    protected abstract ListWithSideBarBaseAdapter getListAdapter();

    /**
     * 创建 viewmodel. 可以进行继承 ConversationListBaseViewModel 并实现获取数据的方法。
     * 来实现自己的数据加载
     * @return
     */
    protected abstract CommonListBaseViewModel createViewModel();

}
