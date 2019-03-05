package com.rogueelement.basestation;


import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentTransaction;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.decibel.civilianc2.fragments.ChannelFragment;
import com.decibel.civilianc2.fragments.RadioControlFragment;
import com.decibel.civilianc2.radios.Channel;
import com.decibel.civilianc2.radios.IReceiver;
import com.decibel.civilianc2.radios.ITransceiver;
import com.decibel.civilianc2.radios.RadioDetection;
import com.decibel.civilianc2.tools.BlueToothManager;
import com.rogueelement.basestation.R;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TabListener, ChannelFragment.OnListFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private static ChannelFragment currentChannelFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Set up the action bar.
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        RadioDetection.getConnectedDevices(this, null, new RadioDetection.IOnRadioDetectedHandler() {
            @Override
            public void onRadioDetected(List<ITransceiver> connectedDevices) {
                if (connectedDevices.size() > 0) {
                    try {
                        BaseStationApplication.setRadio(connectedDevices.get(0));
                        controlFragment.setRadio(BaseStationApplication.getRadio());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == BlueToothManager.REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth detection deactivated.", Toast.LENGTH_LONG).show();
            }
        } else {
            if(currentChannelFragment != null)
                currentChannelFragment.invalidateData();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onTabSelected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }

    @Override
    public Result onListFragmentInteraction(Channel item, ChannelFragment.OnListFragmentInteractionListener.Action action) {
        switch (action){
            case TUNE:
                try {
                    BaseStationApplication.getRadio().setFrequency(item.getRxFreq(), item.getTxFreq());
                    if(item.getRxCTCSS() != null) {
                        BaseStationApplication.getRadio().enableToneSquelch(true);
                        BaseStationApplication.getRadio().setToneSquelch(item.getRxCTCSS());
                    } else {
                        BaseStationApplication.getRadio().enableToneSquelch(false);
                    }
                    controlFragment.setRadio(BaseStationApplication.getRadio());
                    Toast.makeText(getApplicationContext(), "Tuned to channel " + item.getName() + ".", Toast.LENGTH_LONG).show();
                    return Result.SUCCESS;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case SCAN:
                try {
                    long current = System.currentTimeMillis();
                    if(BaseStationApplication.getRadio().getSquelchState() == IReceiver.SquelchState.Open)
                        lastSignalDetectTime = System.currentTimeMillis();
                    long delta = current - lastSignalDetectTime;
                    if (delta > 10 * 1000) {
                        BaseStationApplication.getRadio().setReceiveFreq(item.getRxFreq());
                        if (item.getRxCTCSS() != null) {
                            BaseStationApplication.getRadio().enableToneSquelch(true);
                            BaseStationApplication.getRadio().setToneSquelch(item.getRxCTCSS());
                        } else {
                            BaseStationApplication.getRadio().enableToneSquelch(false);
                        }
                        //controlFragment.setRadio(BaseStationApplication.getRadio());
                        return Result.SUCCESS;
                    }
                } catch(IOException e){
                    e.printStackTrace();
                }

                break;
            case DELETE:
                BaseStationApplication.getChannels().removeChannel(item);
                return Result.SUCCESS;
        }
        return Result.FAILURE;
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if(position == 0){
                controlFragment = new RadioControlFragment();
                controlFragment.setChannelManager(BaseStationApplication.getChannels());
                return controlFragment;
            }
            if(position == 1) {
                 currentChannelFragment = ChannelFragment.newInstance(0);
                currentChannelFragment.setChannels(BaseStationApplication.getChannels());
                return currentChannelFragment;
            }
            else
                return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "RADIO";
                case 1:
                    return "CHANNELS";
                case 2:
                    return "TEXTS";
            }
            return null;
        }
    }

    private RadioControlFragment controlFragment;
    private long lastSignalDetectTime = 0;
}
