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

package com.rdap.odf.data;

import com.rdap.odf.oauthhandler.GoogleHandler;
import com.rdap.odf.utils.Utils;
import io.jsonwebtoken.MalformedJwtException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class stores client's authentication information including tokens, expiration time. It also perform client data
 * management including retrieval and persistence.
 */
@Component
public class ClientDataMgr {
    private final Logger LOG = Logger.getLogger(getClass().getName());

    @Value("${com.rdap.odf.clientData}")
    private String clientDataFile;
    @Value("${com.rdap.odf.tokenRefreshSecondsBeforeExp}")
    private int tokenRefreshSecondsBeforeExp;

    private String accessToken = null;
    private String idToken = null;
    private String refreshToken = null;
    /// token expiration time in seconds
    private String expiresIn = null;
    private String tokenType = null;
    private Instant timestamp = null;

    public String getAccessToken() {
        return accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    /**
     * reset clientdata, drop cliendata file
     */
    public boolean drop() {
        reset();
        File file = new File(clientDataFile);
        return file.delete();
    }

    private void reset() {
        accessToken = null;
        idToken = null;
        refreshToken = null;
        expiresIn = null;
        tokenType = null;
        timestamp = null;
    }

    public long getSecondsForRefresh() {
        Instant expire = timestamp.plusSeconds(Integer.parseInt(expiresIn));
        return expire.getEpochSecond() - tokenRefreshSecondsBeforeExp - Instant.now().getEpochSecond();
    }

    /**
     * @return
     */
    public boolean isValid() {
        if ((accessToken == null)
                || (idToken == null)
                || (refreshToken == null)
                || (expiresIn == null)
                || (tokenType == null)
                || (timestamp == null)) {
            LOG.log(Level.WARNING, "client data is invalid. missing fields");
            return false;
        }

        return true;
    }

    /**
     * check if token is expired. note that it always needs to run after isValid() been performed
     *
     * @return
     */
    public boolean isTokenExpired() {
        if (getSecondsForRefresh() < 0) {
            LOG.log(Level.WARNING, "client data is invalid. token expired");
            return true;
        }
        return false;
    }

    /**
     * load the client data from json response from the provider, stamped with current time
     *
     * @param obj
     * @return
     */
    public boolean loadFromJson(JSONObject obj) {
        if (!GoogleHandler.isValidAuthResp(obj) && !GoogleHandler.isValidRefreshResp(obj)) {
            this.reset();
            return false;
        }

        this.accessToken = obj.get("access_token").toString();
        this.idToken = obj.get("id_token").toString();
        if (obj.containsKey("refresh_token")) {
            this.refreshToken = obj.get("refresh_token").toString();
        }
        this.expiresIn = obj.get("expires_in").toString();
        this.tokenType = obj.get("token_type").toString();
        this.timestamp = Instant.now();
        return true;
    }

    /**
     * write tokens info to file.
     */
    public void persist() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(clientDataFile))) {
            writer.write("access_token=" + accessToken + '\n');
            writer.write("id_token=" + idToken + '\n');
            writer.write("refresh_token=" + refreshToken + '\n');
            writer.write("expires_in=" + expiresIn + '\n');
            writer.write("token_type=" + tokenType + '\n');
            writer.write("timestamp=" + timestamp.toString());
        } catch (IOException e) {
            LOG.log(Level.WARNING, Utils.getStackTraceString(e));
        }
    }

    public boolean loadClientData(String file) {
        clientDataFile = file;
        return loadClientData();
    }

    /**
     * load token data from file clientDataFile
     *
     * @return
     */
    public boolean loadClientData() {
        this.reset();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(clientDataFile))) {
            String line;
            LOG.log(Level.FINE, "loading client data from " + clientDataFile);

            while ((line = bufferedReader.readLine()) != null) {
                String[] data = line.split("=");
                if (data.length != 2) continue;
                if (data[0].equals("access_token")) accessToken = data[1];
                if (data[0].equals("id_token")) idToken = data[1];
                if (data[0].equals("refresh_token")) refreshToken = data[1];
                if (data[0].equals("expires_in")) expiresIn = data[1];
                if (data[0].equals("token_type")) tokenType = data[1];
                if (data[0].equals("timestamp")) this.timestamp = Instant.parse(data[1]);
            }
            if (!this.isValid()) {
                LOG.log(Level.WARNING, "missing fields from the file, reset");
                this.reset();
                return false;
            }
            return true;
        } catch (FileNotFoundException e) {
            LOG.log(Level.WARNING, "Unable to open client data file '" + clientDataFile + "'");
            reset();
            return false;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error reading client data file '" + clientDataFile + "'");
            reset();
            return false;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error loading client data from file '" + clientDataFile + "'");
            reset();
            return false;
        }
    }

    @Override
    public String toString() {
        if (isValid()) {
            return "\033[1;33maccess_token:\033[0m " + this.accessToken
                    + "\n\033[1;33mid_token:\033[0m " + this.idToken
                    + "\n\033[1;33mrefresh_token:\033[0m " + this.refreshToken
                    + "\n\033[1;33mtoken_type:\033[0m " + this.tokenType
                    + "\n\033[1;33mlife (sec):\033[0m " + this.expiresIn
                    + "\n\033[1;33mrefresh after (sec):\033[0m " + this.getSecondsForRefresh()
                    + "\n\033[1;33mtimestamp when latest token is obtained:\033[0m " + this.timestamp;

        }
        return "not exist";
    }
}

