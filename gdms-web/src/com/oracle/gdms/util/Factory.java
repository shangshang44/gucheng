package com.oracle.gdms.util;

import java.util.ResourceBundle;

public class Factory {
	
	private Factory() { }
	
	private static Factory fac = null;
	
	public static Factory getInstance() {
		fac = fac == null ? new Factory() : fac;
		return fac;
	}
	
	
	private static ResourceBundle rb = null;
	static {
		rb = ResourceBundle.getBundle("config/application");
	}
	
	public Object getObject(String key) {
		String clsname = rb.getString(key);  
		try {
			return Class.forName(clsname).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
