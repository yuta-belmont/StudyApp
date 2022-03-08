package com.example.flashcard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadingScreen extends AppCompatActivity {

    private ProgressBar pbLoading;
    private boolean isGuest;
    private ArrayList<Deck> allDecks = new ArrayList<>();
    private ArrayList<String> myDeckKeys = new ArrayList<>();
    private Map<String, Deck> keyedDecks = new HashMap<>();
    private ArrayList<Deck> personalDecks = new ArrayList<>();


    FirebaseDatabase rootRef = FirebaseDatabase.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        pbLoading = (ProgressBar) findViewById(R.id.pbLoadDecks);
        pbLoading.setVisibility(View.VISIBLE);

        isGuest = checkGuest();

        readAllDecks(rootRef.getReference("Decks"), new LoadingScreen.OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onFailure() {
            }
        });

        Log.d("chk","Before while" );
        //waits until the decks are loaded
        //while (allDecks.isEmpty()){
        //}

        Log.d("chk", "after while");
        Intent i = new Intent(this, PublicDecks.class);
        startActivity(i);



    }

    //get all decks from the database:
    public void readAllDecks(DatabaseReference rr, final LoadingScreen.OnGetDataListener listener){

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String uid = ds.child("uid").getValue(String.class);
                    String author = ds.child("author").getValue(String.class);
                    String title = ds.child("title").getValue(String.class);
                    List<List<String>> cards = (List<List<String>>) ds.child("cards").getValue();
                    String did = ds.child("deckId").getValue(String.class);
                    Deck thisDeck = new Deck(did, uid, title, author, cards);
                    Log.d("chk", "inOndatatChagne");
                    allDecks.add(thisDeck);
                }
                listener.onSuccess(dataSnapshot);
                if(!isGuest) {
                    DatabaseReference thisUser = rootRef.getReference("Users").child(mAuth.getCurrentUser().getUid())
                            .child("MyDecks");
                    getUserData(thisUser, new LoadingScreen.OnGetDataListener() {

                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                        }

                        @Override
                        public void onFailure() {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure();
            }
        };
        rr.addListenerForSingleValueEvent(eventListener);
    }

    //get the current users data:
    public void getUserData(DatabaseReference thisUser, final LoadingScreen.OnGetDataListener listener){
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot datasnapshot){
                for (DataSnapshot snapshot : datasnapshot.getChildren()){
                    //usr = snapshot.getValue(User.class);
                    String hi = snapshot.getValue(String.class);
                    myDeckKeys.add(hi);
                }
                for (Deck d : allDecks){
                    keyedDecks.put(d.deckId, d);
                }
                //find user specific decks:
                for (String s : myDeckKeys){
                    personalDecks.add(keyedDecks.get(s));
                }
                listener.onSuccess(datasnapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure();
            }
        };
        thisUser.addListenerForSingleValueEvent(valueEventListener);
    }

    //for getting all decks from this activity
    public ArrayList<Deck> getAllDecks(){
        return allDecks;
    }
    //for getting personal decks from this activity:
    public ArrayList<Deck> getPersonalDecks(){
        return personalDecks;
    }


    //check if the user is a guest:
    public boolean checkGuest(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user==null){
            return true;
        }
        return false;
    }

    public interface OnGetDataListener {
        //this is for callbacks
        void onSuccess(DataSnapshot dataSnapshot);
        void onFailure();
    }
}