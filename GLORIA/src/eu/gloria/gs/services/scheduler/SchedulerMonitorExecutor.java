package eu.gloria.gs.services.scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.core.tasks.ServerThread;
import eu.gloria.gs.services.log.action.ActionLogException;
import eu.gloria.gs.services.log.action.ActionLogInterface;
import eu.gloria.gs.services.repository.image.data.ImageDatabaseException;
import eu.gloria.gs.services.repository.image.data.ImageInformation;
import eu.gloria.gs.services.repository.image.data.ImageRepositoryAdapter;
import eu.gloria.gs.services.repository.rt.RTRepositoryException;
import eu.gloria.gs.services.scheduler.brain.SchedulerBrain;
import eu.gloria.gs.services.scheduler.data.SchedulerAdapter;
import eu.gloria.gs.services.scheduler.data.SchedulerDatabaseException;
import eu.gloria.gs.services.teleoperation.ccd.CCDTeleoperationException;
import eu.gloria.gs.services.teleoperation.ccd.CCDTeleoperationInterface;
import eu.gloria.gs.services.teleoperation.ccd.ImageExtensionFormat;
import eu.gloria.gs.services.teleoperation.ccd.ImageNotAvailableException;

public class SchedulerMonitorExecutor extends ServerThread {

	private SchedulerAdapter adapter;
	private ActionLogInterface alog;
	private String username;
	private String password;
	private SchedulerBrain brain;
	private boolean thereArePending;
	private Map<Integer, Integer> recoverRetries = null;

	public void setAdapter(SchedulerAdapter adapter) {
		this.adapter = adapter;
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

	public void setBrain(SchedulerBrain brain) {
		this.brain = brain;
	}

	@Override
	protected void doWork() {

		GSClientProvider.setCredentials(this.username, this.password);

		try {
			this.brain.refreshPlans();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SchedulerDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RTRepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * if (recoverRetries == null) { recoverRetries = new HashMap<Integer,
		 * Integer>(); }
		 * 
		 * try { if (thereArePending) { Thread.sleep(100);
		 * System.out.println("Image daemon alive...images pending!"); } else {
		 * Thread.sleep(1000);
		 * System.out.println("Image daemon alive...no images pending!"); } }
		 * catch (InterruptedException e) { }
		 * 
		 * GSClientProvider.setCredentials(this.username, this.password);
		 * 
		 * List<ImageInformation> notUrlCompleted = null;
		 * 
		 * try { notUrlCompleted = this.adapter.getAllWithoutUrl(100);
		 * 
		 * if (notUrlCompleted.size() > 0) { try { alog.registerAction(username,
		 * new Date(), "images/fill?" + notUrlCompleted.size()); } catch
		 * (ActionLogException e) { System.out.println(e.getMessage()); } }
		 * 
		 * } catch (ImageDatabaseException e) { try {
		 * alog.registerAction(username, new Date(), e.getMessage()); } catch
		 * (ActionLogException ei) { System.out.println(ei.getMessage()); } }
		 * 
		 * thereArePending = false;
		 * 
		 * for (ImageInformation imageInfo : notUrlCompleted) {
		 * 
		 * String url = null;
		 * 
		 * try { url = ccd.getImageURL(imageInfo.getRt(), imageInfo.getCcd(),
		 * imageInfo.getLocalid(), ImageExtensionFormat.JPG);
		 * 
		 * adapter.setJpgByRT(imageInfo.getRt(), imageInfo.getLocalid(), url);
		 * 
		 * try { alog.registerAction( username, new Date(), "images/" +
		 * imageInfo.getId() + "/setJPG?" + url.substring(0, 15) + "..."); }
		 * catch (ActionLogException e) { System.out.println(e.getMessage()); }
		 * 
		 * url = ccd.getImageURL(imageInfo.getRt(), imageInfo.getCcd(),
		 * imageInfo.getLocalid(), ImageExtensionFormat.FITS);
		 * 
		 * adapter.setFitsByRT(imageInfo.getRt(), imageInfo.getLocalid(), url);
		 * 
		 * try { alog.registerAction( username, new Date(), "images/" +
		 * imageInfo.getId() + "/setFITS?" + url.substring(0, 15) + "..."); }
		 * catch (ActionLogException e) { System.out.println(e.getMessage()); }
		 * 
		 * } catch (ImageNotAvailableException e) { // Ignore the image this
		 * time, it will be treated by the // next // iteration of the executor
		 * 
		 * int gid = imageInfo.getId();
		 * 
		 * if (recoverRetries.containsKey(gid)) { recoverRetries.put(gid,
		 * recoverRetries.get(gid) + 1);
		 * 
		 * if (recoverRetries.get(gid) == 20) { try {
		 * adapter.removeImage(imageInfo.getId());
		 * 
		 * try { alog.registerAction(username, new Date(), "images/remove?" +
		 * imageInfo.getId()); } catch (ActionLogException el) {
		 * System.out.println(el.getMessage()); }
		 * 
		 * } catch (ImageDatabaseException e1) { e1.printStackTrace(); }
		 * 
		 * recoverRetries.remove(gid); } } else { recoverRetries.put(gid, 0);
		 * thereArePending = true; } } catch (CCDTeleoperationException e) {
		 * 
		 * try {
		 * 
		 * if (url == null) { url = ""; }
		 * 
		 * alog.registerAction( username, new Date(), "images/" +
		 * imageInfo.getId() + "/setURL?" + url.substring(0, 15) + "..." +
		 * "->CCD_TELEOPERATION_ERROR"); } catch (ActionLogException el) {
		 * System.out.println(el.getMessage()); }
		 * 
		 * try { adapter.removeImage(imageInfo.getId());
		 * 
		 * try { alog.registerAction(username, new Date(), "images/remove?" +
		 * imageInfo.getId()); } catch (ActionLogException el) {
		 * System.out.println(el.getMessage()); }
		 * 
		 * } catch (ImageDatabaseException e1) { e1.printStackTrace(); }
		 * 
		 * } catch (Exception e) { try { alog.registerAction(username, new
		 * Date(), "images/error->" + e.getMessage()); } catch
		 * (ActionLogException ei) { System.out.println(ei.getMessage()); }
		 * 
		 * try { adapter.removeImage(imageInfo.getId());
		 * 
		 * try { alog.registerAction(username, new Date(), "images/remove?" +
		 * imageInfo.getId()); } catch (ActionLogException el) {
		 * System.out.println(el.getMessage()); }
		 * 
		 * } catch (ImageDatabaseException e1) { e1.printStackTrace(); } } }
		 */
	}
}