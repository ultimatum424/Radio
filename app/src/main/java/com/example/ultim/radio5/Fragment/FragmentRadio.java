package com.example.ultim.radio5.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ultim.radio5.AppConstant;
import com.example.ultim.radio5.NavigationDrawerActivity;
import com.example.ultim.radio5.Pojo.RadioStateEvent;
import com.example.ultim.radio5.R;
import com.example.ultim.radio5.Radio.RadioService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import eu.gsottbauer.equalizerview.EqualizerView;


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
    RadioStateEvent.SateEnum state = RadioStateEvent.SateEnum.STOP;
    ProgressDialog progressDialog;

    // TODO: Rename and change types of parameters
    private String mParam1; //university-name

    private OnFragmentInteractionListener mListener;

    ImageView playButtonImageView;
    String universityName = "Волгатех";


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
    public static FragmentRadio newInstance(String param1) {
        FragmentRadio fragment = new FragmentRadio();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mParam1);
        }
        EventBus.getDefault().register(this);
    }

    View initView(View rootView){
        playButtonImageView = (ImageView) rootView.findViewById(R.id.content_play_btn);
        equalizerView = (EqualizerView) rootView.findViewById(R.id.equalaizer);
        equalizerView.setVisibility(View.INVISIBLE);
        playButtonImageView.setOnClickListener(this);
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            SpannableString ss2=  new SpannableString("Buffering");
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
            progressDialog.setMessage("Buffering");
        }
        return rootView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_radio, container, false);
        return initView(rootView);
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
        View subview = inflater.inflate(R.layout.fragment_radio, viewGroup);
        subview = initView(subview);
        changeState(state);

    }

    //    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        //((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Home");
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
        if (state == RadioStateEvent.SateEnum.PAUSE){
            EventBus.getDefault().post("start");
        }
        if( RadioService.stateRadio != RadioStateEvent.SateEnum.STOP){
            // Stop service
            Intent intent = new Intent(getActivity(), RadioService.class);
            //stopService(intent);
            getActivity().stopService(intent);
            equalizerView.stopBars();
            equalizerView.setVisibility(View.INVISIBLE);
        }
        else {
            // Start service
            Thread t = new Thread(){
                public void run(){
                    getContext().startService(new Intent(getActivity(), RadioService.class));
                    //Intent intent = new Intent(getActivity(), RadioService.class);
                   // getActivity().startService(intent);
                }
            };
            t.start();
           // Intent intent = new Intent(getActivity(), RadioService.class);
          //  getActivity().startService(intent);
        }

    }

    private void changeState(RadioStateEvent.SateEnum inputState){
        state = inputState;
        if (state == RadioStateEvent.SateEnum.STOP) {
            playButtonImageView.setImageResource(R.drawable.play_button_selector);
            equalizerView.stopBars();
            equalizerView.setVisibility(View.INVISIBLE);
            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
        if (state == RadioStateEvent.SateEnum.BUFFERING) {
            playButtonImageView.setImageResource(R.drawable.stop_button_selector);
            equalizerView.stopBars();
            equalizerView.setVisibility(View.INVISIBLE);;
            progressDialog.show();
        }
        if (state == RadioStateEvent.SateEnum.PLAY) {

            playButtonImageView.setImageResource(R.drawable.stop_button_selector);
            equalizerView.animateBars();
            equalizerView.setVisibility(View.VISIBLE);
            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
        if (state == RadioStateEvent.SateEnum.PAUSE) {

            playButtonImageView.setImageResource(R.drawable.play_button_selector);
            equalizerView.animateBars();
            equalizerView.setVisibility(View.INVISIBLE);
            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(RadioStateEvent event){
        changeState( event.getSateEnum());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }



}
