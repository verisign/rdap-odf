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

package com.rdap.odf.shell;

import com.rdap.odf.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class QueryCommandsTests {

    @Test
    public void testGetValidQueryTagParam() {

        QueryCommands commands = new QueryCommands();
        AbstractMap.SimpleEntry<Constants.QueryTag, String> validParam;

        Map<Constants.QueryTag, String> params = new HashMap<>();
        params.put(Constants.QueryTag.DOMAIN_NAME, "test.com");
        params.put(Constants.QueryTag.DOMAIN_NS_NAME, Constants.QUERY_TAG_DEFAULT_VALUE);
        params.put(Constants.QueryTag.DOMAIN_NS_IP, Constants.QUERY_TAG_DEFAULT_VALUE);
        validParam = commands.getValidQueryTagParam(params);
        assertNotNull(validParam);
        assertEquals(validParam.getKey(), Constants.QueryTag.DOMAIN_NAME);
        assertEquals(validParam.getValue(), "test.com");

        params.clear();
        params.put(Constants.QueryTag.NS_NAME, Constants.QUERY_TAG_DEFAULT_VALUE);
        params.put(Constants.QueryTag.NS_IP, Constants.QUERY_TAG_DEFAULT_VALUE);
        validParam = commands.getValidQueryTagParam(params);
        assertNull(validParam);

        params.clear();
        params.put(Constants.QueryTag.NS_NAME, "G4.NSTLD.COM");
        params.put(Constants.QueryTag.NS_IP, "1.2.3.4");
        validParam = commands.getValidQueryTagParam(params);
        assertNull(validParam);
    }

}
