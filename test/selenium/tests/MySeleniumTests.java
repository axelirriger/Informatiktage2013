/*
 * info2012_poll
 * MyTests
 * Created on 21.02.2013
 */

package selenium.tests;

import static org.fest.assertions.Assertions.assertThat;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import play.Logger;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/**
 * MyTests.
 * @author vladutb
 * @version 1.0
 * @since 21.02.2013
 */
public class MySeleniumTests {
    /** <code>driver</code> */
    WebDriver driver;
    /**
     * 
     */
    @Before
    public void initForTest() {
        driver = new HtmlUnitDriver();
        driver.get("http://localhost:9000");
        //clean database
        try {
            cleanDatabaseBeforeTest();
        }
        catch (UnknownHostException exept) {
            if (Logger.isDebugEnabled())
                Logger.debug(exept.getLocalizedMessage());
        }
    }
    /**
     * 
     */
    @Test
    public void testRegisterAndLogin() {
        assertThat(driver.getTitle()).isEqualTo("content");
        WebElement loginElement = driver.findElement(By.id("loginLinkId"));
        loginElement.click();
        assertThat(driver.getCurrentUrl()).endsWith("/login");
        WebElement registerLinkElement = driver.findElement(By.id("registerLinkID"));
        assertThat(registerLinkElement.getText()).isEqualTo("Register");
        registerLinkElement.click();
        assertThat(driver.getCurrentUrl()).endsWith("/userRegister");
        WebElement usernameElement = driver.findElement(By.name("username"));
        WebElement passwordElement = driver.findElement(By.name("password"));
        WebElement emailElement = driver.findElement(By.name("email"));
        usernameElement.sendKeys("usertest");
        passwordElement.sendKeys("usertest");
        emailElement.sendKeys("usertest@mail.com");
        emailElement.submit();//the new user was added
        driver.navigate().to("http://localhost:9000/login");
        usernameElement = driver.findElement(By.name("username"));
        passwordElement = driver.findElement(By.name("password"));
        usernameElement.sendKeys("usertest");
        passwordElement.sendKeys("usertest");
        passwordElement.submit();
        WebElement logoutElement = driver.findElement(By.id("logoutLinkId"));
        assertThat(logoutElement).isNotNull();
    }
    /**
     * 
     */
    @Test
    public void testCreateNewPoll() {
        driver.navigate().to("http://localhost:9000/poll");
        String path = "//input[starts-with(@name,\"optionsName[\")]";
        List<WebElement> optionsName = driver.findElements(By.xpath(path));
        assertThat(optionsName).hasSize(1);
        WebElement addElement = driver.findElement(By.id("addRowID"));
        addElement.click();
        addElement = driver.findElement(By.id("addRowID"));
        addElement.click();
        optionsName = driver.findElements(By.xpath(path));
        assertThat(optionsName).hasSize(3);
        WebElement deleteElement = driver.findElement(By.id("deleteRowID"));
        deleteElement.click();
        optionsName = driver.findElements(By.xpath(path));
        assertThat(optionsName).hasSize(2);
        WebElement nameElement = driver.findElement(By.name("pollName"));
        WebElement descriptionElement = driver.findElement(By.name("pollDescription"));
        nameElement.sendKeys("pollTest");
        descriptionElement.sendKeys("Description Poll Test");
        for (WebElement elem : optionsName) {
            elem.sendKeys("Option" + optionsName.indexOf(elem));
        }
        WebElement submitElement = driver.findElement(By.id("submitID"));
        submitElement.submit();
        driver.navigate().to("http://localhost:9000/polls");
        WebElement pollElement = driver.findElement(By.linkText("pollTest"));
        assertThat(pollElement).isNotNull();
    }
    /**
     * @throws UnknownHostException 
     * 
     */
    private void cleanDatabaseBeforeTest() throws UnknownHostException {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB db = mongoClient.getDB("playDb");
        DBCollection collection = db.getCollection("users");
        collection.remove(new BasicDBObject());
    }
}
