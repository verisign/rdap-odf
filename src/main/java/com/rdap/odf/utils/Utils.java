/*
    Copyright (c) 2018, Verisign, Inc.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

    3. Neither the name of the copyright holder nor the names of its contributors
    may be used to endorse or promote products derived from this software without
    specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
    FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
    DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
    SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
    CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
    OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
    OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.rdap.odf.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rdap.odf.shell.Constants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * utils
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger("Utils");

    public static String getStackTraceString(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    /**
     * append idToken to rdap query for authenticated query
     *
     * @param url
     * @param idToken
     * @return
     */
    public static String getAuthRDAPUrl(String url, String idToken) {
        if (url.contains("?")) {
            return url + "&id_token=" + idToken;
        } else {
            return url + "?id_token=" + idToken;
        }
    }

    public static String toPrettyJson(String json) {
        JsonParser parser = new JsonParser();
        JsonObject jObj= parser.parse(json).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jObj);
    }

    public static void saveRdapResponse(String query, String response, String file) {
        LOG.log(Level.INFO, "saving response for query " + query + " to file " + file) ;
        try(FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)) {
            out.println("\n===== querying " + query + "\n" + response);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "failed to save response\n" + getStackTraceString(e));
            System.out.println("FAILED to save response");
        }
    }
    /**
     * get lookup RDAP path segmentation from command parameters
     * @param queryObject object that the lookup or search RDAP query is for
     * @param value value of the query, e.g. test.com
     * @return
     */
    public static String getLookupPathFromParam(Constants.QueryObject queryObject, String value) {
        switch (queryObject) {
            case DOMAIN:
                return "/domain/" + value;
            case NAMESERVER:
                return "/nameserver/" + value;
            case ENTITY:
                return "/entity/" + value;
            default:
                LOG.log(Level.WARNING, "wrong query object: " + queryObject);
                return Constants.QueryObject.OBJECT_NOT_SUPPORTED.toString();
        }
    }
    /**
     * get search RDAP path segmentation from command parameters
     * @param queryObject object that the lookup or search RDAP query is for
     * @param queryTag tag of the query for search RDAP query, e.g. name, nsLdhName and nsIp for domain search
     * @param value value of the query, e.g. test*.com
     * @return
     */
    public static String getSearchPathFromParam(Constants.QueryObject queryObject, Constants.QueryTag queryTag, String value) {
        switch (queryObject) {
            case DOMAIN_SEARCH:
                if (queryTag == Constants.QueryTag.DOMAIN_NAME) {
                    return "/domains?name=" + value;
                } else if (queryTag == Constants.QueryTag.DOMAIN_NS_NAME) {
                    return "/domains?nsLdhName=" + value;
                } else if (queryTag == Constants.QueryTag.DOMAIN_NS_IP) {
                    return "/domains?nsIp=" + value;
                } else {
                    LOG.log(Level.WARNING, "wrong query tag " + queryTag + " for object " + queryObject);
                    return Constants.QueryTag.TAG_NOT_SUPPORTED.toString();
                }
            case NAMESERVER_SEARCH:
                if (queryTag == Constants.QueryTag.NS_NAME) {
                    return "/nameservers?name=" + value;
                } else if (queryTag == Constants.QueryTag.NS_IP) {
                    return "/nameservers?ip=" + value;
                } else {
                    LOG.log(Level.WARNING, "wrong query tag " + queryTag + " for object " + queryObject);
                    return Constants.QueryTag.TAG_NOT_SUPPORTED.toString();
                }
            case ENTITY_SEARCH:
                if (queryTag == Constants.QueryTag.ENTITY_HANDLE) {
                    return "/entities?handle=" + value;
                } else if (queryTag == Constants.QueryTag.ENTITY_FN) {
                    return "/entities?fn=" + value;
                } else {
                    LOG.log(Level.WARNING, "wrong query tag " + queryTag + " for object " + queryObject);
                    return Constants.QueryTag.TAG_NOT_SUPPORTED.toString();
                }
            default:
                LOG.log(Level.WARNING, "wrong query object: " + queryObject);
                return Constants.QueryObject.OBJECT_NOT_SUPPORTED.toString();
        }
    }
}
