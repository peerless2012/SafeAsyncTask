package com.peerless2012.safeasynctask;

/**
* @Author peerless2012
* @Email  peerless2012@126.com
* @HomePage http://peerless2012.github.io
* @DateTime 2016年5月22日 上午9:12:12
* @Version V1.0
* @Description: 人
*/
public class Person {

	private String name;
	
	private String country;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String toString() {
		return "Person [name=" + name + ", country=" + country + "]";
	}
	
}
