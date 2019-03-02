package com.decibel.civilianc2.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.decibel.civilianc2.fragments.ChannelFragment.OnListFragmentInteractionListener;
import com.decibel.civilianc2.radios.Channel;
import com.decibel.civilianc2.tools.Convert;
import com.example.libcivilian.R;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Channel} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyChannelRecyclerViewAdapter extends RecyclerView.Adapter<MyChannelRecyclerViewAdapter.ViewHolder> {

    private final List<Channel> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyChannelRecyclerViewAdapter(List<Channel> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_channel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Channel item = mValues.get(position);
        holder.mItem = item;
        holder.nameLabel.setText(item.getName());
        holder.rxFreqLabel.setText("Frequency: " + Convert.toFrequencyString(item.getRxFreq(), "MHz"));
        if(item.getRxFreq() != item.getTxFreq()) {
            holder.txFreqLabel.setText("  (" + Convert.toFrequencyString(mValues.get(position).getTxFreq(), "MHz") + ")");
        } else {
            holder.txFreqLabel.setVisibility(View.INVISIBLE);
        }
        Integer ctcss = mValues.get(position).getRxCTCSS();
        holder.ctcssLabel.setText(ctcss != null ? Integer.toString(ctcss) : "");

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem, OnListFragmentInteractionListener.Action.TUNE);
                }
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null & holder.mItem != null){
                    mListener.onListFragmentInteraction(holder.mItem, OnListFragmentInteractionListener.Action.DELETE);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView nameLabel;
        public final TextView rxFreqLabel;
        public final TextView txFreqLabel;
        public final TextView ctcssLabel;

        public final ImageButton deleteButton;

        public Channel mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            nameLabel = (TextView) view.findViewById(R.id.ChannelNameLabel);
            rxFreqLabel = (TextView)view.findViewById(R.id.RxFreqLabel);
            txFreqLabel = view.findViewById(R.id.TxFreqLabel);
            ctcssLabel = view.findViewById(R.id.SquelchToneLabel);
            deleteButton = view.findViewById(R.id.channelDeleteButton);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + nameLabel.getText() + "'";
        }
    }
}
