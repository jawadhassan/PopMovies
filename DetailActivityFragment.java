package com.example.android.popmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    final String VIDEOS = "videos";
    final String REVIEWS = "reviews";
    ArrayAdapter<String> arrayAdapter;
    ListView mListView;
    List<String> movieEntries;
    List<String> reviewEntries;
    String movieId;
    View rootView;
    ListView mUserReviews;
    ArrayAdapter<String> movieAdapter;
    ArrayList<String> trailerList = new ArrayList<>();
    ArrayList<URL> YoutubeURLList = new ArrayList<>();
    String picturePath;
    private MyParcelable mMovieDetail;


    public DetailActivityFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        movieEntries = new ArrayList<>();
        reviewEntries = new ArrayList<>();


        rootView = inflater.inflate(R.layout.fragment_detail, container, false);




        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra("com.example.android.popmovies")) {
            mMovieDetail = intent.getExtras().getParcelable("com.example.android.popmovies");

        }

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

            releaseDate.setText(mMovieDetail.release_date);
            voteAverage.setText(mMovieDetail.vote_average);
            overView.setText(mMovieDetail.overview);
            movieId = mMovieDetail.id.toString();

        }

        mUserReviews = (ListView) rootView.findViewById(R.id.user_review_list);
        movieAdapter = new ArrayAdapter<String>(getActivity(), R.layout.fragment_user_reviews_item, R.id.user_review_text_view, reviewEntries);
        mUserReviews.setAdapter(movieAdapter);


        mListView = (ListView) rootView.findViewById(R.id.movie_list_view);


        arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.fragement_movie_trailer_item_view, R.id.movie_trailer_text_view, movieEntries);
        mListView.setAdapter(arrayAdapter);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!YoutubeURLList.isEmpty()) {
                URL TrailerURL = YoutubeURLList.get(position);
                // String TrailerURLString = TrailerURL.toString();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TrailerURL.toString()));
                    startActivity(intent);
                }


            }
        });


        Button favbutton = (Button) rootView.findViewById(R.id.favButton);
        favbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.d("check", "ok");

                        SavePictoDatabaseTask savePictoDatabaseTask = new SavePictoDatabaseTask();
                        savePictoDatabaseTask.execute();

                        MySQLiteHelper db = new MySQLiteHelper(getContext());
                        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("VOTES", mMovieDetail.vote_average);
                        contentValues.put("OVERVIEW", mMovieDetail.overview);
                        contentValues.put("RELEASEDATE", mMovieDetail.release_date);
                        contentValues.put("TITLE", mMovieDetail.title);
                        contentValues.put("ID", Integer.parseInt(mMovieDetail.id));


                        sqLiteDatabase.insert("MOVIE",

                                null,
                                contentValues);

                        db.close();


                    }
                }
        );


        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String criteria = sharedPreferences.getString(getString(R.string.pref_categories), getString(R.string.pref_categories_default));

        if (isConnectedToInternet()) {
            FetchMovieTrailerTask fetchMovieTrailerTask = new FetchMovieTrailerTask();
            fetchMovieTrailerTask.execute(movieId, VIDEOS);

            FetchMovieReviewTask fetchMovieReviewTask = new FetchMovieReviewTask();
            fetchMovieReviewTask.execute(movieId, REVIEWS);

        }

        arrayAdapter.clear();
        arrayAdapter.add("Not available due to connectivity");

        arrayAdapter.notifyDataSetChanged();
        movieAdapter.clear();
        movieAdapter.add("Not available due to connectivity");
        movieAdapter.notifyDataSetChanged();


    }

    public boolean isConnectedToInternet() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(getContext().CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
            return connected;
        }
        return connected;

    }

    public String DetailActivityTrailerProvider() {
        return YoutubeURLList.get(0).toString();
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
                } else {

                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream != null ? inputStream : null));
                }
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

            arrayAdapter.clear();
            for (String items : strings) {

                try {
                    final String YOUTUBE_TRAILER_LINK_URL = "https://www.youtube.com/watch";
                    final String YOUTUBE_APPID = "v";
                    Uri builtYouTubeTrailerURI = Uri.parse(YOUTUBE_TRAILER_LINK_URL).buildUpon()
                            .appendQueryParameter(YOUTUBE_APPID, items)
                            .build();
                    URL builtYoutubeTrailerURL = new URL(builtYouTubeTrailerURI.toString());
                    YoutubeURLList.add(builtYoutubeTrailerURL);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                //trailerList.add(items);
                arrayAdapter.add("Trailers");
            }
                arrayAdapter.notifyDataSetChanged();
            }

        }


    }

    class FetchMovieReviewTask extends AsyncTask<String, Void, Map<String, String>> {
        @Override
        protected Map doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            String movieReviews = null;
            try {
                final String MOVIE_TRAILER_BASE_URL = "https://api.themoviedb.org/3/movie";
                final String MOVIE_YOUTUBE_APPID = "api_key";
                Uri builtTrailerURI = Uri.parse(MOVIE_TRAILER_BASE_URL).buildUpon()
                        .appendPath(params[0])
                        .appendPath(params[1])
                        .appendQueryParameter(MOVIE_YOUTUBE_APPID, BuildConfig.OPEN_MOVIEDB_API_KEY)
                        .build();

                URL userReviewsTrailer = new URL(builtTrailerURI.toString());
                Log.v("check", "" + userReviewsTrailer.toString());

                httpURLConnection = (HttpURLConnection) userReviewsTrailer.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if (inputStream == null) {
                    stringBuffer = null;

                }

                // Lintcorrections
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream != null ? inputStream : null));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line + "\n");

                }
                if ((stringBuffer != null ? stringBuffer.length() : 0) == 0) {
                    movieReviews = null;
                }
                movieReviews = stringBuffer.toString();
                Log.v("check", movieReviews.toString());


            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                movieReviews = null;
            } catch (IOException ex) {
                ex.printStackTrace();
                movieReviews = null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }
            }

            try {
                return getMovieReviewsDataFromJSON(movieReviews);
            } catch (JSONException ex) {
                ex.printStackTrace();

            }


            //return new String[0];
            return null;
        }

        private Map getMovieReviewsDataFromJSON(String movieReviews) throws JSONException {
            final String RESULTS = "results";
            final String AUTHOR = "author";
            final String CONTENT = "content";

            String author;
            String review;

            Map<String, String> reviewMap = new HashMap();

            JSONObject jsonObject = new JSONObject(movieReviews);
            JSONArray jsonArray = jsonObject.getJSONArray(RESULTS);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject reviewObject = jsonArray.getJSONObject(i);
                author = reviewObject.getString(AUTHOR);
                review = reviewObject.getString(CONTENT);

                reviewMap.put(author, review);
            }

            return reviewMap;


        }

        @Override
        protected void onPostExecute(Map<String, String> map) {
            if (map != null) {
                movieAdapter.clear();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    movieAdapter.add("" + entry.getValue() + "\n" + entry.getKey());


                }
                movieAdapter.notifyDataSetChanged();

            }
        }
    }

    class SavePictoDatabaseTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {

            try {

                Bitmap bitmap = Picasso.with(getContext()).load(mMovieDetail.urlcompared).get();
                File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                        + "/Android/data/"
                        + getContext().getApplicationContext().getPackageName()
                        + "/Files"

                );

                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        return null;
                    }
                }

                // String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                File mediaFile;
                String mImageName = mMovieDetail.id + ".jpg";

                mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);


                File pictureFile = mediaFile;
                Log.v("check", pictureFile.toString());
                ContentValues values = new ContentValues();
                values.put("POSTERPATH", "file://" + pictureFile.toString());

                MySQLiteHelper mySQLiteHelper = new MySQLiteHelper(getContext());
                SQLiteDatabase sqLiteDatabase = mySQLiteHelper.getWritableDatabase();
                sqLiteDatabase.update("MOVIE",
                        values,
                        "ID" + "=?", new String[]{String.valueOf(mMovieDetail.id)});


                if (pictureFile == null) {
                    Log.v("check", "permission check kar !!!! ");
                }
                FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

                fileOutputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return null;
        }
    }


}





