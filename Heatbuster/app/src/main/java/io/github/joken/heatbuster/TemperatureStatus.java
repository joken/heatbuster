package io.github.joken.heatbuster;

/**
 * 温度しきい値に応じた状態表示
 */
public enum TemperatureStatus {
	Emergency(R.drawable.ic_die),
	Warning(R.drawable.ic_sad),
	Safe(R.drawable.ic_smile);

	/** SafeとWarningのしきい値 */
	//TODO 正確な値の設定
	private static final double Safe_Limen = 26.0;

	private final int imageID;

	TemperatureStatus(int id){
		this.imageID = id;
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
}
