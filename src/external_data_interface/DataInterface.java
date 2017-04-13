package external_data_interface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import entity.Schedule;
import entity.Supplier;
import entity.Warehouse;

public interface DataInterface {
	/**
	 * 获取距离
	 * @return
	 */
	public float[][] getDistance();
	/**
	 * 获取计量中心
	 * @param supplier 计量中心实例
	 * @return
	 */
	public Supplier getSupplier(Supplier supplier);
	/**
	 * 所有的需求点
	 * @param warehouses
	 * @return
	 */
	public ArrayList<Warehouse> getWarehouses(ArrayList<Warehouse> warehouses);
	/**
	 * 
	 * @param type fast:时间最短；lowest:成本最低；rate:装载率最高
	 * @param schedules
	 */
	public void saveSchedule(String type,List<Schedule> schedules);
	/**
	 * 
	 * @param type fast:时间最短；lowest:成本最低；rate:装载率最高
	 * @param schedules
	 */
	public void savePlannedSchedule(String type,List<Schedule> schedules);
	//下面是反馈环节需要的方法
//	/**
//	 * 获取四个影响因素的参数
//	 * 0->P
//	 * 1->S
//	 * 2->T
//	 * 3->D
//	 * @return
//	 */
//	public HashMap<Integer, Float> getAttribute();
//	/**
//	 * 将改动后的四个参数存入数据库
//	 * * 获取四个影响因素的参数
//	 * 0->P
//	 * 1->S
//	 * 2->T
//	 * 3->D
//	 */
//	public void saveAttribute(HashMap<Integer,Float> attribute);
//	/**
//	 * 
//	 * @param details
//	 */
//	public void saveWarehDetails(ArrayList<WarehFactorDetails> details);
//	/**
//	 * 
//	 * @return
//	 */
//	public ArrayList<WarehFactorDetails> getWarehDetails(WarehFactorDetails details);
//	/**
//	 * whichOperator 0是×，1是÷
//	 * changedValue 变动的数值，默认是0.95，可以改动
//	 * lastChangedAttribute 上次改动的参数，0是优先级P，1是周围需求点数量S，2是到计量中心的距离D，3是时间窗开启时间-时间窗结束时间T,
//	 * 如果没有改动则为-1
//	 * @param change
//	 */
//	public void saveChangeDetails(LastChangedInBest change);
//	/**
//	 * whichOperator 0是×，1是÷
//	 * changedValue 变动的数值，默认是0.95，可以改动
//	 * lastChangedAttribute 上次改动的参数，0是优先级P，1是周围需求点数量S，2是到计量中心的距离D，3是时间窗开启时间-时间窗结束时间T,
//	 * 如果没有改动则为-1
//	 * @return
//	 */
//	public LastChangedInBest getChangeDetails(LastChangedInBest change);
//	/**
//	 * 
//	 * @return 上次客户节点的满意度
//	 */
//	public float getLastSatisfied();
//	/**
//	 * 
//	 * @return K是需求点的id，V是满意或者不满意，0满意，1不满意
//	 */
//	public HashMap<Integer,Integer> getThisSatisfied();
//	/**
//	 * 
//	 * @param sa K是需求点的id，V是满意或者不满意，0满意，1不满意
//	 */
//	public void saveThisSatisfied(HashMap<Integer,Integer> sa);
}
