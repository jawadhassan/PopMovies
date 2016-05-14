package com.example.android.popmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    final String videos = "videos";
    ArrayAdapter<String> arrayAdapter;
    ListView mListView;
    List<String> movieEntries;
    String movieId;
    View rootView;
    private MyParcelable mMovieDetail;


    //private  static final int LOADER_ID = 1;

    public DetailActivityFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        movieEntries = new ArrayList<>();
//        movieEntries.add("check");
//        movieEntries.add("check");
//        movieEntries.add("check");
//        movieEntries.add("check");
//        movieEntries.add("check");
//        movieEntries.add("check");

        rootView = inflater.inflate(R.layout.fragment_detail, container, false);


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

        Log.v("check", mMovieDetail.id.toString());


        // mListView = (ListView) rootView.findViewById(R.id.movie_list_view);

//        getLoaderManager().initLoader(0, null, this);
        arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.fragement_movie_trailer_item_view, R.id.movie_trailer_text_view, movieEntries);
        //   mListView.setAdapter(arrayAdapter);


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
                Log.v(LOG_TAG, "ok" + TrailerURL.toString());

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
                Log.v(LOG_TAG, "check" + movieJsonTrailer);


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
            String trailerlink;
            ArrayList<String> youTubeLinks = new ArrayList<>();

            JSONObject jsonObject = new JSONObject(movieJsonTrailer);
            JSONArray jsonArray = jsonObject.getJSONArray(Results);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject idJsonObject = jsonArray.getJSONObject(i);
                trailerlink = idJsonObject.getString(KEY);
                youTubeLinks.add(trailerlink);
            }
            Log.v(LOG_TAG, "checklinks" + youTubeLinks.toString());
            return youTubeLinks.toArray(new String[0]);


        }

        @Override
        protected void onPostExecute(String[] strings) {

            if (strings != null) {
            }
            arrayAdapter.clear();
            for (String items : strings) {
                arrayAdapter.add(items);
            }
            arrayAdapter.notifyDataSetChanged();

        }


    }


}





