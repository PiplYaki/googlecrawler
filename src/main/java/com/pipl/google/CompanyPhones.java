package com.pipl.google;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by yakik on 2/28/2017.
 */
public class CompanyPhones {
    String name;
    String url;
    Set<String> phones = new TreeSet<String>();

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void addPhone(String phone) {
        this.phones.add(phone);
    }

    public void addPhones(Set<String> phones) {
        this.phones.addAll(phones);
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Set<String> getPhones() {
        return phones;
    }

    public String getPhonesAsJson() {
        StringBuffer phonesStr = new StringBuffer("{ \"phones\" : [");
        String seperator = "";
        int counter = 0;
        for (String phone : phones) {
            phonesStr.append(seperator);
            phonesStr.append(phoneToJson(phone));
            seperator = ",";
            counter++;
            if (counter >= 5) {
                break;
            }
        }
        phonesStr.append("]}");
        return phonesStr.toString();
    }

    private String phoneToJson(String phone) {
        StringBuffer sb = new StringBuffer("{");
        sb.append("\"type\" : \"\",");
        sb.append("\"number\" : \"" + phone + "\"");
        sb.append("}");
        return sb.toString();
    }


}
