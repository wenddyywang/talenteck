/*
 * In UNIX command line:
 * Compile: javac -cp .:htmlunit-2.31-OSGI.jar IndeedScraper.java
 */

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import javax.faces.component.html.HtmlForm;


public class IndeedScraper {

	public static void main(String[] args) {
		
		//Create a new IndeedClient and log us in!
		IndeedClient client = new IndeedClient("retail", "10027");
		client.login();
		
		//Let's scrape our messages, information behind a login.
		//https://www.reddit.com/message/messages/ is the URL where messages are located.
		//String page = client.get("https://www.reddit.com/message/messages/");
		
		//"div.md" selects all divs with the class name "md", that's where message
		//bodies are stored. You'll find "<div class="md">" before each message.
		// Elements messages = Jsoup.parse(page).select("div.md");
		
		//For each message in messages, let's print out message and a new line.
		// for(Element message : messages){
		// 	System.out.println(message.text() + "\n");
		// }
	}

}

class IndeedClient {
	//Create a new WebClient with any BrowserVersion. WebClient belongs to the
	//HtmlUnit library.
	private final WebClient WEB_CLIENT = new WebClient(BrowserVersion.CHROME);
	
	//This is pretty self explanatory, these are your Reddit credentials.
	//what: job title, keywords, or company
	//where: city, state, or zip code
	private final String what;
	private final String where;
	
	//Our constructor. Sets our what and where and does some client config.
	IndeedClient(String what, String where){
		this.what = what;
		this.where = where;
		
		//Retreives our WebClient's cookie manager and enables cookies.
		//This is what allows us to view pages that require login.
		//If this were set to false, the login session wouldn't persist.
		WEB_CLIENT.getCookieManager().setCookiesEnabled(true);
	}
	
	public void login(){
		//This is the URL where we log in, easy.
		String loginURL = "https://www.indeed.com/";		
		try {
			
			//Create an HtmlPage and get the login page.
			HtmlPage loginPage = WEB_CLIENT.getPage(loginURL);
			
			//Create an HtmlForm by locating the form that pertains to logging in.
			//"//form[@id='login-form']" means "Hey, look for a <form> tag with the
			//id attribute 'login-form'" Sound familiar?
			//<form id="login-form" method="post" ...
			HtmlForm loginForm = loginPage.getFirstByXPath("//form[@class='icl-WhatWhere icl-WhatWhere--lg']");
			
			//This is where we modify the form. The getInputByName method looks
			//for an <input> tag with some name attribute. For example, user or passwd.
			//If we take a look at the form, it all makes sense.
			//<input value="" name="user" id="user_login" ...
			//After we locate the input tag, we set the value to what belongs.
			//So we're saying, "Find the <input> tags with the names "user" and "passwd"
			//and throw in our what and where in the text fields.

			//input form for "what" (job title, keywords, or company)
			loginForm.getInputByName("q").setValueAttribute(what);
			//input form for "where" (city, state, or zip code)
			loginForm.getInputByName("l").setValueAttribute(where);
			
			//<button type="submit" class="c-btn c-btn-primary c-pull-right" ...
			//Okay, you may have noticed the button has no name. What the line
			//below does is locate all of the <button>s in the login form and
			//clicks the first and only one. (.get(0)) This is something that
			//you can do if you come across inputs without names, ids, etc.
			loginForm.getElementsByTagName("button").get(0).click();
			
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String get(String URL){
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