package io.github.joken.heatbuster;

/**
 * 温度しきい値に応じた状態表示
 */
public enum TemperatureStatus {
	Emergency,
	Warning,
	Safe;

	/** SafeとWarningのしきい値 */
	//TODO 正確な値の設定
	private static final double Safe_Limen = 26.0;

	public static TemperatureStatus getStatusbyTemp(int temp){
		if(temp <= Safe_Limen){
			return Safe;
		}else{
			return Warning;
		}
	}
}
