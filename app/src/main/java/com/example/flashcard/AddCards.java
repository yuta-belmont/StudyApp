package com.example.flashcard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddCards extends AppCompatActivity implements View.OnClickListener {

    private List<List<String>> cards = new ArrayList<>();
    private Deck deck = new Deck();
    private TextView tvFront, tvBack, tvDone, tvAdd;
    private EditText edtTitle, edtFront, edtBack;
    private ImageView ivSave, ivAdd;
    private ProgressBar progressBar;
    String deckId, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        username = user.getDisplayName();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cards);

        tvFront = (TextView) findViewById(R.id.tvFront);
        tvBack = (TextView) findViewById(R.id.tvBack);
        tvDone = (TextView) findViewById(R.id.tvDone);
        tvAdd = (TextView) findViewById(R.id.tvAddAnother);
        edtTitle = (EditText) findViewById(R.id.edtAddTitle);
        ivSave = (ImageView) findViewById(R.id.ivSave) ;
        ivAdd = (ImageView) findViewById(R.id.ivAdd);
        edtFront = (EditText) findViewById(R.id.edtFront);
        edtBack = (EditText) findViewById(R.id.edtBack);
        progressBar = (ProgressBar) findViewById(R.id.pgBar);
        progressBar.setVisibility(View.INVISIBLE);


        tvAdd.setOnClickListener(this);
        ivAdd.setOnClickListener(this);
        ivSave.setOnClickListener(this);
        tvDone.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            //set title name, deck contents
        }
        else{
            Date date = new Date();
            deckId = String.valueOf(date.getTime());
            deckId = deckId.substring(deckId.length()-9);
        }






    }
    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.ivAdd:
            case R.id.tvAddAnother:
                if (!edtFront.getText().toString().equals("") && !edtBack.getText().toString().equals("")){
                    cards.add(Arrays.asList(new String[]{edtFront.getText().toString(), edtBack.getText().toString()}));
                    edtFront.setText("");
                    edtBack.setText("");
                    edtFront.requestFocus();
                }
                else{
                    Toast.makeText(this, "Incomplete card." ,Toast.LENGTH_SHORT);
                }

                break;
            case R.id.ivSave:
            case R.id.tvDone:
                progressBar.setVisibility(View.VISIBLE);
                //adds the remaining card to the deck:
                if (!edtFront.getText().toString().equals("") && !edtBack.getText().toString().equals("")){
                    cards.add(Arrays.asList(new String[]{edtFront.getText().toString(), edtBack.getText().toString()}));
                }
                deck.cards = cards;
                deck.title = edtTitle.getText().toString();
                deck.author = username;
                deck.Uid = FirebaseAuth.getInstance().getUid();
                deck.deckId = deckId;
                if (cards.size() == 0){

                    Intent i = new Intent(AddCards.this, PublicDecks.class);
                    startActivity(i);

                }


                 //add deck to firebase:

                FirebaseDatabase.getInstance().getReference("Decks").child(deckId)
                        .setValue(deck).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            FirebaseDatabase.getInstance().getReference("Users").child(deck.Uid).child("MyDecks").child(deckId).setValue(deckId);


                            Intent i = new Intent(AddCards.this, MainActivity.class);
                            startActivity(i);

                        }
                        else{
                            Toast.makeText(AddCards.this, "Failed to upload new deck.", Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });

        }
    }


}