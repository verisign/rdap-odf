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

import com.rdap.odf.statemachine.OdfStateMachine;
import com.rdap.odf.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ShellComponent
public class QueryCommands {
    private final Logger LOG = Logger.getLogger(getClass().getName());

    @Autowired
    private OdfStateMachine odfStateMachine;

    @ShellMethod("Send a domain lookup query to an RDAP service and show response.")
    public String domain(@ShellOption(help = HelpText.lookupTagDomainName, value = {"-n", "--name"}) String name,
                        @ShellOption(help = HelpText.queryType, value = {"-t", "--type"}, defaultValue = Constants.QUERY_TYPE_DEFAULT) String type,
                        @ShellOption(help = HelpText.queryOutput, value = {"-o", "--output"}, defaultValue = Constants.QUERY_OUTPUT_FILE_DEFAULT) String output,
                        @ShellOption(help = HelpText.queryPretty, value = {"-p", "--pretty"}) boolean pretty) {
        return odfStateMachine.cmdQuery(Utils.getLookupPathFromParam(Constants.QueryObject.DOMAIN, name), type, pretty, output);
    }

    @ShellMethod("Send a nameserver lookup query to an RDAP service and show response.")
    public String nameserver(@ShellOption(help = HelpText.lookupTagNsName, value = {"-n", "--name"}) String name,
                        @ShellOption(help = HelpText.queryType, value = {"-t", "--type"}, defaultValue = Constants.QUERY_TYPE_DEFAULT) String type,
                        @ShellOption(help = HelpText.queryOutput, value = {"-o", "--output"}, defaultValue = Constants.QUERY_OUTPUT_FILE_DEFAULT) String output,
                        @ShellOption(help = HelpText.queryPretty, value = {"-p", "--pretty"}) boolean pretty) {
        return odfStateMachine.cmdQuery(Utils.getLookupPathFromParam(Constants.QueryObject.NAMESERVER, name), type, pretty, output);
    }

    @ShellMethod("Send an entity lookup query to an RDAP service and show response.")
    public String entity(@ShellOption(help = HelpText.lookupTagEntityHandle, value = {"-h", "--handle"}) String handle,
                        @ShellOption(help = HelpText.queryType, value = {"-t", "--type"}, defaultValue = Constants.QUERY_TYPE_DEFAULT) String type,
                        @ShellOption(help = HelpText.queryOutput, value = {"-o", "--output"}, defaultValue = Constants.QUERY_OUTPUT_FILE_DEFAULT) String output,
                        @ShellOption(help = HelpText.queryPretty, value = {"-p", "--pretty"}) boolean pretty) {
        return odfStateMachine.cmdQuery(Utils.getLookupPathFromParam(Constants.QueryObject.ENTITY, handle), type, pretty, output);
    }

    @ShellMethod("Send a domain search query to an RDAP service and show response.")
    public String domains(@ShellOption(help = HelpText.searchTagDomainName, value = {"-n", "--name"}, defaultValue = Constants.QUERY_TAG_DEFAULT_VALUE) String name,
                        @ShellOption(help = HelpText.searchTagDomainNsName, value = {"-s", "--nsname"}, defaultValue = Constants.QUERY_TAG_DEFAULT_VALUE) String nsname,
                        @ShellOption(help = HelpText.searchTagDomainNsIp, value = {"-i", "--nsip"}, defaultValue = Constants.QUERY_TAG_DEFAULT_VALUE) String nsip,
                        @ShellOption(help = HelpText.queryType, value = {"-t", "--type"}, defaultValue = Constants.QUERY_TYPE_DEFAULT) String type,
                        @ShellOption(help = HelpText.queryOutput, value = {"-o", "--output"}, defaultValue = Constants.QUERY_OUTPUT_FILE_DEFAULT) String output,
                        @ShellOption(help = HelpText.queryPretty, value = {"-p", "--pretty"}) boolean pretty) {

        Map<Constants.QueryTag, String> params = new HashMap<>();
        params.put(Constants.QueryTag.DOMAIN_NAME, name);
        params.put(Constants.QueryTag.DOMAIN_NS_NAME, nsname);
        params.put(Constants.QueryTag.DOMAIN_NS_IP, nsip);
        AbstractMap.SimpleEntry<Constants.QueryTag, String> validParam = getValidQueryTagParam(params);
        if(validParam == null) {
            return Constants.UNSUPPORTED_QUERY_PARAMS;
        }
        return odfStateMachine.cmdQuery(
                Utils.getSearchPathFromParam(
                        Constants.QueryObject.DOMAIN_SEARCH,
                        validParam.getKey(),
                        validParam.getValue()),
                type,
                pretty,
                output);
    }

    @ShellMethod("Send a nameserver search query to an RDAP service and show response.")
    public String nameservers(@ShellOption(help = HelpText.searchTagNsName, value = {"-n", "--name"}, defaultValue = Constants.QUERY_TAG_DEFAULT_VALUE) String name,
                        @ShellOption(help = HelpText.searchTagNsIp, value = {"-i", "--ip"}, defaultValue = Constants.QUERY_TAG_DEFAULT_VALUE) String ip,
                        @ShellOption(help = HelpText.queryType, value = {"-t", "--type"}, defaultValue = Constants.QUERY_TYPE_DEFAULT) String type,
                        @ShellOption(help = HelpText.queryOutput, value = {"-o", "--output"}, defaultValue = Constants.QUERY_OUTPUT_FILE_DEFAULT) String output,
                        @ShellOption(help = HelpText.queryPretty, value = {"-p", "--pretty"}) boolean pretty) {

        Map<Constants.QueryTag, String> params = new HashMap<>();
        params.put(Constants.QueryTag.NS_NAME, name);
        params.put(Constants.QueryTag.NS_IP, ip);
        AbstractMap.SimpleEntry<Constants.QueryTag, String> validParam = getValidQueryTagParam(params);
        if(validParam == null) {
            return Constants.UNSUPPORTED_QUERY_PARAMS;
        }
        return odfStateMachine.cmdQuery(
                Utils.getSearchPathFromParam(
                        Constants.QueryObject.NAMESERVER_SEARCH,
                        validParam.getKey(),
                        validParam.getValue()),
                type,
                pretty,
                output);
    }

    @ShellMethod("Send an entity search query to an RDAP service and show response.")
    public String entities(@ShellOption(help = HelpText.searchTagEntityHandle, value = {"-h", "--handle"}, defaultValue = Constants.QUERY_TAG_DEFAULT_VALUE) String handle,
                        @ShellOption(help = HelpText.searchTagEntityFn, value = {"-n", "--fullname"}, defaultValue = Constants.QUERY_TAG_DEFAULT_VALUE) String fullname,
                        @ShellOption(help = HelpText.queryType, value = {"-t", "--type"}, defaultValue = Constants.QUERY_TYPE_DEFAULT) String type,
                        @ShellOption(help = HelpText.queryOutput, value = {"-o", "--output"}, defaultValue = Constants.QUERY_OUTPUT_FILE_DEFAULT) String output,
                        @ShellOption(help = HelpText.queryPretty, value = {"-p", "--pretty"}) boolean pretty) {

        Map<Constants.QueryTag, String> params = new HashMap<>();
        params.put(Constants.QueryTag.ENTITY_FN, fullname);
        params.put(Constants.QueryTag.ENTITY_HANDLE, handle);
        AbstractMap.SimpleEntry<Constants.QueryTag, String> validParam = getValidQueryTagParam(params);
        if(validParam == null) {
            return Constants.UNSUPPORTED_QUERY_PARAMS;
        }
        return odfStateMachine.cmdQuery(
                Utils.getSearchPathFromParam(
                        Constants.QueryObject.ENTITY_SEARCH,
                        validParam.getKey(),
                        validParam.getValue()),
                type,
                pretty,
                output);
    }

    /**
     * the query tag params need to be mutual exclusive, e.g., in entity search, the tag can only be either handle
     * or full name, but not both. This method validate if the tag params are mutual exclusive and return the specified
     * tag
     * @param params
     * @return the specified tag, value pair in
     */
    AbstractMap.SimpleEntry<Constants.QueryTag, String> getValidQueryTagParam(Map<Constants.QueryTag, String> params) {
        int validParamCount = 0;
        Constants.QueryTag tag = null;
        String tagValue = null;
        for (Map.Entry<Constants.QueryTag, String> entry: params.entrySet()) {
            if (!entry.getValue().equals(Constants.QUERY_TAG_DEFAULT_VALUE)) {
                validParamCount += 1;
                tag = entry.getKey();
                tagValue = entry.getValue();
            }
        }
        if(validParamCount != 1) {
            LOG.log(Level.INFO, "wrong tag parameters for query command: " + params);
            return null;
        }
        return new AbstractMap.SimpleEntry<>(tag, tagValue);
    }
}
