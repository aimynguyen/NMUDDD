package com.example.diary_app.adapter;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diary_app.model.ReactionItem;
import com.example.diary_app.R;

import java.util.ArrayList;


public class ReactionAdapter
        extends RecyclerView.Adapter<ReactionAdapter.Holder>{


    private ArrayList<ReactionItem> list;


    public ReactionAdapter(ArrayList<ReactionItem> list){
        this.list = list;
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType){

        View v =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_reaction,
                                parent,
                                false
                        );

        return new Holder(v);
    }


    @Override
    public void onBindViewHolder(
            @NonNull Holder h,
            int p){


        ReactionItem item = list.get(p);


        // reset item recycle
        h.layoutEmoji.removeAllViews();


        String name = item.getUsername();

        if(name == null || name.isEmpty()){
            name = "Người dùng";
        }

        h.txtName.setText(name);



        // avatar
        if(item.getAvatarUrl() != null
                && !item.getAvatarUrl().isEmpty()){

            Glide.with(h.itemView.getContext())
                    .load(item.getAvatarUrl())
                    .circleCrop()
                    .into(h.imgAvatar);

        }else{

            h.imgAvatar.setImageResource(
                    R.drawable.avatar_circle
            );
        }



        String r = item.getReaction();


        if(r == null)
            r = "NEUTRAL";


        switch(r.toUpperCase()){


            case "HAPPY":

                h.layoutEmoji.addView(
                        makeEmoji(h.itemView,"😊")
                );

                break;


            case "SAD":

                h.layoutEmoji.addView(
                        makeEmoji(h.itemView,"😭")
                );

                break;


            case "ANGRY":

                h.layoutEmoji.addView(
                        makeEmoji(h.itemView,"😡")
                );

                break;


            case "CALM":

                h.layoutEmoji.addView(
                        makeEmoji(h.itemView,"😌")
                );

                break;


            case "NEUTRAL":

                h.layoutEmoji.addView(
                        makeEmoji(h.itemView,"😳")
                );

                break;


            default:

                h.layoutEmoji.addView(
                        makeEmoji(h.itemView,"😐")
                );

                break;
        }

    }



    private TextView makeEmoji(View v, String emoji){

        TextView tv =
                new TextView(
                        v.getContext()
                );

        tv.setText(emoji);
        tv.setTextSize(24);

        return tv;
    }



    @Override
    public int getItemCount(){
        return list.size();
    }



    static class Holder extends RecyclerView.ViewHolder{


        TextView txtName;
        FrameLayout layoutEmoji;
        ImageView imgAvatar;


        Holder(View v){
            super(v);


            txtName =
                    v.findViewById(R.id.txtName);


            layoutEmoji =
                    v.findViewById(R.id.layoutEmoji);


            imgAvatar =
                    v.findViewById(R.id.imgAvatar);
        }
    }
}