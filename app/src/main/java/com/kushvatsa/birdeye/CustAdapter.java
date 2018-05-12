package com.kushvatsa.birdeye;


import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class CustAdapter extends RecyclerView.Adapter<CustAdapter.CustAdapterViewHolder>  {



    private final Context mContext;

    //Listener for interface for clicking bottom sheets
    private OnSelectedListener mListener;
    private Cursor mCursor;

    //bottom sheet click listener
    public interface OnSelectedListener {
        void onSheetClicked(View view, String number);
    }
    public CustAdapter(Context mContext, OnSelectedListener mListener) {
        this.mContext = mContext;
        this.mListener=mListener;

    }

    @NonNull
    @Override
    public CustAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.item_customers, viewGroup, false);

        view.setFocusable(true);

        return new CustAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustAdapterViewHolder holder, int position) {

        mCursor.moveToPosition(position);
        // for displaying data
        final String f_name = mCursor.getString(MainActivity.INDEX_F_NAME);
        final String l_name = mCursor.getString(MainActivity.INDEX_L_NAME);
        final String email = mCursor.getString(MainActivity.INDEX_EMAIL);
        final String phone = mCursor.getString(MainActivity.INDEX_PHONE);
        final String c_num = mCursor.getString(MainActivity.INDEX_NUMBER);


        final String name = f_name + " " + l_name;

        if((f_name.equals("null") && !l_name.equals("null")))
        {
            holder.d_name.setText(l_name);
        }
        else if((l_name.equals("null") && !f_name.equals("null")))
        {
            holder.d_name.setText(f_name);
        }
        else if((f_name.equals("null") && l_name.equals("null")))
        {
            holder.d_name.setText(Utils.returnName(email));
        }
        else {
            holder.d_name.setText(name);
        }

        holder.d_email.setText(email);
        holder.d_phone.setText(phone);
        holder.d_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSheetClicked(v,c_num);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    //customer adapter view holder
    class CustAdapterViewHolder extends RecyclerView.ViewHolder  {
        TextView d_email;
        TextView d_name;
        TextView d_phone;
        ImageView d_down;
        public CustAdapterViewHolder(View itemView) {
            super(itemView);
            d_email = itemView.findViewById(R.id.d_email);
            d_name = itemView.findViewById(R.id.d_name);
            d_phone = itemView.findViewById(R.id.d_phone);
            d_down = itemView.findViewById(R.id.d_down);
        }
    }
}
