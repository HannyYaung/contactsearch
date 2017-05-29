package com.hanny.contactsearch.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hanny.contactsearch.R;
import com.hanny.contactsearch.StickyHeaderAdapter;
import com.hanny.contactsearch.bean.ContactsBean;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/26.
 */

public class PhoneContactAdapter extends RecyclerView.Adapter<PhoneContactAdapter.ViewHolder> implements StickyHeaderAdapter<PhoneContactAdapter.HeaderHolder> {


    private final ArrayList<ContactsBean> contactLists;
    private final ForegroundColorSpan blueSpan;
    private Context context;
    private final LayoutInflater mInflater;
    private char lastChar = '\u0000';
    private int DisplayIndex = 0;
    SpannableStringBuilder textBuild = new SpannableStringBuilder();

    public PhoneContactAdapter(Context context, ArrayList<ContactsBean> contactLists) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.contactLists = contactLists;
        blueSpan = new ForegroundColorSpan(Color.parseColor("#0094ff"));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.item_phone_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ContactsBean contactsBean = contactLists.get(position);
        if (contactsBean.getMatchType() == 1) {//高亮名字
            textBuild.clear();
            textBuild.append(contactsBean.getName());
            textBuild.setSpan(blueSpan, contactsBean.getHighlightedStart(), contactsBean.getHighlightedEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvNumber.setText(contactsBean.getNumberList().get(contactsBean.getMatchIndex()));
            holder.nickName.setText(textBuild);
        } else if (contactsBean.getMatchType() == 2) {//高亮号码
            textBuild.clear();
            textBuild.append(contactsBean.getNumberList().get(contactsBean.getMatchIndex()));
            textBuild.setSpan(blueSpan, contactsBean.getHighlightedStart(), contactsBean.getHighlightedEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvNumber.setText(textBuild);
            holder.nickName.setText(contactsBean.getName());
        } else {//不高亮
            holder.tvNumber.setText(contactsBean.getNumberList().get(contactsBean.getMatchIndex()));
            holder.nickName.setText(contactsBean.getName() + "");
        }


        if (position == 0) {
            holder.diviView.setVisibility(View.INVISIBLE);
        } else {
            ContactsBean currentItem = contactLists.get(position);
            ContactsBean lastItem = contactLists.get(position - 1);
            if (!currentItem.getPinyinFirst().equals(lastItem.getPinyinFirst())) {
                holder.diviView.setVisibility(View.INVISIBLE);
            } else {
                holder.diviView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return contactLists.size();
    }

    //=================悬浮栏=================
    @Override
    public long getHeaderId(int position) {
        //这里面的是如果当前position与之前position重复（内部判断）  则不显示悬浮标题栏  如果不一样则显示标题栏
        if (null != contactLists.get(position) && contactLists.get(position).getPinyinFirst().charAt(0) != '\0') {
            char ch = contactLists.get(position).getPinyinFirst().charAt(0);
            if (lastChar == '\u0000') {
                lastChar = ch;
                return DisplayIndex;
            } else {
                if (lastChar == ch) {
                    return DisplayIndex;
                } else {
                    lastChar = ch;
                    DisplayIndex++;
                    return DisplayIndex;
                }

            }
        } else {
            return DisplayIndex;
        }
    }

    @Override
    public HeaderHolder onCreateHeaderViewHolder(ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.item_contacts_head, parent, false);
        return new HeaderHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderHolder viewholder, int position) {
        if (contactLists.get(position).getPinyinFirst().charAt(0) == '\0') {
            viewholder.header.setText("#");
        } else {
            viewholder.header.setText(contactLists.get(position).getPinyinFirst() + "");
        }
    }

    public int getPositionForSection(char pinyinFirst) {
        for (int i = 0; i < getItemCount(); i++) {
            char firstChar = contactLists.get(i).getPinyinFirst().charAt(0);
            if (firstChar == pinyinFirst) {
                return i;
            }
        }
        return -1;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView nickName;
        private final TextView tvNumber;
        private final View diviView;

        public ViewHolder(View itemView) {
            super(itemView);
            nickName = (TextView) itemView.findViewById(R.id.tv_name);
            tvNumber = (TextView) itemView.findViewById(R.id.tv_number);
            diviView = itemView.findViewById(R.id.vw_divisition);
        }
    }

    public class HeaderHolder extends RecyclerView.ViewHolder {
        public TextView header;

        public HeaderHolder(View itemView) {
            super(itemView);
            header = (TextView) itemView;
        }
    }
}
