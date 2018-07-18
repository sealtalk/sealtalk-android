package cn.rongcloud.im.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import io.rong.common.ParcelUtils;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.SearchConversationResult;

/**
 * Created by tiankui on 2/7/17.
 */

public class SealSearchConversationResult extends SearchConversationResult implements Parcelable {

    private ArrayList<Message> mMessageArrayList;
    private String id;
    private String title;
    private String portraitUri;

    private Conversation mConversation;
    private int mMatchCount;

    public SealSearchConversationResult() {
    }

    public String getPortraitUri() {
        return portraitUri;
    }

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Message> getMessageArrayList() {
        return mMessageArrayList;
    }

    public void setMessageArrayList(ArrayList<Message> mMessageArrayList) {
        this.mMessageArrayList = mMessageArrayList;
    }

    public Conversation getConversation() {
        return mConversation;
    }

    public void setConversation(Conversation mConversation) {
        this.mConversation = mConversation;
    }

    public int getMatchCount() {
        return mMatchCount;
    }

    public void setMatchCount(int mMatchCount) {
        this.mMatchCount = mMatchCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public SealSearchConversationResult(Parcel in) {
        setId(ParcelUtils.readFromParcel(in));
        setTitle(ParcelUtils.readFromParcel(in));
        setPortraitUri(ParcelUtils.readFromParcel(in));
        setMessageArrayList(ParcelUtils.readListFromParcel(in, Message.class));
        mConversation = ParcelUtils.readFromParcel(in, Conversation.class);
        mMatchCount = ParcelUtils.readIntFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, getId());
        ParcelUtils.writeToParcel(dest, getTitle());
        ParcelUtils.writeToParcel(dest, getPortraitUri());
        ParcelUtils.writeToParcel(dest, getMessageArrayList());
        ParcelUtils.writeToParcel(dest, mConversation);
        ParcelUtils.writeToParcel(dest, mMatchCount);
    }

    public static final Parcelable.Creator<SealSearchConversationResult> CREATOR = new Parcelable.Creator<SealSearchConversationResult>() {

        @Override
        public SealSearchConversationResult createFromParcel(Parcel source) {
            return new SealSearchConversationResult(source);
        }

        @Override
        public SealSearchConversationResult[] newArray(int size) {
            return new SealSearchConversationResult[size];
        }
    };
}
