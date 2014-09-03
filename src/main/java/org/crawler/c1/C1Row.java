package org.crawler.c1;

import java.util.ArrayList;
import java.util.List;

public class C1Row {
	private String propinsi;
	private String kota;
	private String kecamatan;
	private String kelurahan;
	private int kelurahanId;
	private List<Integer> tpsIds = new ArrayList<Integer>();
	
	public String getKota() {
		return kota;
	}
	public void setKota(String kota) {
		this.kota = kota;
	}
	public String getKecamatan() {
		return kecamatan;
	}
	public void setKecamatan(String kecamatan) {
		this.kecamatan = kecamatan;
	}
	public String getKelurahan() {
		return kelurahan;
	}
	public void setKelurahan(String kelurahan) {
		this.kelurahan = kelurahan;
	}
	public int getKelurahanId() {
		return kelurahanId;
	}
	public void setKelurahanId(int kelurahanId) {
		this.kelurahanId = kelurahanId;
	}
	public String getPropinsi() {
		return propinsi;
	}
	public void setPropinsi(String propinsi) {
		this.propinsi = propinsi;
	}
	public List<Integer> getTpsIds() {
		return tpsIds;
	}
	public void setTpsIds(List<Integer> tpsIds) {
		this.tpsIds = tpsIds;
	}			
	public String toString()
	{
		return propinsi + "," +
				kota + "," +
				kecamatan + "," +
				kelurahan + "," +
				kelurahanId + "," +
				tpsIds;
	}
}
