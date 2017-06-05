package com.example.ultim.radio5.Fragment;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ultim.radio5.Genres.GenreData;
import com.example.ultim.radio5.Genres.GenreMessage;
import com.example.ultim.radio5.Genres.GenrePlayerService;
import com.example.ultim.radio5.Genres.TitleGenre;
import com.example.ultim.radio5.R;
import com.example.ultim.radio5.RadioPlayerService;
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

    private LinearLayout downloadLayout;
    private LinearLayout playLayout;
    private TextView mTextView;
    private TextView mSongTitle;
    ProgressBar progressBar;
    private Button mButtonDownload;
    private Button mButtonPlay;
    private Button mButtonNext;
    private Button mButtonPreview;
    private GenreData genreData;
    private GenreMessage genreMessage;
    private int currentPlay;



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
        downloadLayout = (LinearLayout) v.findViewById(R.id.download_layout);
        playLayout = (LinearLayout) v.findViewById(R.id.play_layout);
        mTextView = (TextView) v.findViewById(R.id.genre_fragment_playlist_info_textview);
        mTextView.setText("Playlist " + mPlayListName);
        mSongTitle = (TextView) v.findViewById(R.id.song_title);
        mButtonDownload = (Button) v.findViewById(R.id.download_genre);
        mButtonDownload.setOnClickListener(this);
        mButtonPlay = (Button) v.findViewById(R.id.play_genre) ;
        mButtonPlay.setOnClickListener(this);
        mButtonNext = (Button) v.findViewById(R.id.next_genre) ;
        mButtonNext.setOnClickListener(this);
        mButtonPreview = (Button) v.findViewById(R.id.prev_genre) ;
        mButtonPreview.setOnClickListener(this);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        EventBus.getDefault().register(this);

        if (genreData.findItemByTitle(mPlayListName).isDownloadStatus()){
            playLayout.setVisibility(View.VISIBLE);
            downloadLayout.setVisibility(View.GONE);
        } else {
            playLayout.setVisibility(View.GONE);
            downloadLayout.setVisibility(View.VISIBLE);
        }
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

            for (int i = 0; i < genreData.findItemByTitle(mPlayListName).getLength(); i++){
                DownloadFiles(i);
            }
        }
        else if (v == mButtonPlay)
        {
            StartStopGenre();
        }
        else if (v == mButtonNext){
            clickNext();
        }
        else if (v == mButtonPreview){
            clickPreview();
        }
    }

    private void DownloadFiles(final int i) {
        String url = genreData.findItemByTitle(mPlayListName).getUrl(i);
        downloading = Ion.with(getActivity())
                .load(url)
                .progressBar(progressBar)
                .progressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        //mTextView.setText("" + downloaded + " / " + total);
                       // progressBar.setProgress((int) (30));
                    }
                })
                .write(getActivity().getFileStreamPath("genre" + mPlayListName + i + ".mp3"))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File result) {
                        if (e != null) {
                            Toast.makeText(getActivity(), "Error downloading file", Toast.LENGTH_LONG).show();
                            return;
                        }

                        genreData.findItemByTitle(mPlayListName).setFilePatch(Uri.fromFile(result), i);
                        genreData.findItemByTitle(mPlayListName).setDownloadStatus(true);
                        genreData.saveData();
                        if (i == genreData.findItemByTitle(mPlayListName).getLength() - 1){
                            Toast.makeText(getActivity(), "File upload complete", Toast.LENGTH_LONG).show();
                            playLayout.setVisibility(View.VISIBLE);
                            downloadLayout.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void clickNext(){
        if (genreMessage.getPlay() < genreData.findItemByTitle(mPlayListName).getLength() - 1){
            runGenre(genreMessage.getPlay() + 1);
        } else {
            runGenre(0);

        }
    }

    private void clickPreview(){
        if (genreMessage.getPlay() >0){
            runGenre(genreMessage.getPlay() - 1);
        } else {
            runGenre(0);
        }
    }

    private void runGenre(int i)
    {
        TitleGenre.getInstance().setTitle(mPlayListName);
        Intent intent = new Intent(getActivity(), GenrePlayerService.class).setAction(GenrePlayerService.ACTION_PLAY);
        Uri uri = genreData.findItemByTitle(mPlayListName).getFilePatch(i);
        intent.setData(uri);
        intent.putExtra("title", mPlayListName);
        intent.putExtra("num", i);
        mButtonPlay.setText("Pause");
        mSongTitle.setText(genreData.findItemByTitle(mPlayListName).getList()[i]);
        getActivity().startService(intent);
    }

    private void stopGenre() {
        Intent intent = new Intent(getActivity(), GenrePlayerService.class).setAction(GenrePlayerService.ACTION_PAUSE);
       // mSongTitle.setText(" ");
        mButtonPlay.setText("Play");
        getActivity().startService(intent);

    }

   private void StartStopGenre()
   {
       if(isServiceRunning()){
           Intent intent = new Intent(getActivity(), RadioPlayerService.class).setAction(RadioPlayerService.ACTION_STOP);
           getActivity().startService(intent);
       }
       if (Objects.equals(genreMessage.getGenreName(), mPlayListName)) {
           if (genreMessage.getState() == PlaybackStateCompat.STATE_PAUSED){
               runGenre(genreMessage.getPlay());
           }
           else if (genreMessage.getState() != PlaybackStateCompat.STATE_STOPPED){
               stopGenre();
           }
           else {
               runGenre(genreMessage.getPlay());
           }
       }
       else {
           runGenre(0);
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
        if (genreMessage.getState() != PlaybackState.STATE_PLAYING){
            mButtonPlay.setText("Play");
        }else {
            mButtonPlay.setText("Pause");
        }
        mSongTitle.setText(genreData.findItemByTitle(genreMessage.getGenreName()).getList()[genreMessage.getPlay()]);
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(RadioPlayerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
