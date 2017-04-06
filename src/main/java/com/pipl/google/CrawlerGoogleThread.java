package com.pipl.google;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by yakik on 3/30/2017.
 */
public class CrawlerGoogleThread extends Thread{
    final static public String INPUT_FILE_NAME = "";
    final static public String INPUT_TAB_NAME = "API Companies";

    OrgsPersist persist = null;
    OrgReader reader = null;
    int threadNumber;
    String inputFileName = "";
    public CrawlerGoogleThread(int threadNumber, String inputFileName, String dbUser, String dbPassword, String dbURL) {
        persist =
                new OrgsPersist(
                        "",
                        "",
                        "C:/tmp/hackathon/results/loaded_comps.csv",
                        dbUser,
                        dbPassword,
                        dbURL);
        reader = new OrgReader(persist);
        this.threadNumber = threadNumber;
        this.inputFileName = inputFileName;
    }

    public void run() {

        List<String> orgs = persist.loadOrgsFromExcel(inputFileName, INPUT_TAB_NAME + threadNumber);

        List<CompanyPhones> orgsUrls = reader.getOrgsURLS(orgs);
        List<CompanyPhones> companies = new ArrayList<CompanyPhones>();

        for (CompanyPhones cp : orgsUrls) {
            try {
                StringBuffer sb = reader.readUrl(cp.getUrl());
                Set<String> phones = reader.getPhones(sb);
                cp.addPhones(phones);
                companies.add(cp);
                persist.addPhoneToFile("./leadhack_4.csv", cp);
                persist.insertCompanyInfo(null, cp);
                persist.addCompany(cp.getName());
            }
            catch (Exception e) {
                System.out.println("Failed to retrieve company " + cp.getName() + ". moving to next company");
                e.printStackTrace();
            }
        }

    }
}
