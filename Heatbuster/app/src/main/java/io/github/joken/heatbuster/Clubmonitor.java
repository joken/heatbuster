package io.github.joken.heatbuster;

public class Clubmonitor {
    //部活名
    private String clubname;
    //部活の平均温度
    private float clubTempla;

    public Clubmonitor(String clubname, float clubTempla){
        this.clubname = clubname;
        this.clubTempla = clubTempla;
    }

    public String getName(){
        return this.clubname;
    }

    public String getClubTempla(){
        return String.valueOf(this.clubTempla);
    }

}
