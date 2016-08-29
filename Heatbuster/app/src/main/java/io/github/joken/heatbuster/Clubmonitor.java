package io.github.joken.heatbuster;

public class Clubmonitor {
    //部活名
    private String clubname;
    //部活に所属している人の平均温度
    private float clubTempla=0f;
    //clubTemplaの体温上昇率
    private float templaIncreaseRate=0f;

    public Clubmonitor(String clubname, float clubTempla){
        this.clubname = clubname;
        this.clubTempla = clubTempla;
    }

    public String getName(){
        return this.clubname;
    }

    public String getClubTempla(){
        return "平均深層体温:"+ String.valueOf(this.clubTempla)+"℃";
    }

    public String getTemplaIncreaseRate() { return "体温上昇率:" + String.valueOf(this.templaIncreaseRate)+"%";}

}
