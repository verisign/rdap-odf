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
import java.util.logging.Logger;

/**
 * This is the base class for different client states. It provides some common functionalities also.
 */
public abstract class StateBase {
    protected final Logger LOG = Logger.getLogger(getClass().getName());

    protected OdfStateMachine manager;
    private OdfStateMachine.State state;

    public StateBase(OdfStateMachine.State state, OdfStateMachine odfStateMachine) {
        this.state = state;
        this.manager = odfStateMachine;
    }

    public OdfStateMachine.State getState() {
        return state;
    }

    public static String getStateStr(OdfStateMachine.State state) {
        switch (state) {
            case INIT:
                return "Initialization";
            case UNAUTHED:
                return "Unauthenticated";
            case PENDING:
                return "Pending Authentication";
            case AUTHED:
                return "Authenticated";
        }
        return "UNKNOWN";
    }

    public String getStateStr() {
        return StateBase.getStateStr(state);
    }

    /**
     * needs to be implemented by concrete states for initialization work during state create
     */
    public void enter() {
        LOG.log(Level.INFO, "entering state " + getStateStr(state));
    }

    /**
     * needs to be implemented by concrete states for wrap up work during state exit
     */
    public void exit() {
        LOG.log(Level.INFO, "exiting state " + getStateStr(state));
    }

}
