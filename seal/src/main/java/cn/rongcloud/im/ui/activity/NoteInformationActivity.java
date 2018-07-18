package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.response.SetFriendDisplayNameResponse;
import cn.rongcloud.im.server.widget.LoadDialog;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;

/**
 * Created by AMing on 16/8/10.
 * Company RongCloud
 */
@SuppressWarnings("deprecation")
public class NoteInformationActivity extends BaseActivity {

    private static final int SET_DISPLAYNAME = 12;
    private Friend mFriend;
    private EditText mNoteEdit;
    private TextView mNoteSave;
    private static final int CLICK_CONTACT_FRAGMENT_FRIEND = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noteinfo);
        setHeadVisibility(View.GONE);
        mNoteEdit = (EditText) findViewById(R.id.notetext);
        mNoteSave = (TextView) findViewById(R.id.notesave);
        mFriend = getIntent().getParcelableExtra("friend");
        if (mFriend != null) {
            mNoteSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoadDialog.show(mContext);
                    request(SET_DISPLAYNAME);
                }
            });
            mNoteSave.setClickable(false);
            mNoteEdit.setText(mFriend.getDisplayName());
            mNoteEdit.setSelection(mNoteEdit.getText().length());
            mNoteEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!TextUtils.isEmpty(mFriend.getDisplayName())) {
                        mNoteSave.setClickable(true);
                        mNoteSave.setTextColor(getResources().getColor(R.color.white));
                    } else {
                        if (TextUtils.isEmpty(s.toString())) {
                            mNoteSave.setClickable(false);
                            mNoteSave.setTextColor(Color.parseColor("#9fcdfd"));
                        } else if (s.toString().equals(mFriend.getDisplayName())) {
                            mNoteSave.setClickable(false);
                            mNoteSave.setTextColor(Color.parseColor("#9fcdfd"));
                        } else {
                            mNoteSave.setClickable(true);
                            mNoteSave.setTextColor(getResources().getColor(R.color.white));
                        }
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


        }
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        if (requestCode == SET_DISPLAYNAME) {
            return action.setFriendDisplayName(mFriend.getUserId(), mNoteEdit.getText().toString().trim());
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            if (requestCode == SET_DISPLAYNAME) {
                SetFriendDisplayNameResponse response = (SetFriendDisplayNameResponse) result;
                if (response.getCode() == 200) {
                    String displayName = mNoteEdit.getText().toString();
                    if(displayName != null){
                        displayName = displayName.trim();
                    }
                    SealUserInfoManager.getInstance().addFriend(
                        new Friend(mFriend.getUserId(),
                                   mFriend.getName(),
                                   mFriend.getPortraitUri(),
                                   displayName,
                                   null, null,
                                   mFriend.getStatus(),
                                   mFriend.getTimestamp(),
                                   CharacterParser.getInstance().getSpelling(mFriend.getName()),
                                   CharacterParser.getInstance().getSpelling(displayName)));
                    if (TextUtils.isEmpty(displayName)) {
                        RongIM.getInstance().refreshUserInfoCache(new UserInfo(mFriend.getUserId(), mFriend.getName(), mFriend.getPortraitUri()));
                    } else {
                        RongIM.getInstance().refreshUserInfoCache(new UserInfo(mFriend.getUserId(), displayName, mFriend.getPortraitUri()));
                    }
                    BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_FRIEND);
                    Intent intent = new Intent(mContext, UserDetailActivity.class);
                    intent.putExtra("type", CLICK_CONTACT_FRAGMENT_FRIEND);
                    intent.putExtra("displayName", mNoteEdit.getText().toString().trim());
                    setResult(155, intent);
                    LoadDialog.dismiss(mContext);
                    finish();
                }
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        if (requestCode == SET_DISPLAYNAME) {
            LoadDialog.dismiss(mContext);
        }
        super.onFailure(requestCode, state, result);
    }

    public void finishPage(View view) {
        this.finish();
    }
}
