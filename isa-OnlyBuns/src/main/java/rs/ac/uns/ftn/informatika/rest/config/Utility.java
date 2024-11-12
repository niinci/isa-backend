package rs.ac.uns.ftn.informatika.rest.config;

import jakarta.servlet.http.HttpServletRequest;

public class Utility {

    public static String getSiteURL(HttpServletRequest request) {
        String siteUrl = request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(), "");
    }
}
