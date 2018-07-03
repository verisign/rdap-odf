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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides common HTTP GET and POST functionalities.
 */
public class NetUtils {
    private static final Logger LOG = Logger.getLogger(NetUtils.class.getName());

    public String post(String endpoint, String param) {
        LOG.log(Level.INFO, "post to endpoint: " + endpoint + ", param: " + param);
        OutputStream os = null;
        HttpURLConnection conn = null;
        try {
            URL obj = new URL(endpoint);
            conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestMethod("POST");
            //con.setRequestProperty("User-Agent", USER_AGENT);

            conn.setDoOutput(true);
            os = conn.getOutputStream();
            os.write(param.getBytes());
            os.flush();

            String resp;
            InputStream inputStream;
            // for google, return 200 after user authorized, or 400 if pending authorization
            if (conn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = conn.getErrorStream();
            } else {
                inputStream = conn.getInputStream();
            }

            resp = readData(inputStream);

            LOG.log(Level.INFO, "request returns code " + conn.getResponseCode()
                    + ", response msg: " + conn.getResponseMessage() +
                    ", partial resp: \n" + (resp.length() > 1500 ? resp.substring(0, 1000) : resp));

            return resp;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "caught exception in querying\n" + Utils.getStackTraceString(e));
            return "";
        } finally {
            try {
                if (conn != null) conn.disconnect();
                if (os != null) os.close();
            } catch (IOException e) {
                LOG.log(Level.WARNING, Utils.getStackTraceString(e));
            }
        }
    }

    private String readData(InputStream inputStream) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String aLine, resp;
            resp = "";
            while ((aLine = bufferedReader.readLine()) != null) {
                resp += aLine;
            }

            return resp;
        } catch (IOException e) {
            LOG.log(Level.WARNING, Utils.getStackTraceString(e));
        }
        return "";
    }

    /**
     * @param endpoint    rdap query endpoint, e.g., https://rdap.verisignlabs.com/domain/test.cc
     * @param bearerToken pass in access_token if authenticated, otherwise pass in null
     * @param isTest      for test query, success result is not returned, only error result is returned
     * @param isPretty    is json response in pretty format
     * @return
     */
    public String sendRdapQuery(String endpoint, String bearerToken, boolean isTest, boolean isPretty) {
        LOG.log(Level.INFO, "get from endpoint: " + endpoint);
        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (bearerToken != null) {
                conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
            }

            String resp;
            InputStream inputStream;
            if (conn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = conn.getErrorStream();
            } else {
                inputStream = conn.getInputStream();
            }
            resp = readData(inputStream);

            // truncating the resp to log if it is too long. sometimes the rdap response is very long and logging
            // all of them will impair the program performance
            LOG.log(Level.INFO, "get returns code " + conn.getResponseCode()
                    + ", response msg: " + conn.getResponseMessage() +
                    ", partial resp: \n" + (resp.length() > 1500 ? resp.substring(0, 1000) : resp));

            if (isTest && conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return "";
            }

            return isPretty ? Utils.toPrettyJson(resp) : resp;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "caught exception in querying\n" + Utils.getStackTraceString(e));
            return "something wrong happened when doing query, please refer to rdap-odf.log for more information";
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
