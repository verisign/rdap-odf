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

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

import static org.junit.Assert.*;

public class ClientDataMgrTests {

    private final String clientDataFile = "testClientData.txt";
    private final String accessToken = "testaccesstoken";
    private final String idToken = "testidtoken";
    private final String refreshToken = "testrefreshtoken";
    private final String expiresIn = "testexpiresin";
    private final String tokenType = "testtokentype";

    private void genTmpClientDataFile(boolean partial) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(clientDataFile))) {
            if (!partial) {
                writer.write("access_token=" + accessToken + '\n');
                writer.write("id_token=" + idToken + '\n');
                writer.write("refresh_token=" + refreshToken + '\n');
                writer.write("expires_in=" + expiresIn + '\n');
                writer.write("token_type=" + tokenType + '\n');
                writer.write("timestamp=" + Instant.now().toString());
            } else {
                writer.write("access_token=" + accessToken + '\n');
                writer.write("refresh_token=" + refreshToken + '\n');
                writer.write("token_type=" + tokenType + '\n');
                writer.write("timestamp=" + Instant.now().toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadClientData() {
        genTmpClientDataFile(true);
        ClientDataMgr clientDataMgr = new ClientDataMgr();
        assertFalse(clientDataMgr.loadClientData(clientDataFile));

        genTmpClientDataFile(false);
        assertTrue(clientDataMgr.loadClientData(clientDataFile));

        assertTrue(clientDataMgr.isValid());

        assertEquals(clientDataMgr.getAccessToken(), accessToken);
        assertEquals(clientDataMgr.getIdToken(), idToken);
        assertEquals(clientDataMgr.getRefreshToken(), refreshToken);
        assertEquals(clientDataMgr.getExpiresIn(), expiresIn);
        assertEquals(clientDataMgr.getTokenType(), tokenType);

    }


}
