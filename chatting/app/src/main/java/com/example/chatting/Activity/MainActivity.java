package com.example.chatting.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatting.Adapter.MainRecyclerAdapter;
import com.example.chatting.DAO.UserDAO;
import com.example.chatting.Model.ItemMain;
import com.example.chatting.Model.User;
import com.example.chatting.Provider.ImageConvert;
import com.example.chatting.Provider.SharedPreferenceProvider;
import com.example.chatting.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    List<User> onlines;
    List<User> chats;
    List<ItemMain> itemMains;
    MainRecyclerAdapter mainRecyclerAdapter;

    User user;

    RecyclerView rc_main;
    SwipeRefreshLayout swipe_main;
    BottomNavigationView bottom_nav;
    Intent intent;
    ImageView img_avatar;
    TextView tv_name;

    String name;
    boolean isLoading = false;

    public static String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getModel();
        getView();
        setView();
        setOnClick();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void getModel(){

        user = (User)SharedPreferenceProvider.getInstance(this).get("user");
        onlines = new ArrayList<User>();
        chats = new ArrayList<User>();
        itemMains = new ArrayList<ItemMain>();
    }

    public void getView(){
        rc_main = findViewById(R.id.rc_main);
        swipe_main = findViewById(R.id.swipe_main);
        bottom_nav = findViewById(R.id.bottom_nav);
        img_avatar = findViewById(R.id.img_avatar);
        tv_name = findViewById(R.id.tv_name);
    }

    public void setView(){

        bottom_nav.setSelectedItemId(R.id.chats);
        ImageConvert.setUrlToImageView(img_avatar, user.getAvatar());
        tv_name.setText(user.getName());

        loadData();
        loadDataChats();
//        loadData();
        mainRecyclerAdapter = new MainRecyclerAdapter(itemMains, user);
        rc_main.setLayoutManager(new LinearLayoutManager(this));
        rc_main.setItemAnimator(new DefaultItemAnimator());
        rc_main.setAdapter(mainRecyclerAdapter);
//
//        for (int i = 0; i < 80; i++){
//            String avatar = "https://scontent-sin6-2.xx.fbcdn.net/v/t1.6435-1/p160x160/69910467_1207902709417404_9121282489589956608_n.jpg?_nc_cat=105&ccb=1-3&_nc_sid=7206a8&_nc_ohc=wKK0HteI64oAX9LBpSA&_nc_ht=scontent-sin6-2.xx&tp=6&oh=e03ba07c49fa741f733584adcf77fb5c&oe=60E5E641";
//            User user = new User(avatar, "An Van " + i, "tranan250" + i + "@gmail.com", "123456789");
//            UserDAO.getInstance().add(user);
//        }

//        UserDAO.getInstance().update("-MdlQs3pVz1SLJIqH-Zh", user);
//        UserDAO.getInstance().remove("-MdlQs3pVz1SLJIqH-Zh");
    }

    public void setOnClick(){
        rc_main.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rc_main.getLayoutManager();
                int total = linearLayoutManager.getItemCount();
                int lastVisible = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (total < lastVisible + 3){
                    if (!isLoading){
                        isLoading = true;
                        loadDataChats();
                    }

                }
            }
        });

        bottom_nav.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        img_avatar.setOnClickListener(this);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.img_avatar:
                intent = new Intent(getApplicationContext(), AccountActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void loadData(){
        itemMains.add(new ItemMain(onlines, ItemMain.ItemType.ONLINE_ITEM));
//        for (int i = 0; i < chats.size(); i++){
//            itemMains.add(new ItemMain(chats.get(i), ItemMain.ItemType.CHAT_ITEM));
//        }
    }

    public void loadDataChats(){
        swipe_main.setRefreshing(true);
        UserDAO.getInstance().get(name, 10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<User> usersChat= new ArrayList<User>();
                for (DataSnapshot data: snapshot.getChildren()){
                    User user = data.getValue(User.class);
                    usersChat.add(user);
                    chats.add(user);
                    name = user.getName();
                }
                updateDataOnline(usersChat);
                isLoading = false;
                swipe_main.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                swipe_main.setRefreshing(false);
            }
        });
    }

    public void updateDataOnline(List<User> users) {
//        itemMains.clear();
        List<ItemMain> items = new ArrayList<ItemMain>();
        for (User user :users){
            items.add(new ItemMain(user, ItemMain.ItemType.CHAT_ITEM));
        }
        itemMains.addAll(items);
        mainRecyclerAdapter.notifyDataSetChanged();
    }

    BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.chats:
                    return false;
                case R.id.people:
                    intent = new Intent(getApplicationContext(), PeopleActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        }
    };

}