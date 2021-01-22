package com.example.githubrepos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import android.net.Network;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.widget.Toast;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

    private EditText mSearchBoxEditText;

    private TextView mUrlDisplayTextView;
    private TextView mErrorMsg;

    private TextView mSearchResultsTextView;
    private ProgressBar progress;
    private static final int GITHUB_SEARCH_LOAD=22;
    final static String GITHUB_BASE_URL="https://api.github.com/repositories";
    final static String PARAM_QUERY="q";
    final static String PARAM_SORT="sort";
    private static final String SEARCH_QUERY_URL_EXTRA="query";
    private static final String SEARCH_RESULTS_RAW_JSON="reaults";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    mSearchBoxEditText=findViewById(R.id.git_s);
    mUrlDisplayTextView=findViewById(R.id.head);
    mSearchResultsTextView=findViewById(R.id.git_d);
    mErrorMsg=findViewById(R.id.error);
    progress=findViewById(R.id.p_bar);
    if (savedInstanceState!=null){
        String queryUrl =savedInstanceState.getString(SEARCH_QUERY_URL_EXTRA);

    }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    String QueryURL=mUrlDisplayTextView.getText().toString();
    outState.putString(SEARCH_QUERY_URL_EXTRA,QueryURL);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }
    private void makeGithubSearchQuery(){
        String githubQuery=mSearchBoxEditText.getText().toString();
        URL githubSearchUrl=NetworkUtils.buildUrl(githubQuery);
        mUrlDisplayTextView.setText(githubSearchUrl.toString());
        String githubSearchResults=null;

Bundle queryBundle=new Bundle();
queryBundle.putString(SEARCH_QUERY_URL_EXTRA,githubSearchUrl.toString());
LoaderManager loaderManager=getSupportLoaderManager();
Loader<String> githubSearchLoader=loaderManager.getLoader(GITHUB_SEARCH_LOAD);
if(githubSearchLoader==null){
    loaderManager.initLoader(GITHUB_SEARCH_LOAD,queryBundle,this);
}
else{
    loaderManager.restartLoader(GITHUB_SEARCH_LOAD,queryBundle,this);
}
    }
    private void showJsonDataView(){
        mErrorMsg.setVisibility(View.INVISIBLE);
        mSearchResultsTextView.setVisibility(View.VISIBLE);


    }
    private void showErrorMsg(){
        mErrorMsg.setVisibility(View.VISIBLE);
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int menuItemThatWasSelecteted=item.getItemId();
        if(menuItemThatWasSelecteted==R.id.action_search){
            makeGithubSearchQuery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {

        return new AsyncTaskLoader<String>(this) {
            @Nullable
            @Override
            public String loadInBackground() {
                String searchQueryUrlString=args.getString(SEARCH_QUERY_URL_EXTRA);
                if(searchQueryUrlString==null || TextUtils.isEmpty(searchQueryUrlString)){
                    return null;
                }

                try{URL githubUrl=new URL(searchQueryUrlString);
                    return NetworkUtils.getResponseFromHttpUrl(githubUrl);
                }
                catch (IOException e){
                    e.printStackTrace();


                }

                return null;
            }

            @Override
            protected void onStartLoading() {

                super.onStartLoading();
                if(args==null){
                    return;
                }
                progress.setVisibility(View.VISIBLE);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        progress.setVisibility(View.INVISIBLE);
        if(data!=null && !data.equals("")){
            showJsonDataView();
            mSearchResultsTextView.setText(data);

        }
        else{
            showErrorMsg();

        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    public class GithubQueryTask extends AsyncTask<URL,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(URL... urls){
            URL searchUrl=urls[0];
            String githubSearchResults=null;
            try{
                githubSearchResults=NetworkUtils.getResponseFromHttpUrl(searchUrl);

            }
            catch (IOException e){
                e.printStackTrace();
            }
return githubSearchResults;

        }
        @Override

        protected void onPostExecute(String s){
            progress.setVisibility(View.INVISIBLE);
            if(s!=null && !s.equals("")){
                showJsonDataView();
                mSearchResultsTextView.setText(s);

            }
            else{
                showErrorMsg();

            }
        }

    }
}
