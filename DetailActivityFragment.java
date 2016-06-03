package com.example.android.popmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    final String videos = "videos";


    String movieId;
    View rootView;
    ArrayList<String> myDataSet = new ArrayList<>();

    private MyParcelable mMovieDetail;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;



    public DetailActivityFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        rootView = inflater.inflate(R.layout.fragment_detail, container, false);



        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra("com.example.android.popmovies")) {
            mMovieDetail = intent.getExtras().getParcelable("com.example.android.popmovies");

        }
        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView releaseDate = (TextView) rootView.findViewById(R.id.releaseDate);
        TextView voteAverage = (TextView) rootView.findViewById(R.id.voteAverage);
        TextView overView = (TextView) rootView.findViewById(R.id.overView);
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);

        Picasso.with(getContext()).load(mMovieDetail.urlcompared)
                .error(R.drawable.user_placeholder_error)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        imageView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                        imageView.setVisibility(View.INVISIBLE);

                    }
                });
        if (mMovieDetail != null) {
            title.setText(mMovieDetail.title);
            releaseDate.setText(mMovieDetail.release_date);
            voteAverage.setText(mMovieDetail.vote_average);
            overView.setText(mMovieDetail.overview);
            movieId = mMovieDetail.id.toString();

        }
        //mMovieDetail = intent.getStringExtra(Intent.EXTRA_TEXT);

        //  ((TextView) rootView.findViewById(R.id.detail_text))
        //          .setText(mMovieDetail);


        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.movie_trailer_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());


        mAdapter = new MyAdapter(myDataSet);


        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchMovieTrailerTask fetchMovieTrailerTask = new FetchMovieTrailerTask();
        fetchMovieTrailerTask.execute(movieId, videos);
    }


    class FetchMovieTrailerTask extends AsyncTask<String, Void, String[]> {

        final String LOG_TAG = FetchMovieTrailerTask.class.getSimpleName();


        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            //Movie Trailer JSON
            String movieJsonTrailer = null;
            try {
                final String MOVIE_TRAILER_BASE_URL = "https://api.themoviedb.org/3/movie";
                final String MOVIE_YOUTUBE_APPID = "api_key";

                Uri builtTrailerURI = Uri.parse(MOVIE_TRAILER_BASE_URL).buildUpon()
                        .appendPath(params[0])
                        .appendPath(params[1])
                        .appendQueryParameter(MOVIE_YOUTUBE_APPID, BuildConfig.OPEN_MOVIEDB_API_KEY)
                        .build();
                // May through malformed url exception
                URL TrailerURL = new URL(builtTrailerURI.toString());


                httpURLConnection = (HttpURLConnection) TrailerURL.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    movieJsonTrailer = null;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line + "\n");

                }
                if (buffer.length() == 0) {
                    movieJsonTrailer = null;
                }
                movieJsonTrailer = buffer.toString();



            } catch (Exception ex) {
                ex.printStackTrace();
                movieJsonTrailer = null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException ex) {
                        Log.e(LOG_TAG, "check" + ex);
                    }
                }
            }

            try {
                return getMovieTrailerDataFromJson(movieJsonTrailer);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }


            return null;
        }

        private String[] getMovieTrailerDataFromJson(String movieJsonTrailer) throws JSONException {


            final String KEY = "key";
            final String Results = "results";
            String trailerLink;
            ArrayList<String> youTubeLinks = new ArrayList<>();

            JSONObject jsonObject = new JSONObject(movieJsonTrailer);
            JSONArray jsonArray = jsonObject.getJSONArray(Results);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject idJsonObject = jsonArray.getJSONObject(i);
                trailerLink = idJsonObject.getString(KEY);
                youTubeLinks.add(trailerLink);
            }

            return youTubeLinks.toArray(new String[0]);


        }

        @Override
        protected void onPostExecute(String[] strings) {

            if (strings != null) {


                for (String items : strings) {
                    Log.v(LOG_TAG, items);
                    myDataSet.add(items);
                    Log.v(LOG_TAG, myDataSet.toString());


            }

                mAdapter.notifyDataSetChanged();

        }

        }


    }


}


class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    public ArrayList<String> mDataSet = new ArrayList<>();

    public MyAdapter(ArrayList<String> myDataSet) {


        for (String items : myDataSet) {
            //Log.v("checkitems",mDataSet.toString());
            //mDataSet.add(items);
            mDataSet.add("check");
            mDataSet.add("check");
            mDataSet.add("check");

        }

    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragement_movie_trailer_item_view, parent, false);
        ViewHolder vh = new ViewHolder((TextView) v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mDataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
        //return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

}







