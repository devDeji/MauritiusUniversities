package com.uni.mau.mauritiusuniversities;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cenkgun.chatbar.ChatBarView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uni.mau.mauritiusuniversities.dubai.DubaiContent;
import com.uni.mau.mauritiusuniversities.london.LondonContent;
import com.uni.mau.mauritiusuniversities.malta.MaltaContent;
import com.uni.mau.mauritiusuniversities.mauritius.MauritiusContent;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity
        implements GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener, ItemFragment.OnListFragmentInteractionListener, DubaiFragment.OnListFragmentInteractionListener, ItemFragment2.OnListFragmentInteractionListener, MauritiusFragment.OnListFragmentInteractionListener {

    public static final String MESSAGES_CHILD = "messages";
    private static final int RESULT_CAMERA = 200;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 500;
    private static final int REQUEST_INVITE = 7000;
    private static final int REQUEST_IMAGE = 6000;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private ProgressBar mProgressBar;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>
            mFirebaseAdapter;
    // Firebase instance variables
    private WebView myWebView;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private Button mSendButton;

    //private EditText mMessageEditText;

    private FirebaseAnalytics mFirebaseAnalytics;

    private RelativeLayout chatRel;

    private GoogleApiClient mGoogleApiClient;

    private AdView mAdView;

    private ChatBarView chatBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        chatRel = (RelativeLayout) findViewById(R.id.chatRel);


        navigationView.setItemIconTintList(null);
        try {
            // Initialize Firebase Auth
            mFirebaseAuth = FirebaseAuth.getInstance();
            mFirebaseUser = mFirebaseAuth.getCurrentUser();


            //mMessageEditText = (EditText) findViewById(R.id.messageEditText);
            mProgressBar = (ProgressBar) findViewById(R.id.progressBar);


            mLinearLayoutManager = new LinearLayoutManager(this);

            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.loadAd(adRequest);

            //Log.e("mFirebaseUser", mFirebaseUser.getPhotoUrl().toString());

            Uri imgUri = Uri.parse(mFirebaseUser.getPhotoUrl().toString());

            final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getContentResolver().takePersistableUriPermission(Uri.parse(mFirebaseUser.getPhotoUrl().toString()), takeFlags);
            } else {
                //addMessageImageView.setImageURI(null);
                //addMessageImageView.setImageURI(imgUri);

            }
            // convert uri to bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(mFirebaseUser.getPhotoUrl().toString()));
            // set bitmap to imageview
            //addMessageImageView .setImageBitmap(bitmap);
        } catch (Exception e) {
            //handle exception
            e.printStackTrace();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setSize(FloatingActionButton.SIZE_MINI);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Follow @dev_joe2016", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        myWebView = (WebView) findViewById(R.id.webview);

        chatBarView = (ChatBarView) findViewById(R.id.chatbar);

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);

        // New child entries
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Log.e("referenece eu firebase:", mFirebaseDatabaseReference.toString());
        //mMessageRecyclerView.setHasFixedSize(true);
        try {
            if (mFirebaseUser.getPhotoUrl() != null) {
                chatBarView.setSendClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FriendlyMessage friendlyMessage = new
                                FriendlyMessage(chatBarView.getMessageText(),
                                mFirebaseUser.getDisplayName(),
                                mFirebaseUser.getPhotoUrl().toString(),
                                null /* no image */);
                        mFirebaseDatabaseReference.child(MESSAGES_CHILD)
                                .push().setValue(friendlyMessage);
                    }
                });
                chatBarView.setOnMicListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        //TODO what you want..
                        FriendlyMessage friendlyMessage = new
                                FriendlyMessage(chatBarView.getMessageText(),
                                mFirebaseUser.getDisplayName(),
                                mFirebaseUser.getPhotoUrl().toString(),
                                null /* no image */);
                        mFirebaseDatabaseReference.child(MESSAGES_CHILD)
                                .push().setValue(friendlyMessage);
                        return true;
                    }
                });
            } else {
                chatBarView.setSendClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FriendlyMessage friendlyMessage = new
                                FriendlyMessage(chatBarView.getMessageText(),
                                mFirebaseUser.getDisplayName(),
                                null,
                                null /* no image */);
                        mFirebaseDatabaseReference.child(MESSAGES_CHILD)
                                .push().setValue(friendlyMessage);
                    }
                });
                chatBarView.setOnMicListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        //TODO what you want..
                        FriendlyMessage friendlyMessage = new
                                FriendlyMessage(chatBarView.getMessageText(),
                                mFirebaseUser.getDisplayName(),
                                null,
                                null /* no image */);
                        mFirebaseDatabaseReference.child(MESSAGES_CHILD)
                                .push().setValue(friendlyMessage);
                        return true;
                    }
                });
            }

        } catch (Exception e) {
        }
        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage,
                MessageViewHolder>(
                FriendlyMessage.class,
                R.layout.item_message,
                MessageViewHolder.class,
                mFirebaseDatabaseReference.child(MESSAGES_CHILD)) {

            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder,
                                              FriendlyMessage friendlyMessage, int position) {
                Log.e("friendly messagee", friendlyMessage.getText());
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (friendlyMessage.getText() != null) {
                    viewHolder.messageTextView.setText(friendlyMessage.getText());
                    viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                    viewHolder.messageImageView.setVisibility(ImageView.GONE);
                } else {
                    String imageUrl = friendlyMessage.getImageUrl();
                    if (imageUrl.startsWith("gs://")) {
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl);
                        storageReference.getDownloadUrl().addOnCompleteListener(
                                new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String downloadUrl = task.getResult().toString();
                                            Glide.with(viewHolder.messageImageView.getContext())
                                                    .load(downloadUrl)
                                                    .into(viewHolder.messageImageView);
                                        } else {
                                            Log.w("download successful.",
                                                    task.getException());
                                        }
                                    }
                                });
                    } else {
                        Glide.with(viewHolder.messageImageView.getContext())
                                .load(friendlyMessage.getImageUrl())
                                .into(viewHolder.messageImageView);
                    }
                    viewHolder.messageImageView.setVisibility(ImageView.VISIBLE);
                    viewHolder.messageTextView.setVisibility(TextView.GONE);
                }


                viewHolder.messengerTextView.setText(friendlyMessage.getName());
                if (friendlyMessage.getPhotoUrl() == null) {
                    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(MainActivity.this)
                            .load(friendlyMessage.getPhotoUrl())
                            .into(viewHolder.messengerImageView);
                }

            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        handleIntent(getIntent());


    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
        suggestions.saveRecentQuery(query, null);
        chatRel.setVisibility(View.GONE);
        myWebView.setVisibility(View.VISIBLE);
        myWebView.loadUrl("https://www.google.com/search?q=" + query);
        myWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        /**
         * SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         //SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
         //searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()) );


         MenuItem searchItem = menu.findItem(R.id.app_bar_search);
         SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
         searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()) );


         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        @Override public boolean onQueryTextSubmit(String query) {
        Toast.makeText(MainActivity.this, "onQueryTextListener sucka!", Toast.LENGTH_LONG).show();
        return false;
        }

        @Override public boolean onQueryTextChange(String newText) {
        Toast.makeText(MainActivity.this, "onQueryTextChanged sucka!", Toast.LENGTH_LONG).show();
        return false;
        }
        });

         Get the SearchView and set the searchable configuration
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
         // Assumes current activity is the searchable activity
         searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
         searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default**/
        return true;
    }

    //Called from the search config file should be used instead of the searchView instantiation On the onCreateOptionsMenu
    @Override
    public boolean onSearchRequested() {
        Bundle appData = new Bundle();
        appData.putBoolean("DummyBool", true);
        startSearch(null, false, appData, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.app_bar_search
                ) {
            onSearchRequested();
            return true;
        }
        if (id == R.id.invite_menu) {
            sendInvitation();
            return true;
        } else if (id == R.id.sign_out_menu) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to log out?")
                    .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert)) //<--- icon does not show
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Yes button clicked, do something
                            mFirebaseAuth.signOut();
                            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                            String mUsername = "ANONYMOUS";
                            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", null)                      //Do nothing on no
                    .show();
            return true;
        }

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_london) {
            // Handle the camera action
            myWebView.setVisibility(View.GONE);
            chatRel.setVisibility(View.GONE);
            ItemFragment itemfragment = new ItemFragment();
            FragmentTransaction fragTrans = getSupportFragmentManager().beginTransaction();
            fragTrans.replace(R.id.frame, itemfragment, "London Fragment");
            fragTrans.commit();
        } else if (id == R.id.nav_dubia) {
            myWebView.setVisibility(View.GONE);
            chatRel.setVisibility(View.GONE);
            DubaiFragment dubiafragment = new DubaiFragment();
            FragmentTransaction fragTrans = getSupportFragmentManager().beginTransaction();
            fragTrans.replace(R.id.frame, dubiafragment, "Dubai Fragment");
            fragTrans.commit();
        } else if (id == R.id.nav_malta) {
            myWebView.setVisibility(View.GONE);
            chatRel.setVisibility(View.GONE);
            ItemFragment2 itemfragment2 = new ItemFragment2();
            FragmentTransaction fragTrans2 = getSupportFragmentManager().beginTransaction();
            fragTrans2.replace(R.id.frame, itemfragment2, "Malta Fragment");
            fragTrans2.commit();
        } else if (id == R.id.nav_mauritius) {
            myWebView.setVisibility(View.GONE);
            chatRel.setVisibility(View.GONE);
            MauritiusFragment mauFragment = new MauritiusFragment();
            FragmentTransaction fragTrans3 = getSupportFragmentManager().beginTransaction();
            fragTrans3.replace(R.id.frame, mauFragment, "Mauritius Fragment");
            fragTrans3.commit();
        } else if (id == R.id.nav_share) {
            sendInvitation();
        } else if (id == R.id.nav_chat) {
            //Intent in = new Intent(MainActivity.this, Main2Activity.class);
            //startActivity(in);
            try {
                chatRel.setVisibility(View.VISIBLE);
                myWebView.setVisibility(View.GONE);
                //Remove whatever fragment is on frame
                //getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.frame)).commit();
            } catch (Exception e) {
            } finally {
                Log.e("Seriously", "Seriouslyyy");
                chatRel.setVisibility(View.VISIBLE);
                myWebView.setVisibility(View.GONE);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onListFragmentInteraction(LondonContent.DummyItem item) {
        String url = item.content;
        myWebView.setVisibility(View.VISIBLE);
        chatRel.setVisibility(View.GONE);
        myWebView.loadUrl(url);
        myWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        //Remove whatever fragment is on frame
        getSupportFragmentManager().beginTransaction().
                remove(getSupportFragmentManager().findFragmentById(R.id.frame)).commit();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onListFragmentInteraction(DubaiContent.DummyItem item) {
        String url = item.content;
        myWebView.setVisibility(View.VISIBLE);
        chatRel.setVisibility(View.GONE);
        myWebView.loadUrl(url);
        myWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        //Remove whatever fragment is on frame
        getSupportFragmentManager().beginTransaction().
                remove(getSupportFragmentManager().findFragmentById(R.id.frame)).commit();
    }

    @Override
    public void onListFragmentInteraction(MauritiusContent.DummyItem item) {
        String url = item.content;
        myWebView.setVisibility(View.VISIBLE);
        chatRel.setVisibility(View.GONE);
        myWebView.loadUrl(url);
        myWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        //Remove whatever fragment is on frame
        getSupportFragmentManager().beginTransaction().
                remove(getSupportFragmentManager().findFragmentById(R.id.frame)).commit();
    }

    @Override
    public void onListFragmentInteraction(MaltaContent.DummyItem item) {
        String url = item.content;
        myWebView.setVisibility(View.VISIBLE);
        chatRel.setVisibility(View.GONE);
        myWebView.loadUrl(url);
        myWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        //Remove whatever fragment is on frame
        getSupportFragmentManager().beginTransaction().
                remove(getSupportFragmentManager().findFragmentById(R.id.frame)).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    //Log.d(TAG, "Uri: " + uri.toString());
                    try {
                        FriendlyMessage tempMessage = new FriendlyMessage(null, mFirebaseUser.getDisplayName(), mFirebaseUser.getPhotoUrl().toString(),
                                "");
                        mFirebaseDatabaseReference.child(MESSAGES_CHILD).push()
                                .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError,
                                                           DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            String key = databaseReference.getKey();
                                            StorageReference storageReference =
                                                    FirebaseStorage.getInstance()
                                                            .getReference(mFirebaseUser.getUid())
                                                            .child(key)
                                                            .child(uri.getLastPathSegment());

                                            //putImageInStorage(storageReference, uri, key);
                                        } else {
                                            //Log.w(TAG, "Unable to write message to database.", databaseError.toException());
                                        }
                                    }
                                });
                    } catch (Exception e) {
                    }
                }
            }
        } else if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                //Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                // Sending failed or it was canceled, show failure message to the user
                Log.e("Hello world", "Failed to send invitation.");
            }
        }
        if (requestCode == REQUEST_IMAGE) {
            // ...
        } else if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE,
                        payload);
                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode,
                        data);
                Log.e("Testing", "Invitations sent: " + ids.length);
            } else {
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "not sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE,
                        payload);
                // Sending failed or it was canceled, show failure message to
                // the user
                Log.d("Testing", "Failed to send invitation.");
            }
        }

    }

    protected static class MessageViewHolder extends RecyclerView.ViewHolder {
        final TextView messageTextView;
        final ImageView messageImageView;
        final TextView messengerTextView;
        final CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }
}
