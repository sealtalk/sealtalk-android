package cn.rongcloud.im.ui.fragment;

import android.view.View;

import java.util.List;

import cn.rongcloud.im.ui.adapter.CommonListAdapter;
import cn.rongcloud.im.ui.adapter.ListWithSideBarBaseAdapter;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.viewmodel.CommonListBaseViewModel;


/**
 * 此类是基础列表的基类， 继承于 {@link ListBaseFragment }。 结合 {@link CommonListAdapter} 和 {@link CommonListBaseViewModel}
 * 类实现主要的展现逻辑。
 * 只需继承此类， 按照其规则进行返回所需的 ViewModel， 即可快速的实现列表的展示。
 *
 * 使用示例：
 * <code>
 * public class XXXXListFragment extends CommonListBaseFragment {
 *
 *     @Override
 *     protected CommonListBaseViewModel createViewModel() {
 *         boolean isSelect = getArguments().getBoolean(IntentExtra.IS_SELECT, false);
 *         return ViewModelProviders.of(this).get(XXXListViewModel.class);
 *     }
 * }
 *
 * </code>
 *
 *
 * 可通过使用 {@link #setOnItemClickListener(CommonListAdapter.OnItemClickListener)}
 * 来进行监听 item 的点击事件。
 *
 * 加入想展示使用或关闭 SideBar 侧边栏功能， 则可复写 {@link #isUseSideBar()} 方法进行设置。
 *
 * @see CommonListAdapter
 * @see CommonListBaseViewModel
 * @see ListBaseFragment
 *
 */
public abstract class CommonListBaseFragment extends ListBaseFragment {

    private CommonListAdapter listAdapter;
    private CommonListAdapter.OnItemClickListener listener;
    private CommonListAdapter.OnItemLongClickListener longClickListener;

    @Override
    protected ListWithSideBarBaseAdapter getListAdapter() {

        if (listAdapter == null) {
             createAdapter();
        }
        return listAdapter;
    }


    /**
     * 设置功能item 点击项
     * @param listener
     */
    public void setOnItemClickListener(CommonListAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 设置 item 长按点击事件
     * @param listener
     */
    public void setOnItemLongClickListener(CommonListAdapter.OnItemLongClickListener listener){
        this.longClickListener = listener;
    }
    /**
     * 设置同步已经选择的人
     * @param selectGroupIds
     * @param selectFriendIds
     */
    public void setSelectedIds(List<String> selectGroupIds, List<String> selectFriendIds) {
        if (listAdapter == null) {
            createAdapter();
            listAdapter.setSelected(selectGroupIds, selectFriendIds);
        } else {
            listAdapter.setSelected(selectGroupIds, selectFriendIds);
            reloadData();
        }
    }

    /**
     * 创建 Adapter
     */
    private void createAdapter() {
        listAdapter = new CommonListAdapter();
        listAdapter.setOnItemClickListener(new CommonListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position, ListItemModel data) {
                if (listener != null) {
                    listener.onClick(v, position, data);
                }
            }
        });
        listAdapter.setOnItemLongClickListener(new CommonListAdapter.OnItemLongClickListener() {
            @Override
            public boolean onLongClick(View v, int position, ListItemModel data) {
                if (longClickListener != null){
                    return longClickListener.onLongClick(v, position, data);
                }
                return false;
            }
        });
    }

}
