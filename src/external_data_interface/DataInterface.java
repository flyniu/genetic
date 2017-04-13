package external_data_interface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import entity.Schedule;
import entity.Supplier;
import entity.Warehouse;

public interface DataInterface {
	/**
	 * ��ȡ����
	 * @return
	 */
	public float[][] getDistance();
	/**
	 * ��ȡ��������
	 * @param supplier ��������ʵ��
	 * @return
	 */
	public Supplier getSupplier(Supplier supplier);
	/**
	 * ���е������
	 * @param warehouses
	 * @return
	 */
	public ArrayList<Warehouse> getWarehouses(ArrayList<Warehouse> warehouses);
	/**
	 * 
	 * @param type fast:ʱ����̣�lowest:�ɱ���ͣ�rate:װ�������
	 * @param schedules
	 */
	public void saveSchedule(String type,List<Schedule> schedules);
	/**
	 * 
	 * @param type fast:ʱ����̣�lowest:�ɱ���ͣ�rate:װ�������
	 * @param schedules
	 */
	public void savePlannedSchedule(String type,List<Schedule> schedules);
	//�����Ƿ���������Ҫ�ķ���
//	/**
//	 * ��ȡ�ĸ�Ӱ�����صĲ���
//	 * 0->P
//	 * 1->S
//	 * 2->T
//	 * 3->D
//	 * @return
//	 */
//	public HashMap<Integer, Float> getAttribute();
//	/**
//	 * ���Ķ�����ĸ������������ݿ�
//	 * * ��ȡ�ĸ�Ӱ�����صĲ���
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
//	 * whichOperator 0�ǡ���1�ǡ�
//	 * changedValue �䶯����ֵ��Ĭ����0.95�����ԸĶ�
//	 * lastChangedAttribute �ϴθĶ��Ĳ�����0�����ȼ�P��1����Χ���������S��2�ǵ��������ĵľ���D��3��ʱ�䴰����ʱ��-ʱ�䴰����ʱ��T,
//	 * ���û�иĶ���Ϊ-1
//	 * @param change
//	 */
//	public void saveChangeDetails(LastChangedInBest change);
//	/**
//	 * whichOperator 0�ǡ���1�ǡ�
//	 * changedValue �䶯����ֵ��Ĭ����0.95�����ԸĶ�
//	 * lastChangedAttribute �ϴθĶ��Ĳ�����0�����ȼ�P��1����Χ���������S��2�ǵ��������ĵľ���D��3��ʱ�䴰����ʱ��-ʱ�䴰����ʱ��T,
//	 * ���û�иĶ���Ϊ-1
//	 * @return
//	 */
//	public LastChangedInBest getChangeDetails(LastChangedInBest change);
//	/**
//	 * 
//	 * @return �ϴοͻ��ڵ�������
//	 */
//	public float getLastSatisfied();
//	/**
//	 * 
//	 * @return K��������id��V��������߲����⣬0���⣬1������
//	 */
//	public HashMap<Integer,Integer> getThisSatisfied();
//	/**
//	 * 
//	 * @param sa K��������id��V��������߲����⣬0���⣬1������
//	 */
//	public void saveThisSatisfied(HashMap<Integer,Integer> sa);
}
