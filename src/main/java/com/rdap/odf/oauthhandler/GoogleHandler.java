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

package com.rdap.odf.oauthhandler;

import com.rdap.odf.utils.NetUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.rdap.odf.data.ClientDataMgr;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class handles oauth device flow with google provider
 */
@Component
public class GoogleHandler {
    private final Logger LOG = Logger.getLogger(getClass().getName());

    @Value("${com.rdap.odf.google.deviceEndpoint}")
    private String deviceEndpoint;
    @Value("${com.rdap.odf.google.authEndpoint}")
    private String authEndpoint;
    @Value("${com.rdap.odf.google.scope}")
    private String scope;
    @Value("${com.rdap.odf.google.grantType}")
    private String grantType;
    @Value("${com.rdap.odf.google.clientId}")
    private String clientId;
    @Value("${com.rdap.odf.google.clientSecret}")
    private String clientSecret;
    private NetUtils netUtils = new NetUtils();

    public void setNetUtils(NetUtils netUtils) {
        this.netUtils = netUtils;
    }


    /**
     * @return verification uri, user code
     */
    public JSONObject startOdf() {
        try {
            if (clientId == null || clientSecret == null) {
                LOG.log(Level.SEVERE, "missing client id or secret");
                System.out.println("missing client id or secret in application.properties");
                return null;
            }
            String response = netUtils.post(deviceEndpoint,
                    "client_id=" + clientId + "&scope=" + scope);
            if (response == null) {
                LOG.log(Level.WARNING, "failed accessing google oauth device flow device endpoint");
                return null;
            }

            JSONParser j = new JSONParser();
            JSONObject jobj = (JSONObject) j.parse(response);
            if (jobj.containsKey("error")) {
                LOG.log(Level.WARNING, "failed accessing google oauth device flow device endpoint");
                return null;
            }
            return jobj;

        } catch (ParseException e) {
            LOG.log(Level.WARNING, "google device response parse failed");
        }

        return null;
    }

    /**
     * poll authentication result, started by the timer
     *
     * @return
     */
    public JSONObject requestDeviceAuthTokens(String deviceCode) {
        try {
            String response = netUtils.post(authEndpoint,
                    "client_id=" + clientId
                            + "&client_secret=" + clientSecret
                            + "&code=" + deviceCode
                            + "&grant_type=" + grantType);

            if (response == null) {
                LOG.log(Level.FINE, "pending authorization from user");
                return null;
            }

            JSONParser j = new JSONParser();
            JSONObject jobj = (JSONObject) j.parse(response);
            if (jobj.containsKey("error")) {
                LOG.log(Level.FINE, "pending authorization from user");
                return null;
            }
            return jobj;

        } catch (ParseException e) {
            LOG.log(Level.WARNING, "google auth response parse failed");
        }

        return null;
    }

    /**
     * refresh tokens and update client data file
     *
     * @param clientDataMgr
     */
    public boolean refreshToken(ClientDataMgr clientDataMgr) {
        try {
            System.out.println("refreshing token...");

            if (clientId == null || clientSecret == null) {
                LOG.log(Level.SEVERE, "missing client id or secret");
                System.out.println("missing client id or secret in application.properties");
                clientDataMgr.drop();
                return false;
            }
            String response = netUtils.post(authEndpoint,
                    "refresh_token=" + clientDataMgr.getRefreshToken()
                            + "&client_id=" + clientId
                            + "&client_secret=" + clientSecret
                            + "&grant_type=refresh_token");

            if (response == null) {
                LOG.log(Level.FINE, "refresh token failed");
                System.out.println("refresh token failed.");
                clientDataMgr.drop();
                return false;
            }

            JSONParser j = new JSONParser();
            JSONObject jobj = (JSONObject) j.parse(response);
            if (!isValidRefreshResp(jobj)) {
                LOG.log(Level.WARNING, "invalid token refresh resp");
                System.out.println("refresh token failed.");
                clientDataMgr.drop();
                return false;
            }

            clientDataMgr.loadFromJson(jobj);
            clientDataMgr.persist();
            return true;

        } catch (ParseException e) {
            LOG.log(Level.WARNING, "google auth response parse failed");
        }
        return false;
    }

    public static boolean isValidAuthResp(JSONObject resp) {
        return (resp != null)
                && resp.containsKey("access_token")
                && resp.containsKey("id_token")
                && resp.containsKey("token_type")
                && resp.containsKey("expires_in")
                && resp.containsKey("refresh_token");
    }

    public static boolean isValidRefreshResp(JSONObject resp) {
        return (resp != null)
                && resp.containsKey("access_token")
                && resp.containsKey("id_token")
                && resp.containsKey("token_type")
                && resp.containsKey("expires_in");
    }
}
