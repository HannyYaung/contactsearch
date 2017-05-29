package com.hanny.contactsearch;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.hanny.contactsearch.adapter.PhoneContactAdapter;
import com.hanny.contactsearch.bean.ContactsBean;
import com.hanny.contactsearch.utils.RegexChk;
import com.hanny.contactsearch.utils.Util;
import com.hanny.contactsearch.view.QuickIndexBar;
import com.hanny.contactsearch.view.StickyHeaderDecoration;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private EditText mEtSearch;
    private QuickIndexBar quickIndexBar;
    private RecyclerView rvContacts;
    ArrayList<ContactsBean> contactLists = new ArrayList<>();
    ArrayList<ContactsBean> searchContactLists = new ArrayList<>();//用于搜索的集合数据
    private PhoneContactAdapter contactAdapter;
    private LinearLayoutManager manager;
    private StickyHeaderDecoration decoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        getContactsByPerm();
    }

    private void initView() {
        //搜索框
        mEtSearch = (EditText) findViewById(R.id.mEtSearch);
        //索引
        quickIndexBar = (QuickIndexBar) findViewById(R.id.qiBar);
        //数据列表
        rvContacts = (RecyclerView) findViewById(R.id.rvContacts);
        manager = new LinearLayoutManager(this);
        rvContacts.setLayoutManager(manager);
        rvContacts.setHasFixedSize(true);
        contactAdapter = new PhoneContactAdapter(this, contactLists);
        //设置悬浮索引
        decoration = new StickyHeaderDecoration(contactAdapter);
        rvContacts.setAdapter(contactAdapter);
        rvContacts.addItemDecoration(decoration);
        //索引监听
        quickIndexBar.setOnLetterChangeListener(new QuickIndexBar.OnLetterChangeListener() {
            @Override
            public void onLetterChange(String letter) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                for (int i = 0; i < contactLists.size(); i++) {

                    if (letter.equals(contactLists.get(i).getPinyinFirst() + "")) {

                        int position = contactAdapter.getPositionForSection(contactLists.get(i).getPinyinFirst().charAt(0));
                        if (position != -1) {
                            //滑动到指定位置
                            manager.scrollToPositionWithOffset(position, 0);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onReset() {

            }
        });
        //触摸隐藏键盘
        rvContacts.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                return false;
            }
        });
        //搜索功能
        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //查询数据
                filtDatas(s);
            }
        });
    }

    private void getContactsByPerm() {
        //先获取手机和sim卡联系人
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        } else {
            getContacts(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContacts(this);
            } else {
                Toast.makeText(this, "权限拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getContacts(final Context context) {
        contactLists.clear();
        searchContactLists.clear();
        Observable.create(new ObservableOnSubscribe<ArrayList<ContactsBean>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<ContactsBean>> e) throws Exception {
                if (!e.isDisposed()) {
                    e.onNext(Util.getContactData(context, searchContactLists));
                    e.onComplete();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<ContactsBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ArrayList<ContactsBean> contactsBeen) {
                        contactLists.addAll(contactsBeen);
                        contactAdapter.notifyDataSetChanged();
                    }


                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("");
                    }
                });

    }

    //搜索数据
    private void filtDatas(Editable s) {
        if (searchContactLists.size() == 0) {
            return;
        }
        String inputStr = s.toString();
        if (TextUtils.isEmpty(inputStr)) {
            resetSearchData();
            contactLists.clear();
            contactLists.addAll(searchContactLists);
            contactAdapter.notifyDataSetChanged();
        } else {
            contactLists.clear();
            //因为每次搜索的结果不同，所以匹配类型不同，但是数据源都是同一个数据源，所以每次搜索前，要重置数据
            resetSearchData();
            if (RegexChk.isNumeric(inputStr)) {//如果是数字
                findDataByNumberOrCN(inputStr);
            } else if (RegexChk.isContainChinese(inputStr)) {//如果含义中文，需要精确匹配
                findDataByNumberOrCN(inputStr);
            } else if (RegexChk.isEnglishAlphabet(inputStr)) {//是不是全是英文字母或者是拼音的话
                findDataByEN(inputStr);
            } else {//需要精确匹配
                findDataByNumberOrCN(inputStr);
            }
            contactAdapter.notifyDataSetChanged();
        }
    }

    private void resetSearchData() {
        for (int i = 0; i < searchContactLists.size(); i++) {
            searchContactLists.get(i).setMatchType(0);//重置为没有匹配类型
        }
    }

    private void findDataByNumberOrCN(String inputStr) {
        for (int i = 0; i < searchContactLists.size(); i++) {
            ContactsBean contactsBean = searchContactLists.get(i);
            if (!TextUtils.isEmpty(contactsBean.getName()) && contactsBean.getName().contains(inputStr)) {
                contactsBean.setMatchType(1);//名字匹配
                contactsBean.setHighlightedStart(contactsBean.getName().indexOf(inputStr));
                contactsBean.setHighlightedEnd(contactsBean.getHighlightedStart() + inputStr.length());
                contactLists.add(contactsBean);
                continue;
            }
            if (contactsBean.getNumberList().size() > 0) {
                for (int j = 0; j < contactsBean.getNumberList().size(); j++) {
                    String number = contactsBean.getNumberList().get(j);
                    if (!TextUtils.isEmpty(number) && number.contains(inputStr)) {
                        contactsBean.setShowNumberIndex(j);//显示号码的下标
                        contactsBean.setMatchType(2);//电话匹配
                        contactsBean.setHighlightedStart(number.indexOf(inputStr));
                        contactsBean.setHighlightedEnd(contactsBean.getHighlightedStart() + inputStr.length());
                        contactLists.add(contactsBean);
                    }
                }
            }

        }
    }

    //通过拼音或者英文字母
    private void findDataByEN(String inputStr) {
        //把输入的内容变为大写
        String searPinyin = Util.transformPinYin(inputStr);
        //搜索字符串的长度
        int searLength = searPinyin.length();
        //搜索的第一个大写字母
        String searPinyinFirst = searPinyin.charAt(0) + "";
        for (int i = 0; i < searchContactLists.size(); i++) {
            ContactsBean contactsBean = searchContactLists.get(i);
            contactsBean.setMatchType(1);//字母匹配肯定是名字
            //如果输入的每一个字母都和名字的首字母一样，那就可以匹配比如：你好，NH，输入nh就ok
            if (contactsBean.getMatchPin().contains(searPinyin)) {
                contactsBean.setHighlightedStart(contactsBean.getMatchPin().indexOf(searPinyin));
                contactsBean.setHighlightedEnd(contactsBean.getHighlightedStart() + searLength);
                contactLists.add(contactsBean);
            } else {
                boolean isMatch = false;
                //先去匹配单个字，比如你好：NI,HAO.输入NI，肯定匹配第一个
                for (int j = 0; j < contactsBean.getNamePinyinList().size(); j++) {
                    String namePinyinPer = contactsBean.getNamePinyinList().get(j);
                    if (!TextUtils.isEmpty(namePinyinPer) && namePinyinPer.startsWith(searPinyin)) {
                        //符合的话就是当前字匹配成功
                        contactsBean.setHighlightedStart(j);
                        contactsBean.setHighlightedEnd(j + 1);
                        contactLists.add(contactsBean);
                        isMatch = true;
                        break;
                    }
                }
                if (isMatch) {
                    continue;
                }
                //根据拼音包含来实现，比如你好：NIHAO,输入NIHA或者NIHAO。
                if (!TextUtils.isEmpty(contactsBean.getNamePinYin()) && contactsBean.getNamePinYin().contains(searPinyin)) {
                    //这样的话就要从每个字的拼音开始匹配起
                    for (int j = 0; j < contactsBean.getNamePinyinList().size(); j++) {
                        StringBuilder sbMatch = new StringBuilder();
                        for (int k = j; k < contactsBean.getNamePinyinList().size(); k++) {
                            sbMatch.append(contactsBean.getNamePinyinList().get(k));
                        }
                        if (sbMatch.toString().startsWith(searPinyin)) {
                            //匹配成功
                            contactsBean.setHighlightedStart(j);
                            int length = 0;
                            //比如输入是NIH，或者NIHA,或者NIHAO,这些都可以匹配上，从而就可以通过NIHAO>=NIH,HIHA,NIHAO
                            for (int k = j; k < contactsBean.getNamePinyinList().size(); k++) {
                                length = length + contactsBean.getNamePinyinList().get(k).length();
                                if (length >= searLength) {
                                    contactsBean.setHighlightedEnd(k + 1);
                                    break;
                                }
                            }
                            isMatch = true;
                            contactLists.add(contactsBean);
                        }
                    }
                }

                if (isMatch) {
                    continue;
                }

                //最后一种情况比如：广发银行，输入GuangFY或者GuangFYH都可以匹配成功，这样的情况名字集合必须大于等于3
                if (contactsBean.getNamePinyinList().size() > 2) {
                    for (int j = 0; j < contactsBean.getNamePinyinList().size(); j++) {

                        StringBuilder sbMatch0 = new StringBuilder();
                        sbMatch0.append(contactsBean.getNamePinyinList().get(j));
                        //只匹配到倒数第二个
                        if (j < contactsBean.getNamePinyinList().size() - 2) {
                            for (int k = j + 1; k < contactsBean.getMatchPin().length(); k++) {
                                //依次添加后面每个字的首字母
                                sbMatch0.append(contactsBean.getMatchPin().charAt(k));
                                if (sbMatch0.toString().equals(searPinyin)) {
                                    contactsBean.setHighlightedStart(j);
                                    contactsBean.setHighlightedEnd(k + 1);
                                    contactLists.add(contactsBean);
                                    isMatch = true;
                                    break;
                                }
                            }
                        }

                        if (isMatch) {
                            //跳出循环已找到
                            break;
                        }

                        //sbMatch1是循环匹配对象比如GUANGFYH，GUANGFAYH，GUANGFAYINH,GUANGFAYINHANG，
                        //FAYH,YINH
                        StringBuilder sbMatch1 = new StringBuilder();
                        for (int k = 0; k <= j; k++) {//依次作为初始匹配的起点
                            sbMatch1.append(contactsBean.getNamePinyinList().get(k));
                        }
                        //只匹配到倒数第二个
                        if (j < contactsBean.getNamePinyinList().size() - 2) {
                            for (int k = j + 1; k < contactsBean.getMatchPin().length(); k++) {
                                //依次添加后面每个字的首字母
                                sbMatch1.append(contactsBean.getMatchPin().charAt(k));
                                if (sbMatch1.toString().equals(searPinyin)) {
                                    contactsBean.setHighlightedStart(j);
                                    contactsBean.setHighlightedEnd(k + 1);
                                    contactLists.add(contactsBean);
                                    isMatch = true;
                                    break;
                                }
                            }
                        }
                        if (isMatch) {
                            //跳出循环已找到
                            break;
                        }

                        if (j >= contactsBean.getNamePinyinList().size() - 2) {
                            //如果说是剩余最后两个拼音不需要匹配了
                            break;
                        }
                        StringBuilder sbMatch2 = new StringBuilder();
                        sbMatch2.append(contactsBean.getNamePinyinList().get(j));
                        for (int k = j + 1; k < contactsBean.getNamePinyinList().size(); k++) {
                            sbMatch2.append(contactsBean.getNamePinyinList().get(k));
                            StringBuilder sbMatch3 = new StringBuilder();
                            sbMatch3.append(sbMatch2.toString());
                            //只匹配到倒数第二个
                            if (j < contactsBean.getNamePinyinList().size() - 2) {
                                for (int m = k + 1; m < contactsBean.getMatchPin().length(); m++) {
                                    //依次添加后面每个字的首字母
                                    sbMatch3.append(contactsBean.getMatchPin().charAt(m));
                                    if (sbMatch3.toString().equals(searPinyin)) {
                                        contactsBean.setHighlightedStart(j);
                                        contactsBean.setHighlightedEnd(m + 1);
                                        contactLists.add(contactsBean);
                                        isMatch = true;
                                        break;
                                    }
                                }
                            }
                            if (isMatch) {
                                //跳出循环已找到
                                break;
                            }
                        }

                        if (isMatch) {
                            //跳出循环已找到
                            break;
                        }


                    }
                }

            }
        }
    }
}
