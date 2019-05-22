package com.fudan.sw.dsa.project2.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * For each station of subway
 * If you need other attribute, add it
 * @author zjiehang
 *
 */
public class Address implements Serializable
{
	private String address;
	private double longitude;//经度
	private double latitude;//纬度
	public Address preAddress;  //当前站点的前驱站点
	public int line;//当前站点之前乘坐的线路,便于判断是否换乘
	private ArrayList<Address> path = new ArrayList<>();
	//private ArrayList<Integer> line = new ArrayList<>();
	private ArrayList<Integer> No = new ArrayList<>();

	//以hashmap构建的最小堆
	private Map<Address, Integer> distance = new HashMap<>();
	private Map<Address,Integer> transfer = new HashMap<>();
	public Address(String address,String longitude,String latitude)
	{
		this.address=address;
		this.latitude=Double.parseDouble(latitude);
		this.longitude=Double.parseDouble(longitude);
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public Map<Address, Integer> getDistance() {
		return distance;
	}
	public Map<Address, Integer> getTransfer() {
		return transfer;
	}
	public ArrayList<Address> getPath(){return path;}
	//public ArrayList<Integer> getLine(){return line;}
	public ArrayList<Integer> getNo(){return No;}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		} else if(obj instanceof Address){
			Address s = (Address) obj;
			if(s.getAddress().equals(this.getAddress())){
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	@Override
	public int hashCode() {
		return this.getAddress().hashCode();
	}
}
