package io.github.joken.heatbuster;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class CheckBoxItem implements Serializable{
    //serialナンバー(今はintに設定しているがどうなるかわからない)
    private String serial="N/A";
    //BluetoothDeviceの実体参照
    private BluetoothDevice device;
    //checkされていたらtrue,されていなかったらfalse
    private boolean checked;

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
}
