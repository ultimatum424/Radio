package com.example.ultim.radio5.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ultim.radio5.Genres.GenreData;
import com.example.ultim.radio5.Genres.GenreMessage;
import com.example.ultim.radio5.Genres.GenrePlayerService;
import com.example.ultim.radio5.Genres.TitleGenre;
import com.example.ultim.radio5.R;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Objects;


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
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mPlayListName;
    private OnFragmentInteractionListener mListener;
    private TextView mTextView;
    ProgressBar progressBar;
    private Button mButtonDownload;
    private Button mButtonPlay;
    private GenreData genreData;
    private GenreMessage genreMessage;

    boolean start;

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
        if (genreMessage == null) {
            genreMessage = new GenreMessage();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_genre, container, false);
        mTextView = (TextView) v.findViewById(R.id.genre_fragment_playlist_info_textview);
        mTextView.setText("Playlist " + mPlayListName);
        mButtonDownload = (Button) v.findViewById(R.id.download_genre);
        mButtonDownload.setOnClickListener(this);
        mButtonPlay = (Button) v.findViewById(R.id.play_genre) ;
        mButtonPlay.setOnClickListener(this);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        EventBus.getDefault().register(this);
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
        if (v == mButtonDownload) {
            DownloadFiles();
        }
        else if (v == mButtonPlay)
        {
            StartStopGenre();
        }
    }

    private void DownloadFiles() {
        String url = genreData.findItemByTitle(mPlayListName).getUrl(0);
        downloading = Ion.with(getActivity())
                .load(url)
                .progressBar(progressBar)
                .progressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        mTextView.setText("" + downloaded + " / " + total);
                    }
                })
                .write(getActivity().getFileStreamPath("genre" + mPlayListName + ".mp3"))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File result) {
                        if (e != null) {
                            Toast.makeText(getActivity(), "Error downloading file", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(getActivity(), "File upload complete", Toast.LENGTH_LONG).show();
                        genreData.findItemByTitle(mPlayListName).setFilePatch(Uri.fromFile(result), 0);
                        genreData.findItemByTitle(mPlayListName).setDownloadStatus(true);
                        genreData.saveData();
                    }
                });
    }
    private void runGenre()
    {
        TitleGenre.getInstance().setTitle(mPlayListName);
        Intent intent = new Intent(getActivity(), GenrePlayerService.class).setAction(GenrePlayerService.ACTION_PLAY);
        Uri uri = genreData.findItemByTitle(mPlayListName).getFilePatch(0);
        intent.setData(uri);
        intent.putExtra("title", mPlayListName);
        getActivity().startService(intent);
    }

    private void stopGenre()
    {
        Intent intent = new Intent(getActivity(), GenrePlayerService.class).setAction(GenrePlayerService.ACTION_STOP);
        getActivity().startService(intent);
    }

   private void StartStopGenre()
   {
       if (Objects.equals(genreMessage.getGenreName(), mPlayListName)) {
           if (genreMessage.getState() == PlaybackStateCompat.STATE_PAUSED){
               runGenre();
           }
           else if (genreMessage.getState() != PlaybackStateCompat.STATE_STOPPED){
               stopGenre();
           }
           else {
               runGenre();
           }
       }
       else {
           runGenre();
       }
   }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(GenreMessage genreMessage){
        changeState( genreMessage);
    }

    private void changeState(GenreMessage inputMessage) {
        genreMessage = inputMessage;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
