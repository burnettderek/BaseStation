package com.decibel.civilianc2.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.decibel.civilianc2.model.managers.ChannelManager;
import com.decibel.civilianc2.radios.Channel;
import com.decibel.civilianc2.radios.ITransceiver;
import com.example.libcivilian.R;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.decibel.civilianc2.fragments.ChannelFragment.OnListFragmentInteractionListener.Result.SUCCESS;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ChannelFragment extends Fragment implements ChannelManager.EventListener{

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChannelFragment() {
    }

    public void setChannels(ChannelManager channels){
        this.channels = channels;
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ChannelFragment newInstance(int columnCount) {
        ChannelFragment fragment = new ChannelFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel_list, container, false);

        // Set the adapter

        Context context = view.getContext();
        recyclerView = view.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        (view.findViewById(R.id.fabScanChannel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doChannelScan();
            }
        });
        invalidateData();

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.channels.addEventListener(this);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.channels.removeEventListener(this);
        mListener = null;
    }

    public void invalidateData(){
        List<Channel> items = this.channels.getAll();
        MyChannelRecyclerViewAdapter adapter = new MyChannelRecyclerViewAdapter(items, mListener);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onChannelAdded(Channel channel) {
        this.invalidateData();
    }

    @Override
    public void onChannelRemoved(Channel channel) {
        this.invalidateData();
    }

    @Override
    public void onChannelChanged(Channel channel) {
        this.invalidateData();
    }


    public void doChannelScan(){
        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this.getActivity());
        builder.setTitle("Scanning...");


        View viewInflated = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.scan_channel_dialog, null, false);
// Set up the input
        final TextView channelCallSign = (TextView) viewInflated.findViewById(R.id.txtChannelCallSign);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);

// Set up the buttons

        final List<Channel> items = this.channels.getAll();
        int delay = 1000; //first time pause one second.

        scheduleScan(channelCallSign, items, delay);

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scanTimer.cancel();
                if(mListener != null){
                    if(lastScannedChannel != null)
                        mListener.onListFragmentInteraction(lastScannedChannel, OnListFragmentInteractionListener.Action.TUNE);
                }
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void scheduleScan(final TextView channelCallSign, final List<Channel> items, int delay) {
        this.scanTimer = new Timer();
        this.scanTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                if(mListener != null){
                    Channel nextChannel = items.get(scanChannelIndex);
                    if(SUCCESS == mListener.onListFragmentInteraction(nextChannel, OnListFragmentInteractionListener.Action.SCAN)) {
                        lastScannedChannel = nextChannel;
                        scanDialogLabel = nextChannel.getName();
                        scanChannelIndex++;
                        if (scanChannelIndex == items.size()) scanChannelIndex = 0;
                    } else {
                        int index = scanChannelIndex > 0 ? scanChannelIndex - 1 : items.size() - 1;
                        Channel currentChannel = items.get(index);
                        scanDialogLabel = currentChannel.getName() + " (signal detected)";
                        Log.d("Channel Scanner", "Holding on station '" + currentChannel.getName() + "'. Signal Detected.");
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            channelCallSign.setText(scanDialogLabel);
                        }
                    });
                }


            }
        }, delay, ScanPauseTimeInMillis);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        enum Action{
            TUNE,
            DELETE,
            SCAN
        }
        enum Result {
            SUCCESS,
            FAILURE
        }
        // TODO: Update argument type and name
        Result onListFragmentInteraction(Channel item, Action action);
    }

    private ChannelManager channels;
    private RecyclerView recyclerView;

    private Timer scanTimer;
    private int scanChannelIndex = 0;
    private Handler handler = new Handler();
    private Channel lastScannedChannel = null;
    private long ScanPauseTimeInMillis = 500;
    private String scanDialogLabel = new String();
}
