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

import com.rdap.odf.shell.Constants;
import com.rdap.odf.utils.NetUtils;
import com.rdap.odf.utils.Utils;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 * The Authenticated states of the client after user have logged in via oauth device flow.
 */
public class StateAuthed extends StateBase {

    private Timer refreshTimer;
    private final NetUtils netUtils = new NetUtils();
    private String cachedQuery;
    private boolean cachedQueryPretty;
    private String cachedOutputFile;

    public void setCachedOutputFile(String cachedOutputFile) {
        this.cachedOutputFile = cachedOutputFile;
    }

    public void setCachedQuery(String cachedQuery) {
        this.cachedQuery = cachedQuery;
    }

    public void setCachedQueryPretty(boolean cachedQueryPretty) {
        this.cachedQueryPretty = cachedQueryPretty;
    }

    class RefreshTask extends TimerTask {
        public void run() {
            LOG.log(Level.INFO, "refreshing token");
            if (manager.googleHandler != null) {
                manager.googleHandler.refreshToken(manager.clientDataMgr);
            }
            if (refreshTimer != null) refreshTimer.cancel();
            enter();
        }
    }

    public StateAuthed(OdfStateMachine odfStateMachine) {
        super(OdfStateMachine.State.AUTHED, odfStateMachine);
    }

    @Override
    public void enter() {
        super.enter();
        if (!manager.clientDataMgr.loadClientData()) {
            LOG.log(Level.WARNING, "client data load failed, falls to unauthenticated state");
            manager.stateTransit(OdfStateMachine.State.UNAUTHED);
            return;
        }

        if (!manager.clientDataMgr.isValid()) {
            LOG.log(Level.WARNING, "client data validation failed, falls to unauthenticated state");
            manager.stateTransit(OdfStateMachine.State.UNAUTHED);
            return;
        }

        if (manager.clientDataMgr.isTokenExpired()) {
            LOG.log(Level.INFO, "token expired. refreshing token");
            if (!manager.googleHandler.refreshToken(manager.clientDataMgr)) {
                manager.stateTransit(OdfStateMachine.State.UNAUTHED);
                return;
            }
        }

        LOG.log(Level.INFO, "client data loading succeeded.");

        System.out.println("client is authenticated");

        // start refresh token timer
        refreshTimer = new Timer();
        long refreshSec = manager.clientDataMgr.getSecondsForRefresh();
        LOG.log(Level.INFO, "token refresh scheduled in " + refreshSec + " seconds");
        refreshTimer.schedule(new RefreshTask(), refreshSec * 1000);

        // make the cached rdap query from the previously unauthed user
        if (cachedQuery != null) {
            LOG.log(Level.INFO, "make cached query: " + cachedQuery);
            String resp = netUtils.sendRdapQuery(
                    Utils.getAuthRDAPUrl(cachedQuery, manager.clientDataMgr.getIdToken()),
                    manager.clientDataMgr.getAccessToken(),
                    false, cachedQueryPretty);

            if (!cachedOutputFile.equals(Constants.QUERY_OUTPUT_FILE_DEFAULT)) {
                Utils.saveRdapResponse(cachedQuery, resp, cachedOutputFile);
            }
            System.out.println(resp);
            cachedQuery = null;
        }
    }

    @Override
    public void exit() {
        super.exit();
        if (refreshTimer != null) refreshTimer.cancel();
    }

}
