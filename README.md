# RDAP-ODF

[Verisign Labs’](https://www.verisign.com/en_US/company-information/verisign-labs/) RDAP client POC based on - [OAuth 2.0 Device Flow for Browserless and Input Constrained Devices](https://tools.ietf.org/html/draft-ietf-oauth-device-flow-08)

## Introduction

This software demonstrates how the OAuth device flow can be used for Registration Data Access Protocol (RDAP) clients that have limited user interfaces, such as with a command line client. Google is used as the authorization provider for demonstration purposes.

## Before Using the Client

Before this client can query the RDAP Service, you must first register this client with Google following the instructions at https://developers.google.com/identity/protocols/OAuth2ForDevices.  After registration, you will need to update com.rdap.odf.google.clientId and com.rdap.odf.google.clientSecret properties at https://github.com/verisign/rdap-odf/blob/master/src/main/resources/application.properties with your Client ID and Client secret. 

## Build and Installation
### OS
We use RHEL7 as our development environment. If your operating system uses a package management system other than RPM you should disable the RPM build in pom.xml by removing the following section:

```
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>rpm-maven-plugin</artifactId>
  ....
</plugin>
```

so that only a jar file will be produced.

### Prerequisites

`java-1.8.0-openjdk` or higher version

`maven`

### Build

```
git clone https://github.com/verisign/rdap-odf.git
cd rdap-odf
```

```
mvn clean install -DskipTests
```

After the build is completed, target/rdap-odf-1.0.jar will be created and can be used directly. Alternatively, target/rpm/rdap-odf/RPMS/noarch/rdap-odf-1.0-1.noarch.rpm is also produced and can be installed via the package manager.

### Usage 

#### Run with jar

```
java -jar ./target/rdap-odf-1.0.jar
```
The current directory will be used as your working directory where the client data and logs are stored.

#### Install RPM
You can also choose to install the rpm, which is located at target/rpm/rdap-odf/RPMS/noarch.

Run 
```
sudo rpm -i rdap-odf-1.0-1.noarch.rpm
```

After installation, you will have /usr/local/bin/rdap-odf as the executable and /app/rdap-odf/ as the working directory for the client.

### Build and Run with Docker
Docker version 18.03.1 is used in our test.

```
docker build -t "rdap-odf-docker" .
```
```
docker run -it rdap-odf-docker
```

## Example Execution Flow

When the rdap-odf client is started for the first time it will be in an **Unauthenticated** mode. If the user has logged in before and the token data is still valid, then the client will start in an **Authenticated** mode.  A typical execution flow is shown below:
```
Unauthenticated> domain nic.cc
{"objectClassName":"domain", ...}

Unauthenticated> domain --type auth test.cc
Start oauth device flow. Getting user code...
Please visit https://www.google.com/device on your second device and authorize with code ...

Pending Authentication> client is authenticated
{"objectClassName":"domain", ...}

Authenticated> nameservers --ip 1.2.3.*
{"nameserverSearchResults":[{"objectClassName": ...}

Authenticated> domain --type unauth nic.cc
{"objectClassName":"domain", ...}

Authenticated> quit
```

From the above, you will notice that there are 2 types of queries: _authenticated_ and _unauthenticated_.  The default behavior is to submit queries with privilege corresponding to the client's current state; i.e. an unauthenticated query is sent in Unauthenticated mode.  If authenticated, the ODF client uses the id_token and access_token obtained from the OAuth device flow to obtain a more detailed RDAP response -- if the RDAP service supports access control using OpenID Connect.

## Commands
To view all of the commands supported by this client, type "help" in the console prompt:
```
Unauthenticated> help 
AVAILABLE COMMANDS

Built-In Commands
        clear: Clear the shell screen.
        help: Display help about available commands.
        script: Read and execute commands from a file.
        stacktrace: Display the full stacktrace of the last error.

Exit Cmd
        quit: Quit the client.

Main Commands
        auth: OAuth related functionalities, including start oauth device flow, refresh token, and drop authed state.
        show: Show client information such as status and tokens.
        test: Test batch RDAP queries in authenticated / unauthenticated mode.

Query Commands
        domain: Send a domain lookup query to an RDAP service and show response.
        domains: Send a domain search query to an RDAP service and show response.
        entities: Send an entity search query to an RDAP service and show response.
        entity: Send an entity lookup query to an RDAP service and show response.
        nameserver: Send a nameserver lookup query to an RDAP service and show response.
        nameservers: Send a nameserver search query to an RDAP service and show response.
```

Typing "help *command*" will provide an explanation of the options a command supports:
```
Unauthenticated> help nameservers

NAME
	nameservers - Send a nameserver search query to an RDAP service and show response.

SYNOPSYS
	nameservers [[-n] string]  [[-i] string]  [[-t] string]  [[-o] string]  [-p]  

OPTIONS
	-n or --name  string
		Nameservers search by name, e.g., k4.ns*.com
		[Optional, default = DEFAULT_QUERY_TAG_VALUE_NOT_USE]

	-i or --ip  string
		Nameservers search by IP, e.g., 209.112.114.*
		[Optional, default = DEFAULT_QUERY_TAG_VALUE_NOT_USE]

	-t or --type  string
		Type of the query. The available options are:
		auth: query with authentication information for advanced access to the RDAP service
		unauth: query without authentication information for basic access to the RDAP service
		[Optional, default = DEFAULT_TYPE]

	-o or --output  string
		The file where the rdap query response is stored. The result is appended to the file
		[Optional, default = DEFAULT_NOT_SAVE]

	-p or --pretty
		Json response in pretty format
		[Optional, default = false]
```

## Batch Execution
The *script* command allows support for batch-style execution of RDAP queries.
In this mode, you must specify a single query per line, where each query supports the same format as it does when
executed individually in the console.
