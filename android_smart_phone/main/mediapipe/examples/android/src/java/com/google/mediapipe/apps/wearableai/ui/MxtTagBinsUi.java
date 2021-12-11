package com.google.mediapipe.apps.wearableai.ui;

// some code taken from https://github.com/stairs1/memory-expansion-tools/blob/master/AndroidMXT/app/src/main/java/com/memoryexpansiontools/mxt/StreamFragment.java

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import java.util.ArrayList;
import android.widget.EditText;

import java.util.Arrays;
import java.util.List;
import java.lang.Long;

import com.google.mediapipe.apps.wearableai.database.phrase.Phrase;
import com.google.mediapipe.apps.wearableai.database.voicecommand.VoiceCommandEntity;
import com.google.mediapipe.apps.wearableai.database.voicecommand.VoiceCommandViewModel;
import com.google.mediapipe.apps.wearableai.database.phrase.PhraseViewModel;

import androidx.lifecycle.LiveData;

import android.widget.AdapterView;

import com.google.mediapipe.apps.wearableai.ui.ItemClickListener;

import com.google.mediapipe.apps.wearableai.R;

//menu imports:
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StreamFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MxtTagBinsUi extends Fragment implements ItemClickListener {
    public String TAG = "WearableAi_MxtTagBinsUi";

    private LiveData<List<Phrase>> tagBinPhrases;
    private Observer<List<Phrase>> tagBinPhrasesObserver;
    private PhraseListAdapter adapter;

    private VoiceCommandViewModel mVoiceCommandViewModel;
    private PhraseViewModel mPhraseViewModel;

    private String tmpTag = "wearable";

    @Override
    public void onClick(View view, Phrase phrase){
//        Intent intent = new Intent(MainActivity.this, ViewVoiceCommandActivity.class);
//        intent.putExtra("voiceCommand", voiceCommand.getId());
//        startActivityForResult(intent, VIEW_PHRASE_ACTIVITY_REQUEST_CODE);
        //pass on this for now, need to make the voiceCommand view thing a fragment as well
        Log.d(TAG, "click on phrase");
    }

    public MxtTagBinsUi() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.mxt_tag_bins_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        //populate dropdown with tags
        populateTopMenu(view);
        
        //populate view with phrases matching tag
        RecyclerView recyclerView = view.findViewById(R.id.phrase_wall);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        adapter = new PhraseListAdapter(getContext());
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() -1);


        // Get a new or existing ViewModel from the ViewModelProvider.
        mVoiceCommandViewModel = new ViewModelProvider(this).get(VoiceCommandViewModel.class);
        mPhraseViewModel = new ViewModelProvider(this).get(PhraseViewModel.class);

        updateTag(tmpTag);

//        EditText editText = view.findViewById(R.id.add_voiceCommand_text);
//        Button submitButton = view.findViewById(R.id.submit_button);
//        submitButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String words = editText.getText().toString();
//                if(!words.isEmpty()) {
//                    editText.setText("");
//                    mVoiceCommandViewModel.addVoiceCommand(words, getString(R.string.medium_text));
//                }
//            }
//        });

    }

    public void populateTopMenu(View v){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.menu_item_exposed_dropdown, getResources().getStringArray(R.array.exposed_dropdown_content));

        //setup the optional selections
        AutoCompleteTextView tagMenu = v.findViewById(R.id.tag_menu_tv);
        tagMenu.setAdapter(adapter);

        //set menu to first option
        tagMenu.setText(tagMenu.getAdapter().getItem(0).toString(), false);

        //setup listener
        tagMenu.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                Log.d(TAG, "GOT CLICK ON TAG MENU");
                String newTag = ((TextView) v).getText().toString();
                updateTag(newTag);
            }
        });

    }

    public void updateTag(String newTag){
        newTag = newTag.toLowerCase();

        Log.d(TAG, "Got new tag: " + newTag);

        if (tagBinPhrases != null){
            tagBinPhrases.removeObserver(tagBinPhrasesObserver);
        }

        //here we need to get all of the MXT commands, and then pull in each phrase associated with each command
        tagBinPhrases = mVoiceCommandViewModel.getTagBin(newTag);
        tagBinPhrasesObserver = new Observer<List<Phrase>>() {
            @Override
            public void onChanged(@Nullable final List<Phrase> phrases) {
                adapter.setPhrases(phrases);
            }
        };
        tagBinPhrases.observe(getActivity(), tagBinPhrasesObserver);
    }

}

