package com.example.ultim.radio5.Fragment;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ultim.radio5.Genres.GenreData;
import com.example.ultim.radio5.NavigationDrawerActivity;
import com.example.ultim.radio5.R;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentGenre.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentGenre#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentGenre extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mPlayListName;
    private OnFragmentInteractionListener mListener;
    private TextView mTextView;
    ProgressBar progressBar;
    private Button mButtonDownload;
    private GenreData genreData;

    Future<File> downloading;

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
        Ion.getDefault(getActivity()).configure().setLogging("ion-sample", Log.DEBUG);
        genreData = new GenreData(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_genre, container, false);
        mTextView = (TextView) v.findViewById(R.id.genre_fragment_playlist_info_textview);
        mTextView.setText("Playlist " + mPlayListName);
        mButtonDownload = (Button) v.findViewById(R.id.download_genre);
        mButtonDownload.setOnClickListener(this);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
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

    @Override
    public void onClick(View v) {
        downloading = Ion.with(getActivity())
                .load(genreData.findItemByTitle(mPlayListName).getUrl())
                //.load("https://downloader.disk.yandex.ru/disk/5e8fb36950e521398982b67596283aaf742276d2ffc4390c48d7d76e3e44e3c3/593434b8/AhqCbS4YLYWsy1JxRIDicPWE2ERwApl6bR8JOj2ZEhOg4lVjjOlpi4WPpT6bGZaoNY5DiKGETHBXfxcHrfJ9EQ%3D%3D?uid=0&filename=moby_-_extreme_ways_%28zvukoff.ru%29.mp3&disposition=attachment&hash=VmUiODl0hcGP7j5KDDzO7cEsmd8Ecec8d/vhf1l7bec%3D%3A&limit=0&content_type=audio%2Fmpeg&fsize=3808488&hid=c443be5b1e9331a1a5d99a2e7982bc7a&media_type=audio&tknv=v2")
                .progressBar(progressBar)
                .progressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        mTextView.setText("" + downloaded + " / " + total);
                    }
                })
                .write(getActivity().getFileStreamPath("genre" + genreData.findItemByTitle(mPlayListName).getName() + ".mp3"))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File result) {
                        if (e != null) {
                            Toast.makeText(getActivity(), "Error downloading file", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(getActivity(), "File upload complete", Toast.LENGTH_LONG).show();
                        genreData.findItemByTitle(mPlayListName).setFilePatch(Uri.fromFile(result));
                        genreData.findItemByTitle(mPlayListName).setDownloadStatus(true);
                    }
                });
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
