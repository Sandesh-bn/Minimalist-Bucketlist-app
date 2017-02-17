package sandesh.com.bucketlist.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import sandesh.com.bucketlist.AppBucketDrops;
import sandesh.com.bucketlist.R;
import sandesh.com.bucketlist.beans.Drop;
import sandesh.com.bucketlist.extras.Util;

/**
 * Created by Sandesh on 2/16/2017.
 */

public class AdapterDrops extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SwipeListener {
    private MarkListener mMarkListener;
    LayoutInflater mInflater;
    private RealmResults<Drop> realmResults;
    public static final int COUNT_FOOTER = 1;
    public static final int COUNT_NO_ITEMS = 1;
    public static final int ITEM = 0;
    public static final int NO_ITEM = 1;
    public static final int FOOTER = 2;
    private AddListener mAddListener;
    private Realm mRealm;
    private int mFilterOptions;
    private Context context;

    /*public AdapterDrops(Context context, Realm realm, RealmResults<Drop> results) {
        /*
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mRealm = realm;
        update(results);
        mFilterOptions = AppBucketDrops.load(context);

    }*/

    public AdapterDrops(Context context, Realm realm, RealmResults<Drop> results, AddListener listener, MarkListener markListener) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        update(results);
        mRealm = realm;
        mAddListener = listener;
        mMarkListener = markListener;


    }

    public void update(RealmResults<Drop> results) {
        realmResults = results;
        mFilterOptions = AppBucketDrops.load(context);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (!realmResults.isEmpty()){
            if (position < realmResults.size())
                return ITEM;
            else
                return FOOTER;
        }
        else {
            if (mFilterOptions == Filter.COMPLETE ||
                    mFilterOptions == Filter.INCOMPLETE){
                if (position == 0)
                    return NO_ITEM;
                else
                    return FOOTER;
            }
            else
                return ITEM;
        }
    }

    public static ArrayList<String> generateValues() {
        ArrayList<String> dummyValues = new ArrayList<>();
        for (int i = 1; i <= 100; i++)
            dummyValues.add("Item " + i);
        return dummyValues;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FOOTER) {
            View view = mInflater.inflate(R.layout.footer, parent, false);
            return new FooterHolder(view, mAddListener);
        }
        else if (viewType == NO_ITEM){
            View view = mInflater.inflate(R.layout.no_item, parent, false);
            return new NoItemsHolder(view);
        }
        else {
            View view = mInflater.inflate(R.layout.row_drop, parent, false);
            return new DropHolder(view, mMarkListener);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DropHolder) {
            DropHolder dropHolder = (DropHolder) holder;
            Drop drop = realmResults.get(position);
            //$dropHolder.mTextWhat.setText(drop.getWhat());
            dropHolder.setWhat(drop.getWhat());
            dropHolder.setWhen(drop.getWhen());
            dropHolder.setBackground(drop.isCompleted());
        }
    }

    public long getItemId(int position){
        if (position < realmResults.size())
            return realmResults.get(position).getAdded();

        return RecyclerView.NO_ID;

    }

    @Override
    public int getItemCount() {
        if (!realmResults.isEmpty())
            return realmResults.size() + COUNT_FOOTER;
        else {
            if (mFilterOptions == Filter.LEAST_TIME_LEFT
                    || mFilterOptions == Filter.MOST_TIME_LEFT
                    || mFilterOptions == Filter.NONE)
                return 0;
            else {
                return COUNT_NO_ITEMS + COUNT_FOOTER;
            }
        }
    }

    @Override
    public void onSwipe(int position) {
        if (position < realmResults.size()) {
            mRealm.beginTransaction();
            realmResults.get(position).deleteFromRealm();
            mRealm.commitTransaction();
            notifyItemRemoved(position);
        }
    }

    public void markComplete(int position) {
        if (position < realmResults.size()) {
            mRealm.beginTransaction();
            realmResults.get(position).setCompleted(true);
            mRealm.commitTransaction();
            notifyItemChanged(position);
        }
    }

    public static class DropHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MarkListener mMarkListener;
        TextView mTextWhat;
        TextView mTextWhen;
        Context mContext;
        View mItemView;

        public DropHolder(View itemView, MarkListener mListener) {
            super(itemView);
            mItemView = itemView;
            mContext = itemView.getContext();
            itemView.setOnClickListener(this);
            mMarkListener = mListener;
            mTextWhat = (TextView) itemView.findViewById(R.id.tv_what);
            mTextWhen = (TextView) itemView.findViewById(R.id.tv_when);
        }

        public void setWhat(String what) {
            mTextWhat.setText(what);
        }

        @Override
        public void onClick(View view) {
            mMarkListener.onMark(getAdapterPosition());
        }

        public void setBackground(boolean completed) {
            Drawable drawable;
            if (completed)
                drawable = ContextCompat.getDrawable(mContext, R.color.bg_drop_complete);
            else
                drawable = ContextCompat.getDrawable(mContext, R.drawable.bg_row_drop);

            Util.setBackground(mItemView, drawable);
        }

        public void setWhen(long when) {
            mTextWhen.
                    setText(DateUtils.getRelativeTimeSpanString(when, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
        }
    }

    public static class NoItemsHolder extends RecyclerView.ViewHolder{
        public NoItemsHolder(View view){
            super(view);
        }
    }


    public static class FooterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Button mBtnAdd;
        TextView mTextWhat;
        AddListener mListener;

        public FooterHolder(View itemView) {
            super(itemView);
            mBtnAdd = (Button) itemView.findViewById(R.id.btn_footer);
            mBtnAdd.setOnClickListener(this);
        }

        public FooterHolder(View itemView, AddListener listener) {
            super(itemView);
            mBtnAdd = (Button) itemView.findViewById(R.id.btn_footer);
            mBtnAdd.setOnClickListener(this);
            mListener = listener;
        }

        @Override
        public void onClick(View view) {
            mListener.add();
        }
    }


}
