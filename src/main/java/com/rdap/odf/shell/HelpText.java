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

public class HelpText {
    public static final String show = "Information of the client. The available options are:" +
            "\n\t\t\033[1mstatus\033[0m: the current status of the client" +
            "\n\t\t\033[1mtokens\033[0m: user tokens and related information";

    public static final String Test = "Type of the query. The available options are: " +
            "\n\t\t\033[1mauth\033[0m: test queries with authentication information " +
            "\n\t\t\033[1munauth\033[0m: test queries without authentication information";

    public static final String queryType = "Type of the query. The available options are: " +
            "\n\t\t\033[1mauth\033[0m: query with authentication information for advanced access to the RDAP service " +
            "\n\t\t\033[1munauth\033[0m: query without authentication information for basic access to the RDAP service";

    public static final String queryPretty = "Json response in pretty format";

    public static final String queryUrl = "Path segment of the rdap rest endpoint, e.g. /domain/example.com";

    public static final String queryOutput = "The file where the rdap query response is stored. The result is appended to the file";

    // RDAP lookup parameters in RFC7482, section 3.1
    public static final String lookupTagDomainName = "Domain lookup by name, e.g., 123.com";
    public static final String lookupTagNsName = "Nameserver lookup by name, e.g., k4.nstld.com";
    public static final String lookupTagEntityHandle = "Entity lookup by handle, e.g., 50000101-vrsn";

    // RDAP search parameters in RFC7482, section 3.2.
    public static final String searchTagDomainName = "Domains search by name, e.g., 123*.com";
    public static final String searchTagDomainNsName = "Domains search by nameserver's name, e.g., k4.ns*.com";
    public static final String searchTagDomainNsIp = "Domains search by nameserver's IP, e.g., 209.112.114.*";
    public static final String searchTagNsName = "Nameservers search by name, e.g., k4.ns*.com";
    public static final String searchTagNsIp = "Nameservers search by IP, e.g., 209.112.114.*";
    public static final String searchTagEntityHandle = "Entities search by handles, e.g., 5000*-vrsn";
    public static final String searchTagEntityFn = "Entities search by full name, e.g., drop*-vrsn";

    public static final String auth = "perform actions related with authentication. The available options are: " +
            "\n\t\t\033[1mstart\033[0m: start the oauth device flow " +
            "\n\t\t\033[1mrefreshtoken\033[0m: refresh id token and access token if the client is in authenticated state " +
            "\n\t\t\033[1mdrop\033[0m: clear the user data and log out the client";

}
