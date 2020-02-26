package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.ui.adapter.CommonListAdapter;
import cn.rongcloud.im.ui.adapter.models.FunctionInfo;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.ui.adapter.viewholders.CommonGroupItemViewHolder;
import cn.rongcloud.im.ui.adapter.viewholders.CommonFriendItemViewHolder;
import cn.rongcloud.im.ui.adapter.viewholders.CommonFunItemViewHolder;
import cn.rongcloud.im.ui.adapter.viewholders.CommonTextItemViewHolder;
import cn.rongcloud.im.ui.adapter.viewholders.ViewHolderFactory;
import cn.rongcloud.im.ui.fragment.CommonListBaseFragment;
import cn.rongcloud.im.utils.CharacterParser;


/**
 * 配合 {@link CommonListBaseFragment} 使用。
 * 此类定义了基础的数据获取和监听熟悉的逻辑规则。
 * <p>
 * 此类和 {@link CommonListBaseFragment}、{@link CommonListAdapter}
 * 相互配合使用。以达到快速的实现自定义数据的列表功能展示。
 * <p>
 * {@link #loadData()} 为数据记载方法。 此方法会在{@link cn.rongcloud.im.ui.fragment.ListBaseFragment} 中
 * 主动调用。继承并实现 {@link #loadData()} 方法。 在 loadData（） 方法中实现数据的获取逻辑， 即可实现数据自定义。
 * <p>
 * 加入您获取数据源时需要监听 LiveData ， 则您可使用 <code> conversationLiveData </code> 变量进行一下操作。
 * <code>
 * conversationLiveData.addSource(xxxLiveData, new Observer<Resource<UserInfo>>() {
 *
 * @Override public void onChanged(Resource<UserInfo> resource) {
 * <p>
 * }
 * });
 *
 * </code>
 * <p>
 * 通过上面的代码的方式可进对原数据原进行监听并处理。
 * <p>
 * 此类中的数据类型为 {@link ListItemModel} 。 是为了适配多种数据源。
 * <p>
 * 当获取到原数据后， 可使用 {@link ModelBuilder} 类进行对数据添加、转换、添加首字母以及通知下发数据的处理。
 * <code>
 * <p>
 * ModelBuilder builder = new ModelBuilder();
 * builder.addFriendList(List<FriendShipInfo>);
 * builder.buildFirstChar();
 * builder.addFriend(0, FriendShipInfo);
 * builder.addModelList(0, List<ListItemModel>);
 * builder.post();
 *
 * </code>
 * <p>
 * 通过上面代码即可进行数据处理和数据下发展示。
 * <p>
 * 此类中已经兼容 {@link FriendShipInfo} 和 {@link GroupEntity} 两个类型的默认转换。 只需调用
 * <p>
 * ModelBuilder builder = new ModelBuilder();
 * builder.addFriendList(List<FriendShipInfo>) 或
 * builder.addGroupList(List<GroupEntity) 等方法进行设置即可。
 * <p>
 * 同时也支持其他类型自己进行封装。 类中也通过了其他类型转换的成 ListItemModel 的方法。
 * {@link #createGroupModel(GroupEntity)}
 * {@link #createFriendModel(FriendShipInfo)}
 * {@link #createFunModel(FunctionInfo)} 或 {@link #createFunModel(String, String)}
 * {@link #createTextModel(String)}
 * <p>
 * 上面这四种类型类中已经默认实现了。
 * 当需要丰富或更改填充数据时， 可在子类中进行复写即可， 如下
 *
 * <code>
 * @Override protected ListItemModel createGroupModel(GroupEntity entity) {
 * ListItemModel model = super.createGroupModel(entity);
 * // 修改增添的数据
 * model.setCheckStatus(ListItemModel.CheckStatus.UNCHECKED);
 * return model;
 * }
 * </code>
 * <p>
 * 也可单独实现自己进行调用。
 * 如果有其他的新加类型， 则可使用自己进行封装转换成 ListItemModel。然后使用 builder.addModelList(model)
 * 或 builder.addMode（model）进行添加。
 * <p>
 * ListItemModel 封装转换需注意的是， 必须要添加 id，name ， itemView 字段。
 * 如果想在列表中显示复选按钮， 则需要设置 CheckStatus 字段不为 Node。
 * <p>
 * builder.buildFirstChar(); 方法是把当前设置好的集合生成首字母。用于 Side 的侧栏查询。
 * @see CommonListBaseFragment
 * @see CommonListAdapter
 * @see ModelBuilder
 * @see ListItemModel
 */
public abstract class CommonListBaseViewModel extends AppViewModel {
    private static final String TAG = "ConstactListBaseViewModel";
    protected MediatorLiveData<List<ListItemModel>> conversationLiveData;

    public CommonListBaseViewModel(@NonNull Application application) {
        super(application);
        conversationLiveData = new MediatorLiveData<>();
        onCreate(conversationLiveData);
    }


    /**
     * 群组类型转化为展示数据
     *
     * @param groupEntities
     * @return
     */
    private List<ListItemModel> convertGroups(List<GroupEntity> groupEntities) {
        List<ListItemModel> out = new ArrayList<>();
        for (GroupEntity groupEntity : groupEntities) {
            ListItemModel model = createGroupModel(groupEntity);
            out.add(model);
        }
        return out;
    }

    /**
     * 还有类型转化为展示数据
     *
     * @param friendShipInfos
     * @return
     */
    private List<ListItemModel> convertFriends(List<FriendShipInfo> friendShipInfos) {
        List<ListItemModel> out = new ArrayList<>();
        for (FriendShipInfo info : friendShipInfos) {
            out.add(createFriendModel(info));
        }
        return out;
    }

    /**
     * 好友首字母排序
     *
     * @param models
     */
    private void sortByFirstChar(List<ListItemModel> models) {
//        Collections.sort(models, new Comparator<ListItemModel>() {
//            @Override
//            public int compare(ListItemModel lhs, ListItemModel rhs) {
//                if (lhs.getItemView().getType() == ListItemModel.ItemView.Type.FUN || lhs.getItemView().getType() == ListItemModel.ItemView.Type.TEXT) {
//                    return -1;
//                } else if (rhs.getItemView().getType() == ListItemModel.ItemView.Type.FUN || rhs.getItemView().getType() == ListItemModel.ItemView.Type.TEXT) {
//                    return 1;
//                } else {
//                    if (TextUtils.isEmpty(lhs.getFirstChar())) {
//                        return -1;
//                    }
//                    if (TextUtils.isEmpty(rhs.getFirstChar())) {
//                        return 1;
//                    }
//                    return lhs.getFirstChar().compareTo(rhs.getFirstChar());
//                }
//            }
//        });
        List<ListItemModel> tempModels = new ArrayList<>();
        tempModels.addAll(models);
        for (int i = 0; i < tempModels.size(); i++) {
            String firstChar = tempModels.get(i).getFirstChar();
            if (!TextUtils.isEmpty(firstChar)) {
                if (!firstChar.substring(0, 1).matches("^[A-Za-z]")) {
                    models.add(models.remove(models.indexOf(tempModels.get(i))));
                }
            }
        }
    }


    /**
     * 创建Group 数据对象
     *
     * @param entity
     * @return
     */
    protected ListItemModel createGroupModel(GroupEntity entity) {
        ListItemModel.ItemView itemView = new ListItemModel
                .ItemView(R.layout.item_common_group, ListItemModel.ItemView.Type.GROUP, CommonGroupItemViewHolder.class);
        ListItemModel<GroupEntity> model = new ListItemModel<>(entity.getId(), entity.getName(), entity, itemView);
        model.setPortraitUrl(entity.getPortraitUri());
        model.setFirstChar(entity.getNameSpelling());
        return model;
    }

    /**
     * 创建联系人对象.
     *
     * @param info
     * @return
     */
    protected ListItemModel createFriendModel(FriendShipInfo info) {
        String name = TextUtils.isEmpty(info.getDisplayName()) ? info.getUser().getNickname() : info.getDisplayName();
        ListItemModel.ItemView itemView = new ListItemModel
                .ItemView(R.layout.item_common_conversation, ListItemModel.ItemView.Type.FRIEND, CommonFriendItemViewHolder.class);
        ListItemModel<FriendShipInfo> model = new ListItemModel<>(info.getUser().getId(), name, info, itemView);
        model.setPortraitUrl(info.getUser().getPortraitUri());
        model.setFirstChar(info.getUser().getFirstCharacter());
        return model;
    }

    /**
     * 创建文本的model
     *
     * @param name
     * @return
     */
    protected ListItemModel createTextModel(String name) {
        ListItemModel.ItemView itemView = new ListItemModel
                .ItemView(R.layout.item_common_text, ListItemModel.ItemView.Type.TEXT, CommonTextItemViewHolder.class);
        ListItemModel model = new ListItemModel("", name, name, itemView);
        return model;
    }

    /**
     * 创建功能的数据模型
     *
     * @param funContent
     * @return
     */
    protected ListItemModel createFunModel(String funId, String funContent) {
        ListItemModel.ItemView itemView = new ListItemModel
                .ItemView(R.layout.item_common_fun, ListItemModel.ItemView.Type.FUN, CommonFunItemViewHolder.class);
        ListItemModel model = new ListItemModel(funId, funContent, new FunctionInfo(funId, funContent), itemView);
        return model;
    }

    /**
     * 创建功能的数据模型
     *
     * @param info
     * @return
     */
    protected ListItemModel createFunModel(FunctionInfo info) {
        ListItemModel.ItemView itemView = new ListItemModel
                .ItemView(R.layout.item_common_fun, ListItemModel.ItemView.Type.FUN, CommonFunItemViewHolder.class);
        ListItemModel model = new ListItemModel(info.getId(), info.getName(), info, itemView);
        return model;
    }


    /**
     * 处理首字母
     *
     * @param models
     * @return
     */
    private List<ListItemModel> handleFirstChar(List<ListItemModel> models) {
        if (models == null) {
            return null;
        }
        List<ListItemModel> out = new ArrayList<>();
        String temp = "";
        sortByFirstChar(models);

        for (ListItemModel model : models) {
            ListItemModel.ItemView.Type type = model.getItemView().getType();
            if (type == ListItemModel.ItemView.Type.GROUP
                    || type == ListItemModel.ItemView.Type.FRIEND
                    || type == ListItemModel.ItemView.Type.OTHER) {
                String c = "";
                if (model.getFirstChar() != null && model.getFirstChar().length() > 0) {
                    if (!model.getFirstChar().substring(0, 1).matches("^[A-Za-z]")) {
                        c = "#";
                    } else {
                        c = model.getFirstChar().substring(0, 1);
                    }
                }
                if (TextUtils.isEmpty(c)) {
                    out.add(createTextModel("#"));
                    temp = "#";
                } else if (!temp.equals(c)) {
                    out.add(createTextModel(c));
                    temp = c;
                }
            }
            out.add(model);
        }

//        sortByFirstChar(out);
        return out;
    }


    /**
     * 获取会话数据列表
     *
     * @return
     */
    public LiveData<List<ListItemModel>> getConversationLiveData() {
        return conversationLiveData;
    }

    /**
     * 加载数据， 在此方法中实现加载数据的请求， 并调用 convertAndPost  方法转换成展示类型并
     * LiveData 通知出去
     */
    public abstract void loadData();

    /**
     * ViewModel 创建
     *
     * @param conversationLiveData
     */
    protected void onCreate(MediatorLiveData<List<ListItemModel>> conversationLiveData) {

    }


    /**
     * 这里自动去添加 view 和 ViewHolder 的映射关系， 用于在
     * {@link CommonListAdapter} 中进行自动创建View 对应的 ViewHolder
     *
     * @param out
     */
    private void initViewHzolder(List<ListItemModel> out) {
        for (ListItemModel model : out) {
            final ListItemModel.ItemView itemView = model.getItemView();
            ViewHolderFactory.getInstance().putViewHolder(itemView.getItemResId(), itemView.getViewHolder());
        }
    }


    /**
     * 数据构建对象。 通过对象可添加数据并转成展示数据类型。
     * 通过 ModelBuilder#post（） 方法则可通知数据下发。
     *
     * @return
     */
    public ModelBuilder builderModel() {
        return new ModelBuilder();
    }


    /**
     * 此类用于在获取数据源后对数据源进行封装并下发。
     * 当前支持 FriendShipInfo 和 GroupEntity 类型的装换。
     * 其他类型可自己进行转换。
     * 其中还有对以添加数据进行 Side 列表的排序构造的 {@link #buildFirstChar()}
     * 方法。
     * 处理添加完数据后， 调用 {@link #post()} 方法即可。
     */
    public class ModelBuilder {
        List<ListItemModel> out;

        public ModelBuilder() {
            out = new ArrayList<>();
        }

        /**
         * 添加好友的对象
         *
         * @param info
         */
        public ModelBuilder addFriend(FriendShipInfo info) {
            out.add(createFriendModel(info));
            return this;
        }

        /**
         * 添加好友的对象
         *
         * @param info
         */
        public ModelBuilder addFriend(int index, FriendShipInfo info) {
            out.add(index, createFriendModel(info));
            return this;
        }

        /**
         * 添加群对象
         *
         * @param groupEntity
         */
        public ModelBuilder addGroup(GroupEntity groupEntity) {
            out.add(createGroupModel(groupEntity));
            return this;
        }

        /**
         * 添加群对象
         *
         * @param groupEntity
         */
        public ModelBuilder addGroup(int index, GroupEntity groupEntity) {
            out.add(index, createGroupModel(groupEntity));
            return this;
        }


        /**
         * 添加好友对象列表
         *
         * @param infos
         */
        public ModelBuilder addFriendList(List<FriendShipInfo> infos) {
            out.addAll(convertFriends(infos));
            return this;
        }

        /**
         * 添加好友对象列表
         *
         * @param infos
         */
        public ModelBuilder addFriendList(int index, List<FriendShipInfo> infos) {
            out.addAll(index, convertFriends(infos));
            return this;
        }

        /**
         * 添加群对象列表
         *
         * @param groups
         */
        public ModelBuilder addGroupList(List<GroupEntity> groups) {
            out.addAll(convertGroups(groups));
            return this;
        }

        /**
         * 添加群对象列表
         *
         * @param groups
         */
        public ModelBuilder addGroupList(int index, List<GroupEntity> groups) {
            out.addAll(index, convertGroups(groups));
            return this;
        }

        /**
         * 添加对象
         *
         * @param data
         */
        public ModelBuilder addModel(ListItemModel data) {
            out.add(data);
            return this;
        }

        /**
         * 添加对象
         *
         * @param data
         */
        public ModelBuilder addModel(int index, ListItemModel data) {
            out.add(index, data);
            return this;
        }

        /**
         * 添加对象列表
         *
         * @param datas
         */
        public ModelBuilder addModelList(List<ListItemModel> datas) {
            out.addAll(datas);
            return this;
        }

        /**
         * 添加对象列表
         *
         * @param datas
         */
        public ModelBuilder addModelList(int index, List<ListItemModel> datas) {
            out.addAll(index, datas);
            return this;
        }

        /**
         * 对当前已经添加的数据集合进行操作，生成fisrtChar
         */
        public ModelBuilder buildFirstChar() {
            out = handleFirstChar(out);
            return this;
        }

        /**
         * 清除选择状态
         */
        public void clearCheckedState() {
            if (out == null) return;
            for (ListItemModel itemModel : out) {
                itemModel.setCheckStatus(ListItemModel.CheckStatus.NONE);
            }
        }


        /**
         * 通知下发数据
         */
        public void post() {
            /**
             * 这里自动去添加 view 和 ViewHolder 的映射关系， 用于在
             * {@link CommonListAdapter} 中进行自动创建View 对应的 ViewHolder
             */
            initViewHzolder(out);
            conversationLiveData.postValue(out);
        }

    }

}
