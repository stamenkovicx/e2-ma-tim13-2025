package com.example.myapplication.domain.models;

import java.io.Serializable;
import java.util.Objects;

public class Category implements Serializable {
    private String id;
    private String name;
    private int color;

    // Konstruktor bez argumenata je potreban za Firebase
    public Category() {
    }

    public Category(String id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    // Dodatni konstruktor za lakše kreiranje nove kategorije bez ID-a
    public Category(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}