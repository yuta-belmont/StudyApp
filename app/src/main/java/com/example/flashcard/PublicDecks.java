package com.example.flashcard;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PublicDecks extends AppCompatActivity implements View.OnClickListener{

    ArrayList<Deck> allDecks = new ArrayList<>();
    ArrayList<String> myDeckKeys = new ArrayList<>();
    Map<String, Deck> keyedDecks = new HashMap<>();
    ArrayList<Deck> personalDecks = new ArrayList<>();
    private ListView lvDecks;

    private boolean isGuest = false;
    private boolean inPublic = true;

    FirebaseDatabase rootRef = FirebaseDatabase.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    User usr = new User();


    private ImageView addDeck;
    private TextView myDecks, publicDecks, logout;
    private SearchView svDecks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_decks);

        //check if current user is a guest:

        readAllDecks(rootRef.getReference("Decks"), new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onFailure() {
            }
        });

        isGuest = checkGuest();

        //get the users decks
        if (!isGuest) {
            Log.d("chk_guest", "not guest");
        }

        svDecks = (SearchView) findViewById(R.id.svSearchPublic);
        myDecks = (TextView) findViewById(R.id.tvMyDecks);
        publicDecks = (TextView) findViewById(R.id.tvPublicDecks);
        logout = (TextView) findViewById(R.id.tvLogout);
        addDeck = (ImageView)findViewById(R.id.abPlusPublic);
        lvDecks = (ListView) findViewById(R.id.lvDecksPublic);

        //set logout stuff:
        logout.setTextColor(Color.parseColor("#ff0000"));

        myDecks.setOnClickListener(this);
        addDeck.setOnClickListener(this);
        publicDecks.setOnClickListener(this);
        logout.setOnClickListener(this);

        if (inPublic){
            DeckListAdapter adapter = new DeckListAdapter(isGuest,this, R.layout.deck_lv_item, allDecks);
            lvDecks.setAdapter(adapter);

        }
        else{
            MyDeckListAdapter adapter = new MyDeckListAdapter(isGuest,this, R.layout.deck_lv_item, personalDecks);
            lvDecks.setAdapter(adapter);

        }

        lvDecks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<Deck> sendDeck = new ArrayList<>();
                if(inPublic){
                    sendDeck = allDecks;
                }
                else{
                    sendDeck = personalDecks;
                }

                Intent i = new Intent(PublicDecks.this, ViewCard.class);
                i.putExtra("Deck", sendDeck.get(position));
                startActivity(i);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v){

        switch (v.getId()){

            case R.id.tvMyDecks:

                if (isGuest){
                    Toast.makeText(PublicDecks.this, "You must be signed in.", Toast.LENGTH_LONG).show();
                }
                else {
                    //switch decks
                    inPublic = false;
                    Log.d("chk_clickMyDecks", "keys: " + myDeckKeys + "  keyedDeck: " + keyedDecks.toString() + " personalDeck: " + personalDecks.toString());
                    publicDecks.setTextAppearance(this, android.R.style.TextAppearance_Material_Body1);
                    myDecks.setTextAppearance(this, android.R.style.TextAppearance_Large);
                    Log.d("chk_insideclick", personalDecks.toString());
                    MyDeckListAdapter adapter = new MyDeckListAdapter(isGuest,this, R.layout.deck_lv_item, personalDecks);
                    lvDecks.setAdapter(adapter);
                    switchToMine();
                }
                break;
            case R.id.tvPublicDecks:
                inPublic = true;
                publicDecks.setTextAppearance(this, android.R.style.TextAppearance_Large);
                myDecks.setTextAppearance(this, android.R.style.TextAppearance_Material_Body1);
                DeckListAdapter adapter = new DeckListAdapter(isGuest,this, R.layout.deck_lv_item, allDecks);

                lvDecks.setAdapter(adapter);
                switchToPublic();
                break;
            case R.id.abPlusPublic:
                if (isGuest){
                    Toast.makeText(PublicDecks.this, "You must be signed in.", Toast.LENGTH_LONG).show();
                }
                else {
                    LayoutInflater inflater = (LayoutInflater)
                            getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.create_deck_popup, null);

                    TextView tvPopup = popupView.findViewById(R.id.tvPopup);
                    tvPopup.setText("Create Deck?");

                    // create the popup window
                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    boolean focusable = true; // lets taps outside the popup also dismiss it
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                    // show the popup window
                    // which view you pass in doesn't matter, it is only used for the window tolken
                    popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                    popupView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addCards();
                        }
                    });
                }
                break;
            case R.id.svSearchPublic:
                searchPublic();
                Log.d("search", "Clicked");
                break;
            case R.id.tvLogout:
                LayoutInflater inflater1 = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView1 = inflater1.inflate(R.layout.create_deck_popup, null);

                TextView tvPopup1 = popupView1.findViewById(R.id.tvPopup);
                tvPopup1.setText("Logout?");

                // create the popup window
                int width1 = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height1 = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable1 = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow1 = new PopupWindow(popupView1, width1, height1, focusable1);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow1.showAtLocation(v, Gravity.CENTER, 0, 0);

                popupView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logout();
                    }
                });

                break;
        }
    }
    public void searchPublic(){
        return;
    }
    public boolean checkGuest(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user==null){
            return true;
        }
        return false;
    }


    public void logout(){
        mAuth.signOut();
        Intent i = new Intent(PublicDecks.this, LoginActivity.class);
        startActivity(i);
    }

    public void createDeck(){
        Intent i = new Intent(PublicDecks.this, CreateDeck.class);
        startActivity(i);
    }

    public void addCards(){
        Intent i = new Intent (PublicDecks.this, AddCards.class);
        startActivity(i);
    }

    //get all decks from firebase database:
    public void readAllDecks(DatabaseReference rr, final OnGetDataListener listener){

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
                    allDecks.add(thisDeck);
                }
                listener.onSuccess(dataSnapshot);
                if(!isGuest) {
                    DatabaseReference thisUser = rootRef.getReference("Users").child(mAuth.getCurrentUser().getUid())
                            .child("MyDecks");
                    getUserData(thisUser, new OnGetDataListener() {

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
    public void switchToPublic(){
        inPublic = true;
    }

    public void switchToMine(){
        inPublic = false;
    }

    public void getUserData(DatabaseReference thisUser, final OnGetDataListener listener){


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

    public interface OnGetDataListener {
        //this is for callbacks
        void onSuccess(DataSnapshot dataSnapshot);
        void onFailure();
    }
}