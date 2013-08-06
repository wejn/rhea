# What's this
This WMS 3.X module allows you to restart application via HTTP.

# Installation
Add `wejn-rhea.jar` to Wowza's `lib/` directory.

Add following to appropriate HTTPProviders section of your VHost.xml:

```xml
<HTTPProvider>
	<BaseClass>cz.wejn.rhea.Provider</BaseClass>
	<RequestFilters>rhea*</RequestFilters>
	<AuthenticationMethod>rhea</AuthenticationMethod>
</HTTPProvider>
```

following to Authentication/Methods in your Authentication.xml:

```xml
<Method>
	<Name>rhea</Name>
	<Description>Rhea Authentication</Description>
	<Class>com.wowza.wms.authentication.AuthenticateBasic</Class>
	<!-- XXX: for digest auth use AuthenticateDigest above -->
	<Properties>
		<Property>
			<Name>passwordFile</Name>
			<Value>${com.wowza.wms.context.VHostConfigHome}/conf/rhea.password</Value>
		</Property>
		<Property>
			<Name>realm</Name>
			<Value>Rhea</Value>
		</Property>
	</Properties>
</Method>
```

and `username password` lines (containing your real username/password pairs)
to your `${com.wowza.wms.context.VHostConfigHome}/conf/rhea.password`.

Restart Wowza to finish installation.

# Configuration options, JMX interface
None.

# Example use
Let's say our wowza is running at `wms-dev.wejn.com` and has
multiple applications, one of which is `origin`.

Rhea is installed in the first `<HostPort>` block with port `1935`.

Everything else is set to default.

To terminate application named `origin` one would call:

`http://wms-dev.wejn.com:1935/rhea?app=origin`

which would yield response: `OK` when the application was terminated
successfully and `ERROR: <explanation>` in case of failure.

# License
Copyright (c) 2013 Michal "Wejn" Jirku <box@wejn.org>

This work is licensed under the Creative Commons Attribution 3.0 Czech Republic License. To view a copy of this license, visit [http://creativecommons.org/licenses/by/3.0/cz/](http://creativecommons.org/licenses/by/3.0/cz/).
