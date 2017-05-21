package com.example.ultim.radio5.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ultim.radio5.R;
import com.example.ultim.radio5.Radio.TitleRadio;
import com.example.ultim.radio5.RadioMessage;
import com.example.ultim.radio5.RadioPlayerService;
import com.project.equalizerview.EqualizerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentRadio.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentRadio#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentRadio extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    EqualizerView equalizerView;
    int mState = PlaybackStateCompat.STATE_NONE;
    ProgressDialog progressDialog;
    RadioMessage radioMessage;

    // TODO: Rename and change types of parameters
    private String universityName;
    private String universityUrl;

    private OnFragmentInteractionListener mListener;

    ImageView playButtonImageView;

    public FragmentRadio() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment FragmentRadio.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentRadio newInstance(String param1, String param2) {
        FragmentRadio fragment = new FragmentRadio();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            universityName = getArguments().getString(ARG_PARAM1);
            universityUrl = getArguments().getString(ARG_PARAM2);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(universityName);
        }
        if (radioMessage == null) {
            radioMessage = new RadioMessage();
        }
    }

    View initView(View rootView){
        playButtonImageView = (ImageView) rootView.findViewById(R.id.content_play_btn);
        equalizerView = (EqualizerView) rootView.findViewById(R.id.equalaizer);
        //TODO: make invisible and not animate!!
        equalizerView.setVisibility(View.INVISIBLE);

        playButtonImageView.setOnClickListener(this);
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            SpannableString ss2=  new SpannableString(getString(R.string.buffering));
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            progressDialog.setMessage(ss2);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
        }
        return rootView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_radio, container, false);
        rootView = initView(rootView);
        EventBus.getDefault().register(this);
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        View subview = initView(inflater.inflate(R.layout.fragment_radio, viewGroup));
        changeState(radioMessage);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        startStopRadio();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void startStopRadio() {
        if (Objects.equals(radioMessage.getUniversityName(), universityName)) {

            if (mState == PlaybackStateCompat.STATE_PAUSED){
               runService();
            }

            else if (mState != PlaybackStateCompat.STATE_STOPPED){
                stopService();
            }

            else {
                runService();
            }
            //restart Service
        } else {
            if (mState != PlaybackStateCompat.STATE_STOPPED) {
                runService();
            }
        }
    }

    private void runService(){
        // Start service
        TitleRadio.getInstance().setTitle(universityName);
        Thread t = new Thread(){
            public void run(){
                Intent intent = new Intent(getActivity(), RadioPlayerService.class).setAction(RadioPlayerService.ACTION_PLAY);
                intent.putExtra("title", universityName);
                intent.putExtra("url", universityUrl);
                getActivity().startService(intent);
            }
        };
        t.start();
    }
    private void stopService(){
        // Stop service
        Intent intent = new Intent(getActivity(), RadioPlayerService.class).setAction(RadioPlayerService.ACTION_STOP);
        getActivity().startService(intent);
       // equalizerView.stopBars();
        //equalizerView.setVisibility(View.INVISIBLE);
    }

    private void changeState(RadioMessage inputMessage) {
        radioMessage = inputMessage;
        if (Objects.equals(inputMessage.getUniversityName(), universityName)) {
            mState = inputMessage.getState();
        } else {
            mState = PlaybackStateCompat.STATE_NONE;
        }

        switch (mState) {
            case PlaybackStateCompat.STATE_PAUSED:
                playButtonImageView.setImageResource(R.drawable.play_button_selector);
                equalizerView.stopBars();
                equalizerView.setVisibility(View.INVISIBLE);
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                break;

            case PlaybackStateCompat.STATE_PLAYING:
                playButtonImageView.setImageResource(R.drawable.stop_button_selector);
                equalizerView.animateBars();
                equalizerView.setVisibility(View.VISIBLE);
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                break;

            case PlaybackStateCompat.STATE_BUFFERING:
                playButtonImageView.setImageResource(R.drawable.stop_button_selector);
                equalizerView.stopBars();
                equalizerView.setVisibility(View.INVISIBLE);
                progressDialog.show();
                break;

            case PlaybackStateCompat.STATE_STOPPED: default:
                playButtonImageView.setImageResource(R.drawable.play_button_selector);
                equalizerView.stopBars();
                equalizerView.setVisibility(View.INVISIBLE);
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                break;
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(RadioMessage radioMessage){
        changeState( radioMessage);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }



}
