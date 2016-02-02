package me.blog.colombia2.schoolparser;

import android.app.*;
import android.os.*;
import android.util.*;
import android.widget.*;
import android.net.*;
import android.view.*;
import android.content.*;
import java.util.*;

import android.support.v7.app.*;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.design.widget.*;

public class MainActivity extends AppCompatActivity  {
    private class MyScrollListener extends RecyclerView.OnScrollListener {
        private LinearLayoutManager llm;
        
        public MyScrollListener(LinearLayoutManager llm) {
            super();
            this.llm = llm;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            refresh.setEnabled(llm.findFirstCompletelyVisibleItemPosition() == 0);
        }
    }
    
    private LinearLayout mainContent;
    private Parser parser;
    private RecyclerView articles;
    protected SwipeRefreshLayout refresh;
    private BroadcastReceiver completeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent i) {
            Snackbar.make(mainContent, "/sdcard/Download/에 다운로드가 완료되었습니다.", Snackbar.LENGTH_SHORT).show();
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mainContent = (LinearLayout) findViewById(R.id.maincontent);
        articles = (RecyclerView) findViewById(R.id.articles);
        final LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        articles.setLayoutManager(llm);
        articles.setOnScrollListener(new MyScrollListener(llm));
        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                parser.start();
            }
        });
        refresh.post(new Runnable() {
            @Override
            public void run() {
                refresh.setRefreshing(true);
            }
        });
        
        parser = new Parser(
                "http://cw.hs.kr/index.jsp?SCODE=S0000000213&mnu=M001013"
                /*"http://cw.hs.kr/index.jsp?SCODE=S0000000213&mnu=M001002003"*/
                , new Parser.onParseFinishListener() {
                    @Override
                    public void onFinish(final ArrayList<String[]> list, final ArrayList<ArrayList<String[]>> files) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                articles.setVisibility(View.VISIBLE);
                                refresh.setRefreshing(false);
                                
                                ArticleAdapter adapter = new ArticleAdapter(MainActivity.this, list, files);
                                articles.setAdapter(adapter);
                            }
                        });
                    }
                    
                    @Override
                    public void onInternetError(final Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                articles.setVisibility(View.INVISIBLE);
                                final Snackbar snackbar = Snackbar.make(mainContent, R.string.check_internet, Snackbar.LENGTH_INDEFINITE);
                                snackbar.setAction(R.string.retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        snackbar.dismiss();
                                        parser.start();
                                    }
                                });
                                snackbar.show();
                                refresh.setRefreshing(false);
                            }
                        });
                    }
                });
        parser.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter iFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(completeReceiver, iFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(completeReceiver);
    }
    
    public void changeActivity(ArrayList<String[]> files) {
        Intent i = new Intent(MainActivity.this, AttachmentsActivity.class);
        SharedConstants.data = files;
        startActivity(i);
        
    }
}
