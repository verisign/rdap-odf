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

package com.rdap.odf.statemachine;

import com.rdap.odf.data.ClientDataMgr;
import com.rdap.odf.oauthhandler.GoogleHandler;
import com.rdap.odf.shell.Constants;
import com.rdap.odf.utils.NetUtils;
import com.rdap.odf.utils.Utils;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the state machine which controls the state changes. It also provides context that can be used by each states.
 */
@Component
public class OdfStateMachine {
    private final Logger LOG = Logger.getLogger(getClass().getName());

    public enum State {
        INIT,
        UNAUTHED,
        PENDING,
        AUTHED
    }

    @Value("${com.rdap.odf.rdapBaseUrl}")
    private String rdapBaseUrl;
    @Value("${com.rdap.odf.batch.auth}")
    private String batchAuthPaths;
    @Value("${com.rdap.odf.batch.unauth}")
    private String batchUnauthPaths;

    @Autowired
    public ClientDataMgr clientDataMgr;
    @Autowired
    public GoogleHandler googleHandler;

    private final NetUtils netUtils = new NetUtils();

    private StateInit stateInit = new StateInit(this);
    private StatePending statePending = new StatePending(this);
    private StateAuthed stateAuthed = new StateAuthed(this);
    private StateUnauthed stateUnauthed = new StateUnauthed(this);

    private StateBase currentState = null;

    public void run() {
        LOG.log(Level.INFO, "start run rdap odf state machine");
        System.out.println("Starting rdap oauth device flow client...");
        currentState = stateInit;
        currentState.enter();
    }

    public void stop() {
        LOG.log(Level.INFO, "stop rdap odf state machine");
        currentState.exit();
        currentState = null;
    }

    /**
     * this is to get the string of the enum state of current state, not the current state object
     *
     * @return
     */
    public String getCurrentStateStr() {
        if (currentState == null) {
            return "unknown";
        }
        return currentState.getStateStr();
    }

    /**
     * this is to get the enum state of current state, not the current state object
     *
     * @return
     */
    private State getCurrentState() {
        if (currentState == null) {
            return null;
        }
        return currentState.getState();
    }

    public String getClientDataStr() {
        return clientDataMgr.toString();
    }

    /**
     * transit from current state to the next state
     *
     * @param to
     */
    public void stateTransit(State to) {
        LOG.log(Level.INFO, "state transit" +
                " from " + (currentState == null ? "null" : currentState.getStateStr())
                + " to " + StateBase.getStateStr(to));

        if (currentState != null) currentState.exit();

        switch (to) {
            case INIT:
                currentState = stateInit;
                break;
            case UNAUTHED:
                currentState = stateUnauthed;
                break;
            case PENDING:
                currentState = statePending;
                break;
            case AUTHED:
                currentState = stateAuthed;
                break;
            default:
                LOG.log(Level.WARNING, "unknown state: " + to + ", something really really weird happened.");
                break;
        }
        if (currentState != null) currentState.enter();
    }

    public String authStart() {
        if (currentState.getState() == State.AUTHED) {
            return "client is already connected. abort.";
        }
        if (currentState.getState() == State.PENDING || currentState.getState() == State.INIT) {
            return "authentication is already in process or is not supported at this time. abort";
        }

        if(!this.waitForAuth()) {
            return "Authorization request timed out";
        }

        return "";
    }

    public String authRefreshToken() {
        if (currentState.getState() != State.AUTHED) {
            return "token can only be refreshed after you are authenticated, abort";
        }
        if (!googleHandler.refreshToken(clientDataMgr)) {
            stateTransit(State.UNAUTHED);
            return "refresh token failed, fall back to logged out state";
        }
        return "tokens are successfully refreshed";
    }

    public String authDrop() {
        if (currentState.getState() != State.AUTHED) {
            return "client is not in Authenticated mode, nothing to drop";
        }
        String output;
        if (clientDataMgr.drop()) {
            output = "successfully dropped auth info";
        } else {
            output = "error occurred during dropping auth info";
        }
        stateTransit(OdfStateMachine.State.INIT);
        return output;
    }

    public String cmdTest(String type) {
        if (!type.equals("auth") && !type.equals("unauth")) {
            return Constants.UNSUPPORTED_COMMAND;
        }

        if (type.equals("auth") && getCurrentState() != OdfStateMachine.State.AUTHED) {
            return "please first authenticate client before run RDAP query with authentication information";
        } else if (type.equals("unauth") && getCurrentState() != OdfStateMachine.State.AUTHED && getCurrentState() != OdfStateMachine.State.UNAUTHED) {
            return "RDAP query is not supported in current state";
        }

        boolean isAuth = type.equals("auth");
        String batchPaths = isAuth ? batchAuthPaths : batchUnauthPaths;
        int total = 0;
        int failed = 0;

        if (batchPaths == null) {
            return "configuration file does not have RDAP paths segment for test";
        }
        String[] paths = batchPaths.split(",");
        for (String path : paths) {
            total += 1;
            String res;
            String output = query(path, isAuth, true, true, "");
            if (output.isEmpty()) {
                res = " ...SUCCESS";
            } else {
                res = " ...FAIL";
                failed += 1;
            }
            System.out.println("TEST #" + total + " " + path + res + "\n" + output);
        }
        return "\nTEST COMPLETED, TOTAL: " + total + ", FAILED: " + failed;
    }

    /**
     * service to shell query command
     *
     * @param url
     * @param type
     * @return
     */
    public String cmdQuery(String url, String type, boolean pretty, String output) {
        if (url.equals(Constants.QueryObject.OBJECT_NOT_SUPPORTED) || url.equals(Constants.QueryTag.TAG_NOT_SUPPORTED)) {
            return url;
        }
        if (type.equals(Constants.QUERY_TYPE_DEFAULT)) {
            if (getCurrentState() == OdfStateMachine.State.AUTHED) {
                return query(url, true, false, pretty, output);
            } else if (getCurrentState() == OdfStateMachine.State.UNAUTHED) {
                return query(url, false, false, pretty, output);
            } else {
                return "RDAP query is not supported in current state";
            }
        } else if (type.equals("auth")) {
            return query(url, true, false, pretty, output);
        } else if (type.equals("unauth")) {
            return query(url, false, false, pretty, output);
        } else {
            return Constants.UNSUPPORTED_COMMAND;
        }
    }

    private String query(String query, boolean isAuth, boolean isTest, boolean isPretty, String output) {
        if (rdapBaseUrl == null) {
            return "missing rdap base url in the configuration";
        }
        query = rdapBaseUrl + query;

        if (isTest) {
            if (isAuth) {
                if (currentState.getState() != State.AUTHED) {
                    return "Authed query not supported in current state";
                }
                return netUtils.sendRdapQuery(
                        Utils.getAuthRDAPUrl(query, clientDataMgr.getIdToken()),
                        clientDataMgr.getAccessToken(),
                        true, isPretty);

            } else {
                if (currentState.getState() != State.AUTHED && currentState.getState() != State.UNAUTHED) {
                    return "RDAP query is not supported in current state";
                }
                return netUtils.sendRdapQuery(query, null, true, isPretty);
            }
        } else {
            if (isAuth) {
                if (currentState.getState() == State.UNAUTHED) {
                    if(!this.waitForAuth()) {
                        return "Unable to complete the query, authorization request timed out";
                    }
                }

                if (currentState.getState() == State.AUTHED) {
                    LOG.log(Level.INFO, "make query: " + query);
                    String resp = netUtils.sendRdapQuery(
                            Utils.getAuthRDAPUrl(query, clientDataMgr.getIdToken()),
                            clientDataMgr.getAccessToken(),
                            false,
                            isPretty);

                    if (!output.equals(Constants.QUERY_OUTPUT_FILE_DEFAULT)) {
                        Utils.saveRdapResponse(query, resp, output);
                    }
                    return resp;
                } else {
                    return "RDAP query is not supported in state " + getCurrentStateStr();
                }
            } else {
                if (currentState.getState() != State.AUTHED && currentState.getState() != State.UNAUTHED) {
                    return "RDAP query is not supported in state " + getCurrentStateStr();
                }
                String resp = netUtils.sendRdapQuery(query, null, false, isPretty);

                if (!output.equals(Constants.QUERY_OUTPUT_FILE_DEFAULT)) {
                    Utils.saveRdapResponse(query, resp, output);
                }

                return resp;
            }
        }
    }

    private boolean waitForAuth() {
        stateTransit(State.PENDING);

        while (currentState.getState() != State.AUTHED) {
            // If the authorization times out by the OAuth Timeout
            if(currentState.getState() == State.UNAUTHED) {
                LOG.log(Level.WARNING, "ERROR: Query timed out waiting for authorization");
                return false;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                LOG.log(Level.WARNING, "Query Authentication Interrupted!");
                return false;
            }
        }
        return true;
    }
}
