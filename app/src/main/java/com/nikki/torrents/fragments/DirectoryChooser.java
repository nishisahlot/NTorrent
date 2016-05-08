package com.nikki.torrents.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nikki.torrents.R;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.adapter.DirectoryAdapter;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;
import com.nikki.torrents.models.BaseFile;
import com.nikki.torrents.models.DownloadLocationModel;
import com.nikki.torrents.models.EntryItem;
import com.nikki.torrents.utils.FileUtil;
import com.nikki.torrents.utils.Futils;
import com.nikki.torrents.utils.IconUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Nishi Sahlot on 3/19/2016.
 */
public class DirectoryChooser extends Fragment {

    final Pattern DIR_SEPARATOR = Pattern.compile("/");
    @InjectView(R.id.buttons)
    LinearLayout buttons;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.scroll)
    HorizontalScrollView scroll;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    DirectoryAdapter directoryAdapter;
    PathInterface pathInterface;
    public interface PathInterface{
        void onPathSelected(DownloadLocationModel downloadLocationModel);
    }
    public DirectoryChooser(){}
    public static DirectoryChooser newInstance(Bundle bundle) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (bundle != null)
            directoryChooser.setArguments(bundle);

        return directoryChooser;
    }
    String sdCardPath;
    DownloadLocationModel downloadLocationModel;
    List<EntryItem> entryItems;
    Context context;
    AppCompatActivity appCompatActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        appCompatActivity = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.directory_chooser,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        List<String> strings=getStorageDirectories();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        if(strings!=null){
            entryItems=prepareStorageEntries(strings);

            if(entryItems!=null)
                showSdCards();
        }
        setTitle(getString(R.string.select_directory));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.directory_chooser, menu);
    }


    @OnClick({R.id.cancel,R.id.ok})
    public void onViewsClicked(View view){
        int id=view.getId();
        if(id==R.id.cancel){
            MainActivity.goBack(appCompatActivity);
        }else if(id==R.id.ok){

            if(downloadLocationModel!=null&&pathInterface!=null)
                pathInterface.onPathSelected(downloadLocationModel);
            MainActivity.goBack(appCompatActivity);
        }
    }



    private void setTitle(String title){
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.getSupportActionBar().setTitle(title);
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();
        if(id==android.R.id.home){
            if(downloadLocationModel==null){
                MainActivity.goBack(appCompatActivity);
            }else{
                goBackInDirectory();
            }

            return true;
        }else if(id==R.id.newFolder){
            if(downloadLocationModel!=null){
                new NewFolder();
            }else{
                Toast.makeText(context,getString(R.string.select_sd_card),Toast.LENGTH_SHORT).show();
            }
        }


        return super.onOptionsItemSelected(item);

    }


    private void goBackInDirectory(){
        if(downloadLocationModel!=null){
            if(Futils.isStorage(entryItems,downloadLocationModel.downloadLocationPath)){
                showSdCards();
            }else{
                String path=downloadLocationModel.downloadLocationPath.substring(0,
                        downloadLocationModel.downloadLocationPath.lastIndexOf("/"));
                new LoadList(path);
            }

        }
    }


    class NewFolder{
        @InjectView(R.id.editText)
        EditText editText;
        NewFolder(){
            View view=LayoutInflater.from(context).inflate(R.layout.new_folder_view,null);
            ButterKnife.inject(this,view);
            new AlertDialog.Builder(context).setView(view).
                    setTitle(R.string.create_new_folder).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(downloadLocationModel!=null){
                        String path=downloadLocationModel.downloadLocationPath+File.separator+editText.getText().toString();
                        File file=new File(path);
                        if(file.mkdirs()){
                            new LoadList(path);
                        }
                    }
                }
            }).setNegativeButton(android.R.string.cancel,null).create().show();
        }
    }


    private void showSdCards(){
        if(entryItems!=null){
            downloadLocationModel=null;
            List<BaseFile> baseFiles=new ArrayList<>();
            for(EntryItem entryItem:entryItems){
                BaseFile baseFile=new BaseFile();
                baseFile.path=entryItem.subtitle;
                baseFile.name=entryItem.title;
                baseFiles.add(baseFile);
            }
            setAdapter(baseFiles);

        }
    }






    private void prepareBredGrum(){

        buttons.setVisibility(View.VISIBLE);
        buttons.removeAllViews();
       // buttons.setMinimumHeight(pathbar.getHeight());
        Drawable arrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_holo_dark);
        View view = new View(context);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                144, LinearLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params1);
        buttons.addView(view);


        if(downloadLocationModel==null){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            ImageButton ib = new ImageButton(context);
            ib.setImageDrawable(IconUtils.getRootDrawable());
            ib.setBackgroundColor(Color.parseColor("#00ffffff"));
            ib.setLayoutParams(params);
            buttons.addView(ib);
            return;
        }



        Bundle b = Futils.getPaths(downloadLocationModel.downloadLocationPath, appCompatActivity,entryItems);
        ArrayList<String> names = b.getStringArrayList("names");
        ArrayList<String> rnames = new ArrayList<String>();

        for (int i = names.size() - 1; i >= 0; i--) {
            rnames.add(names.get(i));
        }

        ArrayList<String> paths = b.getStringArrayList("paths");
        final ArrayList<String> rpaths = new ArrayList<String>();

        for (int i = paths.size() - 1; i >= 0; i--) {
            rpaths.add(paths.get(i));
        }






        for (int i = 0; i < names.size(); i++) {
            final int k = i;
            ImageView v = new ImageView(context);
            v.setImageDrawable(arrow);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            v.setLayoutParams(params);
            final int index = i;
            if (rpaths.get(i).equals("/")) {
                ImageButton ib = new ImageButton(context);
                ib.setImageDrawable(IconUtils.getRootDrawable());
                ib.setBackgroundColor(Color.parseColor("#00ffffff"));
                ib.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View p1) {
                        showSdCards();
                  }
                });
                ib.setLayoutParams(params);
                buttons.addView(ib);
                if (names.size() - i != 1)
                    buttons.addView(v);
            } else if (Futils.isStorage(entryItems, rpaths.get(i))) {
                ImageButton ib = new ImageButton(context);
                ib.setImageDrawable(IconUtils.getSdDrawable());
                ib.setBackgroundColor(Color.parseColor("#00ffffff"));
                ib.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View p1) {
                        new LoadList(rpaths.get(k));
                    }
                });
                ib.setLayoutParams(params);
                buttons.addView(ib);
                if (names.size() - i != 1)
                    buttons.addView(v);
            } else {
                Button button = new Button(context);
                button.setText(rnames.get(index));
                button.setTextColor(getResources().getColor(android.R.color.white));
                button.setTextSize(13);
                button.setLayoutParams(params);
                button.setBackgroundResource(0);
                button.setOnClickListener(new Button.OnClickListener() {

                    public void onClick(View p1) {
                        new LoadList(rpaths.get(k));
                    }
                });


                buttons.addView(button);
                if (names.size() - i != 1)
                    buttons.addView(v);
            }
        }

        scroll.post(new Runnable() {
            @Override
            public void run() {
                sendScroll(scroll);

            }
        });
    }
    private void setAdapter(List<BaseFile> baseFiles){
        if(baseFiles!=null){
            recyclerView.setAdapter(directoryAdapter=new DirectoryAdapter(baseFiles,
                    new OnItemClickRecyclerListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    BaseFile baseFile=directoryAdapter.baseFiles.get(position);
                    if(downloadLocationModel==null)
                        sdCardPath=baseFile.path;
                    new LoadList(baseFile.path);
                    prepareBredGrum();

                }
            }));
            prepareBredGrum();
        }
    }




    private class LoadList extends AsyncTask<Void,Void,List<BaseFile>>{
        String path;
        public LoadList(String path){
            this.path=path;
            downloadLocationModel=new DownloadLocationModel(sdCardPath,path);
            execute();
        }
        @Override
        protected List<BaseFile> doInBackground(Void... params) {
            return FileUtil.getFilesList(path, false);
        }

        @Override
        protected void onPostExecute(List<BaseFile> baseFiles) {
            super.onPostExecute(baseFiles);
            if(baseFiles!=null){
                setAdapter(baseFiles);
            }
        }
    }

    void sendScroll(final HorizontalScrollView scrollView) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_RIGHT);
                    }
                });
            }
        }).start();
    }

    public List<String> getStorageDirectories() {
        // Final set of paths
        final ArrayList<String> rv = new ArrayList<>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkStoragePermission())
            rv.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            String strings[] = FileUtil.getExtSdCardPathsForActivity(appCompatActivity);
            for (String s : strings) {
                File f = new File(s);
                if (!rv.contains(s) && Futils.canListFiles(f))
                    rv.add(s);
            }
        }

        File usb = getUsbDrive();
        if (usb != null && !rv.contains(usb.getPath())) rv.add(usb.getPath());



        return rv;
    }

    private List<EntryItem> prepareStorageEntries(List<String> rv){
        List<EntryItem> list = new ArrayList<>();
        for (String file : rv) {
            File f = new File(file);
            String name;
            Drawable icon1 = ContextCompat.getDrawable(appCompatActivity, R.mipmap.ic_sd_storage_white_56dp);
            if ("/storage/emulated/legacy".equals(file) || "/storage/emulated/0".equals(file)) {
                name = getResources().getString(R.string.storage);
            } else if ("/storage/sdcard1".equals(file)) {
                name = getResources().getString(R.string.extstorage);
            } else if ("/".equals(file)) {
                name = getResources().getString(R.string.rootdirectory);
                icon1 = ContextCompat.getDrawable(appCompatActivity, R.mipmap.ic_drawer_root_white);
            } else name = f.getName()+" ("+getResources().getString(R.string.extstorage)+")";
            if (!f.isDirectory() || f.canExecute()) {
                list.add(new EntryItem(name, file, icon1));
            }
        }
        return list;
    }

    public File getUsbDrive() {
        File parent;
        parent = new File("/storage");

        try {
            for (File f : parent.listFiles()) {
                if (f.exists() && f.getName().toLowerCase().contains("usb") && f.canExecute()) {
                    return f;
                }
            }
        } catch (Exception e) {
        }
        parent = new File("/mnt/sdcard/usbStorage");
        if (parent.exists() && parent.canExecute())
            return (parent);
        parent = new File("/mnt/sdcard/usb_storage");
        if (parent.exists() && parent.canExecute())
            return parent;

        return null;
    }
    public boolean checkStoragePermission() {

        // Verify that all required contact permissions have been granted.
        if (ActivityCompat.checkSelfPermission(appCompatActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

}
