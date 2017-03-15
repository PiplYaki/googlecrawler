package com.pipl.google

import org.junit.Test

/**
 * Created by yakik on 3/5/2017.
 */
class OrgReaderTest extends GroovyTestCase {

    @Test
    public void testAddEntryToDict() {
        OrgReader or = new OrgReader();
        or.addUrlToDict("test1", "test1");
        or.addUrlToDict("test2", "test2");
        or.addUrlToDict("test3", "test3");
    }

    @Test
    public void testLoadDict() {
        OrgReader or = new OrgReader();
        or.loadDict();
        assertEquals("test2", or.getDict().get("test2"));
    }
}
