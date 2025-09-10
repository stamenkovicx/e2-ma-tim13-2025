package com.example.myapplication.ui.activities; // Tvoj paket - ne menjaj

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

// Potrebni importi za testiranje
import com.example.myapplication.data.model.Task;
import com.example.myapplication.data.repository.TaskRepository;

// Ostali tvoji importi
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.myapplication.R;

public class MainActivity extends AppCompatActivity {

    // --- DEO KOJI DODAJEMO ---
    private TaskRepository taskRepository;
    // --- KRAJ DODATOG DELA ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- DEO KOJI DODAJEMO ZA TESTIRANJE ---

        // Inicijalizujemo Repository da bismo mogli da pristupamo bazi
        taskRepository = new TaskRepository(getApplication());

        // Pozivamo metodu koja će kreirati i upisati testni zadatak
        testirajBazu();

        // --- KRAJ DODATOG DELA ---
    }

    // --- NOVA METODA ZA TESTIRANJE ---
    private void testirajBazu() {
        // Kreiramo novi objekat klase Task
        Task testniZadatak = new Task();
        testniZadatak.setNaziv("Testiranje baze je uspelo!");
        testniZadatak.setOpis("Ovaj zadatak je upisan u lokalnu bazu.");
        testniZadatak.setStatus("aktivan");

        // Koristimo repository da bismo upisali zadatak u bazu
        // Repository će ovo izvršiti na pozadinskoj niti, tako da je bezbedno
        taskRepository.insertTask(testniZadatak);
    }
    // --- KRAJ NOVE METODE ---
}