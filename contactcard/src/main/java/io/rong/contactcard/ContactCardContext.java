package io.rong.contactcard;

/**
 * Created by Beyond on 2017/4/16.
 */

public class ContactCardContext {
    private IContactCardSelectListProvider iContactCardSelectListProvider;
    private IContactCardInfoProvider iContactCardInfoProvider;
    private static volatile ContactCardContext contactCardContext = null;

    private ContactCardContext() {

    }

    public static ContactCardContext getInstance() {
        if (contactCardContext == null) {
            synchronized (ContactCardContext.class) {
                if (contactCardContext == null) {
                    contactCardContext = new ContactCardContext();
                }
            }
        }
        return contactCardContext;
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
