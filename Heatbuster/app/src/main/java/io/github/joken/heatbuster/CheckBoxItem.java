package io.github.joken.heatbuster;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class CheckBoxItem implements Serializable{
    //serialナンバー(今はintに設定しているがどうなるかわからない)
    private String serial="N/A";
    //BluetoothDeviceの実体参照
    private BluetoothDevice device;
    //checkされていたらtrue,されていなかったらfalse
    private Boolean checked;
    //温度
    private Float temple=0.0f;
    //湿度
    private Float humid=0.0f;
    //emergency_flag
    private Boolean emer_flag=false;
    //status
    private TemperatureStatus stat=TemperatureStatus.Safe;

    public CheckBoxItem(String serial)
    {
        this.serial=serial;
    }

    public String getSerial()
    {
        return this.serial;
    }
    public void setSerial(String serial)
    {
        this.serial= serial;
    }
    public void setDevice(BluetoothDevice d){this.device = d;}
    public BluetoothDevice getDevice(){return this.device;}
    public boolean getChecked()
    {
        return this.checked;
    }
    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }
    public float getTemple(){return this.temple;};
    public void setTemple(float temple){this.temple=temple;}
    public float getHumid(){return  this.humid;}
    public void setHumid(float humid){this.humid=humid;}
    public boolean getEmer_flag(){return this.emer_flag;}
    public void setEmer_flag(boolean flag){this.emer_flag=flag;}
    public String getStat(){
        if (this.emer_flag){
            return "EMER";
        }
        this.stat = TemperatureStatus.getStatusbyTemp(this.getTemple());
        switch (stat){
            case Warning:
                return "WARN";
            case Safe:
                return "SAFE";
        }
        return "SAFE";
    }
}
