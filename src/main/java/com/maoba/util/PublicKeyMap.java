package com.maoba.util;
/**
 * @author kitty daddy
 */
public class PublicKeyMap {
	private String ownModulus;
	private String exponent;

	public String getOwnModulus() {
		return ownModulus;
	}

	public void setOwnModulus(String ownModulus) {
		this.ownModulus = ownModulus;
	}

	public String getExponent() {
		return exponent;
	}

	public void setExponent(String exponent) {
		this.exponent = exponent;
	}

	public String toString() {
		return "PublicKeyMap [modulus=" + ownModulus + ", exponent=" + exponent + "]";
	}
}
