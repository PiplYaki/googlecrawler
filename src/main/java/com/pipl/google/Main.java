package com.pipl.google;

/**
 * Created by yakik on 2/23/2017.
 */


import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;
import static java.lang.System.setOut;

public class Main {
    public static void main(String[] args) throws Exception {

        if (args.length < 4) {
            System.out.println("Expected parameters: <mod (zip|google)> <input> <DB user> <DB password> [DB URL] [threads number]");
            exit(-1);
        }

        String mod = args[0].toLowerCase();
        String input = args[1];
        String dbUser = args[2];
        String dbPassword = args[3];
        String dbURL = "";

        if (args.length > 4) {
            dbURL = args[4];
        }

        int threadsNumber = 8;
        if (args.length > 5) {
            threadsNumber = Integer.getInteger(args[5]);
        }

        System.out.println("Executing. mod: " + mod + ", input: " + input + "DB: " + dbURL + " with user: " + dbUser);

         if (mod.equals("zip")) {
            updateFromZip(threadsNumber, dbUser, dbPassword, dbURL, input);
        }
        else if (mod.equals("google")) {
            updateFromGoogle(input, dbUser, dbPassword, dbURL);
        }
        else {
            System.out.println("Expected parameters for zip mod: <mod (zip|google)> <DB user> <DB password> <DB URL> <input folder> [threads number]");
            exit(-1);
        }


    }

    static private void updateFromGoogle(String inputFile, String dbUser, String dbPassword, String dbURL) {

        OrgsPersist persist = new OrgsPersist("", "", dbUser, dbPassword, dbURL);
        OrgReader reader = new OrgReader();

        List<String> orgs = persist.loadOrgsFromExcel(inputFile);

        List<CompanyPhones> orgsUrls = reader.getOrgsURLS(orgs);
        List<CompanyPhones> companies = new ArrayList<CompanyPhones>();

        for (CompanyPhones cp : orgsUrls) {
            try {
                StringBuffer sb = reader.readUrl(cp.getUrl());
                Set<String> phones = reader.getPhones(sb);
                cp.addPhones(phones);
                companies.add(cp);
                persist.addPhoneToFile("./leadhack_4.csv", cp);
            }
            catch (Exception e) {
                System.out.println("Failed to retrieve company " + cp.getName() + ". moving to next company");
                e.printStackTrace();
            }
        }

//        persist.writePhonesToDb(companies);
        //persist.writePhonesToFile(companies);
    }

    static private void updateFromZip(int threadsNumber, String dbUser, String dbPassword, String dbURL, String inputFolder) {

        File folder = new File(inputFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Input folder " + inputFolder + " does not exist");
            exit(-1);
        }

//        CrawlerThread ct = new CrawlerThread(1, inputFolder, dbUser, dbPassword, dbURL);
//        ct.run();
        threadsNumber = 1;
        List<CrawlerThread> crawlers = new ArrayList<CrawlerThread>();
        for (int i = 1; i <= threadsNumber; i++) {
            CrawlerThread ct = new CrawlerThread(i, inputFolder, dbUser, dbPassword, dbURL);
            crawlers.add(ct);
            ct.start();;
        }

        for (CrawlerThread ct : crawlers) {
            try {
                ct.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static private void printOrg(String org, String url, Set<String> phones) {
        String seperator = "";
        String phonesStr = "";
        for (String phone : phones) {
            phonesStr += seperator + phone;
            seperator = ", ";
        }

        System.out.println(org + ", " + url + ", " + phonesStr);
    }


    //private static final Pattern MIDDLE_PATTERN = Pattern.compile("(?:hell\\w*)(.*)(?:wor\\w*)");
    private static final Pattern MIDDLE_PATTERN = Pattern.compile("(.+?(?=<h3 class=\"r\"><a href=\")<h3 class=\"r\"><a href=\")(.+?(?=\" onmousedown))(.*)");
//    private static final Pattern MIDDLE_PATTERN = Pattern.compile("(.*h3 class)(.*)(onmousedown.*)");

    private static String match(String str) {
        Matcher matcher = MIDDLE_PATTERN.matcher(str);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }



}





