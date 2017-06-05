package com.pipl.google;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.System.exit;
import static java.lang.System.setOut;

/**
 * Created by yakik on 3/2/2017.
 */
public class CrawlerZipThread extends Thread {

    static final String LOADED_COMPS_FILE = "loaded_comps.csv";

    OrgReader reader = null;
    OrgsPersist persist = null;
    int threadNumber;

    public CrawlerZipThread(int threadNumber, String inputFolder, String dbUser, String dbPassword, String dbURL) {
        persist =
                new OrgsPersist(
                        inputFolder,
                        inputFolder + "/output/" + threadNumber,
                        inputFolder + "/" + LOADED_COMPS_FILE,
                        dbUser,
                        dbPassword,
                        dbURL);
        reader = new OrgReader(persist);
        this.threadNumber = threadNumber;
    }

    @Override
    public void run() {
        List<File> zipFiles = persist.getZipFiles();
        Set<String> loadedCompanies = persist.getLoadedCompanies();
        List<CompanyPhones> companies = new ArrayList<CompanyPhones>();

        for (File zipFile : zipFiles) {
            List<StringBuffer> filesInZip = persist.getFilesFromZip(zipFile);
            for (StringBuffer fileContent : filesInZip) {
                GooglePlusCompany googleplus = new GooglePlusCompany(fileContent);
                String companyUrl =  reader.getCompanyUrl(fileContent);
                String companyName = googleplus.name!=null ? googleplus.name : reader.getCompanyNameByAddress(fileContent);
                if (companyName == null) {
                    System.out.println("Failed to get company name ");
                    continue;
                }
                if (loadedCompanies.contains(companyName)) {
                    continue;
                }


                if (!googleplus.phones.isEmpty())
                    System.out.println("yay");

                if (companyUrl == null) {
                    System.out.println("Failed to get company URL for company " + companyName);
                    continue;
                }
                CompanyPhones cp = new CompanyPhones();
                cp.setName(companyName);
                cp.setDomain(googleplus.domain!=null ? googleplus.domain : companyUrl);
                StringBuffer sb = reader.readUrl(companyUrl);
                Set<String> phones = reader.getPhones(sb);
                cp.addGooglePlusPhones(googleplus.phones);
                cp.addPhones(phones);
                companies.add(cp);
                persist.addCompany(companyName);
                loadedCompanies.add(companyName);
                try {
                    persist.insertCompanyInfo(null, cp);
                } catch (SQLException e) {
                    System.out.println("Fail to insert company " + companyName + " into DB");
                    e.printStackTrace();
                }
            }
        }

        String outputName = "./output/" + threadNumber;
        File output = new File(outputName);
        if (!output.exists()) {
            try {
                output.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        persist.writePhonesToFile(companies,outputName + "/output.csv");
    }
}
