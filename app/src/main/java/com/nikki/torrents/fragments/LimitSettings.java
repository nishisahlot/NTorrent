package com.nikki.torrents.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.nikki.torrents.R;
import com.nikki.torrents.activities.MainActivity;
import com.nikki.torrents.databaseModels.TorrentModel;
import com.nikki.torrents.downloadService.TorrentService;
import com.nikki.torrents.enums.UploadDownload;
import com.nikki.torrents.interfaces.LimitInterface;
import com.nikki.torrents.models.LimitSettingsPreference;
import com.nikki.torrents.utils.Constant;
import com.nikki.torrents.utils.ShowUploadLimit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Nishi Sahlot on 3/28/2016.
 */
public class LimitSettings extends Fragment {

    Context context;
    AppCompatActivity appCompatActivity;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.downloadLimit)
    TextView downloadLimit;
    @InjectView(R.id.uploadLimit)
    TextView uploadLimit;
    @InjectView(R.id.shareCheckBox)
    CheckBox shareCheckBox;
    @InjectView(R.id.seedingCheckBox)
    CheckBox seedingCheckBox;
    @InjectView(R.id.shareRatioEdit)
    ViewGroup shareRatioEdit;
    @InjectView(R.id.seedingLimitContainer)
    ViewGroup seedingLimitContainer;
    @InjectView(R.id.shareRatioText)
    TextView shareRatioText;
    @InjectView(R.id.seedingTime)
    TextView seedingTime;

    public static Fragment newInstance(Bundle bundle){

        LimitSettings settings=new LimitSettings();
        if(bundle!=null)settings.setArguments(bundle);

        return settings;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context =context;
        appCompatActivity =(AppCompatActivity)getActivity();
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.limit_settings,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);


        LimitSettingsPreference limitSettingsPreference=LimitSettingsPreference.getMe(LimitSettingsPreference.class);
        if(limitSettingsPreference.downloadLimit==0){
            downloadLimit.setText(getString(R.string.unlimited));
        }else{
            downloadLimit.setText(String.format("%s/s", Constant.byteCountToDisplaySize(limitSettingsPreference.downloadLimit)));
        }
        if(limitSettingsPreference.uploadLimit==0){
            uploadLimit.setText(getString(R.string.unlimited));
        }else{
            uploadLimit.setText(String.format("%s/s", Constant.byteCountToDisplaySize(limitSettingsPreference.uploadLimit)));
        }
        shareCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LimitSettingsPreference limitSettingsPreference=LimitSettingsPreference.getMe(LimitSettingsPreference.class);
                limitSettingsPreference.enableShareRatio=isChecked;
                limitSettingsPreference.saveMe();
                Constant.disableEnableViews(shareRatioEdit,isChecked);
                if(isChecked){
                    shareRatioEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LimitSettingsPreference limitSettingsPreference=LimitSettingsPreference.getMe(LimitSettingsPreference.class);
                            shareRatioText.setText(String.format("%s", limitSettingsPreference.shareRatioLimit));
                            new ShowShareEdit(limitSettingsPreference);

                        }
                    });
                }else{
                    shareRatioEdit.setOnClickListener(null);
                }
            }
        });
        if(limitSettingsPreference.enableShareRatio){
            shareCheckBox.setChecked(true);
        }else{
            shareCheckBox.setChecked(false);
        }

        seedingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LimitSettingsPreference limitSettingsPreference = LimitSettingsPreference.getMe(LimitSettingsPreference.class);
                limitSettingsPreference.stopSeedingAfter = isChecked;
                limitSettingsPreference.saveMe();
                Constant.disableEnableViews(seedingLimitContainer, isChecked);
                if (isChecked) {
                    seedingLimitContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LimitSettingsPreference limitSettingsPreference = LimitSettingsPreference.getMe(LimitSettingsPreference.class);
                            seedingTime.setText(Constant.hourToMinutes(limitSettingsPreference.seedingTimeLimit));
                            new ShowSeeding(limitSettingsPreference);
                        }
                    });
                } else {
                    seedingLimitContainer.setOnClickListener(null);
                }
            }
        });

        if(limitSettingsPreference.stopSeedingAfter)
            seedingCheckBox.setChecked(true);
        else
            seedingCheckBox.setChecked(false);

        Constant.disableEnableViews(seedingLimitContainer, limitSettingsPreference.stopSeedingAfter);
        Constant.disableEnableViews(shareRatioEdit,limitSettingsPreference.enableShareRatio);

        seedingTime.setText(Constant.hourToMinutes(limitSettingsPreference.seedingTimeLimit));
        shareRatioText.setText(String.format("%s", limitSettingsPreference.shareRatioLimit));


        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.getSupportActionBar().
                setDisplayHomeAsUpEnabled(true);
        appCompatActivity.getSupportActionBar().
                setTitle(getString(R.string.limits));
    }


    class ShowSeeding{
        LimitSettingsPreference limitSettingsPreference;
        @InjectView(R.id.hourPicker)
        NumberPicker hourPicker;
        @InjectView(R.id.minutePicker)
        NumberPicker minutePicker;
        View view;
        ShowSeeding(LimitSettingsPreference limitSettingsPreference){
            this.limitSettingsPreference=limitSettingsPreference;
            view=LayoutInflater.from(context).inflate(R.layout.seeding_number_layout,null);
            ButterKnife.inject(this,view);
            hourPicker.setMinValue(0);
            hourPicker.setMaxValue(Integer.MAX_VALUE);
            hourPicker.setWrapSelectorWheel(false);
            hourPicker.setValue(limitSettingsPreference.seedingTimeLimit / 60);
            minutePicker.setMinValue(0);
            minutePicker.setMaxValue(59);
            minutePicker.setValue(limitSettingsPreference.seedingTimeLimit%60);
            show();
        }

        void show(){
            new AlertDialog.Builder(context).setTitle(R.string.stop_seeding_time).setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            limitSettingsPreference.seedingTimeLimit=hourPicker.getValue()*60+minutePicker.getValue();
                            limitSettingsPreference.saveMe();
                            seedingTime.setText(Constant.hourToMinutes(limitSettingsPreference.seedingTimeLimit));
                        }
                    }).setNegativeButton(android.R.string.cancel,null).create().show();
        }
    }

    class ShowShareEdit{
        LimitSettingsPreference limitSettingsPreference;
        @InjectView(R.id.editText)
        EditText editText;
        View view;
        AlertDialog alertDialog;
        ShowShareEdit(LimitSettingsPreference limitSettingsPreference){
            this.limitSettingsPreference=limitSettingsPreference;
            view=LayoutInflater.from(context).inflate(R.layout.alert_edit_text,null);
            ButterKnife.inject(this, view);
            editText.setText(String.format("%s", limitSettingsPreference.shareRatioLimit));
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId== EditorInfo.IME_ACTION_DONE){
                        alertDialog.dismiss();
                        setLimitSettingsPreference();
                    }
                    return false;
                }

            });
            show();
        }
        private void show(){
            alertDialog= new AlertDialog.Builder(context).setTitle(R.string.share_ratio_preference).setView(view).
                    setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setLimitSettingsPreference();
                        }
                    }).setNegativeButton(android.R.string.cancel,null).create();
            alertDialog.show();
        }

        private void setLimitSettingsPreference(){
            limitSettingsPreference.shareRatioLimit=Float.parseFloat(editText.getText().toString());
            limitSettingsPreference.saveMe();
            shareRatioText.setText(String.format("%s", limitSettingsPreference.shareRatioLimit));
        }
    }






    @OnClick({R.id.seedingCheckboxContainer,
            R.id.stopSeedingAfterTime,
            R.id.downloadLimitContainer
    ,R.id.uploadLimitContainer})
    public void onViewClicked(View view){
        int id=view.getId();
        if(id==R.id.seedingCheckboxContainer){
            if(shareCheckBox.isChecked())
                shareCheckBox.setChecked(false);
            else
                shareCheckBox.setChecked(true);
        }else if(id==R.id.stopSeedingAfterTime){
            if(seedingCheckBox.isChecked())
                seedingCheckBox.setChecked(false);
            else
                seedingCheckBox.setChecked(true);
        }else if(id==R.id.downloadLimitContainer){
            LimitSettingsPreference limitSettingsPreference = LimitSettingsPreference.getMe(LimitSettingsPreference.class);
            TorrentModel torrentModel=new TorrentModel();
            torrentModel.DownloadLimit=limitSettingsPreference.downloadLimit;
            new ShowUploadLimit(context, torrentModel, UploadDownload.DOWNLOAD_LIMIT, new LimitInterface() {
                @Override
                public void selectedSpeedInBytes(int speedInBytes) {
                    LimitSettingsPreference limitSettingsPreference = LimitSettingsPreference.getMe(LimitSettingsPreference.class);
                    limitSettingsPreference.downloadLimit=speedInBytes;
                    limitSettingsPreference.saveMe();
                    if(limitSettingsPreference.downloadLimit==0){
                        downloadLimit.setText(getString(R.string.unlimited));
                    }else{
                        downloadLimit.setText(Constant.byteCountToDisplaySize(limitSettingsPreference.downloadLimit)+"/s");
                    }

                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TorrentService.UPDATE_GLOBAL_SPEED));
                }
            });
        }else if(id==R.id.uploadLimitContainer){
            LimitSettingsPreference limitSettingsPreference = LimitSettingsPreference.getMe(LimitSettingsPreference.class);
            TorrentModel torrentModel=new TorrentModel();
            torrentModel.UploadLimit=limitSettingsPreference.uploadLimit;
            new ShowUploadLimit(context, torrentModel, UploadDownload.UPLOAD_LIMIT, new LimitInterface() {
                @Override
                public void selectedSpeedInBytes(int speedInBytes) {
                    LimitSettingsPreference limitSettingsPreference = LimitSettingsPreference.getMe(LimitSettingsPreference.class);
                    limitSettingsPreference.uploadLimit=speedInBytes;
                    limitSettingsPreference.saveMe();
                    if(limitSettingsPreference.uploadLimit==0){
                        uploadLimit.setText(getString(R.string.unlimited));
                    }else{
                        uploadLimit.setText(Constant.byteCountToDisplaySize(limitSettingsPreference.uploadLimit)+"/s");
                    }
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TorrentService.UPDATE_GLOBAL_SPEED));
                }
            });
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
