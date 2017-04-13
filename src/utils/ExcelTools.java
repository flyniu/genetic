package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import entity.Carrier;
import entity.Products;
import entity.Supplier;
import entity.Warehouse;
import global.Category;

public class ExcelTools {
	private static HashMap<Integer, Integer> countToid = new HashMap<Integer,Integer>();
	private static HashMap<Integer,Integer> idTocount = new HashMap<Integer, Integer>();

	public static HashMap<Integer, Integer> getCountToid() {
		return countToid;
	}

	public static HashMap<Integer, Integer> getIdTocount() {
		return idTocount;
	}

	public static String[][] getData(File file, int ignoreRows)
	           throws FileNotFoundException, IOException {
	       List<String[]> result = new ArrayList<String[]>();
	       int rowSize = 0;
	       BufferedInputStream in = new BufferedInputStream(new FileInputStream(
	              file));
	       // 打开HSSFWorkbook
	       POIFSFileSystem fs = new POIFSFileSystem(in);
	       HSSFWorkbook wb = new HSSFWorkbook(fs);
	       HSSFCell cell = null;
	       for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
	           HSSFSheet st = wb.getSheetAt(sheetIndex);
	           // 第一行为标题，不取
	           for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
	              HSSFRow row = st.getRow(rowIndex);
	              if (row == null) {
	                  continue;
	              }
	              int tempRowSize = row.getLastCellNum() + 1;
	              if (tempRowSize > rowSize) {
	                  rowSize = tempRowSize;
	              }
	              String[] values = new String[rowSize];
	              Arrays.fill(values, "");
	              boolean hasValue = false;
	              for (short columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {
	                  String value = "";
	                  cell = row.getCell(columnIndex);
	                  if (cell != null) {
	                     // 注意：一定要设成这个，否则可能会出现乱码
	                     cell.setEncoding(HSSFCell.ENCODING_UTF_16);
	                     switch (cell.getCellType()) {
	                     case HSSFCell.CELL_TYPE_STRING:
	                         value = cell.getStringCellValue();
	                         break;
	                     case HSSFCell.CELL_TYPE_NUMERIC:
	                         if (HSSFDateUtil.isCellDateFormatted(cell)) {
	                            Date date = cell.getDateCellValue();
	                            if (date != null) {
	                                value = new SimpleDateFormat("yyyy-MM-dd")
	                                       .format(date);
	                            } else {
	                                value = "";
	                            }
	                         } else {
	                            value = new DecimalFormat("###0.####").format(cell
	                                   .getNumericCellValue());
	                         }
	                         break;
	                     case HSSFCell.CELL_TYPE_FORMULA:
	                         // 导入时如果为公式生成的数据则无值
	                         if (!cell.getStringCellValue().equals("")) {
	                            value = cell.getStringCellValue();
	                         } else {
	                            value = cell.getNumericCellValue() + "";
	                         }
	                         break;
	                     case HSSFCell.CELL_TYPE_BLANK:
	                         break;
	                     case HSSFCell.CELL_TYPE_ERROR:
	                         value = "";
	                         break;
	                     case HSSFCell.CELL_TYPE_BOOLEAN:
	                         value = (cell.getBooleanCellValue() == true ? "Y"
	                                : "N");
	                         break;
	                     default:
	                         value = "";
	                     }
	                  }
	                  if (columnIndex == 0 && value.trim().equals("")) {
	                     break;
	                  }
	                  values[columnIndex] = rightTrim(value);
	                  hasValue = true;
	              }

	              if (hasValue) {
	                  result.add(values);
	              }
	           }
	       }
	       in.close();
	       String[][] returnArray = new String[result.size()][rowSize];
	       for (int i = 0; i < returnArray.length; i++) {
	           returnArray[i] = (String[]) result.get(i);
	       }
	       return returnArray;
	    }

	    /**
	     * 去掉字符串右边的空格
	     * @param str 要处理的字符串
	     * @return 处理后的字符串
	     */
	     public static String rightTrim(String str) {
	       if (str == null) {
	           return "";
	       }
	       int length = str.length();
	       for (int i = length - 1; i >= 0; i--) {
	           if (str.charAt(i) != 0x20) {
	              break;
	           }
	           length--;
	       }
	       return str.substring(0, length);
	    }
	     
	     public static ArrayList<Warehouse> getWarehouseFromExcel(){
	 		ArrayList<Warehouse> warehouses = new ArrayList<Warehouse>();
	 		File file = new File(Category.warehouseDataSource);
	 		String[][] result;
	 		
	 		try {
	 			result = ExcelTools.getData(file, 1);
	 			int rowLength = result.length;
	 			
	 			for (int i = 0; i < rowLength; i++) {
	 				Warehouse warehouse = new Warehouse();
	 				Products demands = new Products();;
	 				Products recyle = new Products();;
	 				for (int j = 0; j < result[i].length; j++) {
	 					if(j == 0){
	 						warehouse.setId(Integer.valueOf(result[i][j]));
	 					}else if(j == 1 || j == 2 || j == 3){
	 						
	 						demands.setAmount(j - 1, Integer.valueOf(result[i][j]));
	 						//System.out.println("catagory:" + (j - 1) + " demands:" + result[i][j]);
	 						if(j == 1){
	 								demands.setOccupation(j - 1, Category.productCategory1);
	 							}else if(j == 2){
	 								demands.setOccupation(j - 1, Category.productCategory2);
	 							}else if(j == 3){
	 								demands.setOccupation(j - 1, Category.productCategory3);
	 						}	
	 					}else if(j == 4){
	 						warehouse.setDemands(demands);
	 						warehouse.setPriority(Integer.valueOf(result[i][j]));
	 					}else if(j == 5){
	 						if(result[i][j].equals("y")){
	 							warehouse.setIfUrgent(true);
	 						}else{
	 							warehouse.setIfUrgent(false);
	 						}
	 					}else if(j == 6){
	 						
	 						//System.out.println("catagory:" + j + " recycle:" + result[i][j]);
	 						recyle.setAmount(1, Integer.valueOf(result[i][j]));
	 						recyle.setOccupation(1, Category.productCategory1);
	 						warehouse.setRecycles(recyle);
	 					}
	 					
	 				}
	 				warehouses.add(warehouse);
	 			}
	 		} catch (FileNotFoundException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		} catch (IOException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		}
	 		return warehouses;
	 	}
	 	public static Supplier getSupplierFromExcel(){
	 		Supplier supplier = new Supplier();
	 		File file = new File(Category.supplierDataSource);
	 		String[][] result;
	 		Carrier[] carriers = null;
	 		try {
	 			result = ExcelTools.getData(file, 1);
	 			int rowLength = result.length;
	 			carriers = new Carrier[rowLength];
	 			for (int i = 0; i < rowLength; i++) {
	 				carriers[i] = new Carrier();
	 				for (int j = 0; j < result[i].length; j++) {
	 					//System.out.println(i + " " + j + " " + result[i][j]);
	 					//System.out.println(carriers[i].hashCode());
	 					if(j == 0){
	 						carriers[i].setCarId(Integer.valueOf(result[i][j]));
	 					}else if(j == 1){
	 						float fvolumn = Float.valueOf(result[i][j]);
	 						int ivolumn = (int) Math.floor(fvolumn);
	 						carriers[i].setVolumn(ivolumn);
	 						//System.out.println("carrier" + i + ": " + carriers[i].getVolumn());
	 					}else if(j == 2){
	 						carriers[i].setCostPerKim(Integer.valueOf(result[i][j]));
	 					}else if(j == 3){
	 						carriers[i].setSpeed(Integer.valueOf(result[i][j]));
	 					}
	 				}
	 			}
	 		} catch (FileNotFoundException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		} catch (IOException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		}
	 		supplier.setCarriers(carriers);
	 		return supplier;
	 	}
	 	public static float[][] getDistancesFromExcel() {
	 		float distances[][] = new float[21][21];
	 		File file = new File(Category.distancesDataSource);
	 		String[][] result;
	 		int x,y = 0;
	 		try {
	 			result = ExcelTools.getData(file, 1);
	 			int rowLength = result.length;
	 			for (int i = 0; i < rowLength; i++) {
	 				if(i == 0){
	 					continue;
	 				}
	 				for (int j = 0; j < result[i].length; j++) {
	 					if(j == 0 || result[i][j].equals("")){
	 						
	 						continue;
	 					}
	 					//System.out.println("**key" + result[0][j] + "value" + (j - 1));
	 					countToid.put(j - 1, Integer.valueOf(result[0][j]));
	 					idTocount.put(Integer.valueOf(result[0][j]), j - 1);
	 					x = i - 1;
	 					y = j - 1;
	 					//System.out.print(x + "," + y +  "\t");
	 					//System.out.print(result[i][j] + "*");
	 					if(result[i][j].equals("-1")){
	 						distances[x][y] = distances[y][x];
	 					}else{
	 						if(Float.valueOf(result[i][j]) < 0){
	 							distances[x][y] = 0f;
	 						}else{
	 							distances[x][y] = Float.valueOf(result[i][j]);
	 						}	
	 					}
	 				}
	 				//System.out.println();
	 			}
	 		} catch (FileNotFoundException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		} catch (IOException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		}
	 		return distances;

	 	}
}
