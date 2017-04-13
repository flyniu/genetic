package utils;

import global.Global;

public class CommonUtils {
	public static void MyLog(String className,String methodName,Object ...objects){
		if(Global.ifTest){
			System.out.print(className + "-" + methodName + "-");
			for(Object o : objects){
				System.out.print(o.toString());
			}
			System.out.println();
		}
	}
}
