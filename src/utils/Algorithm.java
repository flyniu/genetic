package utils;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.org.apache.bcel.internal.generic.IMUL;
import com.sun.xml.internal.bind.util.Which;

import entity.Cluster;
import entity.Individual;
import entity.Supplier;
import entity.Warehouse;
import global.Category;
import global.Global;

public class Algorithm {
	private Cluster cluster;
	public static int id_c = 0;
	public static int id_i = 0;
	private HashMap<Integer, Integer> countToid = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> idTocount = new HashMap<Integer, Integer>();
	private float[][] Distances;
	private ArrayList<Warehouse> warehouses;
	private Supplier supplier;
	private int[] crossedPosition = new int[2];
	private ArrayList<Integer> mutatedPosition = new ArrayList<Integer>();
	Individual bestOne = new Individual();
	boolean NanBreak = false;
	public Algorithm(float[][] distances,ArrayList<Warehouse> warehouses,
			Supplier supplier,HashMap<Integer, Integer> countToid,
			HashMap<Integer, Integer> idTocount){
		this.Distances = distances;
		this.warehouses = warehouses;
		this.supplier = supplier;
		this.countToid = countToid;
		this.idTocount = idTocount;
		bestOne = null;
		//cluster = new Cluster(id_c,this);
	}
	public Individual getBestChild(){
		//��ʼ��Ⱥ���Ҽ�����Ӧ��
		cluster = new Cluster(id_c,this);
		int regressCount = 0;
		int crossCount = 0;
		int mutateCount = 0;
		Individual[] crossedIndividuals = new Individual[2];
		Individual[] mutatedIndividuals;
		while((regressCount <= Global.REGRESS_NUM) && !ifAllBest()){
			CommonUtils.MyLog("Algorithm", "getBestChild","��",regressCount,"�ε���");
			//updateCluster();
			String line = "";
			
			while(crossCount <= Global.CROSS_MUTATE_NUM ){
				//CommonUtils.MyLog("Algorithm", "getBestChild","��---------------",crossCount,"-------------------�ν���");
				crossedIndividuals = Cross();
				if(crossedPosition[0] != crossedPosition[1]){
					crossedIndividuals[0].changedCarsLoadedCargo();
					crossedIndividuals[1].changedCarsLoadedCargo();
				}
				if(!ifCrossedHasUnworkable(crossedIndividuals)){
					//����жϺ������Ѿ�������������յ�������1����Ϊ0�������Ѿ���û����ĵ��1����Ϊ0,
					//����ֻҪ��1����������Ҫ��һ���������ĵ�
					//ͬʱ������ж����Ѿ��ж��������ɵ��Դ��ǲ���		
					cluster.setIndividual(crossedPosition[0], copyIndividual(crossedIndividuals[0]));
					cluster.setIndividual(crossedPosition[1], copyIndividual(crossedIndividuals[1]));
					break;
				}
				crossCount++;
			}		
			crossCount = 0;
			
			
			while(mutateCount <= Global.CROSS_MUTATE_NUM){
				CommonUtils.MyLog("Algorithm", "getBestChild", mutateCount,"---------------------�α���-------------------");
				mutatedIndividuals = Mutate();
				//����ʱ�Ѿ������еı�����Ϣ���£�Ȼ�������ｫÿ�����Ѿ�װ�˶��ٸ��£�Ȼ����������ж��������¼���һ����Ӧ��
				for(int i = 0;i<mutatedIndividuals.length;i++){
					mutatedIndividuals[i].changedCarsLoadedCargo();
					//CommonUtils.MyLog("Algorithm", "getBestChild","mutatedIndividuals length ",mutatedIndividuals.length);
				}
				mutatedIndividuals = ifMutatedHasUnworkable(mutatedIndividuals);
				if(mutatedIndividuals != null){					
					//����жϺ������Ѿ�������������յ�������1����Ϊ0�������Ѿ���û����ĵ��1����Ϊ0,
					//����ֻҪ��1����������Ҫ��һ���������ĵ�					
					for(int i = 0;i<mutatedIndividuals.length;i++){
						cluster.setIndividual(mutatedPosition.get(i), copyIndividual(mutatedIndividuals[i]));
					}
					
								
					break;
				}
				mutateCount++;
			}
			mutateCount = 0;

			updateCluster();	
			//Individual[] individuals = cluster.getIndividuals();
		/*	for (int i = 0; i < individuals.length; i++) {
				int [][] gene_displat = new int[5][20];
				gene_displat = individuals[i].getGene();
				for(int j=0;j<5;j++){
					for(int k=0;k<20;k++){
						System.out.print(gene_displat[j][k]+" ");								
					}
					System.out.println();
				}
				System.out.println();
			}	*/
			
			regressCount++;
			getBestOne(regressCount);

			if(Global.ifTest){
				System.out.println("___________________________________�ָ���"+ regressCount +"_____________________________");
			}
		
		}
		return bestOne;
	}
	private Individual getBestOne(int regressCount){
		int whichIndividual = 0;
		Individual[] individuals = cluster.getIndividuals();
		for(int i = 0;i<Global.INDIVIDUAL_NUM;i++){
			//CommonUtils.MyLog("Algorithm", "getBestOne", "individuals",i,":",individuals[i].getFitness());
			individuals[i].calculateFit();
			if(individuals[whichIndividual].getFitness() < individuals[i].getFitness()){
				whichIndividual = i;				
			}		
		}
		
		if(regressCount == 1){
			bestOne = copyIndividual(individuals[whichIndividual]);
		}else{
			if(bestOne.getFitness() < individuals[whichIndividual].getFitness()){
				bestOne = copyIndividual(individuals[whichIndividual]);
			}
		}
		bestOne.calculateFit();
		CommonUtils.MyLog("Algorithm", "getBestOne", "bestOne",bestOne.getFitness());
		CommonUtils.MyLog("Algorithm", "getBestOne", "individuals[whichIndividual]:",individuals[whichIndividual].getFitness());
		return bestOne;		
	}
	//������Ӧ�Ⱥ󣬸�����Ⱥ
	//Ҫ����fitness=-1�����
	//ѡȡ������������ĸ�Ⱦɫ��������ڣ���ѡ�����������ǿ��н⣬�����н���continue��
	//ֱ��ѡ��Global.INDIVIDUAL_NUM��
	private void updateCluster(){
		System.out.println("update-----------------------------update");
		Individual[] individuals = cluster.getIndividuals();
		
	/*	for (int i = 0; i < individuals.length; i++) {
			int [][] gene_displat = new int[5][20];
			gene_displat = individuals[i].getGene();
			for(int j=0;j<5;j++){
				for(int k=0;k<20;k++){
					System.out.print(gene_displat[j][k]+" ");								
				}
				System.out.println();
			}
			System.out.println();
		}	*/
		float[] fitnesses = new float[individuals.length];
		float allFitnesses = 0f;
		float random = 0f;
		float fitnessCount = 0f;
		
		for(int l = 0;l<Global.INDIVIDUAL_NUM;l++){
			for(int i = 0;i<Global.CAR_NUM;i++){
				for(int j = 0;j<Global.WAREHOUSE_NUM;j++){					
					if(cluster.getIndividuals()[l].getGene()[i][j] == 1){
						if(cluster.getIndividuals()[l].getLoadedCargo()[i][j] == 0){
							cluster.getIndividuals()[l].getGene()[i][j] = 0;
						}
					}
					if(cluster.getIndividuals()[l].getGene()[i][j] == 0){
						if(cluster.getIndividuals()[l].getLoadedCargo()[i][j] != 0){
							cluster.getIndividuals()[l].getGene()[i][j] = 1;
						}
					}
					if(cluster.getIndividuals()[l].getLoadedCargo()[i][j] <= 0){
						cluster.getIndividuals()[l].getCarRestVolumn()[i] += cluster.getIndividuals()[l].getLoadedCargo()[i][j];
						cluster.getIndividuals()[l].getLoadedCargo()[i][j] = 0;
					}
				}
			}
		}
		for(int i = 0;i<individuals.length;i++){
			individuals[i].calculateFit();
			fitnesses[i] = individuals[i].getFitness();
			//CommonUtils.MyLog("Algorithm", "updateCluster", "��",i,"���������Ӧ��Ϊ��",individuals[i].getFitness(), "  ���Ϊ��",i);
			if(fitnesses[i] == -1){
				continue;
			}
			allFitnesses += individuals[i].getFitness();
		}
		//CommonUtils.MyLog("Algorithm", "updateCluster","������Ӧ��Ϊ��" + allFitnesses);
		//FileWriterTools.WriteToLog(allFitnesses + "");
		for(int i = 0;i<individuals.length;i++){
			//System.out.println("changdudududududududud" + individuals.length);
			random = (float) (Math.random()*allFitnesses);
			for(int j = 0;j<individuals.length;j++){
				//���fitness=-1����ô���ǲ����н⣬��Ҫcontinue
				if(fitnesses[i] == -1){
					continue;
				}
				if(fitnesses[i] == 0){
					continue;
				}
				//���µ�ǰ��fitness����
				fitnessCount += fitnesses[j];
				//������ڵ���ǰһ��������С�ں�һ�����������˺�һ���������ڣ���Ϊ�����Ǵ�0��ʼ��[0,��һ����
				if((fitnessCount - fitnesses[j]) <= random && random < fitnessCount){
					cluster.setIndividual(i, copyIndividual(individuals[j]));
					fitnessCount = 0f;
				}
			}
		}
	}
	//�漴ѡ���������
	//����������ҳ����ŵ�һ��
	//�����ѡ���������
	//�ҳ����ŵ�һ��
	//�����������棬���ѡ��Ҫ�����λ��
	//���棬����
	//�����������ĸ������ú�LastChanged�ڲ���
	private Individual[] Cross(){
		//int count = 0;
		float random = 0;
		ArrayList<Individual> individuals1 = new ArrayList<Individual>();
		ArrayList<Individual> individuals2 = new ArrayList<Individual>();
		int position1 = 0;
		int position2 = 0;
		Individual individual1 = null;
		Individual individual2 = null;
		ArrayList<Integer> joinedPosition1 = new ArrayList<Integer>();
		ArrayList<Integer> joinedPosition2 = new ArrayList<Integer>();
		int randomPosition = 0;
		Individual[] individuals = new Individual[2];
		
		crossedPosition[0] = 0;
		crossedPosition[1] = 0;
		//ѡ��������̭��
		for(int j = 0;j<2;j++){
			for(int i = 0;i<Global.INDIVIDUAL_NUM;i++){
				random = (float) Math.random();
				if(random < Global.CROSS_RATE){
					if(j == 0){
						individuals1.add(copyIndividual(cluster.getIndividual(i)));
						joinedPosition1.add(i);
						//CommonUtils.MyLog("Algorithm", "Cross", "���Ϊ"+i+"�ĸ�����뵽��̭��1");				
					}else{
						individuals2.add(copyIndividual(cluster.getIndividual(i)));
						joinedPosition2.add(i);
						//CommonUtils.MyLog("Algorithm", "Cross", "���Ϊ"+i+"�ĸ�����뵽��̭��2");
					}
				}
			}
		}
		//���һ����ûѡ����
		if(individuals1.size() == 0){
			randomPosition = (int) (Math.random()*(Global.INDIVIDUAL_NUM));
			joinedPosition1.add(randomPosition);
			individuals1.add(copyIndividual(cluster.getIndividual(randomPosition)));
			CommonUtils.MyLog("Algorithm", "Cross","��̭��һ���˶�û�У����ѡ��������뵽��̭��1");
		}
		if(individuals2.size() == 0){
			randomPosition = (int) (Math.random()*(Global.INDIVIDUAL_NUM));
			joinedPosition2.add(randomPosition);
			individuals2.add(copyIndividual(cluster.getIndividual(randomPosition)));
			CommonUtils.MyLog("Algorithm", "Cross","��̭��һ���˶�û�У����ѡ��������뵽��̭��2");	
		}
		CommonUtils.MyLog("Algorithm", "Cross","individuals1_size:" + individuals1.size() + "  joinedPosition1_size:" + joinedPosition1.size());
		//���������зֱ�ѡ��һ����Ӧ����ߵ�
		for(int i = 0;i<2;i++){
			if(i == 0){
				for(int j = 0;j<individuals1.size();j++){
					if(j == 0){
						//CommonUtils.MyLog("Algorithm", "Cross","��̭��" + 1 +"��ǰ��ߵ�Ϊ" + position1);
						position1 = j;
						individual1 = copyIndividual(individuals1.get(j));
						crossedPosition[0] = joinedPosition1.get(j);
					}else{
						if(individual1.getFitness() < individuals1.get(j).getFitness()){
							//CommonUtils.MyLog("Algorithm", "Cross","��̭��" + 1 +"��ǰ��ߵ�Ϊ" + position1 + "fitness:" + individuals1.get(j).getFitness());
//							if(Global.ifTest){
//								System.out.println();
//							}						
							position1 = j;
							individual1 = copyIndividual(individuals1.get(j));
							crossedPosition[0] = joinedPosition1.get(j);
						}
					}
				}
			}else{
				for(int j = 0;j<individuals2.size();j++){
					if(j == 0){
						//CommonUtils.MyLog("Algorithm", "Cross","��̭��" + 2 +"��ǰ��ߵ�Ϊ" + position2);
						position2 = j;
						individual2 = copyIndividual(individuals2.get(j));
						crossedPosition[1] = joinedPosition2.get(j);
					}else{
						if(individual2.getFitness() < individuals2.get(j).getFitness()){
							//CommonUtils.MyLog("Algorithm", "Cross","��̭��" + 2 +"��ǰ��ߵ�Ϊ" + position2 + " fitness:" + individuals2.get(j).getFitness());
							position2 = j;
							individual2 = copyIndividual(individuals2.get(j));
							crossedPosition[1] = joinedPosition2.get(j);
						}
					}
				}
			}
		}
		
		System.out.println(crossedPosition[0]+ "---" + crossedPosition[1]);
		if(crossedPosition[0] == crossedPosition[1]){
			individual1.lastChangedClear();
			individual2.lastChangedClear();
			individuals[0] = individual1;
			individuals[1] = individual2;
			return individuals;
		}
		//individual1 �� individual2��������̭������õ����������н������
		//�漴ѡ��һ�������
		int crossPosition = (int) (Math.random()*(Global.CAR_NUM * Global.WAREHOUSE_NUM));
		int previousCar = crossPosition/Global.WAREHOUSE_NUM;
		int whichPosition = crossPosition%Global.WAREHOUSE_NUM;
		CommonUtils.MyLog("Algorithm", "Cross","�����Ϊ����" + previousCar + "�ĵ�" + whichPosition + "����");
		
		individual1.lastChangedClear();
		individual2.lastChangedClear();
		int temp1,temp2;
		for(int i = 0;i<=previousCar;i++){
			for(int j = 0;j<Global.WAREHOUSE_NUM;j++){
				//�����˽���㣬���˳�
				if(j > whichPosition ){
					if(i >= previousCar){
						break;
					}
				}
				//System.out.println("individual1 " + individual1.hashCode() + " [i][j]:" + i + "][" + j + " " + individual1.getGene()[i][j]);
				//System.out.println("individual2 " + individual2.hashCode() + " [i][j]:" + i + "][" + j + " " + individual2.getGene()[i][j]);
				//�����ģ�����
				//���ȼ�¼�±䶯��λ�ã��䶯ǰ��Ļ���
				temp1 = individual1.getGene()[i][j];
				temp2 = individual2.getGene()[i][j];
				individual1.addBeforeChanged(temp1);
				individual1.addAfterChanged(temp2);
				
				individual1.addWhichCarChanged(i);
				individual1.addWhichWarehChanged(j);
				
				individual1.setChangedType(0);
				individual2.addAfterChanged(temp1);
				individual2.addBeforeChanged(temp2);
				individual2.addWhichCarChanged(i);
				individual2.addWhichWarehChanged(j);
				individual2.setChangedType(0);	
				individual1.setOneGene(i, j, temp2);
				individual2.setOneGene(i, j, temp1);
				//System.out.println("individual1 " + individual1.hashCode() + " [i][j]:" + i + "][" + j + " " + individual1.getGene()[i][j]);
				//System.out.println("individual2 " + individual2.hashCode() + " [i][j]:" + i + "][" + j + " " + individual2.getGene()[i][j]);
			}
		}		
		individuals[0] = individual1;
		individuals[1] = individual2;
		return individuals;
	}
	//һ����5*20*50 = 5000������
	//������5000*0.005 = 25���������
	//����Щ�����ų�һ�У���ÿһ������ȡ������������0.005����ô�ͽ�������
	//������ϣ�����
	//�����������ĸ������ú�LastChanged�ڲ���
	private Individual[] Mutate(){
		Individual[] individuals;
		ArrayList<Integer> positions = new ArrayList<Integer>();
		float random = 0f;
		//��Ȼ��ǰһ�����壬������Ϊ�Ǵ��㿪ʼ���������Կ���ֱ��get(previousIndividual)
		ArrayList<Integer> previousIndividual = new ArrayList<Integer>();
		ArrayList<Integer> previousCar = new ArrayList<Integer>();
		ArrayList<Integer> whichGene = new ArrayList<Integer>();
		int position = 0;
		mutatedPosition.clear();
		/*if(Global.ifTest){
			System.out.print("�����Ϊ��");
		}*/
		
		for(int i = 0;i<Global.CAR_NUM*Global.WAREHOUSE_NUM*Global.INDIVIDUAL_NUM;i++){			
			
			random = (float) Math.random();
			if(random <Global.VARIATION_RATE){
				positions.add(i + 1);
				/*if(Global.ifTest){
					System.out.print(i + " ");
				}	*/						
			}
			
		}
		System.out.println();
		/*if(Global.ifTest){
			System.out.println();
		}*/
		//boolean judge = false;
		int count = 0;
		for(int i = 0;i<Global.INDIVIDUAL_NUM;i++){
			for(int j = 0;j<Global.CAR_NUM;j++){
				for(int l = 0;l<Global.WAREHOUSE_NUM;l++){					
					if(count == positions.size()){
						break;
					}
					if(((i) * Global.CAR_NUM * Global.WAREHOUSE_NUM
							+ (j) * Global.WAREHOUSE_NUM + l + 1) == positions.get(count)){
						count += 1;
						previousIndividual.add(i);
						previousCar.add(j);
						whichGene.add(l);
					}
				}
			}
		}
//		for(int i = 0;i<positions.size();i++){
//			position = positions.get(i);
//			previousIndividual.add(position/(Global.CAR_NUM*Global.WAREHOUSE_NUM));
////			CommonUtils.MyLog("Algorithm", "Mutate","previousIndividual:",previousIndividual);
////			CommonUtils.MyLog("Algorithm", "Mutate","lastIndividual:",lastIndividual);
//			previousCar.add((position - previousIndividual.get(i)*Global.CAR_NUM*Global.WAREHOUSE_NUM)/(Global.WAREHOUSE_NUM));
//			whichGene.add( (position - previousIndividual.get(i)*Global.CAR_NUM*Global.WAREHOUSE_NUM)%(Global.WAREHOUSE_NUM));
//		}
		individuals = new Individual[positions.size()];
		Individual individual = null;
		for(int i = 0;i<positions.size();i++){
			mutatedPosition.add(previousIndividual.get(i));
			if(i == 0){
				individual = copyIndividual(cluster.getIndividual(previousIndividual.get(i)));
			}else{
				if(previousIndividual.get(i) == previousIndividual.get(i - 1)){
					individual = copyIndividual(individual);
				}else{
					individual = copyIndividual(cluster.getIndividual(previousIndividual.get(i)));
				}
			}
				
			//individual.lastChangedClear();���ִ���
			
			individual.addWhichCarChanged(previousCar.get(i));
			individual.addWhichWarehChanged(whichGene.get(i));
			individual.setChangedType(1);
			if(individual.getGene()[previousCar.get(i)][whichGene.get(i)] == 0){
				individual.addBeforeChanged(0);
				individual.addAfterChanged(1);
				individual.setOneGene(previousCar.get(i), whichGene.get(i), 1);
			}else{
				individual.addBeforeChanged(1);
				individual.addAfterChanged(0);
				individual.setOneGene(previousCar.get(i), whichGene.get(i), 0);
			}
			
			int [][] gene_temp = new int[5][20];
			gene_temp = individual.getGene();	
			int[] judge = new int [5];
			for(int k=0;k<5;k++){
				judge[k] = 0;
			}	
			//ArrayList<Integer> list0 = new ArrayList<Integer>();
			for(int k=0;k<Global.CAR_NUM;k++){
				for(int j=0;j<Global.WAREHOUSE_NUM;j++){
					if(gene_temp[k][j]!=0){
						break;
					}
					if(j==Global.WAREHOUSE_NUM-1){
						judge[k]=1;						
					}
				}
			}
			
			
			
			for(int k=0;k<Global.CAR_NUM;k++){				
				if(judge[k]==0){
					continue;
				}else{
				for(int j=0;j<Global.WAREHOUSE_NUM;j++){
					if((float)Math.random()<Global.VARIATION_RATE2){
					//System.out.println("0------1");
					//individual.lastChangedClear();
					individual.addWhichCarChanged(k);
					individual.addWhichWarehChanged(j);
					individual.addBeforeChanged(0);
					individual.addAfterChanged(1);
					individual.setOneGene(k, j, 1);
					}
					}
				}
			}		
			
			
			individuals[i] = individual;
		}
		
		
		ArrayList<Individual> indiList = new ArrayList<Individual>();
		if(individuals.length == 0){
			return individuals;
		}
		indiList.add(individuals[0]);
		int prePosition = 0;
		prePosition = previousIndividual.get(0);
		for(int i = 0;i<positions.size();i++){
			if(prePosition == previousIndividual.get(i)){
				indiList.set(indiList.size() - 1, individuals[i]);
			}else{
				indiList.add(individuals[i]);
			}
			prePosition = previousIndividual.get(i);
		}
		ArrayList<Integer> positionTemp = new ArrayList<Integer>();
		positionTemp.add(mutatedPosition.get(0));
		for(int i = 0;i<mutatedPosition.size();i++){
			if(positionTemp.get(positionTemp.size() - 1) != mutatedPosition.get(i)){
				positionTemp.add(mutatedPosition.get(i));
			}
		}
		mutatedPosition.clear();
		for(int i = 0;i<positionTemp.size();i++){
			mutatedPosition.add(positionTemp.get(i));
		}
		individuals = new Individual[indiList.size()];
		for(int i = 0;i<indiList.size();i++){
			individuals[i] = indiList.get(i);
		}
		
		/*for (int i = 0; i < individuals.length; i++) {
			int [][] gene_displat = new int[5][20];
			gene_displat = individuals[i].getGene();
			for(int j=0;j<5;j++){
				for(int k=0;k<20;k++){
					System.out.print(gene_displat[j][k]+" ");
					
				}
				System.out.println();
			}
			System.out.println();
		}*/
		
		return individuals;
	}
	//�鿴�Ƿ��в����н�
	//ʱ��
	//����
	//�Ƿ���û����ĵ�����Ϊ��1
	private Individual[] ifMutatedHasUnworkable(Individual[] individuals) {
		if(individuals.length == 0){
			return null;
		}
		// �Ƿ���û��������������Ϊ��1
		ArrayList<Individual> thisIndividuals = new ArrayList<Individual>();
		for(int i = 0;i<individuals.length;i++){
			thisIndividuals.add(individuals[i]);
		}
		int ifZeroCount = 0;
		for (int i = 0; i < individuals.length; i++) {
			for (int j = 0; j < Global.CAR_NUM; j++) {
				for (int l = 0; l < Global.WAREHOUSE_NUM; l++) {
					if (individuals[i].getGene()[j][l] == 1) {
						for (int c = 0; c < Category.productCategoryCount; c++) {
							if (warehouses.get(l).getDemands().getAmount(c) == 0) {
								ifZeroCount += 1;
							}
						}
						if (ifZeroCount == 3) {
							/*CommonUtils.MyLog("Algorithm", "IfHasUnworkable",
									"���Ϊ" + l + "�������û���󵫱�����Ϊ��1");*/
							thisIndividuals.get(i).getGene()[j][l] = 0;
						}
						ifZeroCount = 0;
					}
				}
			}
		}
		
		// ʱ��������ǲ��Ǻ���
		for (int i = 0; i < thisIndividuals.size(); i++) {
			if (!thisIndividuals.get(i).calculateFit()) {
			/*	CommonUtils.MyLog("Algorithm", "IfHasUnworkable", "���Ϊ"
						+ i + "�ĸ��岻����");*/
				thisIndividuals.remove(i);
				mutatedPosition.remove(i);
			} else {
				/*CommonUtils.MyLog("Algorithm", "IfHasUnworkable", "���Ϊ"
						+ i + "�ĸ��������");*/
			}
		}
		if(thisIndividuals.size() == 0){
			return null;
		}else{
			individuals = new Individual[thisIndividuals.size()];
			for(int i = 0;i<thisIndividuals.size();i++){
				individuals[i] = thisIndividuals.get(i);
			}
			
			
			
			
			return individuals;
		}
	}
	
	private boolean ifCrossedHasUnworkable(Individual[] individuals) {
		boolean result = false;
		// �Ƿ���û��������������Ϊ��1
		int ifZeroCount = 0;
		for (int i = 0; i < individuals.length; i++) {
			for (int j = 0; j < Global.CAR_NUM; j++) {
				for (int l = 0; l < Global.WAREHOUSE_NUM; l++) {
					if (individuals[i].getGene()[j][l] == 1) {
						for (int c = 0; c < Category.productCategoryCount; c++) {
							if (warehouses.get(l).getDemands().getAmount(c) == 0) {
								ifZeroCount += 1;
							}
						}
						if (ifZeroCount == 3) {
						/*	CommonUtils.MyLog("Algorithm", "IfHasUnworkable",
									"���Ϊ" + l + "�������û���󵫱�����Ϊ��1");*/

							individuals[i].getGene()[j][l] = 0;
						}
						ifZeroCount = 0;

					}
				}
			}
		}
		// ʱ��������ǲ��Ǻ���
		for (int i = 0; i < individuals.length; i++) {
			if (!individuals[i].calculateFit()) {
			/*	CommonUtils.MyLog("Algorithm", "IfHasUnworkable", "���Ϊ"
						+ i + "�ĸ��岻����");*/
				result = true;
			} else {
				/*CommonUtils.MyLog("Algorithm", "IfHasUnworkable", "���Ϊ"
						+ i + "�ĸ��������");*/
			}
		}
		if (!result) {
		}
		return result;
	}
	//�ǲ������еĸ��嶼����ͬ����Ӧ��
	private boolean ifAllBest(){
		boolean result = false;
		float fitness = 0f;
		Individual[] individuals = cluster.getIndividuals();
		for(int i = 0;i<Global.INDIVIDUAL_NUM;i++){
			if(i == 0){
				fitness = individuals[i].getFitness();
			}else{
				if(fitness != individuals[i].getFitness()){
					result = false;
				}
			}
			//CommonUtils.MyLog("Algorithm", "ifAllBest","��" + i + "���������Ӧ��Ϊ��" + individuals[i].getFitness());
		
		}
		//System.out.println("");
		return result;
	}
	//TODO
	public Individual copyIndividual(Individual i){
		Individual individual = new Individual();
		individual.setCarRestVolumn(i.getCarRestVolumn());
		individual.setCars(i.getCars());
		individual.setCountToid(i.getCountToid());
		individual.setDistances(i.getDistances());
		individual.setFitness(i.getFitness());
		individual.setGene(i.getGene());
		individual.setIdTocount(i.getIdTocount());
		individual.setLoadedCargo(i.getLoadedCargo());
		individual.setSupplier(i.getSupplier());
		individual.setWarehouses(i.getWarehouses());
		individual.setWarehousesRest(i.getWarehousesRest());
		individual.setWhichType(i.getWhichType());
		individual.setAfterChanged(i.getAfter());
		individual.setBeforeChanged(i.getBefore());
		individual.setWhichCar(i.getWhichCar());
		individual.setWhichWare(i.getWhichWare());
		//individual.calculateFit();
		return individual;
		//return i;
	}
	public HashMap<Integer, Integer> getCountToid() {
		return countToid;
	}
	public HashMap<Integer, Integer> getIdTocount() {
		return idTocount;
	}
	public float[][] getDistances() {
		return Distances;
	}
	public ArrayList<Warehouse> getWarehouses() {
		return warehouses;
	}
	public Supplier getSupplier() {
		return supplier;
	}
	
}
