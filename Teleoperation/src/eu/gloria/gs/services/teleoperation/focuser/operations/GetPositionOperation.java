package eu.gloria.gs.services.teleoperation.focuser.operations;

import eu.gloria.gs.services.teleoperation.base.OperationArgs;
import eu.gloria.gs.services.teleoperation.base.OperationReturn;
import eu.gloria.rti.client.RTSException;
import eu.gloria.rti.client.devices.Focuser;

public class GetPositionOperation extends FocuserOperation {

	public GetPositionOperation(OperationArgs args) throws Exception {
		super(args);
	}

	@Override
	protected void operateFocuser(Focuser focuser, OperationReturn returns)
			throws RTSException {

		long position = focuser.getPosition();

		returns.setMessage("Get focuser position operation executed: "
				+ position + ", " + this.getServer() + ","
				+ this.getFocuserName());
	}
}