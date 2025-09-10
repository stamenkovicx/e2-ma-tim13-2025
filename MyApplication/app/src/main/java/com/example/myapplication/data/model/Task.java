package com.example.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks_table")
public class Task {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // --- Polja iz specifikacije ---

    private String naziv;
    private String opis;
    private String kategorija;
    private String uceslost;
    private String tezina;
    private String bitnost;
    private long vremeIzvrsenja;
    private String status;// Zadaci mogu imati status: aktivan, urađen, neurađen, pauziran... [cite: 115]

    // --- Konstruktor ---

    public Task() {
        // Prazan konstruktor je potreban za Room i Firebase
    }

    // --- Svi Getteri i Setteri ---

    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }

    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }

    public String getKategorija() { return kategorija; }
    public void setKategorija(String kategorija) { this.kategorija = kategorija; }

    public String getUceslost() { return uceslost; }
    public void setUceslost(String uceslost) { this.uceslost = uceslost; }

    public String getTezina() { return tezina; }
    public void setTezina(String tezina) { this.tezina = tezina; }

    public String getBitnost() { return bitnost; }
    public void setBitnost(String bitnost) { this.bitnost = bitnost; }

    public long getVremeIzvrsenja() { return vremeIzvrsenja; }
    public void setVremeIzvrsenja(long vremeIzvrsenja) { this.vremeIzvrsenja = vremeIzvrsenja; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}