/*
 Scraper for https://www.bls.gov/lau/
 Labor force data by county, [LATEST YEAR] annual averages

 Compile in UNIX command line: javac -cp .:jsoup-1.11.3.jar BlsUnemploymentCntyByYear.java
 Run: java -cp .:jsoup-1.11.3.jar BlsUnemploymentCntyByYear
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;

import java.util.*;
import java.io.*;
   
public class BlsUnemploymentCntyByYear {

	public static void main(String[] args) {
		String bls_url = "https://www.bls.gov/lau/#tables";
		Document doc = null;
		try {
			
			doc = Jsoup.connect(bls_url).get();
			
		}catch (IOException e) {
			e.printStackTrace();
		}

		Element county_sec_title = doc.getElementById("cntyaa");
		Element nextSibling = county_sec_title.nextElementSibling();
		while(!nextSibling.tagName().equals("ul")){
			nextSibling = nextSibling.nextElementSibling();
		}

		Element li = nextSibling.child(0);

		Element county_txt_file = li.child(0);
		String file_url = county_txt_file.attr("abs:href");

		//get txt file on county data to manipulate
		ArrayList<String[]> allData = new ArrayList<>();
		try {

			URL url = new URL(file_url);
			Scanner scanner = new Scanner(url.openStream());

			//skip the headers
			String title = scanner.nextLine();
			for(int i = 0; i<5; i++){
				String n = scanner.nextLine();
			}

			while(scanner.hasNextLine()){
				String[] cnty_data = scanner.nextLine().trim().replaceAll(",", "").split("\\s{2,}");
				if(cnty_data.length > 3){
					StringBuffer sb = new StringBuffer(cnty_data[3]);
					sb.insert(cnty_data[3].lastIndexOf(" "), ",");
					cnty_data[3] = sb.toString();
				}
				
				allData.add(cnty_data);
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}

		//write data to csv file
		String fileName = "BlsUnemploymentCntyByYear.csv";
		String fileHeader = "LAUS Code, State FIPS, County FIPS, County, State, Year, Labor Force, Employed, Unemployment Level, Unemployment Rate";

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

	}

}
