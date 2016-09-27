package io.github.joken.heatbuster;

public class CheckBoxItem {
    //serialナンバー(今はintに設定しているがどうなるかわからない)
    private String serial="N/A";
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
    public boolean getChecked()
    {
        return this.checked;
    }
    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }
}
