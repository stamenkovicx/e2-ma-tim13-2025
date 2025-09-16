package com.example.myapplication.presentation.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.presentation.ui.adapters.UserSearchAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersActivity extends AppCompatActivity {

    private EditText searchEditText;
    private ImageButton searchButton;
    private RecyclerView usersRecyclerView;
    private UserSearchAdapter adapter;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);

        // Inicijalizacija UI komponenti
        searchEditText = findViewById(R.id.editTextSearchUsername);
        searchButton = findViewById(R.id.buttonSearch);
        usersRecyclerView = findViewById(R.id.recyclerViewUsers);

        // Inicijalizacija UserRepository
        userRepository = new UserRepositoryFirebaseImpl();

        // Podešavanje RecyclerView-a
        adapter = new UserSearchAdapter(new ArrayList<>());
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(adapter);

        // Postavljanje listener-a na dugme za pretragu
        searchButton.setOnClickListener(v -> searchUsers());

        // Pretraga u realnom vremenu (na unos teksta)
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    searchUsers();
                } else {
                    adapter.setUsers(new ArrayList<>());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchUsers() {
        String username = searchEditText.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username to search.", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.searchUsers(username, new UserRepository.OnCompleteListener<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                if (result.isEmpty()) {
                    Toast.makeText(SearchUsersActivity.this, "No users found.", Toast.LENGTH_SHORT).show();
                }
                // Ažuriranje RecyclerView adaptera sa rezultatima
                adapter.setUsers(result);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SearchUsersActivity.this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}