package com.example.tinycare;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class DataSource {

    private ArrayList<CardData> dataArrayList;
    private ArrayList<String> idsAdded;

    DataSource() {
        dataArrayList = new ArrayList<>();
        idsAdded = new ArrayList<>();
    }

    void addData(String name, String path, String type, String id){
        CardData c = new CardData(name, path, type, id);
        dataArrayList.add(c);
        idsAdded.add(id);
    }

    void removeData(int position) {
        dataArrayList.remove(position);
        idsAdded.remove(position);
    }

    boolean idAlreadyAdded(String id) {
        if (idsAdded.contains(id)) {
            return true;
        }
        return false;
    }

    String getName(int i) {
        return dataArrayList.get(i).getName();
    }

    String getPath(int i) {
        return dataArrayList.get(i).getPath();
    }

    String getType(int i) {
        return dataArrayList.get(i).getType();
    }

    String getId(int i) {
        return dataArrayList.get(i).getId();
    }

    Bitmap getImage(int i) {
        String name = dataArrayList.get(i).getName();
        String path = dataArrayList.get(i).getPath();
        return Utils.loadImageFromStorage(path, name);
    }

    int getSize() {
        return dataArrayList.size();
    }

    private static class CardData{
        private String name;
        private String path;
        private String type;
        private String id;

        private CardData(String name, String path, String type, String id){
            this.name = name;
            this.path = path;
            this.type = type;
            this.id = id;
        }

        private String getName() {
            return name;
        }

        private String getPath() {
            return path;
        }

        private String getType() {
            return type;
        }

        private String getId() {
            return id;
        }
    }
}
