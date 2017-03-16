package com.pipl.google;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import sun.reflect.annotation.ExceptionProxy;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yakik on 2/26/2017.
 */
public class OrgReader {

    static final Pattern DIGITS10 = Pattern.compile("(.+?(?=\\d{1}-?\\d{3}-\\d{3}-\\d{4}))(\\d{1}-\\d{3}-\\d{3}-\\d{4})(.*)");
    static final Pattern SEPERATED = Pattern.compile("(.+?(?=\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}))(\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4})(.*)");
    static final Pattern BRACKETS = Pattern.compile("(.+?(?=\\(\\d{3}\\) ?\\d{3}-\\d{4}))(\\(\\d{3}\\) ?\\d{3}-\\d{4})(.*)");
    static final Pattern EXT = Pattern.compile("(.+?(?=\\d{3}-\\d{3}-\\d{4}\\sx|ext\\d{3,5}))(\\d{3}-\\d{3}-\\d{4}\\sx|ext\\d{3,5})(.*)");

    static final Pattern URLPATTERN = Pattern.compile("(.+?(?=<h3 class=\"r\"><a href=\")<h3 class=\"r\"><a href=\")(.+?(?=\" onmousedown))(.*)");

    static final Pattern MIDDLE_PATTERN = Pattern.compile("(.+?(?=<h3 class=\"r\"><a href=\")<h3 class=\"r\"><a href=\")(.+?(?=\" onmousedown))(.*)");
    static final Pattern ORG_NAME_PATTERN = Pattern.compile("(.+?(?=<)<h3 class=\"r\"><a href=\")(.+?(?=\" onmousedown))(.*)");

    static final public String DICT_FILE_NAME = "C:/tmp/hackathon/dict.txt";

    Hashtable<String, String> dict = null;

    public List<CompanyPhones> getOrgsURLS(List<String> orgs) {
        List<CompanyPhones> orgsUrls = new ArrayList<CompanyPhones>();
        for (String org : orgs) {

            if (dict == null) {
                loadDict();
            }
            String orgUrl = dict.get(org);
            if (orgUrl == null) {
                StringBuffer googlePage = searchGoogle(org);
                if (googlePage == null) {
                    System.out.println("Failed to find " + org + " headquarters phone in google");
                    continue;
                }

                orgUrl = getCompanyUrl(googlePage);
                addUrlToDict(org, orgUrl);
            }
            if (orgUrl == null) {
                System.out.println("Failed to get " + org + " next hope in google result");
                continue;
            }
            CompanyPhones cp = new CompanyPhones();
            cp.setName(org);
            cp.setUrl(orgUrl);
            OrgsPersist op = null;//new OrgsPersist();
            try {
                orgsUrls.add(cp);
            }
            catch (Exception e) {
                System.out.println("Fail saving " + cp.getName() + ". continue to next company");
                e.printStackTrace();
            }
        }
        return orgsUrls;
    }

    public List<CompanyPhones> getOrgsURLSFromZip(List<StringBuffer> orgs) {
        List<CompanyPhones> orgsUrls = new ArrayList<CompanyPhones>();
        for (StringBuffer googlePage : orgs) {
            if (googlePage == null) {
                continue;
            }

            String orgUrl = getCompanyUrl(googlePage);
            String org = getOrgName(googlePage);
            if (orgUrl == null) {
                System.out.println("Failed to get " + org + " next hope in google result");
                continue;
            }
            CompanyPhones cp = new CompanyPhones();
            cp.setName(org);
            cp.setUrl(orgUrl);
            OrgsPersist op = null;//new OrgsPersist();
            try {
                int dbId = op.insertCompanyUrl(null, cp.getName(), cp.getUrl());
                orgsUrls.add(cp);
            }
            catch (Exception e) {
                System.out.println("Fail saving " + cp.getName() + ". continue to next company");
                e.printStackTrace();
            }
        }
        return orgsUrls;
    }

//    String getOrgUrlFromFile() {
//
//    }

    private StringBuffer searchGoogle(String query) {
        String googleSearchString = "http://www.google.com/search?q=";
        String fullQuery = setPlusSign(query) + "+" + "headquarters+phone+number";
        String charset = "UTF-8";
        try {
            System.out.println("Searching for " + fullQuery);
            return readUrl(googleSearchString + URLEncoder.encode(fullQuery, charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadDict() {
        BufferedReader br = null;
        FileReader fr = null;

        dict = new Hashtable<String, String>();
        try {
            br = new BufferedReader(new FileReader(DICT_FILE_NAME));

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("###");
                dict.put(parts[0], parts[1]);
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
    }

    public void addUrlToDict(String org, String url) {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            File file = new File(DICT_FILE_NAME);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(org + "###" + url + "\n");

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

    public Hashtable<String, String> getDict() {
        return dict;
    }


    public StringBuffer readUrl(String loadUrl) {

        StringBuffer result = new StringBuffer();
        System.out.println("Reading URL: " + loadUrl);

        int timeout = 10;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        HttpGet request = new HttpGet(loadUrl);
        // add request header
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        try {
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<String> readURL1(String loadUrl) {
        try {

            java.net.URL url = new URL(loadUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);  //you still need to handle redirect manully.
            HttpURLConnection.setFollowRedirects(true);
            conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
            int responseCode = conn.getResponseCode();
            // Request not successfu

            long res = conn.getResponseCode();
            System.out.println("Respons e is: " + res);
            if (res != HttpURLConnection.HTTP_OK && res != HttpURLConnection.HTTP_MOVED_TEMP
                    && res == HttpURLConnection.HTTP_MOVED_PERM && res == HttpURLConnection.HTTP_SEE_OTHER) {
                throw new RuntimeException("Request Failed. HTTP Error Code: " + conn.getResponseCode());
            }

            if (res == HttpURLConnection.HTTP_MOVED_TEMP || res == HttpURLConnection.HTTP_MOVED_PERM || res == HttpURLConnection.HTTP_SEE_OTHER) {

                String cookies = conn.getHeaderField("Set-Cookie");
                String newUrl = conn.getHeaderField("Location");
                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                conn.setRequestProperty("Cookie", cookies);
                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.addRequestProperty("User-Agent", "Mozilla");
                conn.addRequestProperty("Referer", "google.com");
                System.out.println("Redirect to URL : " + newUrl);
            }

            // Read response
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            List<String> lines = new ArrayList<String>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(new String(line));
            }
            br.close();
            conn.disconnect();
            return lines;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String setPlusSign(String query) {
        String updatedQuery = query;
        String[] arr = query.split(" ");
        if (arr.length > 1) {
            updatedQuery = arr[0];
            for (int i = 1; i < arr.length; i++) {
                updatedQuery += "+" + arr[i];
            }
        }
        return updatedQuery;
    }

    public String getCompanyName(StringBuffer file) {
        if (file == null) {
            return null;
        }

        String searchStr = "search?q=";

        int urlLocation = file.toString().indexOf(searchStr);
        if (urlLocation == -1) {
            return null;
        }
        urlLocation += searchStr.length();

        int headquartersLocation = file.toString().indexOf("+headquarters");
        if (headquartersLocation == -1) {
            return null;
        }

        if (headquartersLocation < urlLocation) {
            return null;
        }

        String companyName = file.toString().substring(urlLocation, headquartersLocation);
        return companyName;
    }

    public String getCompanyUrl(StringBuffer htmlAsLines) {

        Matcher matcher = URLPATTERN.matcher(htmlAsLines.toString());
        if (matcher.find()) {
            String str =  matcher.group(2);
            try {
                str = java.net.URLDecoder.decode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
            return str;
        }
        return null;
        //String nextHope = null;
    }

    public String getOrgName(StringBuffer htmlAsLines) {

        Matcher matcher = URLPATTERN.matcher(htmlAsLines.toString());
        if (matcher.find()) {
            String str =  matcher.group(2);
            return str;
        }
        return null;
        //String nextHope = null;
    }



    public Set<String> getPhones(StringBuffer sb) {
        Set<String> phones = new TreeSet<String>();
//        phones.addAll(getPhonesByFormat(DIGITS10, sb));
//        phones.addAll(getPhonesByFormat(SEPERATED, sb));
//        phones.addAll(getPhonesByFormat(BRACKETS, sb));
//        phones.addAll(getPhonesByFormat(EXT, sb));
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Iterable<PhoneNumberMatch> numbers = phoneUtil.findNumbers(sb.toString(), "US");
        //numbers.forEach(x -> phones.add(Integer.toString(x.number().getCountryCode()) + Long.toString(x.number().getNationalNumber())));
        for (PhoneNumberMatch number : numbers) {
            phones.add(Integer.toString(number.number().getCountryCode()) + Long.toString(number.number().getNationalNumber()));
        }
        return phones;
    }

    private List<String> getPhonesByFormat(Pattern pattern, StringBuffer sb) {
        List<String> phones = new ArrayList<String>();
        String seperator = "";

        Matcher matcher = pattern.matcher(sb.toString());
        while (true) {
            if (matcher.find()) {
                phones.add(matcher.group(2));
                seperator = ", ";
                matcher = pattern.matcher(matcher.group(3));
            }
            else {
                break;
            }
        }
        return phones;
    }

}
