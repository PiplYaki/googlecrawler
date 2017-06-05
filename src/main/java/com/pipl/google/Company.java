package com.pipl.google;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Dvir Arad on 5/29/17.
 */
class Company {
    String name;
    Set<String> phones = new TreeSet<String>();
    Set<String> googlePlusPhone = new TreeSet<String>();
    String address;
    String domain;

    public Company() {
    }

    public Company(String name) {
        this.name = name;
    }

    public Company(String name, String address, String domain) {
        this.name = name;
        this.address = address;
        this.domain = domain;
    }

    public Company(String name, String address, String domain, Set<String> phones) {
        this.name = name;
        this.address = address;
        this.domain = domain;
        this.phones.addAll(phones);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public Set<String> getPhone() {
        return phones;
    }
    public Set<String> getGooglePlusPhone() {
        return googlePlusPhone;
    }

    public void addPhones(Set<String> phone) {
        if (phone != null)
            this.phones.addAll(phone);
    }

    public void addGooglePlusPhones(Set<String> phone) {
        this.googlePlusPhone.addAll(phone);
    }

    public void addPhone(String phone) {
        if (phone!=null) {
            this.phones.add(phone);
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }


    public String getPhonesAsJson() {
        StringBuffer phonesStr = new StringBuffer("{ \"phones\" : [");
        addPhoneToStringBufferAsJsonFormat(phonesStr,"googlePlus",this.getGooglePlusPhone());
        addPhoneToStringBufferAsJsonFormat(phonesStr,"number",this.getPhone());
        phonesStr.append("]}");
        return phonesStr.toString();
    }

    private void addPhoneToStringBufferAsJsonFormat(StringBuffer phonesStr, String type, Set<String> phoneSet) {
        String seperator = "";
        int counter = 0;
        for (String phone : phoneSet) {
            phonesStr.append(seperator);
            phonesStr.append(phoneToJson(phone,type));
            seperator = ",";
            counter++;
            if (counter >= 5) {
                break;
            }
        }
    }

    private String phoneToJson(String phone,String type) {
        StringBuffer sb = new StringBuffer("{");
//        sb.append("\"type\" : \"\",");
        sb.append("\""+type+"\" : \"" + phone + "\"");
        sb.append("}");
        return sb.toString();
    }

}

class CompanyPhones extends Company {

    StringBuffer googlePage;


    public void setGooglePage(StringBuffer googlePage) { this.googlePage = googlePage; }

    public StringBuffer getGooglePage() {
        return googlePage;
    }



    public Company getAsCompany(){
        return new Company(name,"",domain,phones);
    }

}

class GooglePlusCompany extends Company {
     final GooglePlusPattern GOOGLE_PHONE_PATTERN = new GooglePlusPattern ("data-dtype=\"d3ph\"><span dir=\"ltr\">","</span></span></span>");
     final GooglePlusPattern GOOGLE_NAME_PATTERN = new GooglePlusPattern("<span><span dir=\"ltr\">","</span></span></div><div");
     final GooglePlusPattern GOOGLE_ADDRESS_PATTERN =new GooglePlusPattern ("<span dir=\"ltr\" class=\"_Xbe\">","</span></div></div>");
     final GooglePlusPattern GOOGLE_DOMAIN_PATTERN = new GooglePlusPattern("<div class=\"_idf\"><a class=\"ab_button\" herf=\"","");
    public GooglePlusCompany(StringBuffer googlePage) {
        super();
        Document doc = Jsoup.parse(googlePage.toString());
        Elements googlePlus = doc.getElementsByClass("xpdopen");

        addPhones(getPhoneByPhoneNumber(googlePlus));


//        setName(getInfoByPattern(GOOGLE_NAME_PATTERN,googlePage,false));
//        addPhone(getInfoByPattern(GOOGLE_PHONE_PATTERN,googlePage,true));
//        setAddress(getInfoByPattern(GOOGLE_ADDRESS_PATTERN,googlePage,false));
//        setDomain(getInfoByPattern(GOOGLE_DOMAIN_PATTERN,googlePage,false));
    }

    private Set<String> getPhoneByPhoneNumber(Elements googlePlus) {
        if (!googlePlus.isEmpty()) {
            return OrgReader.getPhones(new StringBuffer(googlePlus.text()));
        }
        return null;
    }

    public GooglePlusCompany(String name) {
        super(name);
    }

    public GooglePlusCompany(String name, String address, String domain) {
        super(name, address, domain);
    }

    public String getInfoByPattern(GooglePlusPattern pattern,StringBuffer googlePage,boolean isPhone) {

        String[] parts1 = googlePage.toString().split(pattern.start);
        if (parts1.length < 2) {
            return null;
        }
        String[] parts2 = parts1[1].toString().split(pattern.end);
        if (parts2.length < 2) {
            return null;
        }
        String object = parts2[0].trim();
        if (isPhone) {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            try {
                Phonenumber.PhoneNumber phone = phoneUtil.parse(object, "US");
            } catch (Exception e) {
                return null;
            }
        }
        return object;
    }

}

class GooglePlusPattern{

    String start;
    String end;

    public GooglePlusPattern(String start, String end) {
        this.start = start;
        this.end = end;
    }
}