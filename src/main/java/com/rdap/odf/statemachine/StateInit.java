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

import java.util.logging.Level;

/**
 * A short states where the client does initialization work. The client will then enter Authenticated or
 * Unauthenticated states based on if user has already logged in or not.
 */
public class StateInit extends StateBase {

    public StateInit(OdfStateMachine odfStateMachine) {
        super(OdfStateMachine.State.INIT, odfStateMachine);
    }

    @Override
    public void enter() {
        super.enter();
        System.out.println("Checking if user is already logged in...");

        if (!manager.clientDataMgr.loadClientData()) {
            LOG.log(Level.WARNING, "client data loading failed, falls to unauthenticated state");
            manager.stateTransit(OdfStateMachine.State.UNAUTHED);
            return;
        }

        if (!manager.clientDataMgr.isValid()) {
            LOG.log(Level.WARNING, "client data validation failed, falls to unauthenticated state");
            manager.stateTransit(OdfStateMachine.State.UNAUTHED);
            return;
        }

        manager.stateTransit(OdfStateMachine.State.AUTHED);

    }

}
