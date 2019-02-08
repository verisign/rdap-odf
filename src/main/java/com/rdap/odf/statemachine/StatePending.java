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

import java.time.LocalDateTime;
import org.json.simple.JSONObject;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 * This class implements the Pending state where user perform the oauth device flow authentication.
 * It transits to Authenticated States upon successful authentication or falls back to Unauthenticated state
 * if it fails.
 */
public class StatePending extends StateBase {

    private final int pollDelaySec = 2;

    // number of polling times
    private int pollCnt = 0;
    private Timer pollTimer = null;
    private boolean success;

    public StatePending(OdfStateMachine odfStateMachine) {
        super(OdfStateMachine.State.PENDING, odfStateMachine);
        success = false;
    }

    @Override
    public void enter() {
        super.enter();
        success = false;
        // start oauth device flow when enter this state
        LOG.log(Level.INFO, "start oauth device flow");
        System.out.println("Start oauth device flow. Getting user code...");

        JSONObject resp = manager.googleHandler.startOdf();
        if (resp == null) {
            LOG.log(Level.WARNING, "failed to start oauth device flow");
            System.out.println("Failed to start oauth device flow");
            manager.stateTransit(OdfStateMachine.State.UNAUTHED);
            return;
        }

        String verificationUrl = resp.get("verification_url").toString();
        String userCode = resp.get("user_code").toString();
        String deviceCode = resp.get("device_code").toString();
        int interval = ((Long) resp.get("interval")).intValue();
        int expiresIn = ((Long) resp.get("expires_in")).intValue();

        LOG.log(Level.FINE, "interval: " + interval + ", expire in: " + expiresIn);

        requestDeviceAuthTokens(deviceCode, interval, expiresIn);

        System.out.println("Please visit \033[1;33m" + verificationUrl + "\033[0m on your second device " +
                "and authorize with code \033[1;33m" + userCode + "\033[0m");
        System.out.println("This authorization request will expire at " + LocalDateTime.now().plusSeconds(expiresIn));
    }

    public void exit() {
        super.exit();
        if(success) {
            manager.clientDataMgr.persist();
        }
        if (pollTimer != null) pollTimer.cancel();
    }

    private void requestDeviceAuthTokens(String deviceCode, int interval, int expiresIn) {
        pollCnt = 0;
        pollTimer = new Timer();
        pollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // poll response every "interval" seconds
                pollCnt += 1;
                if (pollCnt > (expiresIn / interval)) {
                    LOG.log(Level.WARNING, "polling expired before user logged in");
                    pollTimer.cancel();
                    manager.stateTransit(OdfStateMachine.State.UNAUTHED);
                    return;
                }
                JSONObject res = manager.googleHandler.requestDeviceAuthTokens(deviceCode);
                if (res == null) {
                    LOG.log(Level.FINE, "pending authorization from user, polling #" + pollCnt);
                    return;
                }
                LOG.log(Level.FINE, "poll auth server returns: " + res.toJSONString());

                LOG.log(Level.INFO, "user authorized, device connected");

                if (!manager.clientDataMgr.loadFromJson(res)) {
                    LOG.log(Level.WARNING, "failed to load json to client data: " + res.toJSONString());
                }
                pollTimer.cancel();
                success = true;
                manager.stateTransit(OdfStateMachine.State.AUTHED);
            }
        }, pollDelaySec * 1000, interval * 1000); // poll after delay for a short time
    }
}
