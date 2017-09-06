package com.qaprosoft.zafira.services.services;

import com.qaprosoft.zafira.models.db.Attachment;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by irina on 5.7.17.
 */
public class SeleniumServiceTest {
    @Test
    public void testCaptureScreenshoots() throws Exception {
        String url = "http://localhost:3000/#!/dashboards";
        List<String> urls = new ArrayList<>();
        urls.add(url);
        String domain = "localhost";
        String auth = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI4NCIsInBhc3N3b3JkIjoiZGpOOC81TGhFZXVRVEpzVzhHcTZ3cGYzVU54dWNmMm0iLCJleHAiOjEzMDM0ODI3Nzc4OX0.Pma01y9I-nQfrY7nbYOenytSp7UBxnJxO-tZxzrpNK-4doBgxjyQ72jmCnA6jMyH6oFB5fnZK-I0sUKRstgKQg";
        // By areaLocator = By.ById("content");
        //By titleLocator =;
        //Dimension dimension = null;
        SeleniumService seleniumService = new SeleniumService();
        List<Attachment> attachments = seleniumService.captureScreenshoots(urls, domain, auth, null, null, null);
        assertNotNull(attachments);

    }

}