package com.hanny.contactsearch.bean;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/26.
 */

public class ContactsBean implements Comparable<ContactsBean> {
    private String contactId;//通讯录ID
    private String name; //名字
    private String number;//电话号码
    private String pinyinFirst;//拼音首字母用于悬浮栏
    private int showNumberIndex = 0;//因为一个人可能会有多个号码
    private int highlightedStart = 0;//需要高亮的开始下标
    private int highlightedEnd = 0;//需要高亮的结束下标
    private String matchPin = "";//用来匹配的拼音每个字的首字母比如：你好，NH
    private String namePinYin = "";//全名字拼音,比如：你好,NIHAO
    private int matchType = 0;//匹配类型，名字1，电话号码2，其他0,根据输入的来判断
    private ArrayList<String> namePinyinList = new ArrayList<>();//名字拼音集合，比如你好，NI,HAO
    private ArrayList<String> numberList = new ArrayList<>();//电话号码集合，一个人可能会有多个号码
    private int matchIndex = 0;//匹配到号码后的下标

    public int getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(int matchIndex) {
        this.matchIndex = matchIndex;
    }

    public String getPinyinFirst() {
        return pinyinFirst;
    }

    public void setPinyinFirst(String pinyinFirst) {
        this.pinyinFirst = pinyinFirst;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getShowNumberIndex() {
        return showNumberIndex;
    }

    public void setShowNumberIndex(int showNumberIndex) {
        this.showNumberIndex = showNumberIndex;
    }

    public int getHighlightedStart() {
        return highlightedStart;
    }

    public void setHighlightedStart(int highlightedStart) {
        this.highlightedStart = highlightedStart;
    }

    public int getHighlightedEnd() {
        return highlightedEnd;
    }

    public void setHighlightedEnd(int highlightedEnd) {
        this.highlightedEnd = highlightedEnd;
    }

    public String getMatchPin() {
        return matchPin;
    }

    public void setMatchPin(String matchPin) {
        this.matchPin = matchPin;
    }

    public String getNamePinYin() {
        return namePinYin;
    }

    public void setNamePinYin(String namePinYin) {
        this.namePinYin = namePinYin;
    }

    public int getMatchType() {
        return matchType;
    }

    public void setMatchType(int matchType) {
        this.matchType = matchType;
    }

    public ArrayList<String> getNamePinyinList() {
        return namePinyinList;
    }

    public void setNamePinyinList(ArrayList<String> namePinyinList) {
        this.namePinyinList = namePinyinList;
    }

    public ArrayList<String> getNumberList() {
        return numberList;
    }

    public void setNumberList(ArrayList<String> numberList) {
        this.numberList = numberList;
    }

    @Override
    public int compareTo(@NonNull ContactsBean o) {
        return 0;
    }
}
