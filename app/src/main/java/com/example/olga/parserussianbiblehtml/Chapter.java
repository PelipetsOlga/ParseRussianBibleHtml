package com.example.olga.parserussianbiblehtml;

import java.util.ArrayList;

/**
 * Created by Olga on 02.10.2015.
 */
public class Chapter {
    private String title="";
    private int num=0;
    private ArrayList<String> content=new ArrayList<String>();

    public String getTitle() {
        return title;
    }

    public int getNum() {
        return num;
    }

    public ArrayList<String> getContent(){
        return content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void addText(String s){
        content.add(s);
    }

    @Override
    public String toString() {
        return "Chapter title: "+title+", num "+num+", count positions "+content.size();
    }
}


