package com.tools;

import java.util.Date;
/**
 * ��־��¼
 * @author Administrator
 *
 */
public class Log {
	public static void log(String msg){
		Date date=new Date();
		System.out.println("["+date+"]"+msg);
	}
}
