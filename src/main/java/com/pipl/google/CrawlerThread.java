package com.pipl.google;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.System.exit;

/**
 * Created by yakik on 3/2/2017.
 */
public class CrawlerThread extends Thread {

    static final String LOADED_COMPS_FILE = "loaded_comps.csv";

    OrgReader reader = new OrgReader();
    OrgsPersist persist = null;
    int threadNumber;

    public CrawlerThread(int threadNumber, String inputFolder, String dbUser, String dbPassword, String dbURL) {
        persist =
                new OrgsPersist(
                        inputFolder + "/input/" + threadNumber,
                        inputFolder + "/output/" + threadNumber,
                        inputFolder + "/" + LOADED_COMPS_FILE,
                        dbUser,
                        dbPassword,
                        dbURL);
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
                cp.addPhones(phones);
                loadedCompanies.add(companyName);
                persist.addCompany(companyName);
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
