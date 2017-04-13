package global;

public class Global {
	//需求点数量
	public static int WAREHOUSE_NUM = 20;
	//车辆数量
	public static int CAR_NUM = 5;
	//种群内个体数量
	public static int INDIVIDUAL_NUM = 50;
	//迭代次数
	public static int REGRESS_NUM = 1000;
	//变异概率
	public static float VARIATION_RATE = 0.001f;
	//变异概率2
	public static float VARIATION_RATE2 = 0.8f;
	//交叉和变异最大次数
	public static float CROSS_MUTATE_NUM = 1500;
	//交叉概率
	public static float CROSS_RATE = 0.8f;
	//是否调试
	public static boolean ifTest = true;
	//适应度的放大倍数
	public static int FITNESS_ENLARGE = 1000;	
}
