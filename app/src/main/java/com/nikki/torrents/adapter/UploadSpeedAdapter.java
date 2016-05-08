package com.nikki.torrents.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nikki.torrents.R;
import com.nikki.torrents.interfaces.OnItemClickRecyclerListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Nishi Sahlot on 3/27/2016.
 */
public class UploadSpeedAdapter extends RecyclerView.Adapter<UploadSpeedAdapter.ViewHolder>{

    List<Integer> integers;
    public int checkPosition;
    Context context;
    OnItemClickRecyclerListener onItemClickRecyclerListener;
    public UploadSpeedAdapter(List<Integer> integers,OnItemClickRecyclerListener onItemClickRecyclerListener,int checkPosition){
        this.integers=integers;
        this.onItemClickRecyclerListener=onItemClickRecyclerListener;
        this.checkPosition=checkPosition;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context==null)
            context=parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).
                inflate(R.layout.upload_speed_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setupView(position);
    }

    @Override
    public int getItemCount() {
        return integers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        int position;
        @InjectView(R.id.speedCheckBox)
        CheckBox speedCheckBox;
        @InjectView(R.id.container)
        View container;
        @InjectView(R.id.textView)
        TextView textView;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkPosition = integers.get(position);
                    notifyDataSetChanged();
                    if (onItemClickRecyclerListener != null)
                        onItemClickRecyclerListener.onItemClicked(v, position);
                }
            });
            speedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked&&checkPosition!=integers.get(position)){
                        checkPosition = integers.get(position);
                        if (onItemClickRecyclerListener != null)
                            onItemClickRecyclerListener.onItemClicked(buttonView, position);
                        notifyDataSetChanged();
                    }
                    if(!isChecked&&checkPosition==integers.get(position))
                        notifyDataSetChanged();
                }
            });
        }

        public void setupView(int position){
            this.position=position;
            int speed=integers.get(position);
            textView.setText(String.format("%d kB/s", integers.get(position)));
            if(checkPosition==speed)
                speedCheckBox.setChecked(true);
            else
                speedCheckBox.setChecked(false);
        }
    }

}
