package com.pipl.google;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.*;
import java.sql.*;
//import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by yakik on 2/26/2017.
 */
public class OrgsPersist {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://storage-proc2.pipl.com/leadhack";
    static final String COMPANY_SQL = "insert into company (name, retrieve_method, url) values (?, ?, ?)";
    static final String UPDATE_URL = "update linkedin_company_v4 set google_url = ? where company_norm = ?";
    static final String UPDATE_COMP = "update linkedin_company_v4 set google_by_address_url = ?, phones_by_address_serach = ?, google_plus_phone = ? where company_norm = ?";

    static final String PHONE_SQL = "insert into company_phone (phone, fk_company) values (?, ?)";

    static final String LOADED_COMPS_FILE = "loaded_comps.csv";
    public static final int COMPANY_NAME = 0;
    public static final int COMPANY_DOMAIN = 2;
    public static final int COMPANY_ADDRESS = 1;


    String inputFolder;
    String outputFolder;
    String loadedCompaniesFile;
    String dbUser;
    String dbPassword;
    String dbURL;

    public OrgsPersist(String inputFolder, String outputFolder, String loadedCompaniesFile, String dbUser, String dbPassword, String dbURL) {
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        this.loadedCompaniesFile = loadedCompaniesFile;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbURL = dbURL;
    }

    public List<String> loadOrgsFromDb() {

        List<String> orgs = new ArrayList<String>();

        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);
            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);
            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            String sql = "SELECT id, first, last, age FROM Employees";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orgs;
    }

    public List<File> getZipFiles() {
        List<File> zipFiles = new ArrayList<File>();

        File folder = new File(inputFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Input folder " + inputFolder + " does not exist");
            return zipFiles;
        }

        File[] files = folder.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                zipFiles.add(f);
            }
        }
        return zipFiles;
    }

    public List<StringBuffer> getFilesFromZip(File zipFile) {
        List<StringBuffer> filesInZip = new ArrayList<StringBuffer>();
        ZipFile zfile = null;
        try {
            ZipFile zFile = new ZipFile(zipFile);
            for (Enumeration e = zFile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                StringBuffer sb = getFileFromZip(zFile, entry);
                //Here I need to get the simple name instead the full name with its root
                if (sb != null) {
                    filesInZip.add(sb);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filesInZip;
    }



    public Set<String> getLoadedCompanies() {
        Set<String> loaddedCompanies = new TreeSet<String>();
        BufferedReader br = null;
        FileReader fr = null;
        try {
            File file = new File(loadedCompaniesFile);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            br = new BufferedReader(new FileReader(loadedCompaniesFile));

            String line;
            while ((line = br.readLine()) != null) {
                loaddedCompanies.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return loaddedCompanies;
    }

    synchronized public void addCompany(String company) {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            File file = new File(loadedCompaniesFile);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(company + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    StringBuffer getFileFromZip(ZipFile zipFile, ZipEntry zEntry) {
        StringBuffer sb = new StringBuffer();

        try {
            InputStream stream = zipFile.getInputStream(zEntry);
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            Scanner inputStream = new Scanner(reader);
            sb.append(inputStream.nextLine());

            while (inputStream.hasNext()) {
                sb.append(inputStream.nextLine());
            }

            inputStream.close();
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return sb;
    }

    public List<Company> loadOrgsFromExcel(String fileName, String tabName) {

//        final String orgsSheetName = "Search Engine Companies";
        final String orgsSheetName = tabName;

        List<Company> orgs = new ArrayList<Company>();

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(fileName));
            String[] line;
            reader.readNext();//header
            while ((line = reader.readNext()) != null) {
                orgs.add(new Company(line[COMPANY_NAME],line[COMPANY_DOMAIN],line[COMPANY_ADDRESS]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }




//        BufferedReader br = null;
//        String line = "";
//        String cvsSplitBy = ",";
//        try {
//
//            br = new BufferedReader(new FileReader(fileName));
//            br.readLine();//header
//            while ((line = br.readLine()) != null) {
//
//                // use comma as separator
//                String[] country = line.split("\\s*,\\s*");
//
//                orgs.add(new Company(country[COMPANY_NAME],country[COMPANY_DOMAIN],country[COMPANY_ADDRESS]));
//
//            }
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }


//        Workbook wb = openExcel(fileName);
//        if (wb == null) {
//            System.out.println("Fail to open excel " + fileName);
//            return orgs;
//        }
//
//        Sheet sheet = wb.getSheet(orgsSheetName);
//        int rows = sheet.getPhysicalNumberOfRows();
//
//        String seperator = "";
//        String allOrgs = "";
//        for (int r = 1; r < rows; r++) {
//            Row row = sheet.getRow(r);
//            if (row == null) {
//                continue;
//            }
//            Cell cell = row.getCell(0);
//            if (cell != null) {
//                String cellVal = cell.toString();
//                cellVal = cellVal.trim();
//                orgs.add(cellVal);
//            }
//        }
        /*
        if (allOrgs.length() > 0) {

            Connection conn = null;
            try {
                Class.forName(JDBC_DRIVER);
                conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);
                Statement stmt = conn.createStatement();
                String sql = "SELECT mc.company as origName, lc.company as linkedinName FROM manual_companies mc, linkedin_company_v2 lc " +
                        "where mc.company in ("+
                        allOrgs +
                        ") and mc.Company_Norm in (select company_norm from linkedin_company_v2) " +
                        "and lc.company_norm = mc.company_norm";

                //String sql1 = "select lc.Company from linkedin_company_v2 lc, manual_companies mc where mc.company = '" + getEscapedName(orgName) + "' && mc.company_norm = lc.company_norm;";
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    orgs.add(rs.getString("linkedinName"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
////                String orgName = escapeOrgName(cellVal);
//                if (!cellVal.isEmpty()) {
//                    String updatedOrgName = getOrgName(null, cellVal);
//                    if (updatedOrgName != null) {
//                        orgs.add(updatedOrgName);
//                    }
//                }
//            }
//        }*/
        return orgs;
    }

    public String getEscapedName(String org) {
        String localOrg = org;
        String newCompName = "";
        String[] namePieces = localOrg.split("'");
        String seperator = "";
        for (String piece : namePieces) {
            newCompName += seperator + piece;
            seperator = "\\'";
        }
        return newCompName;
    }

    public String getOrgName(Connection pConn, String orgName) {
        Connection conn = null;
        boolean closeConnection = false;
        try {
            if (pConn == null) {
                Class.forName(JDBC_DRIVER);
                conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);
                closeConnection = true;
            }
            else {
                conn = pConn;
            }
            Statement stmt = conn.createStatement();
            String sql = "select lc.Company from linkedin_company_v3 lc, manual_companies mc where mc.company = '" + getEscapedName(orgName) + "' && mc.company_norm = lc.company_norm;";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getString("company");
            }
            else {
                return "";
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return "";
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
        finally {
            if (closeConnection && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
//        return false;
    }

    public boolean isOrgInDb(String orgName) {
//        Connection conn = null;
//        try {
//            Class.forName(JDBC_DRIVER);
//            conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);
//
//            //STEP 4: Execute a query
//            Statement stmt = conn.createStatement();
//            String sql = "SELECT Company FROM linkedin_company_v2 where company ='" + orgName + "'";
//            ResultSet rs = stmt.executeQuery(sql);
//            if (rs.next()) {
//
//                return true;
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            return false;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//        finally {
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return false;
        return true;
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


    public void writeRowToFile(String fileName, String row){
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            File file = new File(fileName);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(row);
            bw.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    public void addGooglePlusCompanyToFile(String fileName, GooglePlusCompany googleplus) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(googleplus);
        } catch (JsonProcessingException e) {
            jsonInString = "";
        }

        writeRowToFile(fileName,googleplus.getName() + "\t" + jsonInString);
    }

    public void addPhoneToFile(String fileName, CompanyPhones cp) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(cp.getAsCompany());
        } catch (JsonProcessingException e) {
            jsonInString = ""+cp.getName() + "," + cp.getDomain() + "," + cp.getPhonesAsJson();
        }

        writeRowToFile(fileName,cp.getName() + "\t" + jsonInString);
    }

    public void writePhonesToFile(List<CompanyPhones> cps) {
        writePhonesToFile(cps, "./leadhack_3.csv");
    }

    public void writePhonesToFile(List<CompanyPhones> cps, String fileName) {

        File f = new File(fileName);
        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            fos = new FileOutputStream(f);

            bw = new BufferedWriter(new OutputStreamWriter(fos));
            for (CompanyPhones cp : cps) {
                bw.write(cp.getName() + "," + cp.getDomain() + "," + cp.getPhonesAsJson());
                bw.newLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void writePhonesToDb(List<CompanyPhones> cps) {
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(dbURL, "root", "root");
            for (CompanyPhones cp : cps) {
                    insertCompanyInfo(conn, cp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int insertCompanyUrl(Connection conn, String name, String url) throws SQLException {

        boolean shouldCloseConnection = false;
        Connection localConnection = conn;
        int status = -1;
        try {
            if (localConnection == null) {
                localConnection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
                shouldCloseConnection = true;

            }

            PreparedStatement stmt = localConnection.prepareStatement(UPDATE_URL, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, url);
            stmt.setString(2, name);
            stmt.executeUpdate();

        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        finally {

            if (shouldCloseConnection) {
                localConnection.close();
            }
        }
        return status;
    }

    public int insertCompanyUrl(Connection conn, String name, String url, String phones) throws SQLException {

        boolean shouldCloseConnection = false;
        Connection localConnection = conn;
        int status = -1;
        try {
            if (localConnection == null) {
                localConnection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
                shouldCloseConnection = true;

            }

            PreparedStatement stmt = localConnection.prepareStatement(UPDATE_URL, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, url);
            stmt.setString(2, name);
            stmt.executeUpdate();

        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        finally {

            if (shouldCloseConnection) {
                localConnection.close();
            }
        }
        return status;
    }

    public void insertCompanyInfo(Connection conn, CompanyPhones cp) throws SQLException {

        boolean shouldCloseConnection = false;
        Connection localConnection = conn;
        try {
            if (localConnection == null) {
                localConnection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
                shouldCloseConnection = true;

            }
            System.out.println("Inserting into DB. Company: " + cp.getName() + ", URL: " + cp.getDomain() + ", phones: " + cp.getPhonesAsJson());

            PreparedStatement stmt = localConnection.prepareStatement(UPDATE_COMP, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, cp.getDomain());
            stmt.setString(2, cp.getPhone().toString());
            stmt.setString(3, cp.getGooglePlusPhone().toString());
            stmt.setString(4, cp.getName());
            stmt.executeUpdate();
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        finally {

            if (shouldCloseConnection) {
                localConnection.close();
            }
        }
    }

    public void insertCompanyPhones(Connection conn, Set<String> phones, int compId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(PHONE_SQL);
        for (String phone : phones) {
            stmt.setString(1, phone);
            stmt.setInt(2, compId);
            stmt.executeUpdate();
        }
        conn.commit();
    }




//    public String escapeOrgName(String orgName) {
//        String localOrgName = orgName;
//        String newCompName = "";
//        while (true) {
//            int i = localOrgName.indexOf("'");
//            if (i > -1) {
//                newCompName += localOrgName.substring(0, i) + "\\'";
//                localOrgName = localOrgName.substring(i+1, localOrgName.length() -1);
//            }
//            else {
//                break;
//            }
//        }
//        newCompName += localOrgName;
//        return newCompName;
//    }


}
