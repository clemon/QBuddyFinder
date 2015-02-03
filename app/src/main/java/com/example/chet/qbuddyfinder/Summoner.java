package com.example.chet.qbuddyfinder;

import java.util.HashMap;

/**
 * Created by Chet on 2/1/2015.
 */
public class Summoner {

    private String name;
    private String division;
    private String position;
    private HashMap<String, Integer> frequentChamps;

    public Summoner(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getDivision() {
        return this.division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getPosition() {
        return this.position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
