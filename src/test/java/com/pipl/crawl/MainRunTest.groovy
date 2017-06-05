package com.pipl.crawl

import com.pipl.google.CrawlerGoogleThread
import org.junit.Test

/**
 * Created by yakik on 3/2/2017.
 */
class MainRunTest extends groovy.util.GroovyTestCase {

    @Test
    public static void testRealRun() {
        CrawlerGoogleThread ct = new CrawlerGoogleThread(1, "./test_input_file", "", "", "");
        ct.start();
        assertTrue(true)
    }

}