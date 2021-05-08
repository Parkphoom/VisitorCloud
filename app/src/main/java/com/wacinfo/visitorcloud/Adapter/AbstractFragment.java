package com.wacinfo.visitorcloud.Adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wacinfo.visitorcloud.R;

import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.common.SmoothScrollStaggeredLayoutManager;

/**
 * @author Davide Steduto
 * @since 15/04/2016
 */
public abstract class AbstractFragment extends Fragment {

    public static final String TAG = AbstractFragment.class.getSimpleName();
    protected static final String ARG_COLUMN_COUNT = "column_count";
    protected static final String ARG_CONFIGURE = "dynamic_list";

    protected OnFragmentInteractionListener mListener;
    protected int mColumnCount = 2;
    protected RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        //Contribution for specific action buttons in the Toolbar
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.log_fragment, container, false);
    }


    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    protected LinearLayoutManager createNewLinearLayoutManager() {
        return new SmoothScrollLinearLayoutManager(getActivity());
    }

    protected GridLayoutManager createNewGridLayoutManager() {
        return new SmoothScrollGridLayoutManager(getActivity(), mColumnCount);
    }

    protected StaggeredGridLayoutManager createNewStaggeredGridLayoutManager() {
        return new SmoothScrollStaggeredLayoutManager(getActivity(), mColumnCount);
    }



    @CallSuper
    public void showNewLayoutInfo(final MenuItem item) {
        item.setEnabled(false);
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setEnabled(true);
            }
        }, 1000L);
    }

}