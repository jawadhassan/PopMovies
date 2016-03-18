package com.example.android.popmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class PopMovieFragment extends Fragment {

    public PopMovieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        View view = inflater.inflate(R.layout.fragment_main,container,false);
//
//        GridView gridView = (GridView) view.findViewById(R.id.gridview);
//
//        ImageAdapter imageAdapter = new ImageAdapter(getActivity());
//
//        gridView.setAdapter(imageAdapter);

////        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
////            @Override
////            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.v("check","ok");
//            }
//        });

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    public void updateMovies() {
        FetchMovieTask movieTask = new FetchMovieTask();
        movieTask.execute();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, String[]> {


        final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private String[] getMovieImageFromJson(String movieJsonStr)
                throws JSONException {

            ArrayList<String> movieResults = new ArrayList<>();
            String posterUrl;
            final String OWM_MOVIE_RESULTS = "results";
            JSONObject jsonObject = new JSONObject(movieJsonStr);
            if (jsonObject.has(OWM_MOVIE_RESULTS)) {
                JSONArray movieArray = jsonObject.getJSONArray(OWM_MOVIE_RESULTS);

                //JSONArray movieArray = jsonObject.getJSONArray(OWM_MOVIE_RESULTS);

                for (int i = 0; i < movieArray.length(); i++) {
                    JSONObject movieObject = movieArray.getJSONObject(i);
                    posterUrl = movieObject.getString("poster_path");
                    movieResults.add(posterUrl);
                    Log.v(LOG_TAG, posterUrl);
                }
                for (String items : movieResults){
                    Log.v(LOG_TAG,items);
                }



                // return null;
            }

            return movieResults.toArray(new String[0]);



            // Log.v(LOG_TAG,posterUrl);


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
                final String CRITERIA_FOR_MOVIE_SELECTION_TOP = "top_rated";
                final String CRITERIA_FOR_MOVIE_SELECTION_POPULAR = "popular";


//            URL url = new URL("https://api.themoviedb.org/3/movie/top_rated?api_key=73ffdcc612f97ab172f6411ce48c3a15");
                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(CRITERIA_FOR_MOVIE_SELECTION_TOP)
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_MOVIEDB_API_KEY)
                        .build();

                Log.v("TAG","BuiltURI" + builtUri.toString());

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
                reader = new BufferedReader(new InputStreamReader(inputstream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    movieJsonStr = null;
                }

                movieJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Movie Json String" + movieJsonStr);


            } catch (IOException e) {
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
                 String [] check_items = getMovieImageFromJson(movieJsonStr);

                for (String items : check_items){
                    Log.v(LOG_TAG,items+"new");
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;

        }
    }
}



//public  class ImageAdapter extends BaseAdapter{
//
//        private Context mContext;
//
//        public ImageAdapter(Context c){
//            mContext = c;
//
//        }
//
//        public int getCount(){
//            return mThumbsIds.length;
//        }
//
//        public Object getItem(int position){
//            return  null;
//        }
//
//        public long getItemId(int position){
//            return  0;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ImageView imageView;
//            if (convertView == null){
////            if it is not recycled, initialize some attributes
//                imageView = new ImageView(mContext);
//                imageView.setLayoutParams(new GridView.LayoutParams(85,85));
//                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                imageView.setPadding(8,8,8,8);
//
//            }else{
//                imageView = (ImageView) convertView;
//            }
//            imageView.setImageResource(mThumbsIds[position]);
//            return  imageView;
//        }
//
//        //references to our Images
//        private Integer[] mThumbsIds = {
//
//        };
//
//
//
//
//}



