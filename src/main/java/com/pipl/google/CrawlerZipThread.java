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
                        inputFolder + "/input/" + threadNumber,
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
                String companyUrl = reader.getCompanyUrl(fileContent);
                String googlePhone = reader.getCompanyUrl(fileContent);
                String companyName = reader.getCompanyName(fileContent);
                if (companyName == null) {
                    System.out.println("Failed to get company name ");
                    continue;
                }
                if (loadedCompanies.contains(companyName)) {
                    continue;
                }

                if (companyUrl == null) {
                    System.out.println("Failed to get company URL for company " + companyName);
                    continue;
                }
                CompanyPhones cp = new CompanyPhones();
                cp.setName(companyName);
                cp.setUrl(companyUrl);
                companies.add(cp);
                StringBuffer sb = reader.readUrl(companyUrl);
                Set<String> phones = reader.getPhones(sb);
                phones.add(googlePhone);
                cp.addPhones(phones);
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
