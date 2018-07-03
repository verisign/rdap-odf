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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StateAuthedTests {

    private ClientDataMgr mockClientDataMgr = mock(ClientDataMgr.class);
    private OdfStateMachine odfStateMachine = new OdfStateMachine();

    @Before
    public void setUp() {
        odfStateMachine.clientDataMgr = mockClientDataMgr;
    }

    private void resetClientDataMgr() {
        // reset to a valid client data manager
        when(mockClientDataMgr.loadClientData()).thenReturn(true);
        when(mockClientDataMgr.isValid()).thenReturn(true);
        when(mockClientDataMgr.isTokenExpired()).thenReturn(false);
        when(mockClientDataMgr.getSecondsForRefresh()).thenReturn((long) 1000);
    }

    @Test
    public void testEnter() {

        resetClientDataMgr();
        when(mockClientDataMgr.loadClientData()).thenReturn(false);
        odfStateMachine.stateTransit(OdfStateMachine.State.AUTHED);
        assertEquals(odfStateMachine.getCurrentStateStr(), "Unauthenticated");

        resetClientDataMgr();
        when(mockClientDataMgr.isValid()).thenReturn(false);
        odfStateMachine.stateTransit(OdfStateMachine.State.AUTHED);
        assertEquals(odfStateMachine.getCurrentStateStr(), "Unauthenticated");

        resetClientDataMgr();
        odfStateMachine.stateTransit(OdfStateMachine.State.AUTHED);
        assertEquals(odfStateMachine.getCurrentStateStr(), "Authenticated");
    }

}
