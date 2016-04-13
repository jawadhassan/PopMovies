package com.example.android.popmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private MyParcelable mMovieDetail;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);


//        Bundle bundle = getActivity().getIntent().getExtras();
//        Object obj = bundle.getParcelable("com.example.android.popmovies");

        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra("com.example.android.popmovies")) {
            mMovieDetail = intent.getExtras().getParcelable("com.example.android.popmovies");

        }
        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView releaseDate = (TextView) rootView.findViewById(R.id.releaseDate);
        TextView voteAverage = (TextView) rootView.findViewById(R.id.voteAverage);
        TextView overView = (TextView) rootView.findViewById(R.id.overView);
        if (mMovieDetail != null) {
            title.setText(mMovieDetail.title);
            releaseDate.setText(mMovieDetail.release_date);
            voteAverage.setText(mMovieDetail.vote_average);
            overView.setText(mMovieDetail.overview);
        }
        //mMovieDetail = intent.getStringExtra(Intent.EXTRA_TEXT);

        //  ((TextView) rootView.findViewById(R.id.detail_text))
        //          .setText(mMovieDetail);

        return rootView;
    }

}
