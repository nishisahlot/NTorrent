package com.nikki.torrents.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frostwire.jlibtorrent.Priority;
import com.nikki.torrents.R;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.adapter.FilesSelectAdapter;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;
import com.nikki.torrents.models.FileMetadata;
import com.nikki.torrents.utils.Constant;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/26/2016.
 */
public class FilesSelectFragment extends Fragment {

    Context context;
    AppCompatActivity appCompatActivity;
    public FilesSelectFragment(){}
    public static FilesSelectFragment newInstance(Bundle bundle) {
        FilesSelectFragment directoryChooser = new FilesSelectFragment();
        if (bundle != null)
            directoryChooser.setArguments(bundle);

        return directoryChooser;
    }
    public List<FileMetadata> fileMetadataList;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.fileSize)
    TextView fileSize;
    @InjectView(R.id.fileSelected)
    TextView fileSelected;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    FilesSelectAdapter filesSelectAdapter;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
        appCompatActivity=(AppCompatActivity)getActivity();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.files_select_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setFilesData();
        setTitle(getString(R.string.select_files_to_download));
    }
    private void setTitle(String title){
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.getSupportActionBar().setTitle(title);
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void setFilesData(){
        if(fileMetadataList!=null){
            recyclerView.setAdapter(filesSelectAdapter=new FilesSelectAdapter(fileMetadataList, new OnItemClickRecyclerListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    FileMetadata fileMetadata = fileMetadataList.get(position);
                    if (fileMetadata.priority != Priority.IGNORE)
                        fileMetadata.priority = Priority.IGNORE;
                    else
                        fileMetadata.priority = Priority.NORMAL;

                    filesSelectAdapter.notifyItemChanged(position);
                    writeFileMetadata();
                }
            }));
            writeFileMetadata();
        }
    }
    private void writeFileMetadata(){
        if(fileMetadataList!=null){
            Pair<Integer,String> integerStringPair= Constant.getSelecteWithSize(fileMetadataList);
            fileSelected.setText(String.format("%d %s", integerStringPair.first, getString(R.string.selected)));
            fileSize.setText(integerStringPair.second);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();
        if(id==android.R.id.home){
            MainActivity.goBack(appCompatActivity);

            return true;
        }


        return super.onOptionsItemSelected(item);

    }
}
