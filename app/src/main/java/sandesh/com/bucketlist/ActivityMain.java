package sandesh.com.bucketlist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import sandesh.com.bucketlist.adapters.AdapterDrops;
import sandesh.com.bucketlist.adapters.AddListener;
import sandesh.com.bucketlist.adapters.CompleteListener;
import sandesh.com.bucketlist.adapters.Divider;
import sandesh.com.bucketlist.adapters.Filter;
import sandesh.com.bucketlist.adapters.MarkListener;
import sandesh.com.bucketlist.adapters.SimpleTouchCallback;
import sandesh.com.bucketlist.beans.Drop;

import static sandesh.com.bucketlist.AppBucketDrops.load;

public class ActivityMain extends AppCompatActivity implements View.OnClickListener{
    ImageView imageLogo;
    Toolbar mToolbar;
    Button mBtnAdd;
    BucketRecyclerView mRecycler;
    Realm mRealm;
    RealmResults<Drop> results;
    AdapterDrops mAdatper;
    View mEmptyView;
    private AddListener mAddListener = new AddListener() {
        @Override
        public void add() {
            showDialogAdd();
        }
    };

    private RealmChangeListener realmChangeListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            mAdatper.update(results);
        }
    };

    private MarkListener mMarkListener = new MarkListener() {
        @Override
        public void onMark(int position) {
            showDialogMark(position);
        }
    };

    private CompleteListener mCompleteListener = new CompleteListener() {
        @Override
        public void onComplete(int position) {
            //Toast.makeText(ActivityMain.this, "done", Toast.LENGTH_SHORT).show();;
            mAdatper.markComplete(position);
        }
    };
    private void showDialogMark(int position) {
        DialogMark dialog = new DialogMark();
        Bundle bundle = new Bundle();
        bundle.putInt("POSITION", position);
        dialog.setArguments(bundle);
        dialog.setCompleteListener(mCompleteListener);
        dialog.show(getSupportFragmentManager(), "Mark");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageLogo = (ImageView) findViewById(R.id.iv_logo);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mBtnAdd = (Button) findViewById(R.id.btn_add);
        mBtnAdd.setOnClickListener(this);
        mEmptyView = findViewById(R.id.empty_drops);
        mRealm = Realm.getDefaultInstance();
        int filterOption = load(this);
        loadResults(filterOption);
        results = mRealm.where(Drop.class).findAllAsync();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecycler = (BucketRecyclerView) findViewById(R.id.rv_drops);
        mRecycler.addItemDecoration(new Divider(this, LinearLayoutManager.VERTICAL));
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        mRecycler.hideIfEmpty(mToolbar);
        mRecycler.showIfEmpty(mEmptyView);
        mAdatper = new AdapterDrops(this, mRealm, results, mAddListener, mMarkListener);
        mAdatper.setHasStableIds(true);

        mRecycler.setAdapter(mAdatper);
        mRecycler.setLayoutManager(linearLayoutManager);
        SimpleTouchCallback callback = new SimpleTouchCallback(mAdatper);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecycler);
        setSupportActionBar(mToolbar);
        //$initBackgroundImage();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean handled = true;
        int filterOption = Filter.NONE;
        switch (id){
            case R.id.action_add:
                showDialogAdd();
                break;
            case R.id.action_sort_ascending_date:
                filterOption = Filter.LEAST_TIME_LEFT;
                break;
            case R.id.action_sort_descending_date:
                filterOption = Filter.MOST_TIME_LEFT;
                break;
            case R.id.sort_show_complete:
                filterOption = Filter.COMPLETE;
                break;
            case R.id.sort_show_incomplete:
                filterOption = Filter.INCOMPLETE;
                break;
            default:
                handled = false;
        }
        AppBucketDrops.save(this, filterOption);
        loadResults(filterOption);
        return handled;
    }


    private void loadResults(int filterOptions){
        switch (filterOptions){
            case Filter.NONE:
                results = mRealm.where(Drop.class).findAllAsync();
                break;
            case Filter.LEAST_TIME_LEFT:
                results = mRealm.where(Drop.class).findAllSortedAsync("when");
                break;
            case Filter.MOST_TIME_LEFT:
                results = mRealm.where(Drop.class).findAllSortedAsync("when", Sort.DESCENDING);
                break;
            case Filter.COMPLETE:
                results = mRealm.where(Drop.class).equalTo("completed", true).findAllAsync();
                break;
            case Filter.INCOMPLETE:
                results = mRealm.where(Drop.class).equalTo("completed", false).findAllAsync();
                break;
        }
        results.addChangeListener(realmChangeListener);
    }

    private void initBackgroundImage() {
        ImageView background = (ImageView) findViewById(R.id.iv_background);
        Glide.with(this)
                .load(R.drawable.gradient)
                .centerCrop()
                .into(background);
    }

    @Override
    public void onClick(View view) {
        showDialogAdd();
    }

    private void showDialogAdd() {
        DialogAdd dialog = new DialogAdd();
        dialog.show(getSupportFragmentManager(), "Add");
    }

    @Override
    protected void onStart() {
        super.onStart();
        results.addChangeListener(realmChangeListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        results.removeChangeListener(realmChangeListener);
    }
}
