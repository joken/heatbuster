package io.github.joken.heatbuster;

public class Clubmonitor {
    //部活名
    private String clubname;
    //部活に所属している人の平均温度
    private float clubTemp;
    //clubTemplaの体温上昇率
    private float tempIncreaseRate;
    //状態
    private TemperatureStatus selfStatus;

    public Clubmonitor(String clubname, float clubTemp, TemperatureStatus selfStatus){
        this.clubname = clubname;
        this.clubTemp = clubTemp;
        this.selfStatus = selfStatus;
    }

    public String getName(){
        return this.clubname;
    }

    public String getClubTemp(){
        return "平均深層体温:"+ String.valueOf(this.clubTemp)+"℃";
    }

    public String getTempIncreaseRate() { return "体温上昇率:" + String.valueOf(this.tempIncreaseRate)+"%";}

    public TemperatureStatus getSelfStatus() {return selfStatus;}

}
