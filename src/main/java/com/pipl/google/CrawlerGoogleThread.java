package com.pipl.google;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by yakik on 3/30/2017.
 */
public class CrawlerGoogleThread extends Thread{
    final static public String INPUT_FILE_NAME = "";
    final static public String INPUT_TAB_NAME = "API Companies";
    public static final String GOOGLE_FIRST_LINK_FILE = "./google_first_link.csv";
    public static final String GOOGLE_PLUS_FILE = "./google_plus.csv";
    public static final String LOADED_COMPANIES_FILE = "./loaded_comps.csv";

    OrgsPersist persist = null;
    OrgReader reader = null;
    int threadNumber;
    String inputFileName = "";
    public CrawlerGoogleThread(int threadNumber, String inputFileName, String dbUser, String dbPassword, String dbURL) {
        persist =
                new OrgsPersist(
                        "",
                        "",
                        LOADED_COMPANIES_FILE,
                        dbUser,
                        dbPassword,
                        dbURL);
        reader = new OrgReader(persist);
        this.threadNumber = threadNumber;
        this.inputFileName = inputFileName;
    }

    public void run() {

        List<Company> orgs = persist.loadOrgsFromExcel(inputFileName, INPUT_TAB_NAME + threadNumber);
        List<CompanyPhones> orgsUrls = reader.getOrgsURLS(orgs);
        List<CompanyPhones> companies = new ArrayList<CompanyPhones>();

        for (CompanyPhones cp : orgsUrls) {
            try {
                StringBuffer sb = reader.readUrl(cp.getDomain());
                StringBuffer googlePage=cp.getGooglePage();

                Set<String> phones = reader.getPhones(sb);
                cp.addPhones(phones);
                companies.add(cp);
                persist.addPhoneToFile(GOOGLE_FIRST_LINK_FILE, cp);
                if(googlePage!=null) {
                    GooglePlusCompany googleplus = new GooglePlusCompany(googlePage);
                    if (googleplus.name != null)
                        persist.addGooglePlusCompanyToFile(GOOGLE_PLUS_FILE, googleplus);

                }
//                persist.insertCompanyInfo(null, cp);
                persist.addCompany(cp.getName());
            }
            catch (Exception e) {
                System.out.println("Failed to retrieve company " + cp.getName() + ". moving to next company");
                e.printStackTrace();
            }
        }

    }
}
