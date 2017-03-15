package com.pipl.google

import com.google.i18n.phonenumbers.PhoneNumberMatch
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.junit.Test

import static junit.framework.Assert.assertEquals

/**
 * Created by yakik on 3/2/2017.
 */
class OrgsPersistTest {

    @Test
    public void insertCompanyUrlTest() {
        OrgsPersist op = new OrgsPersist("", "root", "root", "2017 hackathon api list.xlsx");
        op.insertCompanyUrl(null, "000 Emergency", "kishkush");
    }

    @Test
    public void insertCompanyInfoTest() {
        OrgsPersist op = new OrgsPersist("", "root", "root", "jdbc:mysql://localhost/mytest");
        op.insertCompanyInfo(null, "000 Emergency", "kishkush11", "kishkush11");
    }

    @Test
    public void companyPhoneJsonTest() {
        CompanyPhones cp = new CompanyPhones();
        cp.addPhone("aaa");
        assertEquals(
                "{\n" +
                        "\"phones\" : [\n" +
                        "{\n" +
                        "\"type\" : \"\",\n" +
                        "\"number\" : \"aaa\"\n" +
                        "}\n" +
                        "]\n" +
                        "}",
                cp.getPhonesAsJson(),
        );
    }

    @Test
    public void companyPhonesJsonTest() {
        CompanyPhones cp = new CompanyPhones();
        cp.addPhone("aaa");
        cp.addPhone("bbb");
        assertEquals(
                "{" +
                        "\"phones\" : [" +
                        "{" +
                        "\"type\" : \"\"," +
                        "\"number\" : \"aaa\"" +
                        "}" +
                        ","+
                        "{" +
                        "\"type\" : \"\"," +
                        "\"number\" : \"bbb\"" +
                        "}" +
                        "]" +
                        "}",
                cp.getPhonesAsJson(),
        );
    }

    @Test
    public void readSample() {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        FileInputStream fis = null;
        StringBuffer sb = new StringBuffer();
        try {
            fis = new FileInputStream("C:/tmp/hackathon/sample3.txt");
            //Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Iterable<PhoneNumberMatch> numbers = phoneUtil.findNumbers(sb.toString(), "US");
        for (PhoneNumberMatch pnm : numbers) {
            System.out.println(pnm.toString());
            System.out.println("phone: " + pnm.rawString());
        }

    }

    @Test
    public void testEscaping() {
        OrgsPersist op = new OrgsPersist("", "", "", "");
        assertEquals("ggg\\'ddd", op.getEscapedName("ggg'ddd"));
    }

    @Test
    public void testgetOrgName() {
        OrgsPersist op = new OrgsPersist("", "root", "root", "jdbc:mysql://localhost/mytest");
        assertEquals("Tiffany & Co.", op.getOrgName(null, "Tiffany.com"));
    }
}
