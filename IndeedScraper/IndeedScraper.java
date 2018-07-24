/*
 * In UNIX command line:
 * Compile: javac -cp .:htmlunit-2.31-OSGI.jar IndeedScraper.java
 */

import java.io.*;
import java.util.*;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import java.net.MalformedURLException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.time.LocalDate;

public class IndeedScraper {
	public static void main(String[] args) throws InterruptedException{
		
		//Create a new IndeedClient and log us in!
		//String[] jobs = {"retail"};
		String[] jobs = {"retail", "call+center", "software+engineer", "fast+food", "manufacturing"};
		HashSet<String> zipCodes = readZipCodes();
		
		String fileName = "IndeedJobCount.csv";
		String fileHeader = "Job Title, Zip Code, Number of Jobs";
		
		/*
		 * Some zip codes like 09069 can't be found - what to do with these?
		 * (currently skipping entirely but should there be a special message output?)
		 */
		try {
			FileWriter fileWriter = new FileWriter(fileName);
			fileWriter.append(fileHeader + "\n");
			
			int totalCount = 0;
			int numZipCodes = 4000;
			for(String job : jobs) {
				int count = 0;
				for(String zip : zipCodes) {
/*					IndeedClient client = new IndeedClient(job, zip);
					String url = client.search();
					System.out.println(url);*/

					String url = "https://www.indeed.com/jobs?q=" 
								+ job + "&l="
								+ zip + "&radius=25";
					url = url.replaceAll("\"", "");
					
					System.out.println(url);
					
					Document doc = null;
					try {
						
						doc = Jsoup.connect(url).get();
						/*
						 * Get total number of jobs
						 */
						Element totalJobCountElement = doc.getElementById("searchCount");
						String totalJobCountString = "";
						if(totalJobCountElement==null ) {
							continue;
						}
						else {
							totalJobCountString = totalJobCountElement.ownText();
						}
						String totalJobsTxt = totalJobCountString.split(" ")[3];
						int totalJobs = Integer.parseInt(totalJobsTxt.replaceAll(",", ""));
						System.out.println(totalJobs);
						
						String[] sideBarIds = {"SALARY_rbo", "JOB_TYPE_rbo", "LOCATION_rbo", "COMPANY_rbo", "EXP_LVL_rbo"};
						for(String s : sideBarIds) {
							TreeMap<String, Integer> tree = readSideList(doc, s);
							System.out.println(tree.toString());
						}
						
						LocalDate date = java.time.LocalDate.now();
						String[] data = {date.toString(), job, zip, String.valueOf(totalJobs)};
						String csvLine = String.join(",", data);
						System.out.println(csvLine);
						fileWriter.append(csvLine);
						fileWriter.append("\n");
						
						count++;
						totalCount++;
						System.out.println("TOTAL: " + totalCount);
						if(count >= numZipCodes) {
							break;
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}
					Thread.sleep(1000);
				}
				Thread.sleep(60000);
			}
			fileWriter.close();	
			System.out.println("DONE");		
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static HashSet<String> readZipCodes() {
		HashSet<String> zipCodes = new HashSet<>();
		
		File file = new File("free-zipcode-database.csv");

        try{
            Scanner in = new Scanner(file);
            in.nextLine();
            while(in.hasNextLine()){
                String line = in.nextLine();
                String[] arr = line.split(",");
                String zipCode = arr[1];
                zipCodes.add(zipCode);
            }
            in.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
		
		return zipCodes;
	}
	
	/*
	 * Get job counts per "Category" from the left side bar on indeed.com
	 * IDS:
	 * "Salary Estimate": "SALARY_rbo"
	 * "Job Type": "JOB_TYPE_rbo"
	 * "Location": "LOCATION_rbo"
	 * "Company": "COMPANY_rbo"
	 * "Experience Level": "EXP_LVL_rbo"
	 */
	public static TreeMap<String, Integer> readSideList(Document htmlDoc, String divId){
		TreeMap<String, Integer> jobCountPerCategory = new TreeMap<>();
		try {
			Element ul = htmlDoc.getElementById(divId).child(0);
			Elements li = ul.children();
			for(Element e : li) {
				String subcategory = e.child(0).ownText();
				String countTxt = e.ownText().replaceAll("[^\\d.]", "");
				if(!countTxt.matches("\\d+")) {
					continue;
				}
				int jobCount = Integer.parseInt(countTxt);
				jobCountPerCategory.put(subcategory, jobCount);
			}	
		}
		catch(NullPointerException e) {
			System.out.println("\n\n" + divId + " invalid" + "\n\n");
		}
		return jobCountPerCategory;
	}

}

class IndeedClient {
	//Create a new WebClient with any BrowserVersion. WebClient belongs to the
	//HtmlUnit library.
	private final WebClient WEB_CLIENT = new WebClient(BrowserVersion.CHROME);
	
	//what: job title, keywords, or company
	//where: city, state, or zip code
	private final String what;
	private final String where;
	
	//Our constructor. Sets our what and where and does some client config.
	IndeedClient(String what, String where){
		this.what = what;
		this.where = where;
		
		WEB_CLIENT.getCookieManager().setCookiesEnabled(true);
	}
	
	public String search(){
		String searchURL = "https://www.indeed.com/";	
		String returnURL = "";
		try {
			//Create an HtmlPage and get the search page.
			HtmlPage searchPage = WEB_CLIENT.getPage(searchURL);
			
			HtmlForm searchForm = searchPage.getForms().get(0);
			
			int childNumber = 0;
			for(DomElement e : searchForm.getChildElements()) {
				if(childNumber == 0) {
					DomElement c = e.getLastElementChild();
					DomElement inputField = c.getLastElementChild();
					((HtmlInput) inputField).setValueAttribute(what);
				}
				else if(childNumber == 1) {
					DomElement c = e.getLastElementChild();
					DomElement inputField = c.getLastElementChild();
					((HtmlInput) inputField).setValueAttribute(where);
				}
				else {
					DomElement button = e.getLastElementChild();
					HtmlPage newPage = (HtmlPage) button.click();
					returnURL = newPage.getUrl().toString();
				}
				childNumber++;
			}
		
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnURL;
	}
	
	public String getHtml(String URL){
		try {
			//All this method does is return the HTML response for some URL.
			//We'll call this after we log in!
			return WEB_CLIENT.getPage(URL).getWebResponse().getContentAsString();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}