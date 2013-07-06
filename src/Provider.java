package cz.wejn.rhea;

import java.util.*;
import java.io.*;
import com.wowza.wms.application.*;
import com.wowza.wms.http.*;
import com.wowza.wms.logging.*;
import com.wowza.wms.stream.*;
import com.wowza.wms.vhost.*;
import edu.emory.mathcs.backport.java.util.concurrent.locks.*;
import com.wowza.wms.httpstreamer.model.*;
import com.wowza.wms.rtp.model.*;
import com.wowza.wms.client.*;
import com.wowza.wms.mediacaster.*;

public class Provider extends HTTProvider2Base
{
	@Override
	public void onHTTPRequest(IVHost vhost, IHTTPRequest req, IHTTPResponse resp) {
		if (!doHTTPAuthentication(vhost, req, resp))
			return;

		Map<String, List<String>> params = req.getParameterMap();

		String response = null;

		try {
			if (! req.getMethod().equalsIgnoreCase("get") &&
					!req.getMethod().equalsIgnoreCase("post"))
				throw new Exception("unsupported HTTP method");

			if (! params.containsKey("app"))
				throw new Exception("need 'app' parameter.");

			String param_app = params.get("app").get(0);

			if (! vhost.isApplicationLoaded(param_app))
				throw new Exception("app not loaded.");

			IApplication app = vhost.getApplication(param_app);
			if (app == null)
				throw new Exception("couldn't find application: " + param_app);

			WMSReadWriteLock appLock = vhost.getApplicationLock();
			WMSReadWriteLock cLock = null;
			appLock.writeLock().lock();
			try {
				for (String sai: app.getAppInstanceNames()) {
					IApplicationInstance ai = app.getAppInstance(sai);
					cLock = ai.getClientsLockObj();
					cLock.writeLock().lock();
					for (IClient c : ai.getClients())
						c.setShutdownClient(true);

					for (IHTTPStreamerSession hss : ai.getHTTPStreamerSessions())
						hss.rejectSession();

					for (RTPSession rs : ai.getRTPSessions())
						rs.rejectSession();

					MediaCasterStreamMap mcsm = ai.getMediaCasterStreams();
					if (mcsm != null)
						mcsm.shutdown(true);

					app.removeAppInstance(app.getAppInstance(sai));

					cLock.writeLock().unlock();
					cLock = null;
				}
				app.shutdown(false);
			} catch (Exception e) {
				throw new Exception("couldn't kill application '" +
					param_app + "': " + e.getMessage());
			} finally {
				appLock.writeLock().unlock();
				if (cLock != null)
					cLock.writeLock().unlock();
			}

			response = "OK\n";
		} catch (Exception e) {
			response = "ERROR: " + e.getMessage() + "\n";
		}

		try {
			OutputStream out = resp.getOutputStream();
			resp.setHeader("Content-Type", "text/plain");
			byte[] outBytes = response.getBytes();
			out.write(outBytes);
		} catch(Exception e) {
			WMSLoggerFactory.getLogger(null).error("Rhea#Provider: " +
				e.toString());
		}

	}
}
