
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * This class is used for HTML parsing from URL using Jsoup.
 * @author Andrew Schwartz
 */
public class ParseZillowGuide {
	public static void main(String args[]){
		print("running...");
		Document document;
		try {
			//Get Document object after parsing the html from given url.
			document = Jsoup.connect("http://www.zillow.com/denver-co/").get();

			String title = document.title(); //Get title
			print("  Title: " + title); //Print title.

			Elements price = document.select(".zsg-photo-card-price:contains($)"); //Get price
			Elements address = document.select("span[itemprop]:contains(Denver):contains(CO)"); //Get address
			
			FileOutputStream fout=new FileOutputStream("output_zillow.csv");  
			PrintStream csv=new PrintStream(fout);  
			csv.println("name	price	number sold");
			for (int i=0; i < price.size(); i++) {
				csv.println(address.get(i).text() + ": " + price.get(i).text());
				print(address.get(i).text() + "	" + price.get(i).text());
			}
			fout.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void print(String string) {
		System.out.println(string);
	}

}