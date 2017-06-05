package com.pipl.google

import com.google.i18n.phonenumbers.PhoneNumberMatch
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.junit.Test

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by yakik on 3/2/2017.
 */
class MainTest extends groovy.util.GroovyTestCase {

    @Test
    public static void new_main() {
//        System.out.println(match("hellbbo 1Yaki"));
//        System.out.println(match("hello 2Yaki's world"));
//        System.out.println(match("aahellox 3Yaki's world"));


        FileInputStream fis = null;
        StringBuffer lines = new StringBuffer();
        try {
            fis = new FileInputStream("C:/tmp/hackathon/sample.txt");
            //Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while ((line = br.readLine()) != null) {
                lines.append(line);
            }

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        OrgReader reader = new OrgReader();
        String ur = reader.getNextHope(lines);
        System.out.println("Res: " + ur);
    }


    @Test
    public void new_main2() {

        final Pattern DIGITS10 = Pattern.compile("(.+?(?=\\d{1}-\\d{3}-\\d{3}-\\d{4}))(\\d{1}-\\d{3}-\\d{3}-\\d{4})(.*)");
        final Pattern SEPERATED = Pattern.compile("(.+?(?=\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}))(\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4})(.*)");
        final Pattern BRACKETS = Pattern.compile("(.+?(?=\\(\\d{3}\\) ?\\d{3}-\\d{4}))(\\(\\d{3}\\) ?\\d{3}-\\d{4})(.*)");
        final Pattern BRACKETSWITHHIPER = Pattern.compile("(.+?(?=\\(<a href=\"\\.\\./areacode\\.htm\">\\d{3}</a>\\) ?\\d{3}-\\d{4}))(\\(<a href=\"\\.\\./areacode\\.htm\">\\d{3}</a>\\) ?\\d{3}-\\d{4})(.*)");
        final Pattern EXT = Pattern.compile("(.+?(?=\\d{3}-\\d{3}-\\d{4}\\sx|ext\\d{3,5}))(\\d{3}-\\d{3}-\\d{4}\\sx|ext\\d{3,5})(.*)");

//        final Pattern MY_PATTERN = Pattern.compile("([\\w\\s]+) (\\(\\d{3}\\) ?\\d{3}-\\d{4})");
//        final Pattern MY_PATTERN = Pattern.compile("([\\w\\s]+) (\\(\\d{3}\\) ?\\d{3}-\\d{4})");
//        final Pattern MY_PATTERN = Pattern.compile("([\\w\\s]+) (\\(\\d{3}\\) ?\\d{3}-\\d{4})");
//        final Pattern MY_PATTERN = Pattern.compile("([\\w\\s]+) (\\(\\d{3}\\) ?\\d{3}-\\d{4})");
//        final Pattern MY_PATTERN = Pattern.compile("([\\w\\s]+) (\\(\\d{3}\\) ?\\d{3}-\\d{4})");

        //Matcher matcher = MY_PATTERN.matcher("ssd (345)121-9876 ddf ggg (345)121-9876 wwwewrw (345)121-9876 fgadf");
        Matcher matcher = DIGITS10.matcher("safasd 1-800-925-6278 (1-800 dsfgsdf 0987654321 adfa");
        if (matcher.find()) {
            String str1 = matcher.group(1);
            String str2 = matcher.group(2);
            String str3 = matcher.group(3);

            System.out.println("found " + str1 + ", " + str2 + ", " + str3);
        } else {
            System.out.println("nada");
        }
    }

    @Test
    public void new_main3() {
        FileInputStream fis = null;
        List<String> lines = new ArrayList<String>();
        try {
            fis = new FileInputStream("C:/tmp/hackathon/sample2.txt");
            //Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        OrgReader reader = new OrgReader();
        StringBuffer sb = new StringBuffer();
        for (String line : lines) {
            sb.append(line);
        }
        Set<String> phones = reader.getPhones(sb);
        for (String phone : phones) {
            System.out.println("Phone: " + phone);
        }
    }

    @Test
    public void new_main4() {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        FileInputStream fis = null;
        StringBuffer sb = new StringBuffer();
        try {
            fis = new FileInputStream("C:/tmp/hackathon/sample2.txt");
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
    public void new_main5() {

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        Document doc = null;
//        try {
//            doc = Jsoup.connect("https://gethuman.com/phone-number/General-Motors-GM").get();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        OrgReader reader = new OrgReader();
        StringBuffer sb = reader.readUrl("https://www.philippineairlines.com/AboutUs/ContactUs");
        doc = Jsoup.parse(sb.toString());

        Elements elements = doc.getAllElements();
        Hashtable<String, String> phonesAndDesc = new Hashtable<String, String>();
        for (Element e : elements) {
            String text = e.text();

            Iterable<PhoneNumberMatch> numbers = phoneUtil.findNumbers(text, "US");
            for (PhoneNumberMatch phone : numbers) {
                String phoneStr = phone.rawString();
                String phoneDesc = text.substring(0, text.indexOf(phoneStr));
                if (phonesAndDesc.get(phoneStr) == null) {
                    phonesAndDesc.put(phoneStr, phoneDesc);
                } else {
                    if (!phoneDesc.trim().isEmpty()) {
                        phonesAndDesc.put(phoneStr, phoneDesc);
                    }
                }
            }
        }
        for (Map.Entry entry : phonesAndDesc.entrySet()) {
            System.out.println("Phone: " + entry.getKey() + ", Desc: " + entry.getValue());
        }
    }

    @Test
    public void new_main_6(String[] args) {
        String phone = "United Kingdom\n" +
                "Tel: +44 20 7888 8888\n" +
                "Fax: +44 20 7888 1600";
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Iterable<PhoneNumberMatch> numbers = phoneUtil.findNumbers(phone, "US");
        for (PhoneNumberMatch phone1 : numbers) {
            int i = 5;
        }
    }

    @Test
    public void new_main_7(String[] args) {

        List<CompanyPhones> cps = new ArrayList<CompanyPhones>();
        for (int i =0; i < 5; i++) {
            CompanyPhones cp = new CompanyPhones();
            cp.setName("Comp" + i);
            cp.setUrl("comp" + i);
            Set<String> phones = new TreeSet<String>();
            for (int j = 0; j < 3; j++) {
                phones.add("phone" + i + j);
            }
            cp.addPhones(phones);
            cps.add(cp);
        }
        OrgsPersist op = new OrgsPersist();
        op.writePhonesToDb(cps);
    }

    @Test
    public void writePhones() {

        String csvFile1 = "C:\\tmp\\hackathon\\results\\company.csv";
        String csvFile2 = "C:\\tmp\\hackathon\\results\\company_phone - Copy.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy1 = ";";
        String cvsSplitBy2 = ",";

        List<String[]> phones = new ArrayList<String[]>();

        try {
            Hashtable<String, String> companies = new Hashtable<String, String>();
            br = new BufferedReader(new FileReader(csvFile1));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] details = line.split(cvsSplitBy1);
                String[] compParts = details[1].split("\"");
                companies.put(details[0], compParts[1]);
            }
            br.close();
            br = new BufferedReader(new FileReader(csvFile2));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] details = line.split(cvsSplitBy2);
                String phone1 = details[0];
                String compName = companies.get(details[1]);

                String[] phone2 = new String[2];
                phone2[0] = phone1;
                phone2[1] = compName;
                phones.add(phone2);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Connection conn = null;
        int status = -1;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://storage-proc2.pipl.com/leadhack", "root", "");

            for (String[] phone3 : phones) {
                if (!phone3[1].equals("SFP")) {
                    continue;
                }
                System.out.println("inserting : " + phone3[0] + ", " + phone3[1]);
                String selectStr = "select * from company where retrieve_method = 'GoogleSearch' and name ='" + phone3[1] + "';";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(selectStr);
                if (!rs.next()) {
                    System.out.println("No ID for company " + phone3[1]);
                }
                else {
                    int compId = rs.getInt("id");
                    String insertStr = "insert into company_phone (phone, fk_company) values (" + phone3[0] + "," + compId + ")";
                    stmt.executeUpdate(insertStr);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    public void testWatForThread() {
        List<Thread> crawlers = new ArrayList<Thread>();
        for (int i = 0; i < 10; i++) {
            Thread ct = new Thread() {
                public void run() {
                    println ("thread run ....")
                    try {
                        Thread.sleep(1000);
                    }
                    catch (Exception e) {
                        e.printStackTrace()
                    }
                }
            };
            crawlers.add(ct);
            ct.start();;
        }

        for (Thread ct :crawlers) {
            ct.join();
        }
    }

    @Test
    public void testReadZip() {
        File f = new File("C:\\tmp\\hackathon\\hackathon.zip");
        String testName = "sample.html,sample.txt,sample2.txt";

        OrgsPersist op = new OrgsPersist("", "", "", "");
        List<String> names = op.getFilesNamesFromZip(f);
        for (String name : names) {
            assertTrue(testName.indexOf(name) > -1);
        }
    }

    @Test
    public void makeList() {
        Workbook wb = openExcel("C:\\tmp\\hackathon\\2017 hackathon api list.xlsx");
        if (wb == null) {
            System.out.println("Fail to open excel ");
        }

        Sheet sheet = wb.getSheet(orgsSheetName);
        int rows = sheet.getPhysicalNumberOfRows();

        String comps = "";

        for (int r = 1; r < rows; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            Cell cell = row.getCell(0);
            if (cell != null) {
                String cellVal = cell.toString();
                cellVal = cellVal.trim();
                if (!cellVal.isEmpty()) {
                    comps += cellVal + ",";
                }
            }
        }
        System.out.println("comps: " + comps);
        assertTrue(True)
    }
    private Workbook openExcel(String fileName) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        try {
            Workbook wb = new XSSFWorkbook(fis);
            return wb;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
