package cn.rongcloud.im.contact;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.model.SimplePhoneContactInfo;

public class PhoneContactManager {
    private static volatile PhoneContactManager instance;
    private Context context;

    public static PhoneContactManager getInstance(){
        if(instance == null){
            synchronized (PhoneContactManager.class){
                if (instance == null){
                    instance = new PhoneContactManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context){
        this.context = context.getApplicationContext();
    }

    public List<SimplePhoneContactInfo> getAllContactInfo(){
        List<SimplePhoneContactInfo> resultList = new ArrayList<>();
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 取得联系人名字和电话号
                int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String name = cursor.getString(nameFieldColumnIndex);
                String number = cursor.getString(numberFieldColumnIndex);
                number = PhoneNumberUtils.stripSeparators(number);
                SimplePhoneContactInfo contactInfo = new SimplePhoneContactInfo();
                contactInfo.setName(name);
                contactInfo.setPhone(number);
                resultList.add(contactInfo);
            }
            cursor.close();
        }

        return resultList;
    }

    public List<String> getAllContactPhoneNumber(){
        List<String> resultList = new ArrayList<>();
        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 取得联系人名字和电话号
                int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(numberFieldColumnIndex);
                number = PhoneNumberUtils.stripSeparators(number);
                resultList.add(number);
            }
            cursor.close();
        }

        return resultList;
    }


}
