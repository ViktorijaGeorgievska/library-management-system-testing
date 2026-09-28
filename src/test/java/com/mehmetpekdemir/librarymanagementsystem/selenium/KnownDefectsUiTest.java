package com.mehmetpekdemir.librarymanagementsystem.selenium;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class KnownDefectsUiTest extends BaseSeleniumTest {
    private void assertTemplateErrorPage(String url, String missingTemplate) {
        driver.get(baseUrl + url);

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Whitelabel Error Page"));
        assertTrue(pageSource.contains(missingTemplate));
    }

    @Test
    @DisplayName("DEFECT: /author/1 shows error page (no list-author template)")
    void authorDetails_showsErrorPage() {
        assertTemplateErrorPage("/author/1", "list-author");
    }

    @Test
    @DisplayName("DEFECT: /category/1 shows error page (no list-category template)")
    void categoryDetails_showsErrorPage() {
        assertTemplateErrorPage("/category/1", "list-category");
    }

    @Test
    @DisplayName("DEFECT: /publisher/1 shows error page (no list-publisher template)")
    void publisherDetails_showsErrorPage() {
        assertTemplateErrorPage("/publisher/1", "list-publisher");
    }
}
