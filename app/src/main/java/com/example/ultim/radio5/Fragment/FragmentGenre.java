package com.example.ultim.radio5.Fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ultim.radio5.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentGenre.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentGenre#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentGenre extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mPlayListName;
    private OnFragmentInteractionListener mListener;
    private TextView mTextView;

    public FragmentGenre() {
        // Required empty public constructor
    }

    public static FragmentGenre newInstance(String playListName) {
        FragmentGenre fragment = new FragmentGenre();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, playListName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlayListName = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_genre, container, false);
        mTextView = (TextView) v.findViewById(R.id.genre_fragment_playlist_info_textview);
        mTextView.setText("Playlist " + mPlayListName);
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
