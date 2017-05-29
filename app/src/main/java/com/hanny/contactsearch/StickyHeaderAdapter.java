package com.hanny.contactsearch;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/5/26.
 */

public interface StickyHeaderAdapter<T extends RecyclerView.ViewHolder> {
    long getHeaderId(int position);

    T onCreateHeaderViewHolder(ViewGroup parent);

    void onBindHeaderViewHolder(T viewholder, int position);
}
