/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.experiment.operations;

import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.experiment.base.data.NoSuchExperimentException;
import eu.gloria.gs.services.experiment.base.operations.ExperimentOperationException;
import eu.gloria.gs.services.experiment.base.parameters.ExperimentParameterException;
import eu.gloria.gs.services.experiment.base.reservation.ExperimentNotInstantiatedException;
import eu.gloria.gs.services.teleoperation.base.DeviceOperationFailedException;
import eu.gloria.gs.services.teleoperation.fw.FilterWheelTeleoperationException;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 * 
 */
public class SelectFilter extends ServiceOperation {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.gloria.gs.services.experiment.operations.ServiceOperation#execute()
	 */
	@Override
	public void execute() throws ExperimentOperationException {
		try {

			String rtNameParameter = (String) this.getArguments()[0];
			String rtName = (String) this.getContext().getExperimentContext()
					.getParameterValue(rtNameParameter);

			String fwNameParameter = (String) this.getArguments()[1];
			String fwName = (String) this.getContext().getExperimentContext()
					.getParameterValue(fwNameParameter);

			String filterParameter = (String) this.getArguments()[2];
			String filterName = (String) this.getContext()
					.getExperimentContext().getParameterValue(filterParameter);

			GSClientProvider.setCredentials(this.getUsername(),
					this.getPassword());

			this.getFilterWheelTeleoperation()
					.selectFilter(rtName, fwName, filterName);

		} catch (ExperimentParameterException | NoSuchExperimentException
				| ExperimentNotInstantiatedException
				| FilterWheelTeleoperationException
				| DeviceOperationFailedException e) {
			throw new ExperimentOperationException(e.getMessage());
		}
	}
}