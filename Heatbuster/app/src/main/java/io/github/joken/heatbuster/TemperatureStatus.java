package io.github.joken.heatbuster;

import com.beardedhen.androidbootstrap.api.attributes.BootstrapBrand;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;

/**
 * 温度しきい値に応じた状態表示
 */
public enum TemperatureStatus {
	Emergency(R.drawable.ic_die, DefaultBootstrapBrand.SECONDARY),
	Warning(R.drawable.ic_sad, DefaultBootstrapBrand.WARNING),
	Safe(R.drawable.ic_smile, DefaultBootstrapBrand.PRIMARY);

	/** SafeとWarningのしきい値 */
	//TODO 正確な値の設定
	private static final double Safe_Limen = 26.0;

	private final int imageID;
	private final BootstrapBrand brand;

	TemperatureStatus(int id, BootstrapBrand br){
		this.imageID = id;
		this.brand = br;
	}

	public static TemperatureStatus getStatusbyTemp(int temp){
		if(temp <= Safe_Limen){
			return Safe;
		}else{
			return Warning;
		}
	}

	public int getImageID(){
		return this.imageID;
	}

	public BootstrapBrand getBrand(){
		return this.brand;
	}

	public String getStatusText(){
		switch(this){
			case Emergency:
				return "直ちに水分補給することを推奨します";
			case Warning:
				return "2分以内の水分補給を推奨します";
			case Safe:
				return "30分後の水分補給を推奨します";
			default:
				return "";
		}
	}
}
