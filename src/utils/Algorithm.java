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
		//初始种群并且计算适应度
		cluster = new Cluster(id_c,this);
		int regressCount = 0;
		int crossCount = 0;
		int mutateCount = 0;
		Individual[] crossedIndividuals = new Individual[2];
		Individual[] mutatedIndividuals;
		while((regressCount <= Global.REGRESS_NUM) && !ifAllBest()){
			CommonUtils.MyLog("Algorithm", "getBestChild","第",regressCount,"次迭代");
			//updateCluster();
			String line = "";
			
			while(crossCount <= Global.CROSS_MUTATE_NUM ){
				//CommonUtils.MyLog("Algorithm", "getBestChild","第---------------",crossCount,"-------------------次交叉");
				crossedIndividuals = Cross();
				if(crossedPosition[0] != crossedPosition[1]){
					crossedIndividuals[0].changedCarsLoadedCargo();
					crossedIndividuals[1].changedCarsLoadedCargo();
				}
				if(!ifCrossedHasUnworkable(crossedIndividuals)){
					//这个判断函数里已经把需求量已清空的需求点的1设置为0，并且已经将没需求的点的1设置为0,
					//所以只要是1，就是至少要送一点的有需求的点
					//同时在这个判断里已经判断了新生成的自带是不是		
					cluster.setIndividual(crossedPosition[0], copyIndividual(crossedIndividuals[0]));
					cluster.setIndividual(crossedPosition[1], copyIndividual(crossedIndividuals[1]));
					break;
				}
				crossCount++;
			}		
			crossCount = 0;
			
			
			while(mutateCount <= Global.CROSS_MUTATE_NUM){
				CommonUtils.MyLog("Algorithm", "getBestChild", mutateCount,"---------------------次变异-------------------");
				mutatedIndividuals = Mutate();
				//变异时已经将所有的变异信息更新，然后在这里将每辆车已经装了多少更新，然后在下面的判断里面重新计算一次适应度
				for(int i = 0;i<mutatedIndividuals.length;i++){
					mutatedIndividuals[i].changedCarsLoadedCargo();
					//CommonUtils.MyLog("Algorithm", "getBestChild","mutatedIndividuals length ",mutatedIndividuals.length);
				}
				mutatedIndividuals = ifMutatedHasUnworkable(mutatedIndividuals);
				if(mutatedIndividuals != null){					
					//这个判断函数里已经把需求量已清空的需求点的1设置为0，并且已经将没需求的点的1设置为0,
					//所以只要是1，就是至少要送一点的有需求的点					
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
				System.out.println("___________________________________分割线"+ regressCount +"_____________________________");
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
	//计算适应度后，更新种群
	//要考虑fitness=-1的情况
	//选取随机数，落入哪个染色体的区间内，则选中它（必须是可行解，不可行解则continue）
	//直到选出Global.INDIVIDUAL_NUM个
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
			//CommonUtils.MyLog("Algorithm", "updateCluster", "第",i,"个个体的适应度为：",individuals[i].getFitness(), "  编号为：",i);
			if(fitnesses[i] == -1){
				continue;
			}
			allFitnesses += individuals[i].getFitness();
		}
		//CommonUtils.MyLog("Algorithm", "updateCluster","总体适应度为：" + allFitnesses);
		//FileWriterTools.WriteToLog(allFitnesses + "");
		for(int i = 0;i<individuals.length;i++){
			//System.out.println("changdudududududududud" + individuals.length);
			random = (float) (Math.random()*allFitnesses);
			for(int j = 0;j<individuals.length;j++){
				//如果fitness=-1，那么就是不可行解，需要continue
				if(fitnesses[i] == -1){
					continue;
				}
				if(fitnesses[i] == 0){
					continue;
				}
				//更新当前的fitness总数
				fitnessCount += fitnesses[j];
				//如果大于等于前一个，并且小于后一个，就落在了后一个的区间内，因为区间是从0开始的[0,第一个）
				if((fitnessCount - fitnesses[j]) <= random && random < fitnessCount){
					cluster.setIndividual(i, copyIndividual(individuals[j]));
					fitnessCount = 0f;
				}
			}
		}
	}
	//随即选出五个个体
	//从五个里面找出最优的一个
	//再随机选出五个个体
	//找出最优的一个
	//将这两个交叉，随机选出要交叉的位置
	//交叉，保存
	//将两个交叉后的个体设置好LastChanged内部类
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
		//选出两个淘汰组
		for(int j = 0;j<2;j++){
			for(int i = 0;i<Global.INDIVIDUAL_NUM;i++){
				random = (float) Math.random();
				if(random < Global.CROSS_RATE){
					if(j == 0){
						individuals1.add(copyIndividual(cluster.getIndividual(i)));
						joinedPosition1.add(i);
						//CommonUtils.MyLog("Algorithm", "Cross", "编号为"+i+"的个体加入到淘汰组1");				
					}else{
						individuals2.add(copyIndividual(cluster.getIndividual(i)));
						joinedPosition2.add(i);
						//CommonUtils.MyLog("Algorithm", "Cross", "编号为"+i+"的个体加入到淘汰组2");
					}
				}
			}
		}
		//如果一个都没选出来
		if(individuals1.size() == 0){
			randomPosition = (int) (Math.random()*(Global.INDIVIDUAL_NUM));
			joinedPosition1.add(randomPosition);
			individuals1.add(copyIndividual(cluster.getIndividual(randomPosition)));
			CommonUtils.MyLog("Algorithm", "Cross","淘汰组一个人都没有，随机选出个体加入到淘汰组1");
		}
		if(individuals2.size() == 0){
			randomPosition = (int) (Math.random()*(Global.INDIVIDUAL_NUM));
			joinedPosition2.add(randomPosition);
			individuals2.add(copyIndividual(cluster.getIndividual(randomPosition)));
			CommonUtils.MyLog("Algorithm", "Cross","淘汰组一个人都没有，随机选出个体加入到淘汰组2");	
		}
		CommonUtils.MyLog("Algorithm", "Cross","individuals1_size:" + individuals1.size() + "  joinedPosition1_size:" + joinedPosition1.size());
		//在这两个中分别选出一个适应度最高的
		for(int i = 0;i<2;i++){
			if(i == 0){
				for(int j = 0;j<individuals1.size();j++){
					if(j == 0){
						//CommonUtils.MyLog("Algorithm", "Cross","淘汰组" + 1 +"当前最高的为" + position1);
						position1 = j;
						individual1 = copyIndividual(individuals1.get(j));
						crossedPosition[0] = joinedPosition1.get(j);
					}else{
						if(individual1.getFitness() < individuals1.get(j).getFitness()){
							//CommonUtils.MyLog("Algorithm", "Cross","淘汰组" + 1 +"当前最高的为" + position1 + "fitness:" + individuals1.get(j).getFitness());
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
						//CommonUtils.MyLog("Algorithm", "Cross","淘汰组" + 2 +"当前最高的为" + position2);
						position2 = j;
						individual2 = copyIndividual(individuals2.get(j));
						crossedPosition[1] = joinedPosition2.get(j);
					}else{
						if(individual2.getFitness() < individuals2.get(j).getFitness()){
							//CommonUtils.MyLog("Algorithm", "Cross","淘汰组" + 2 +"当前最高的为" + position2 + " fitness:" + individuals2.get(j).getFitness());
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
		//individual1 和 individual2是两个淘汰组中最好的两个，进行交叉编译
		//随即选出一个交叉点
		int crossPosition = (int) (Math.random()*(Global.CAR_NUM * Global.WAREHOUSE_NUM));
		int previousCar = crossPosition/Global.WAREHOUSE_NUM;
		int whichPosition = crossPosition%Global.WAREHOUSE_NUM;
		CommonUtils.MyLog("Algorithm", "Cross","交叉点为：第" + previousCar + "的第" + whichPosition + "个点");
		
		individual1.lastChangedClear();
		individual2.lastChangedClear();
		int temp1,temp2;
		for(int i = 0;i<=previousCar;i++){
			for(int j = 0;j<Global.WAREHOUSE_NUM;j++){
				//超过了交叉点，则退出
				if(j > whichPosition ){
					if(i >= previousCar){
						break;
					}
				}
				//System.out.println("individual1 " + individual1.hashCode() + " [i][j]:" + i + "][" + j + " " + individual1.getGene()[i][j]);
				//System.out.println("individual2 " + individual2.hashCode() + " [i][j]:" + i + "][" + j + " " + individual2.getGene()[i][j]);
				//其他的，交换
				//首先记录下变动的位置，变动前后的基因
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
	//一共有5*20*50 = 5000个基因
	//所以又5000*0.005 = 25个变异基因
	//将这些基因排成一行，给每一个基因取随机数，如果≤0.005，那么就将它变异
	//变异完毕，保存
	//将两个交叉后的个体设置好LastChanged内部类
	private Individual[] Mutate(){
		Individual[] individuals;
		ArrayList<Integer> positions = new ArrayList<Integer>();
		float random = 0f;
		//虽然是前一个个体，但是因为是从零开始计数，所以可以直接get(previousIndividual)
		ArrayList<Integer> previousIndividual = new ArrayList<Integer>();
		ArrayList<Integer> previousCar = new ArrayList<Integer>();
		ArrayList<Integer> whichGene = new ArrayList<Integer>();
		int position = 0;
		mutatedPosition.clear();
		/*if(Global.ifTest){
			System.out.print("变异点为：");
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
				
			//individual.lastChangedClear();出现错误
			
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
	//查看是否有不可行解
	//时间
	//载重
	//是否有没需求的点设置为了1
	private Individual[] ifMutatedHasUnworkable(Individual[] individuals) {
		if(individuals.length == 0){
			return null;
		}
		// 是否有没需求的需求点设置为了1
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
									"编号为" + l + "的需求点没需求但被设置为了1");*/
							thisIndividuals.get(i).getGene()[j][l] = 0;
						}
						ifZeroCount = 0;
					}
				}
			}
		}
		
		// 时间和载重是不是合适
		for (int i = 0; i < thisIndividuals.size(); i++) {
			if (!thisIndividuals.get(i).calculateFit()) {
			/*	CommonUtils.MyLog("Algorithm", "IfHasUnworkable", "编号为"
						+ i + "的个体不可行");*/
				thisIndividuals.remove(i);
				mutatedPosition.remove(i);
			} else {
				/*CommonUtils.MyLog("Algorithm", "IfHasUnworkable", "编号为"
						+ i + "的个个体可行");*/
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
		// 是否有没需求的需求点设置为了1
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
									"编号为" + l + "的需求点没需求但被设置为了1");*/

							individuals[i].getGene()[j][l] = 0;
						}
						ifZeroCount = 0;

					}
				}
			}
		}
		// 时间和载重是不是合适
		for (int i = 0; i < individuals.length; i++) {
			if (!individuals[i].calculateFit()) {
			/*	CommonUtils.MyLog("Algorithm", "IfHasUnworkable", "编号为"
						+ i + "的个体不可行");*/
				result = true;
			} else {
				/*CommonUtils.MyLog("Algorithm", "IfHasUnworkable", "编号为"
						+ i + "的个个体可行");*/
			}
		}
		if (!result) {
		}
		return result;
	}
	//是不是所有的个体都有相同的适应度
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
			//CommonUtils.MyLog("Algorithm", "ifAllBest","第" + i + "个个体的适应度为：" + individuals[i].getFitness());
		
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
