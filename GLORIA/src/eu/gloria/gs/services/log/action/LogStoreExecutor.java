package eu.gloria.gs.services.log.action;

import java.io.IOException;
import java.util.List;

import eu.gloria.gs.services.core.ErrorLogEntry;
import eu.gloria.gs.services.core.InfoLogEntry;
import eu.gloria.gs.services.core.LogEntry;
import eu.gloria.gs.services.core.LogStore;
import eu.gloria.gs.services.core.WarningLogEntry;
import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.core.tasks.ServerThread;
import eu.gloria.gs.services.log.action.ActionLogInterface;
import eu.gloria.gs.services.log.action.data.JSONConverter;
import eu.gloria.gs.services.utils.ObjectResponse;

public class LogStoreExecutor extends ServerThread {

	private LogStore logStore;
	private ActionLogInterface alog;
	private String username;
	private String password;

	public void setLogStore(LogStore logStore) {
		this.logStore = logStore;
	}

	public void setActionLog(ActionLogInterface alog) {
		this.alog = alog;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private void registerAction(LogEntry entry) throws ActionLogException {

		Object actionObj = entry.getAction();
		if (!(actionObj instanceof String)) {
			try {
				actionObj = JSONConverter.toJSON(actionObj);
			} catch (IOException e) {
			}
		}

		ObjectResponse action = new ObjectResponse(actionObj);

		if (entry instanceof InfoLogEntry) {
			if (entry.getRid() != null) {
				alog.registerContextInfo(entry.getUsername(), entry.getDate(),
						entry.getRid(), action);
			} else if (entry.getRt() != null) {
				alog.registerRtInfo(entry.getUsername(), entry.getDate(), entry.getRt(), action);				
			} else {
				alog.registerInfo(entry.getUsername(), entry.getDate(), action);
			}
		} else if (entry instanceof ErrorLogEntry) {
			if (entry.getRid() != null) {
				alog.registerContextError(entry.getUsername(), entry.getDate(),
						entry.getRid(), action);
			} else if (entry.getRt() != null) {
				alog.registerRtError(entry.getUsername(), entry.getDate(), entry.getRt(), action);				
			} else {
				alog.registerError(entry.getUsername(), entry.getDate(), action);
			}
		} else if (entry instanceof WarningLogEntry) {
			if (entry.getRid() != null) {
				alog.registerContextWarning(entry.getUsername(),
						entry.getDate(), entry.getRid(), action);
			} else if (entry.getRt() != null) {
				alog.registerRtWarning(entry.getUsername(), entry.getDate(), entry.getRt(), action);				
			} else {
				alog.registerWarning(entry.getUsername(), entry.getDate(), action);
			}
		}

	}

	@Override
	protected void doWork() {

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		GSClientProvider.setCredentials(this.username, this.password);

		List<LogEntry> entries = logStore.getEntries();

		for (int i = 0; i < entries.size(); i++) {
			LogEntry entry = logStore.getEntry(i);

			if (entry == null) {
				break;
			}

			try {
				this.registerAction(entry);
				logStore.removeEntry(entry);

			} catch (ActionLogException e) {
			}
		}
	}
}