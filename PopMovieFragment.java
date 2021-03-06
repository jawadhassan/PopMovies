package com.example.android.popmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class PopMovieFragment extends Fragment {
    ImageAdapter imageAdapter;
    JSONArray movieArray;
    String criteria;
    MyParcelable myParcelable;

    public PopMovieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) view.findViewById(R.id.gridview);

        imageAdapter = new ImageAdapter(getActivity(), new ArrayList<String>());

        gridView.setAdapter(imageAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Map movieObjectDetailList;
                String urlcompared = imageAdapter.getItem(position);
                MovieObjectDetail movieObjectDetail = new MovieObjectDetail();
                if (criteria.equals("favourite")) {
                    String savedurlcompared = imageAdapter.getItem(position);
                    MySQLiteHelper mySQLiteHelper = new MySQLiteHelper(getContext());
                    SQLiteDatabase sqLiteDatabase = mySQLiteHelper.getWritableDatabase();
                    Cursor cursor = sqLiteDatabase.query("MOVIE", null,
                            "POSTERPATH=?", new String[]{String.valueOf(savedurlcompared)}, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        myParcelable = new MyParcelable(cursor.getString(cursor.getColumnIndex("TITLE")), cursor.getString(cursor.getColumnIndex("RELEASEDATE")), cursor.getString(cursor.getColumnIndex("OVERVIEW")), cursor.getString(cursor.getColumnIndex("VOTES")), cursor.getString(cursor.getColumnIndex("POSTERPATH")), cursor.getString(cursor.getColumnIndex("ID")));
                        Intent intent = new Intent(getContext(), DetailActivity.class);
                        intent.putExtra("com.example.android.popmovies", myParcelable);
                        startActivity(intent);
                    }


                } else {

                    try {
                        movieObjectDetailList = movieObjectDetail.getMovieDetailsFromJson(movieArray, urlcompared);
                        myParcelable = new MyParcelable(movieObjectDetailList.get("title").toString(), movieObjectDetailList.get("release_date").toString(), movieObjectDetailList.get("overview").toString(), movieObjectDetailList.get("vote_average").toString(), movieObjectDetailList.get("poster_url").toString(), movieObjectDetailList.get("id").toString());
                        Intent intent = new Intent(getContext(), DetailActivity.class);
                        intent.putExtra("com.example.android.popmovies", myParcelable);
                        startActivity(intent);


                    } catch (Exception e) {
                        //Log.v("check", e.toString());
                    }
                }



            }
        });


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }


    public void updateMovies() {
        FetchMovieTask movieTask = new FetchMovieTask();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        criteria = sharedPreferences.getString(getString(R.string.pref_categories), getString(R.string.pref_categories_default));
        if (criteria.equals("favourite")) {
            FetchSavedFavourites fetchSavedFavourites = new FetchSavedFavourites();
            fetchSavedFavourites.execute();
        } else {


            movieTask.execute(criteria);
        }
    }

    public class FetchMovieTask extends AsyncTask<String, Void, String[]> {


        final String LOG_TAG = FetchMovieTask.class.getSimpleName();


        private String[] getMovieImageFromJson(String movieJsonStr)
                throws JSONException {

            ArrayList<String> movieResults = new ArrayList<>();
            String posterUrl;
            final String OWM_MOVIE_RESULTS = "results";
            try {
                JSONObject jsonObject = new JSONObject(movieJsonStr);
                if (jsonObject.has(OWM_MOVIE_RESULTS)) {
                    movieArray = jsonObject.getJSONArray(OWM_MOVIE_RESULTS);

                    //JSONArray movieArray = jsonObject.getJSONArray(OWM_MOVIE_RESULTS);

                    for (int i = 0; i < movieArray.length(); i++) {
                        JSONObject movieObject = movieArray.getJSONObject(i);
                        posterUrl = movieObject.getString("poster_path");
                        movieResults.add(posterUrl);
                        Log.v(LOG_TAG, posterUrl);
                    }
//                for (String items : movieResults){
//                    //Log.v(LOG_TAG,items);
//                }


                    // return null;
                }

                return movieResults.toArray(new String[0]);

            } catch (Exception ex) {
                Log.v(LOG_TAG, ex.toString());
            }



            // Log.v(LOG_TAG,posterUrl);
            return null;

        }


        @Override
        protected String[] doInBackground(String... params) {


            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            String movieJsonStr = null;
            //parameters for api link will be here


            try {

                final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie";
                final String APPID_PARAM = "api_key";
                //final String CRITERIA_FOR_MOVIE_SELECTION_TOP = "top_rated";
                //final String CRITERIA_FOR_MOVIE_SELECTION_POPULAR = "popular";


//            URL url = new URL("https://api.themoviedb.org/3/movie/top_rated?api_key=73ffdcc612f97ab172f6411ce48c3a15");
                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(params[0])
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_MOVIEDB_API_KEY)
                        .build();

                Log.v("TAG", "BuiltURI" + builtUri.toString());

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read input stream into a String

                InputStream inputstream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputstream == null) {

                    movieJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputstream != null ? inputstream : null));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    movieJsonStr = null;
                }

                movieJsonStr = buffer.toString();

                //Log.v(LOG_TAG, "Movie Json String" + movieJsonStr);


            } catch (IOException e) {
                Log.v(LOG_TAG, "Check Your Internet Connection");
                Log.e(LOG_TAG, "error here" + e);


                movieJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "ErrorClosingStream", e);
                    }
                }


            }

            try {
                // here will be method call for JSon parsing.
                String[] check_items = getMovieImageFromJson(movieJsonStr);



                return check_items;

            } catch (Exception e) {
                //Log.e(LOG_TAG, e.getMessage(), e);
                Log.v("check", "Connection Lost");
                e.printStackTrace();
            }
            return null;

        }


        @Override
        protected void onPostExecute(String[] result) {

            if (result != null) {
                imageAdapter.clear();

                for (String resultItems : result) {
                    try {
                        //Log.v(LOG_TAG, resultItems + "GoodLuck");
                        final String PICTURE_BASE_URL = "http://image.tmdb.org/t/p/w185/";
                        final String PICTURE_URL_END = resultItems;
                        Uri builtUri = Uri.parse(PICTURE_BASE_URL).buildUpon()
                                .appendPath(PICTURE_URL_END.replace("/", ""))
                                .build();

                        imageAdapter.setmresultItems(new String((new URL(builtUri.toString()).toString())));
                        //      Log.v(LOG_TAG, resultItems + "GoodLuck" + builtUri.toString());

                    } catch (MalformedURLException ex) {
                        //Log.e(LOG_TAG, "onPostExecute: " );
                    }
                }
                //  Log.d(LOG_TAG,"the count"+imageAdapter.getCount());
                imageAdapter.notifyDataSetChanged();
            }
        }

    }

    public class FetchSavedFavourites extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {

            ArrayList<String> arrayList = new ArrayList<>();

            MySQLiteHelper mySQLiteHelper = new MySQLiteHelper(getContext());
            SQLiteDatabase sqLiteDatabase = mySQLiteHelper.getWritableDatabase();

            String query = "SELECT * FROM MOVIE";
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    String temp = cursor.getString(cursor.getColumnIndex("POSTERPATH"));
                    arrayList.add(temp);
                } while (cursor.moveToNext());
            }
            Log.v("check", arrayList.toString());


            return arrayList.toArray(new String[0]);
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                imageAdapter.clear();
                for (String items : strings) {
                    imageAdapter.setmresultItems(items);
                }
                imageAdapter.notifyDataSetChanged();
            }
        }
    }




}


class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> mresultItems;


    public ImageAdapter(Context c, ArrayList<String> resultItems) {
        mContext = c;
        mresultItems = resultItems;

    }

    public int getCount() {
        // return mThumbsIds.length;
        return mresultItems.size();
    }

    public String getItem(int position) {
        return mresultItems.get(position);


        //return  null;
    }

    public long getItemId(int position) {
        return 0;
    }

    void clear() {
        mresultItems.clear();
    }


    public void setmresultItems(String resultItems) {
        mresultItems.add(resultItems);
        //  Log.v("check", mresultItems.toString());

    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageView imageView;

        if (convertView == null) {
//            if it is not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            //imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 278));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);

        } else {

            imageView = (ImageView) convertView;
        }


        String picUrl = getItem(position);


        Picasso.with(mContext).load(picUrl)
                .resize(185, 278)
                .centerCrop()
                //.placeholder(R.drawable.user_placeholder)
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
                        }
                );
//            Log.v("check", "check",new Exception());
        //imageView.setImageResource(R.drawable.user_placeholder);
        return imageView;
    }


    //references to our Images

    //private Integer[] mThumbsIds ;
}

class MovieObjectDetail {
    public Map getMovieDetailsFromJson(JSONArray movieArray, String urlcompared) throws JSONException {
        Map movieObjectDetailList = new HashMap();
        //ArrayList movieObjectDetailList = new ArrayList();
        String posterUrl = urlcompared;
        int Index = posterUrl.lastIndexOf("/");
        posterUrl = posterUrl.substring(Index);
        //posterUrl = "\\"+posterUrl;
        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject movieObjectDetail = movieArray.getJSONObject(i);
            //Log.v("check", movieObjectDetail.getString("poster_path"));

            if ((movieObjectDetail.getString("poster_path")).equals(posterUrl)) {

                movieObjectDetailList.put("title", movieObjectDetail.getString("title"));
                movieObjectDetailList.put("release_date", movieObjectDetail.getString("release_date"));
                movieObjectDetailList.put("overview", movieObjectDetail.getString("overview"));
                movieObjectDetailList.put("vote_average", movieObjectDetail.getString("vote_average"));
                movieObjectDetailList.put("poster_url", urlcompared.toString());
                movieObjectDetailList.put("id", movieObjectDetail.getString("id"));

                //movieObjectDetailList.add(movieObjectDetail.getString("title"));

            }
        }
        return movieObjectDetailList;
    }


}

class MyParcelable implements Parcelable {

    public static final Parcelable.Creator<MyParcelable> CREATOR
            = new Parcelable.Creator<MyParcelable>() {
        @Override
        public MyParcelable createFromParcel(Parcel in) {
            return new MyParcelable(in);
        }

        @Override
        public MyParcelable[] newArray(int size) {
            return new MyParcelable[size];
        }
    };
    String title, release_date, overview, vote_average, urlcompared, id;

    public MyParcelable(String title, String release_date, String overview, String vote_average, String urlcompared, String id) {
        this.title = title;
        this.release_date = release_date;
        this.overview = overview;
        this.vote_average = vote_average;
        this.urlcompared = urlcompared;
        this.id = id;
    }

    private MyParcelable(Parcel in) {
        title = in.readString();
        release_date = in.readString();
        overview = in.readString();
        vote_average = in.readString();
        urlcompared = in.readString();
        id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(release_date);
        dest.writeString(overview);
        dest.writeString(vote_average);
        dest.writeString(urlcompared);
        dest.writeString(id);

    }

    @Override
    public int describeContents() {
        return 0;
    }

}

