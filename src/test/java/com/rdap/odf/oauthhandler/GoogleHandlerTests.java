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

import com.rdap.odf.TestConfig;
import com.rdap.odf.data.ClientDataMgr;
import com.rdap.odf.utils.NetUtils;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class GoogleHandlerTests {

    private NetUtils mockNetUtils = mock(NetUtils.class);

    @Autowired
    private GoogleHandler googleHandler;

    @Test
    public void testStartOdf() {
        NetUtils netUtils = new NetUtils();
        googleHandler.setNetUtils(netUtils);
        JSONObject deviceResp = googleHandler.startOdf();

        assertNotNull(deviceResp);
        assertFalse(deviceResp.isEmpty());
        assertTrue(deviceResp.containsKey("verification_url"));
        assertTrue(deviceResp.containsKey("expires_in"));
        assertTrue(deviceResp.containsKey("interval"));
        assertTrue(deviceResp.containsKey("device_code"));
        assertTrue(deviceResp.containsKey("user_code"));
    }

    @Test
    public void testRefreshToken() {
        ClientDataMgr mockClientDataMgr = mock(ClientDataMgr.class);
        when(mockClientDataMgr.getRefreshToken()).thenReturn("testrefreshtoken");

        String goodResp = "{" +
                "\"access_token\" : \"test\" ," +
                "\"id_token\" : \"test\" ," +
                "\"token_type\" : \"test\" ," +
                "\"expires_in\" : \"test\" }";

        String badResp = "{" +
                "\"access_token\" : \"test\" ," +
                "\"id_token\" : \"test\" ," +
                "\"expires_in\" : \"test\" }";

        googleHandler.setNetUtils(mockNetUtils);

        when(mockNetUtils.post(any(), any())).thenReturn(goodResp);
        System.out.println(mockNetUtils);
        assertTrue(googleHandler.refreshToken(mockClientDataMgr));

        when(mockNetUtils.post(any(), any())).thenReturn(badResp);
        assertFalse(googleHandler.refreshToken(mockClientDataMgr));
    }


}
