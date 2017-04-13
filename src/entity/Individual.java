package entity;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.xml.internal.bind.util.Which;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import utils.Algorithm;
import utils.CommonUtils;
import utils.FileWriterTools;
import global.Category;
import global.Global;


public class Individual {
	private int[][] gene;
	private float fitness;
	private float[][] distances;
	private ArrayList<Warehouse> warehouses;
	private Supplier supplier;
	private Carrier[] cars;

	private float[] carRestVolumn;
	private float[] warehousesRest;

	private int rectCount = Global.CAR_NUM * Global.WAREHOUSE_NUM;

	public HashMap<Integer, Integer> countToid;
	public HashMap<Integer, Integer> idTocount;

	private float[][] loadedCargo;
	private LastChanged lastChanged;
	public Individual(int id, Algorithm algorithm) {
		gene = new int[Global.CAR_NUM][Global.WAREHOUSE_NUM];
		loadedCargo = new float[Global.CAR_NUM][Global.WAREHOUSE_NUM];
		distances = algorithm.getDistances();
		supplier = algorithm.getSupplier();
		warehouses = algorithm.getWarehouses();
		cars = supplier.getCarriers();
		carRestVolumn = new float[cars.length];
		warehousesRest = new float[warehouses.size()];
		countToid = algorithm.getCountToid();
		idTocount = algorithm.getIdTocount();
		lastChanged = new LastChanged();
		setRest();
		generateGene();
		calculateFit();
	}
	public Individual(){
		lastChanged = new LastChanged();
	}
	// �漴���ɸ������,ÿһ��������ÿһ���㣬����һ��0-100�����������40��Ϊ0����������㣩,С��40��������㣬����Ϊ1
	// ���������㣬��ô�����ж�������������������ʣ�����Ļ���Ȼ���ж��Ƿ����������󣬳���ʱ�䴰��
	// �Ƿ������ص��������Ļᳬ������ʱ�����������������Ϊ1�����ҽ�ʣ������ջ��߽�������ʣ������գ�
	// ѭ��ֱ�����г�����������������������������ջ��߾���ѭ�����
	// ���������ĳ����ض��ǿ��н�
	/**
	 * ÿ����Ҫװ���٣� 10 20 Ҫװ�ĵ��ж��٣�0 1 0 1 0| 0 0 0 0 0 0 0
	 * 
	 * ÿ����Ҫװ���٣�5 5 Ҫװ�ĵ��ж��٣�1 0 1 0 0| 0 0 0 0 0 0 0
	 * 
	 * ��������Ļ������ܻ���������������������� �������ڳ��ٲ�һ���������������
	 */
	private void generateGene() {
		for (int i = 0; i < Global.CAR_NUM; i++) {
			for (int j = 0; j < Global.WAREHOUSE_NUM; j++) {
				gene[i][j] = 0;
			}
		}
		while ((!ifAllCarLoaded()) && (!ifAllWarehCleaned()) && (rectCount > 0)) {
			for (int carCount = 0; carCount < cars.length; carCount++) {
				for (int wareCount = 0; wareCount < warehouses.size(); wareCount++) {
					int random = (int) (Math.random() * 100);
					// ���С��(������/���������)*100
					if (random < ( 18/ Global.WAREHOUSE_NUM) * 100) {
						// ��������û�����
						if (warehousesRest[wareCount] > 0) {
							// ���������������
							if (carRestVolumn[carCount] > 0
									&& !ifOverTime(carCount)) {
								// �䶯���ߵ�ʣ����,�䶯loadedCargo
								if (carRestVolumn[carCount] > warehousesRest[wareCount]) {
									carRestVolumn[carCount] -= warehousesRest[wareCount];
									warehousesRest[wareCount] = 0;
									loadedCargo[carCount][wareCount] += warehousesRest[wareCount];
								} else {
									warehousesRest[wareCount] -= carRestVolumn[carCount];
									carRestVolumn[carCount] = 0;
									loadedCargo[carCount][wareCount] += carRestVolumn[carCount];
								}
								gene[carCount][wareCount] = 1;
							}
							
						}
					}
					rectCount--;
				}
			}
		}
		
	/*	for (int i = 0; i < Global.CAR_NUM; i++) {
			for (int j = 0; j < Global.WAREHOUSE_NUM; j++) {
				System.out.print(gene[i][j]);
			}
			System.out.println();
			
		}	*/
	}
	// ���������ȷ��û����
	// ����������Ӧ��
	// �ɱ���ͣ��͹���������/����·�����
	// ʱ����̣��͹���������/����ʱ�����
	// ���ó����١�100��Ϊ�����ݺÿ�
	// ��Ҫ���ǲ��ǿ��н⣨�����˳������أ�������ʱ�����ƣ�
	// �Ȱ��ɱ����д
	// ���������ô����true�������з���false������fitness����Ϊ-1
	public boolean calculateFit() {
		int lastCount = Global.WAREHOUSE_NUM;
		int toGoCount = 0;
		int checkedCount = 0;
		float allLength = 0f;
		float allCarLength = 0f;
		float allDemand = 0f;
		boolean ifFirst = true;
		float[] oneCarDemand = new float[Global.CAR_NUM];
		float[] oneCarTime = new float[Global.CAR_NUM];
		float lastLength = 0f;
		float lastDemand = 0f;
		ArrayList<Integer> warehouseList = new ArrayList<Integer>();
		//setRest();
		for (int i = 0; i < Global.CAR_NUM; i++) {
			warehouseList.clear();
			// �ж��ٸ�1
			for (int j = 0; j < Global.WAREHOUSE_NUM; j++) {
				if (gene[i][j] == 1) {
					toGoCount++;//��¼�ܹ����������˶��ٸ���
				}
			}
//			// û��1��ֱ������
			// ��ǰ������������������˶����� 
			for (int j = 0; j < Global.WAREHOUSE_NUM; j++) {
				if (gene[i][j] == 1) {	
						// �Ƚ��������ۼ�
						allDemand += loadedCargo[i][j];
						oneCarDemand[i] = oneCarDemand[i] + loadedCargo[i][j];
						checkedCount++;//����������
						warehouseList.add(j);					
				}
//				FileWriterTools.WriteToLog("Individual-changedLoadedCargo-individual" + this.id + "gene[" + i + "][" + j + "]:" + gene[i][j]
//						+ " loadedCargo:" + loadedCargo[i][j] + " warehouseRest:" + warehousesRest[j]
//								+ " carRest:" + carRestVolumn[i] + " carVolumn:" + cars[i].getVolumn());
			}
			//CommonUtils.MyLog("Individual", "calculateFit", "��",i,"�����ܹ���",oneCarDemand[i]);
			int[] warehouseCount = new int[warehouseList.size()];//�����
			for (int j = 0; j < warehouseList.size(); j++) {
				warehouseCount[j] = warehouseList.get(j);
			}
			allLength = getTravelLength(warehouseCount);//�����������ߵ���̾���
			
			if(allLength != 1){
				allCarLength += allLength;
			}else{
				allCarLength += 0;
			}
			
//			// ���������װ�Ķ���
			// �������������˶���
			if(allLength == 1){
				oneCarTime[i] = 0;
			}else{
				oneCarTime[i] = (allLength / cars[i].getSpeed());
			}
			
		/*	CommonUtils.MyLog("Individual", "calculateFit",
					"allLength and speed car", i, ":", allLength, " and ",
					cars[i].getSpeed());*/
			toGoCount = 0;
			checkedCount = 0;
			lastCount = Global.WAREHOUSE_NUM;
			ifFirst = true;
//			FileWriterTools.WriteToLog("oneCarLength:" + allLength);
			
		}
		//FileWriterTools.WriteToLog("allDemand:" + allDemand);
		for (int i = 0; i < Global.CAR_NUM; i++) {
			if (oneCarDemand[i] > cars[i].getVolumn()) {
				/*CommonUtils.MyLog(
						"Individual",
						"calculateFit",
						"over car volumn:" + oneCarDemand[i] + ">"
								+ cars[i].getVolumn());*/

				fitness = -1;
				return false;
			} else {
			/*	CommonUtils.MyLog(
						"Individual",
						"calculateFit",
						"one car volumn:" + oneCarDemand[i] + "<"
								+ cars[i].getVolumn(), "  workable");*/
			}
			if (oneCarTime[i] > Category.driverWorkTime) {
			/*	CommonUtils.MyLog("Individual", "calculate", "over car time:"
						+ oneCarTime[i] + ">" + Category.driverWorkTime);*/
				fitness = -1;
				return false;
			} else {
			/*	CommonUtils.MyLog("Individual", "calculate", "one car time:"
						+ oneCarTime[i] + "<" + Category.driverWorkTime,
						"  workable");*/
			}
		}
//		FileWriterTools.WriteToLog("allLength:" + allCarLength);
//		FileWriterTools.WriteToLog("allDemand:" + allDemand);
		// ����·���̵ĸ��������
		if (allCarLength < 1) {
			fitness = 0;
		}else{
			fitness = (float) ((allDemand  +  (1/Math.pow(allCarLength, 2)) * Global.FITNESS_ENLARGE));
		}
//		FileWriterTools.WriteToLog(fitness + "");
		// ������Ӧ�ȸߵĸ��������
		// fitness = (float) Math.pow(fitness, 2);
		return true;
	}


	public boolean ifCarRebundant() {
		boolean result = false;

		return result;
	}

	// �Ƿ񳬳�ʱ������
	private boolean ifOverTime(int whichCar) {
		boolean result = false;
		float distance = 0f;
		int lastCount = warehouses.size();
		for (int i = 0; i < Global.WAREHOUSE_NUM; i++) {
			if (gene[whichCar][i] == 1) {
				distance += distances[i][lastCount];
				lastCount = i;
			}
		}
		// ������ʲô�㶼����
		if (lastCount == warehouses.size()) {
			return false;
		}
		// �Ƿ񳬳���ʱ�䴰
		if (distance / cars[whichCar].getSpeed()
				+ warehouses.get(countToid.get(lastCount)).getOpenTime() > warehouses
				.get(countToid.get(lastCount)).getCloseTime()) {
			result = true;
			return result;
		}
		// �Ƿ񳬳�����ʱ��
		distance += distances[lastCount][warehouses.size()];
		if (distance / cars[whichCar].getSpeed() > Category.driverWorkTime) {
			result = true;
			return result;
		}
		return result;
	}

	// ���Rest
	private void setRest() {
		for (int i = 0; i < cars.length; i++) {
			carRestVolumn[i] = cars[i].getVolumn();
		}
		for (int i = 0; i < warehouses.size(); i++) {
			warehousesRest[i] = 0f;
			for (int j = 0; j < Category.productCategoryCount; j++) {
				warehousesRest[i] += warehouses.get(i).getDemands()
						.getAmount(j) * warehouses.get(i).getDemands().getOccupation(j);
				
				
			}

			//System.out.println(warehousesRest[i]);
		}
	}

	// �ǲ������еĳ���װ����
	private boolean ifAllCarLoaded() {
		boolean result = true;
		for (int i = 0; i < carRestVolumn.length; i++) {
			if (carRestVolumn[i] != 0) {
				result = false;
				break;
			}
		}
		return result;
	}

	// �Ƿ���������㶼������
	private boolean ifAllWarehCleaned() {
		boolean result = true;
		for (int i = 0; i < warehousesRest.length; i++) {
			if (warehousesRest[i] != 0) {
				result = false;
				break;
			}
		}
		return result;
	}
	//�����ȷ��û����
	private float getTravelLength(int[] warehousesCount) {
		if (warehousesCount.length == 0) {
			return 1;
		}
		//System.out.print("Individual-getTravelLength-warehousesCount:");
		//for (int i = 0; i < warehousesCount.length; i++) {
			//System.out.print(" " + warehousesCount[i]);
		//}
		//System.out.println();
		float length = 0f;
		int closestToSupplier = warehousesCount[0];
		int[] checkedCount = new int[warehousesCount.length];
		int closestPosition = 0;
		for (int i = 0; i < warehousesCount.length; i++) {
			if (distances[warehousesCount[i]][Global.WAREHOUSE_NUM] < distances[closestToSupplier][Global.WAREHOUSE_NUM]) {
				closestToSupplier = warehousesCount[i];
				closestPosition = i;
			}
		}
		checkedCount[closestPosition] = 1;
		int i = 0;
		int nowClosest = closestPosition;
		boolean ifFirst = true;
		int addCount = 0;
		while (true) {

			// i�Ǽ�¼��ǰ��warehouseCount���ĸ�һ����
			if (i == warehousesCount.length) {
				i = 0;
			}
			// ����ǵ�һ�ν���ѭ��
			if (ifFirst) {
				ifFirst = false;

				length += distances[closestToSupplier][Global.WAREHOUSE_NUM];
				/*CommonUtils.MyLog("Individual", "getTravelLength",
						"first length : ",
						distances[closestToSupplier][Global.WAREHOUSE_NUM],
						" waresouse:", closestToSupplier);*/
			} else {
				float tempLength = 99999f;
				// ������������ҳ��뵱ǰ������ĵ㣬��һ�ν���ѭ���󣬵�ǰ����ĵ�ΪclosestToSuppliere
				for (int j = 0; j < warehousesCount.length; j++) {
					// �߹��ĵ������
					if (checkedCount[j] == 1) {
						continue;
					}

					// �������еĵ㣬�ҳ�����ģ���ClosestSupplier��¼����ĵ�
					if (distances[warehousesCount[j]][closestToSupplier] < tempLength) {
						nowClosest = j;
						tempLength = distances[warehousesCount[j]][closestToSupplier];
					}

				}
				// ���³���
				// CommonUtils.MyLog("Individual",
				// "getTravelLength","nowClosest",nowClosest," closestToSupplier ",closestToSupplier);
				length += distances[warehousesCount[nowClosest]][closestToSupplier];
				/*CommonUtils
						.MyLog("Individual",
								"getTravelLength",
								"middle length : ",
								distances[warehousesCount[nowClosest]][closestToSupplier],
								" waresouse:", warehousesCount[nowClosest]);*/
				// �ҳ��������
				closestToSupplier = warehousesCount[nowClosest];
				// ����checkedcount
				checkedCount[nowClosest] = 1;
			}
			// �Ƿ����еĵ㶼�߹��ˣ�0��û�߹� 1���߹�
			for (int j = 0; j < warehousesCount.length; j++) {
				if (checkedCount[j] == 1) {
					addCount += 1;
				}
			}
		/*	CommonUtils
					.MyLog("Individual", "getTravelLength", "addCount",
							addCount, " warehousesCount.length",
							warehousesCount.length);*/
			// ������еĵ㶼���߹��ˣ���ô������ѭ��
			if (addCount == warehousesCount.length) {
				length += distances[closestToSupplier][Global.WAREHOUSE_NUM];
		/*		CommonUtils.MyLog("Individual", "getTravelLength",
						"end length : ",
						distances[Global.WAREHOUSE_NUM][closestToSupplier],
						" waresouse:", closestToSupplier);*/
				break;
			}
			addCount = 0;
		}
		return length;
	} 
	//���������ȷ��û����
	public void changedCarsLoadedCargo() {
		// �Ƚ���1��0�ĵ㣬�������ͷ�
		ArrayList<Integer> beforeChanged = lastChanged.getBeforeChanged();
		ArrayList<Integer> afterChanged = lastChanged.getAfterChanged();
		ArrayList<Integer> whichCar = lastChanged.getWhichCar();
		ArrayList<Integer> whichGene = lastChanged.getWhichWarehouse();
		ArrayList<Integer> wareHQueue = new ArrayList<Integer>();		
			for (int i = 0; i < beforeChanged.size(); i++) {
				/*CommonUtils.MyLog("Individual", "changedCarsLoadedCargo","before:",beforeChanged.get(i),
						"  after:",afterChanged.get(i));		*/
				if (!ifHas(wareHQueue, whichGene.get(i))) {
					wareHQueue.add(whichGene.get(i));
				}
				
				if (beforeChanged.get(i) == 1 && afterChanged.get(i) == 0) {
					//CommonUtils.MyLog("Individual", "changedCarsLoadedCargo","1->0");
					carRestVolumn[whichCar.get(i)] += loadedCargo[whichCar.get(i)][whichGene
							.get(i)];
					warehousesRest[whichGene.get(i)] += loadedCargo[whichCar.get(i)][whichGene
							.get(i)];
					loadedCargo[whichCar.get(i)][whichGene.get(i)] = 0f;
				}
			}
			//FileWriterTools.WriteBefore("wareHqueue:" + wareHQueue.size() + " " + wareHQueue.get(0));
			//FileWriterTools.WriteAfter("wareHqueue:" + wareHQueue.size() + " " + wareHQueue.get(0));
			int theCar = 0;
			int theWarehouse = 0;
			// ����������������Щ����
			
			for (int i = 0; i < wareHQueue.size(); i++) {
				//CommonUtils.MyLog("Individual", "changedCarsLoadedCargo",
						//"�����ǲֿ�",wareHQueue.get(i));
				theWarehouse = wareHQueue.get(i);
				// ���������ʣ���������Ѿ�Ϊ0����ô�Ͳ������ɳ��ˣ�ͬʱ�ѻ�����0
				//FileWriterTools.WriteBefore(wareHQueue.get(i) + " wareH loop");
				//FileWriterTools.WriteAfter(wareHQueue.get(i) + " wareH loop");
				// �������������������г�
				for (int j = 0; j < whichCar.size(); j++) {
					//FileWriterTools.WriteBefore("whichCar loop" + j);
					//FileWriterTools.WriteAfter("whichCar loop" + j);
					if (whichGene.get(j) == theWarehouse) {
						if (warehousesRest[theWarehouse] == 0) {
							//FileWriterTools.WriteBefore("whichCar loop warehousesrest = 0" + j);
							//FileWriterTools.WriteAfter("whichCar loop warehousesrest = 0" + j);
						/*	CommonUtils.MyLog("Individual",
									"changedCarsLoadedCargo", whichCar.get(j),
									"����", whichGene.get(j), "�������Ѿ�ʣ��������Ϊ0������������");*/
							theCar = whichCar.get(j);
							gene[theCar][theWarehouse] = 0;
							continue;
						}
						if (beforeChanged.get(j) == 1 && afterChanged.get(j) == 0) {
							//FileWriterTools.WriteBefore("whichCar loop before = 1 after = 0" + j);
							//FileWriterTools.WriteAfter("whichCar loop before = 1 after = 0" + j);
							continue;
						} else if (beforeChanged.get(j) == afterChanged.get(j)) {
							//FileWriterTools.WriteBefore("whichCar loop before = after" + j);
							//FileWriterTools.WriteAfter("whichCar loop before = after" + j);
							continue;
						} else if(beforeChanged.get(j) == 0 && afterChanged.get(j) == 1) {
							/*CommonUtils.MyLog("Individual",
									"changedCarsLoadedCargo", whichCar.get(j),
									"����", whichGene.get(j), "�ֿ���");*/
							// ����������������ж�Ϊ��ǰ���������
							
							
				//�����ж��Ѿ����		//if (whichGene.get(j) == theWarehouse) {
								/*CommonUtils.MyLog("Individual",
										"changedCarsLoadedCargo", whichCar.get(j),
										"����", whichGene.get(j), "�ֿ���");
								theCar = whichCar.get(j);*/
								// �����ѭ�����Ѿ���������������������ˣ���ô������0���Թ�����Ĳ��裬������һ��
								if (warehousesRest[theWarehouse] == 0) { 
									/*CommonUtils.MyLog("Individual",
											"changedCarsLoadedCargo",
											whichGene.get(j), "�ֿ��Ѿ���������������");*/
									gene[theCar][theWarehouse] = 0;
									//FileWriterTools.WriteBefore("whichCar loop warehousesrest = 0" + j);
									//FileWriterTools.WriteAfter("whichCar loop warehousesrest = 0" + j);
									continue;
								}
								// �������Ѿ����ˣ�Ҳ��0
								if (carRestVolumn[theCar] == 0) {
									/* CommonUtils.MyLog("Individual",
								"changedCarsLoadedCargo",
											whichCar.get(j), "���Ѿ�����");*/
									gene[theCar][theWarehouse] = 0;									
									//FileWriterTools.WriteBefore("whichCar loop car full" + j);
									//FileWriterTools.WriteAfter("whichCar loop car full" + j);
									continue;
								}
								//if (loadedCargo[theCar][theWarehouse] ==0)
							/*	CommonUtils.MyLog("Individual",
										"changedCarsLoadedCargo", whichCar.get(j),
									   "��ʣ������", carRestVolumn[theCar], "�ֿ�",
										theWarehouse, "ʣ��������",
										warehousesRest[theWarehouse]);*/
								//FileWriterTools.WriteBefore("whichCar loop loading" + j);
								//FileWriterTools.WriteAfter("whichCar loop loading" + j);
								// ��������������㲻�գ���ʼװ
								if (carRestVolumn[theCar] > warehousesRest[theWarehouse]) {
									loadedCargo[theCar][theWarehouse] = warehousesRest[theWarehouse];
									carRestVolumn[theCar] = carRestVolumn[theCar] - warehousesRest[theWarehouse];
									warehousesRest[theWarehouse] = 0;
				
									
								} else {
									loadedCargo[theCar][theWarehouse] = carRestVolumn[theCar];
									warehousesRest[theWarehouse] = warehousesRest[theWarehouse] - carRestVolumn[theCar];									
									carRestVolumn[theCar] = 0;
								}
							}
						//}
					}

				}

			}
//			if(lastChanged.getChangedType() == 0){
//				FileWriterTools.WriteAfter("after");
//				for(int i = 0;i<whichGene.size();i++){
//					FileWriterTools.WriteAfter("gene:" + whichGene.get(i) + " before:" + beforeChanged.get(i)
//							 + " after:" + afterChanged.get(i));
//				}
//				for(int i = 0;i<Global.CAR_NUM;i++){
//					for(int j = 0;j<Global.WAREHOUSE_NUM;j++){
//						FileWriterTools.WriteAfter("wareRest:" + warehousesRest[j] + 
//								" carRest:" + carRestVolumn[i] + 
//								" loadedCargo:" + loadedCargo[i][j]);
//					}
//				}
//			}
	}
	
	//list���Ƿ���one
	private boolean ifHas(ArrayList<Integer> list,int one){
		for(int i = 0;i<list.size();i++){
			if(list.get(i) == one){
				return true;
			}
		}
		return false;
	}
	protected class LastChanged{
		//�����ֱ任��0�ǽ��棬1�Ǳ���
		protected int changedType;
		//�任�Ļ���λ��������
		protected ArrayList<Integer> whichCar = new ArrayList<Integer>();
		//�任�Ļ������ĸ������
		protected ArrayList<Integer> whichWarehouse = new ArrayList<Integer>();
		//�仯ǰ�Ļ���
		protected ArrayList<Integer> beforeChanged = new ArrayList<Integer>();
		//�仯��Ļ���
		protected ArrayList<Integer> afterChanged = new ArrayList<Integer>();
		public void clear(){
			whichCar.clear();
			whichWarehouse.clear();
			beforeChanged.clear();
			afterChanged.clear();
		}
		public int getChangedType() {
			return changedType;
		}
		public void setChangedType(int changedType) {
			this.changedType = changedType;
		}
		public ArrayList<Integer> getWhichCar() {
			return whichCar;
		}
		public void addWhichCar(int whichCar) {
			this.whichCar.add(whichCar);
			//System.out.println(this.whichCar.size());
		}
		public ArrayList<Integer> getWhichWarehouse() {
			return whichWarehouse;
		}
		public void addWhichWarehouse(int whichWarehouse) {
			this.whichWarehouse.add(whichWarehouse);
			//System.out.println(this.whichWarehouse.size());
		}
		public ArrayList<Integer> getAfterChanged() {
			return afterChanged;
		}
		public void addAfterChanged(int afterChanged) {
			this.afterChanged.add(afterChanged);
			//System.out.println(this.afterChanged.size());
		}
		public ArrayList<Integer> getBeforeChanged() {
			return beforeChanged;
		}
		public void addBeforeChanged(int beforeChanged) {
			this.beforeChanged.add(beforeChanged);
			//System.out.println(this.beforeChanged.size());
		}
		public void setWhichCar(ArrayList<Integer> whichCar) {
			this.whichCar = whichCar;
		}
		public void setWhichWarehouse(ArrayList<Integer> whichWarehouse) {
			this.whichWarehouse = whichWarehouse;
		}
		public void setBeforeChanged(ArrayList<Integer> beforeChanged) {
			this.beforeChanged = beforeChanged;
		}
		public void setAfterChanged(ArrayList<Integer> afterChanged) {
			this.afterChanged = afterChanged;
		}
		
	}
	


	public int[][] getGene() {
		return gene;
	}

	public void setGene(int[][] gene) {
		this.gene = gene;
	}

	public float getFitness() {
		return fitness;
	}

	public void setFitness(float fitness) {
		this.fitness = fitness;
	}

	public void setOneGene(int i, int j, int value) {
		gene[i][j] = value;
	}
	public void setChangedType(int type){
		lastChanged.setChangedType(type);
	}
	public void addWhichCarChanged(int cars){
		lastChanged.addWhichCar(cars);
	}
	public void addWhichWarehChanged(int warehouses){
		lastChanged.addWhichWarehouse(warehouses);
	}
	public void addAfterChanged(int after){
		lastChanged.addAfterChanged(after);
	}
	public void addBeforeChanged(int before){
		lastChanged.addBeforeChanged(before);
	}
	public void lastChangedClear(){
		lastChanged.clear();
	}
	public float[][] getDistances() {
		return distances;
	}
	public void setDistances(float[][] distances) {
		this.distances = distances;
	}
	public ArrayList<Warehouse> getWarehouses() {
		return warehouses;
	}
	public void setWarehouses(ArrayList<Warehouse> warehouses) {
		this.warehouses = warehouses;
	}
	public Supplier getSupplier() {
		return supplier;
	}
	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}
	public Carrier[] getCars() {
		return cars;
	}
	public void setCars(Carrier[] cars) {
		this.cars = cars;
	}
	public float[] getCarRestVolumn() {
		return carRestVolumn;
	}
	public void setCarRestVolumn(float[] carRestVolumn) {
		this.carRestVolumn = carRestVolumn;
	}
	public float[] getWarehousesRest() {
		return warehousesRest;
	}
	public void setWarehousesRest(float[] warehousesRest) {
		this.warehousesRest = warehousesRest;
	}
	public int getRectCount() {
		return rectCount;
	}
	public void setRectCount(int rectCount) {
		this.rectCount = rectCount;
	}
	public HashMap<Integer, Integer> getCountToid() {
		return countToid;
	}
	public void setCountToid(HashMap<Integer, Integer> countToid) {
		this.countToid = countToid;
	}
	public HashMap<Integer, Integer> getIdTocount() {
		return idTocount;
	}
	public void setIdTocount(HashMap<Integer, Integer> idTocount) {
		this.idTocount = idTocount;
	}
	public float[][] getLoadedCargo() {
		return loadedCargo;
	}
	public void setLoadedCargo(float[][] loadedCargo) {
		this.loadedCargo = loadedCargo;
	}
	public void setWhichType(int type){
		lastChanged.setChangedType(type);
	}
	public int getWhichType(){
		return lastChanged.getChangedType();
	}
	public void setWhichCar(ArrayList<Integer> whichCar){
		lastChanged.setWhichCar(whichCar);
	}
	public ArrayList<Integer> getWhichCar(){
		return lastChanged.getWhichCar();
	}
	public void setWhichWare(ArrayList<Integer> whichWarehouses){
		lastChanged.setWhichWarehouse(whichWarehouses);
	}
	public ArrayList<Integer> getWhichWare(){
		return lastChanged.getWhichWarehouse();
	}
	public void setBeforeChanged(ArrayList<Integer> before){
		lastChanged.setBeforeChanged(before);
	}
	public ArrayList<Integer> getBefore(){
		return lastChanged.getBeforeChanged();
	}
	public void setAfterChanged(ArrayList<Integer> after){
		lastChanged.setAfterChanged(after);
	}
	public ArrayList<Integer> getAfter(){
		return lastChanged.getAfterChanged();
	}
}
