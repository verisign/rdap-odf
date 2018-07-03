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
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.commands.Quit;

import javax.annotation.PostConstruct;
import java.util.logging.Level;
import java.util.logging.Logger;


@ShellComponent
public class MainCommands {
    private final Logger LOG = Logger.getLogger(getClass().getName());

    @Autowired
    private OdfStateMachine odfStateMachine;

    @PostConstruct
    public void Init() {
        System.out.println(Banner.getBanner());
        odfStateMachine.run();
        LOG.log(Level.INFO, "state machine is started");
    }

    @ShellMethod(value = "Show client information such as status and tokens.")
    public String show(@ShellOption(help = HelpText.show, value = {"-o", "--object"}) String object) {
        if (object.equals("status")) {
            return odfStateMachine.getCurrentStateStr();
        } else if (object.equals("tokens")) {
            return odfStateMachine.getClientDataStr();
        } else {
            return Constants.UNSUPPORTED_COMMAND;
        }
    }

    @ShellMethod("OAuth related functionalities, including start oauth device flow, refresh token, and drop authed state.")
    public String auth(@ShellOption(help = HelpText.auth, value = {"-c", "--cmd"}) String cmd) {
        if (cmd.equals("start")) {
            return odfStateMachine.authStart();
        } else if (cmd.equals("refreshtoken")) {
            return odfStateMachine.authRefreshToken();
        } else if (cmd.equals("drop")) {
            return odfStateMachine.authDrop();
        } else {
            return Constants.UNSUPPORTED_COMMAND;
        }
    }

    @ShellMethod("Test batch RDAP queries in authenticated / unauthenticated mode.")
    public String test(@ShellOption(help = HelpText.Test, value = {"-t", "--type"}) String type) {
        // "type" can be "auth" or "unauth"
        return odfStateMachine.cmdTest(type);
    }

    /**
     * gracefully quit the shell
     */
    @ShellComponent
    public class ExitCmd implements Quit.Command {

        @ShellMethod("Quit the client.")
        public void quit() {
            odfStateMachine.stop();
            throw new ExitRequest();
        }
    }

    @ShellComponent
    public class CustomPromptProvider implements PromptProvider {
        public AttributedString getPrompt() {
            AttributedStringBuilder prompBuilder = new AttributedStringBuilder();
            prompBuilder.append("\033[33m");
            prompBuilder.append(odfStateMachine.getCurrentStateStr() + "> ", AttributedStyle.BOLD);
            prompBuilder.append("\033[0m");
            return prompBuilder.toAttributedString();
        }
    }
}
