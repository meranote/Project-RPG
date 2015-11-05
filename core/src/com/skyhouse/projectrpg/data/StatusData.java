package com.skyhouse.projectrpg.data;

/**
 * Status data of character.
 * @author Meranote
 */
public class StatusData extends Data {
	
	public String name;
	public int job;
	public int level;
	public int exp;
	public int str;
	public int agi;
	public int intel;
	public int[] equip;
	
	/**
	 * Construct a new status data.
	 */
	public StatusData() {
		equip = new int[7];
	}

}
