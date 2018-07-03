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

public class Constants {

    public static final String UNSUPPORTED_COMMAND = "Unsupported command.";
    public static final String UNSUPPORTED_QUERY_PARAMS = "Unsupported query params. Check if query tags are missing" +
            " or obscure. Note that one and only one tag should be provided for search queries";

    public static final String QUERY_TYPE_DEFAULT = "DEFAULT_TYPE";
    public static final String QUERY_OUTPUT_FILE_DEFAULT = "DEFAULT_NOT_SAVE";
    public static final String QUERY_TAG_DEFAULT_VALUE = "DEFAULT_QUERY_TAG_VALUE_NOT_USE";

    public enum QueryObject {
        DOMAIN,                 // domain lookup
        NAMESERVER,             // nameserver lookup
        ENTITY,                 // entity lookup
        DOMAIN_SEARCH,          // domain search
        NAMESERVER_SEARCH,      // nameserver search
        ENTITY_SEARCH,          // entity search
        OBJECT_NOT_SUPPORTED    // object not supported
    }

    public enum QueryTag {
        DOMAIN_NAME,        // search domain by name
        DOMAIN_NS_NAME,     // search domain by nameserver name
        DOMAIN_NS_IP,       // search domain by nameserver ip
        NS_NAME,            // search nameserver by name
        NS_IP,              // search nameserver by ip
        ENTITY_FN,          // search entity by full name
        ENTITY_HANDLE,      // search entity by handle
        TAG_NOT_SUPPORTED   // tag not supported
    }
}
