package entity;

import utils.Algorithm;
import utils.CommonUtils;
import global.Global;

public class Cluster {
	//��Ⱥid����Ⱥ��Ӧ�ȣ���ȺȺ��
	private int id;
	private Individual[] individuals;
	Algorithm algorithm;
	public Cluster(int id,Algorithm algorithm){
		this.id = id;
		this.algorithm = algorithm;
		individuals = new Individual[Global.INDIVIDUAL_NUM];
		generateIndividuals();
	}
	// ����������и���
	private void generateIndividuals(){
		CommonUtils.MyLog("Cluster", "generateIndividuals","generate individuals");
		for(int i = 0;i<Global.INDIVIDUAL_NUM;i++){
			Individual individual = new Individual(++Algorithm.id_i,algorithm);
			individuals[i] = individual;
		}
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Individual[] getIndividuals() {
		return individuals;
	}
	public void setIndividuals(Individual[] individuals) {
		this.individuals = individuals;
	}
	public void setIndividual(int position,Individual individual){
		this.individuals[position] = null;
		this.individuals[position] = individual;
	}
	public Individual getIndividual(int position){
		return this.individuals[position];
	}
	
	
}
