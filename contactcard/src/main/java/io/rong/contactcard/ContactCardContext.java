package io.rong.contactcard;

/**
 * Created by Beyond on 2017/4/16.
 */

public class ContactCardContext {
    private IContactCardSelectListProvider iContactCardSelectListProvider;
    private IContactCardInfoProvider iContactCardInfoProvider;

    private ContactCardContext() {

    }

    private static class SingletonHolder {
        static ContactCardContext sInstance = new ContactCardContext();
    }

    public static ContactCardContext getInstance() {
        return SingletonHolder.sInstance;
    }

    public void setContactCardSelectListProvider(IContactCardSelectListProvider iContactCardSelectListProvider) {
        this.iContactCardSelectListProvider = iContactCardSelectListProvider;
    }

    public IContactCardSelectListProvider getContactCardSelectListProvider() {
        return iContactCardSelectListProvider;
    }

    public void setContactCardInfoProvider(IContactCardInfoProvider iContactCardInfoProvider) {
        this.iContactCardInfoProvider = iContactCardInfoProvider;
    }

    public IContactCardInfoProvider getContactCardInfoProvider() {
        return iContactCardInfoProvider;
    }

}
