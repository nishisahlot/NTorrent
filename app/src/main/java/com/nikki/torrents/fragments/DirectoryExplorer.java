package com.nikki.torrents.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nikki.torrents.R;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.adapter.DirectoryExplorerAdapter;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;
import com.nikki.torrents.utils.Icons;
import com.nikki.torrents.utils.MimeTypes;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 4/15/2016.
 */
public class DirectoryExplorer extends FragmentWithBackButton {

    public DirectoryExplorer() {
    }

    public static Fragment newInstance(Bundle bundle) {
        DirectoryExplorer directoryExplorer = new DirectoryExplorer();
        if (bundle != null)
            directoryExplorer.setArguments(bundle);

        return directoryExplorer;
    }
    int stack;
    @InjectView(R.id.directoryPath)
    TextView directoryPath;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    DirectoryExplorerAdapter directoryExplorerAdapter;
    AppCompatActivity appCompatActivity;
    Context context;

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
        View view = inflater.inflate(R.layout.directory_explorer, container, false);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        Bundle bundle = getArguments();
        if (bundle != null) {
            String directory = bundle.getString("directory");

            if (!TextUtils.isEmpty(directory)) {
                File file = new File(directory);
                if (file.exists()) {
                    setFileAdapter(file,false);
                }

            }
        }
        setTitle(getString(R.string.directory));
        return view;
    }


    private void setFileAdapter(File file,boolean isStacked){
        directoryPath.setText(file.getAbsolutePath());
        File[] fileArray=file.listFiles();
        if(fileArray!=null){
            if(isStacked)
                stack++;
            if(directoryExplorerAdapter==null){
                recyclerView.setAdapter(directoryExplorerAdapter = new DirectoryExplorerAdapter(fileArray,
                        new OnItemClickRecyclerListener() {
                            @Override
                            public void onItemClicked(View view, int position) {
                                try {
                                    File pathFile = directoryExplorerAdapter.file[position];

                                    if(pathFile.isDirectory()){
                                        setFileAdapter(pathFile,true);
                                    }else{
                                        openunknown(pathFile,context,true);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }));
            }else{
                directoryExplorerAdapter.file=fileArray;
                directoryExplorerAdapter.notifyDataSetChanged();
            }

        }

    }
    public void openunknown(File f, Context c, boolean forcechooser) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);

        String type = MimeTypes.getMimeType(f);
        if (type != null && type.trim().length() != 0 && !type.equals("*/*")) {
            Uri uri = fileToContentUri(c, f);
            if (uri == null) uri = Uri.fromFile(f);
            intent.setDataAndType(uri, type);
            Intent startintent;
            if (forcechooser)
                startintent = Intent.createChooser(intent, c.getResources().getString(R.string.openwith));
            else
                startintent = intent;
            try {
                c.startActivity(startintent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(c, R.string.noappfound, Toast.LENGTH_SHORT).show();
                openWith(f, c);
            }
        } else {
            openWith(f, c);
        }

    }

    public void openWith(final File f, final Context c) {
        String[] items = new String[]{getString(R.string.text), getString(R.string.image),
                getString(R.string.video), getString(R.string.audio), getString(R.string.other)};

        new AlertDialog.Builder(context).setTitle(getString(R.string.openas)).
                setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, items), new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = fileToContentUri(c, f);
                                if (uri == null) uri = Uri.fromFile(f);
                                Intent intent = new Intent();
                                intent.setAction(android.content.Intent.ACTION_VIEW);
                                switch (which) {
                                    case 0:
                                        intent.setDataAndType(uri, "text/*");
                                        break;
                                    case 1:
                                        intent.setDataAndType(uri, "image/*");
                                        break;
                                    case 2:
                                        intent.setDataAndType(uri, "video/*");
                                        break;
                                    case 3:
                                        intent.setDataAndType(uri, "audio/*");
                                        break;
                                    case 4:
                                        intent.setDataAndType(uri, "*/*");
                                        break;
                                }
                                try {
                                    c.startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(c, R.string.noappfound, Toast.LENGTH_SHORT).show();
                                    openWith(f, c);
                                }
                            }
                        }).create().show();


    }

    public static Uri fileToContentUri(Context context, File file) {
        // Normalize the path to ensure media search
        final String normalizedPath = normalizeMediaPath(file.getAbsolutePath());

        // Check in external and internal storages
        Uri uri = fileToContentUri(context, normalizedPath, EXTERNAL_VOLUME);
        if (uri != null) {
            return uri;
        }
        uri = fileToContentUri(context, normalizedPath, INTERNAL_VOLUME);
        if (uri != null) {
            return uri;
        }
        return null;
    }

    private static Uri fileToContentUri(Context context, String path, String volume) {
        String[] projection = null;
        final String where = MediaStore.MediaColumns.DATA + " = ?";
        Uri baseUri = MediaStore.Files.getContentUri(volume);
        boolean isMimeTypeImage = false, isMimeTypeVideo = false, isMimeTypeAudio = false;
        isMimeTypeImage = Icons.isPicture(path);
        if (!isMimeTypeImage) {
            isMimeTypeVideo = Icons.isVideo(path);
            if (!isMimeTypeVideo) {
                isMimeTypeAudio = Icons.isVideo(path);
            }
        }
        if (isMimeTypeImage || isMimeTypeVideo || isMimeTypeAudio) {
            projection = new String[]{BaseColumns._ID};
            if (isMimeTypeImage) {
                baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if (isMimeTypeVideo) {
                baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if (isMimeTypeAudio) {
                baseUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
        } else {
            projection = new String[]{BaseColumns._ID, MediaStore.Files.FileColumns.MEDIA_TYPE};
        }
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(baseUri, projection, where, new String[]{path}, null);
        try {
            if (c != null && c.moveToNext()) {
                boolean isValid = false;
                if (isMimeTypeImage || isMimeTypeVideo || isMimeTypeAudio) {
                    isValid = true;
                } else {
                    int type = c.getInt(c.getColumnIndexOrThrow(
                            MediaStore.Files.FileColumns.MEDIA_TYPE));
                    isValid = type != 0;
                }

                if (isValid) {
                    // Do not force to use content uri for no media files
                    long id = c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID));
                    return Uri.withAppendedPath(baseUri, String.valueOf(id));
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }

    private static final String INTERNAL_VOLUME = "internal";
    public static final String EXTERNAL_VOLUME = "external";

    private static final String EMULATED_STORAGE_SOURCE = System.getenv("EMULATED_STORAGE_SOURCE");
    private static final String EMULATED_STORAGE_TARGET = System.getenv("EMULATED_STORAGE_TARGET");
    private static final String EXTERNAL_STORAGE = System.getenv("EXTERNAL_STORAGE");

    public static String normalizeMediaPath(String path) {
        // Retrieve all the paths and check that we have this environment vars
        if (TextUtils.isEmpty(EMULATED_STORAGE_SOURCE) ||
                TextUtils.isEmpty(EMULATED_STORAGE_TARGET) ||
                TextUtils.isEmpty(EXTERNAL_STORAGE)) {
            return path;
        }

        // We need to convert EMULATED_STORAGE_SOURCE -> EMULATED_STORAGE_TARGET
        if (path.startsWith(EMULATED_STORAGE_SOURCE)) {
            path = path.replace(EMULATED_STORAGE_SOURCE, EMULATED_STORAGE_TARGET);
        }
        return path;
    }

    private void setTitle(String title) {
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.getSupportActionBar().setTitle(title);
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {

            if(stack>0){
                commongBackAction();
            }else{
                MainActivity.goBack(appCompatActivity);
            }

            return true;
        }


        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onBackButtonPressed() {
        return backButtonAction();
    }
    private void commongBackAction(){
        stack--;
        String directoryPathString=directoryPath.getText().toString();
        String path=directoryPathString.substring(0,
                directoryPathString.lastIndexOf("/"));
        setFileAdapter(new File(path),false);
    }
    private boolean backButtonAction(){
        if(stack>0){
            commongBackAction();
            return true;
        }
        return false;
    }
}
