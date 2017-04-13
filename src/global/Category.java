package global;

public class Category {
	public static int productCategoryCount = 3;
	public static int driverWorkTime = 6;
	public static int driverADayTimes = 2;
	public static int warehouseOpen = 9;
	public static int warehouseClose = 17;
	public static float givenValueToChangeInBest = 0.95f;
	//三类表的体积
//	public static float productCategory1 = 0.001272f;
//	public static float productCategory2 = 0.004191f;
//	public static float productCategory3 = 0.000585f;
	public static float productCategory1 = 0.005f;
	public static float productCategory2 = 0.005f;
	public static float productCategory3 = 0.005f;
//	public static float kitOcupation1 = 0.03888f;
//	public static float kitOcupation2 = 0.0648f;
//	public static float kitOcupation3 = 0.039212f;
	public static float kitOcupation1 = 0.05f;
	public static float kitOcupation2 = 0.05f;
	public static float kitOcupation3 = 0.05f;
	public static int kitVolumn1 = 10;
	public static int kitVolumn2 = 10;
	public static int kitVolumn3 = 10;
	public static float efficiency = 500f;//装卸货效率,假定一小时100体积，装卸货都一样
	public static String supplierDataSource = "F:\\ele_net_data\\carriers.xls";
	public static String warehouseDataSource = "F:\\ele_net_data\\warehouse.xls";
	public static String distancesDataSource = "F:\\ele_net_data\\distances.xls";
}
