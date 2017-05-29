package com.hanny.contactsearch.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.github.promeg.pinyinhelper.Pinyin;
import com.hanny.contactsearch.bean.ContactsBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Administrator on 2017/5/27.
 */

public class Util {
    private static String indexStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";


    public static ArrayList<ContactsBean> getContactData(Context context, ArrayList<ContactsBean> searchContactLists) {
        //得到ContentResolver对象
        ContentResolver cr = context.getContentResolver();
        //取得电话本中开始一项的光标
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        //向下移动光标
        while (cursor.moveToNext()) {
            //取得联系人名字
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            String name = cursor.getString(nameFieldColumnIndex);
            //取得联系人ID
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            //要获取所有的联系人,一个联系人会有多个号码
            getContactById(cr, contactId, name, searchContactLists);
        }
        Collections.sort(searchContactLists, new SortByPinyin());//数据排序
        return searchContactLists;
    }

    private static void getContactById(ContentResolver cr, String contactId, String name, ArrayList<ContactsBean> searchContactLists) {
        if (!TextUtils.isEmpty(contactId)) {
            Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
            if (null != phone) {
                ContactsBean contact = new ContactsBean();
                contact.setName(name);
                contact.setContactId(contactId);

                if (!TextUtils.isEmpty(contact.getName())) {
                    getPinyinList(contact);
                } else {
                    contact.setPinyinFirst("#");
                }
                while (phone.moveToNext()) {
                    String phoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contact.getNumberList().add(phoneNumber);
                }
                searchContactLists.add(contact);
            }
        }
    }

    private static void getPinyinList(ContactsBean contactsBean) {
        StringBuffer bufferNamePiny = new StringBuffer();//NIHAO
        StringBuffer bufferNameMatch = new StringBuffer();//NH
        String name = contactsBean.getName();
        for (int i = 0; i < name.length(); i++) {
            StringBuffer bufferNamePer = new StringBuffer();
            String namePer = name.charAt(i) + "";//名字的每个字
            for (int j = 0; j < namePer.length(); j++) {
                char character = namePer.charAt(j);
                String pinCh = Pinyin.toPinyin(character).toUpperCase();
                bufferNamePer.append(pinCh);
                bufferNameMatch.append(pinCh.charAt(0));
                bufferNamePiny.append(pinCh);
            }
            contactsBean.getNamePinyinList().add(bufferNamePer.toString());//单个名字集合
        }
        contactsBean.setNamePinYin(bufferNamePiny.toString());
        contactsBean.setMatchPin(bufferNameMatch.toString());
        String firstPinyin = contactsBean.getNamePinYin().charAt(0) + "";
        if (indexStr.contains(firstPinyin)) {
            contactsBean.setPinyinFirst(firstPinyin);
        } else {
            contactsBean.setPinyinFirst("#");
        }
    }
    public static String transformPinYin(String character) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < character.length(); i++) {
            buffer.append(Pinyin.toPinyin(character.charAt(i)).toUpperCase());
        }
        return buffer.toString();
    }
    /**
     * 按照名字分类方便索引
     */
    static class SortByPinyin implements Comparator {
        public int compare(Object o1, Object o2) {
            ContactsBean s1 = (ContactsBean) o1;
            ContactsBean s2 = (ContactsBean) o2;
            if (s1.getPinyinFirst().equals("#")) {
                return 1;
            } else if (s2.getPinyinFirst().equals("#")) {
                return -1;
            }
            return s1.getPinyinFirst().compareTo(s2.getPinyinFirst());
        }
    }

}
