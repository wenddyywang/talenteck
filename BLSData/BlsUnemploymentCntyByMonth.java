/*
 Scraper for https://www.bls.gov/lau/
 Labor force data by county, not seasonally adjusted, latest 14 months

 Compile in UNIX command line: javac -cp .:jsoup-1.11.3.jar BlsUnemploymentCntyByMonth.java
 Run: java -cp .:jsoup-1.11.3.jar BlsUnemploymentCntyByMonth
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;

import java.util.*;
import java.io.*;
   
public class BlsUnemploymentCntyByMonth {

	public static void main(String[] args) {
		String bls_url = "https://www.bls.gov/lau/#tables";
		Document doc = null;
		try {
			
			doc = Jsoup.connect(bls_url).get();
			
		}catch (IOException e) {
			e.printStackTrace();
		}

		Element county_sec_title = null;
		for(Element h4 : doc.getElementsByTag("h4")){
			if(h4.text().equals("COUNTY DATA") && !h4.hasAttr("id")){
				county_sec_title = h4;
			}
		}
		
		Element ul1 = county_sec_title.nextElementSibling();
		while(!ul1.tagName().equals("ul")){
			ul1 = ul1.nextElementSibling();
		}

		Element ul2 = ul1.child(1);
		Element li = ul2.child(0);
		Element county_txt_file = li.child(0);
		String file_url = county_txt_file.attr("abs:href");

		//get txt file on county data to manipulate
		ArrayList<String[]> allData = new ArrayList<>();
		HashMap<String, ArrayList<County>> cntyMap = new HashMap<>();
		try {

			URL url = new URL(file_url);
			Scanner scanner = new Scanner(url.openStream());

			//skip the headers
			String title = scanner.nextLine();
			for(int i = 0; i<5; i++){
				String n = scanner.nextLine();
			}

			while(scanner.hasNextLine()){
				String[] cnty = scanner.nextLine().replaceAll("\\|","").replaceAll(",", "").trim().split("\\s{2,}");
				if(cnty.length != 9){
					break;
				}
				//add comma back between county and state
				StringBuffer sb = new StringBuffer(cnty[3]);
				sb.insert(cnty[3].lastIndexOf(" "), ",");
				cnty[3] = sb.toString();
				allData.add(cnty);
				//System.out.println(Arrays.toString(cnty) + "\nLength: " + cnty.length);
				County c = new County(cnty);
				if(cntyMap.containsKey(c.laus)){
					cntyMap.get(c.laus).add(c);
				}
				else{
					cntyMap.put(c.laus, new ArrayList<County>());
					cntyMap.get(c.laus).add(c);
				}


			}

		}
		catch(IOException e) {
			e.printStackTrace();
		}
		//write data to csv file
		String fileName = "BlsUnemploymentCntyByMonth.csv";
		String fileHeader = "LAUS Code, State FIPS, County FIPS, County, State, Period, Labor Force, Employed, Unemployment Level, Unemployment Rate";

		try{
			FileWriter fileWriter = new FileWriter(fileName);
			fileWriter.append(fileHeader + "\n");
			for(String[] cnty : allData){
				fileWriter.append(String.join(",", cnty));
				fileWriter.append("\n");
			}
			fileWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}		
		//write data to csv file (organize by LAUS code/County)
		String fileName2 = "BlsUnemploymentCntyMonthly-byCounty.csv";
		String fileHeader2 = "LAUS Code, State FIPS, County FIPS, County, State, Period, Labor Force, Employed, Unemployment Level, Unemployment Rate";

		try{
			FileWriter fileWriter = new FileWriter(fileName2);
			fileWriter.append(fileHeader2 + "\n");
			for(String laus : cntyMap.keySet()){
				for(County cnty : cntyMap.get(laus)){
					fileWriter.append(String.join(",", cnty.allInfo));
					fileWriter.append("\n");
				}
				
			}
			fileWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}

class County{

	public String laus;
	public int fipsState;
	public int fipsCounty;
	public String areaTitle;
	public String period;
	public int laborForce;
	public int employed;
	public int unemploymentLevel;
	public double unemploymentRate;

	public String[] allInfo;

	//construct county object with string array of length 9
	public County(String[] arr){
		laus = arr[0];
		fipsState = Integer.parseInt(arr[1]);
		fipsCounty = Integer.parseInt(arr[2]);
		areaTitle = arr[3];
		period = arr[4];
		laborForce = Integer.parseInt(arr[5]);
		employed = Integer.parseInt(arr[6]);
		unemploymentLevel = Integer.parseInt(arr[7]);
		unemploymentRate = Double.parseDouble(arr[8]);

		allInfo = arr;
	}

}
