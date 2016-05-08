package com.nikki.torrents.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.android.gms.ads.AdView;
import com.nikki.torrents.R;
import com.nikki.torrents.downloadService.TaskInFront;
import com.nikki.torrents.enums.MovieTv;
import com.nikki.torrents.fragments.FragmentWithBackButton;
import com.nikki.torrents.fragments.LimitSettings;
import com.nikki.torrents.fragments.MainFragment;
import com.nikki.torrents.fragments.MoviesFragment;
import com.nikki.torrents.utils.Constant;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {


    @InjectView(R.id.drawer_layout)
    public DrawerLayout drawer;
    @InjectView(R.id.nav_view)
    NavigationView navigationView;
    @InjectView(R.id.adView)
    AdView adView;

    Context context;
    int permissionCode=112;
    private static final int REQUEST_INVITE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TaskInFront.isTaskInFront=true;
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        context=this;
        Constant.loadAds(adView);

        if (savedInstanceState == null){
            Intent intent=getIntent();

            if(intent.getData()!=null){
                String torrent=intent.getData().toString();
                Bundle bundle=new Bundle();
                bundle.putString("torrent", torrent);
                bringFragment(this, MainFragment.newInstance(bundle), false, MainFragment.class.getName());
                Intent intent1=new Intent(this,TorrentDialog.class);
                intent1.putExtra("torrent", torrent);
                startActivity(intent1);
            }else if(!TextUtils.isEmpty(intent.getStringExtra("torrent"))){
                Bundle bundle=new Bundle();
                bundle.putString("torrent", intent.getStringExtra("torrent"));
                bringFragment(this, MainFragment.newInstance(bundle), false, MainFragment.class.getName());
            }else{
                bringFragment(this, MainFragment.newInstance(null), false, MainFragment.class.getName());
            }

        }

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    permissionCode);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                closeDrawer();
                if (item.getItemId() == R.id.settings) {
                    bringFragment(MainActivity.this, LimitSettings.newInstance(null), true);
                } else if (item.getItemId() == R.id.movies) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("movieTv", MovieTv.MOVIE);
                    bringFragment(MainActivity.this, MoviesFragment.newInstance(bundle), true, MoviesFragment.class.getName());
                } else if (item.getItemId() == R.id.tvShows) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("movieTv", MovieTv.TV);
                    bringFragment(MainActivity.this, MoviesFragment.newInstance(bundle), true, MoviesFragment.class.getName());
                } else if (item.getItemId() == R.id.bookMark) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("movieTv", MovieTv.BOOKMARKS);
                    bringFragment(MainActivity.this, MoviesFragment.newInstance(bundle), true, MoviesFragment.class.getName());
                }/* else if (item.getItemId() == R.id.rateUs) {
                    rateApp();
                }*/else if(item.getItemId()==R.id.sendSuggestion){
                    Constant.sendSuggestion(context,new String[]{"nikkistudio.ntorrent@gmail.com"});
                }/*else if(item.getItemId()==R.id.inviteFriend){
                    onInviteClicked();
                }*/

                return false;
            }
        });
        //RateThisApp.onStart(context);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Check how many invitations were sent and log a message
                // The ids array contains the unique invitation ids for each invitation sent
                // (one for each contact select by the user). You can use these for analytics
                // as the ID will be consistent on the sending and receiving devices.
               // String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
//                Log.d(TAG, getString(R.string.sent_invitations_fmt, ids.length));
            } else {
                // Sending failed or it was canceled, show failure message to the user
                //showMessage(getString(R.string.send_failed));
            }
        }
    }

//    private void onInviteClicked() {
//        try {
//            Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
//                    .setMessage(getString(R.string.invitation_message))
//                    .setCallToActionText(getString(R.string.invitation_cta))
//                    .build();
//            startActivityForResult(intent, REQUEST_INVITE);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void rateApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    @Override
//    protected void onStart() {
//        super.onStart();
//        RateThisApp.showRateDialogIfNeeded(context);
//    }

    private void closeDrawer(){
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        }
    }




    public static void bringFragment(AppCompatActivity appCompatActivity, Fragment fragment,
                                     boolean backstack) {

        try {
            if (backstack) {
                appCompatActivity.getSupportFragmentManager().beginTransaction().
                        replace(R.id.frameLayout, fragment).addToBackStack(null).commit();
            } else {
                appCompatActivity.getSupportFragmentManager().beginTransaction().
                        replace(R.id.frameLayout, fragment).commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void bringFragment(AppCompatActivity appCompatActivity, Fragment fragment,
                                     boolean backstack, String tag) {


        try {

            if (backstack) {
                appCompatActivity.getSupportFragmentManager().beginTransaction().
                        replace(R.id.frameLayout, fragment, tag).addToBackStack(null).commit();
            } else {
                appCompatActivity.getSupportFragmentManager().beginTransaction().
                        replace(R.id.frameLayout, fragment, tag).commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void goBack(AppCompatActivity appCompatActivity) {
        try {
            FragmentManager fragmentManager = appCompatActivity.getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0)
                appCompatActivity.getSupportFragmentManager().popBackStack();
            else
                appCompatActivity.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void goBackToFirst(AppCompatActivity appCompatActivity) {
        try {
            appCompatActivity.getSupportFragmentManager().popBackStack(null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        Fragment mainFragment =  getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if(mainFragment!=null && mainFragment instanceof FragmentWithBackButton&&
                ((FragmentWithBackButton)mainFragment).onBackButtonPressed())
            return;

        super.onBackPressed();

    }
}
