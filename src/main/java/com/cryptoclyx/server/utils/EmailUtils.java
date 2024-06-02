package com.cryptoclyx.server.utils;

import org.apache.commons.lang3.StringUtils;

public class EmailUtils {

    public static String maskEmail(String email) {
        String EMPTY_STRING = "<can't mask email>";
        if(StringUtils.isNotBlank(email) && email.contains("@")) {
            String[] emailParts = email.split("@");
            String emailPart1 = emailParts[0];

            StringBuffer sb = new StringBuffer();
            sb.append(emailPart1.charAt(0));

            if(emailPart1.length()>3) {
                sb.append(emailPart1.charAt(1));
            }

            sb.append("****");

            if(emailPart1.length()>3) {
                sb.append(emailPart1.charAt(emailPart1.length()-2));
            }
            sb.append(emailPart1.charAt(emailPart1.length()-1))
                    .append("@")
                    .append(emailParts[1]);

            return sb.toString();
        }
        return EMPTY_STRING;
    }
}
