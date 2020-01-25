package com.example.tinycare;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Date;

public class PetDataSource {

    private ArrayList<CardData> dataArrayList;
    private ArrayList<String> idsAdded;

    PetDataSource() {
        dataArrayList = new ArrayList<>();
        idsAdded = new ArrayList<>();
    }

    void addData(String name, String path, String type, String id){
        Date date = new Date();
        CardData c = new CardData(name, path, type, id, date);
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

    void setName(int i, String name) {
        dataArrayList.get(i).setName(name);
    }

    String getPath(int i) {
        return dataArrayList.get(i).getPath();
    }

    void setPath(int i, String path) {
        dataArrayList.get(i).setPath(path);
    }

    String getType(int i) {
        return dataArrayList.get(i).getType();
    }

    String getId(int i) {
        return dataArrayList.get(i).getId();
    }

    Date getDate(int i) { return dataArrayList.get(i).getDate(); }

    Bitmap getImage(int i) {
        //String name = dataArrayList.get(i).getName();
        String path = dataArrayList.get(i).getPath();
        return Utils.decodeBase64(path);
        //return Utils.loadImageFromStorage(path, name);
    }

    int getSize() {
        return dataArrayList.size();
    }

    private static class CardData{
        private String name;
        private String path;
        private String type;
        private String id;
        private Date date;

        private CardData(String name, String path, String type, String id, Date date){
            this.name = name;
            this.path = path;
            this.type = type;
            this.id = id;
            this.date = date;
        }

        private String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        private String getPath() {
            return path;
        }

        private void setPath(String path) {
            this.path = path;
        }

        private String getType() {
            return type;
        }

        private String getId() {
            return id;
        }

        private Date getDate() {return date;}
    }
}
