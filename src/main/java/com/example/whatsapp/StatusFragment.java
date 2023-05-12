package com.example.whatsapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.hdodenhof.circleimageview.CircleImageView;


public class StatusFragment extends Fragment {

    Activity context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();

        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_status, container, false);
        return root;
    }
    public void onStart(){
        super.onStart();
        CircleImageView img =(CircleImageView) context.findViewById(R.id.idBtnStories);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,StoryActivity.class);
                startActivity(intent);

            }
        });
    }
}