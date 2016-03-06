package jj.webdriver.provider;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import jj.webdriver.WebDriverProvider;
import org.openqa.selenium.WebDriver;

/**
 * Created by jason on 3/6/16.
 */
public class JBrowserWebDriverProvider implements WebDriverProvider {
	@Override
	public WebDriver get() {

		return new JBrowserDriver();
	}
}
