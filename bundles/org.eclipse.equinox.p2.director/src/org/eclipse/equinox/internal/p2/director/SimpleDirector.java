/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.internal.p2.director;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.rollback.FormerState;
import org.eclipse.equinox.internal.provisional.p2.core.location.AgentLocation;
import org.eclipse.equinox.internal.provisional.p2.director.*;
import org.eclipse.equinox.internal.provisional.p2.engine.*;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.osgi.util.NLS;

public class SimpleDirector implements IDirector {
	private static final String ROLLBACK_LOCATION = "rollback"; //$NON-NLS-1$
	static final int PlanWork = 10;
	static final int EngineWork = 100;
	private IEngine engine;
	private IPlanner planner;

	public SimpleDirector() {
		initializeRollbackRepository();
		engine = (IEngine) ServiceHelper.getService(DirectorActivator.context, IEngine.SERVICE_NAME);
		if (engine == null)
			throw new IllegalStateException("Provisioning engine is not registered"); //$NON-NLS-1$
		planner = (IPlanner) ServiceHelper.getService(DirectorActivator.context, IPlanner.class.getName());
		if (planner == null)
			throw new IllegalStateException("Unable to find provisioning planner"); //$NON-NLS-1$
	}

	private void initializeRollbackRepository() {
		new FormerState(getRollbackRepositoryLocation());
	}

	public URL getRollbackRepositoryLocation() {
		AgentLocation agentLocation = (AgentLocation) ServiceHelper.getService(DirectorActivator.context, AgentLocation.class.getName());
		try {
			return new URL(agentLocation.getDataArea(DirectorActivator.PI_DIRECTOR), ROLLBACK_LOCATION);
		} catch (MalformedURLException e) {
			//we know this can't happen because the above URL is well-formed
			return null;
		}
	}

	public IStatus revert(IInstallableUnit target, IProfile profile, ProvisioningContext context, IProgressMonitor monitor) {
		SubMonitor sub = SubMonitor.convert(monitor, Messages.Director_Task_Updating, PlanWork + EngineWork);
		try {
			ProvisioningPlan plan = planner.getRevertPlan(target, context, sub.newChild(PlanWork));
			if (!plan.getStatus().isOK())
				return plan.getStatus();
			return engine.perform(profile, new DefaultPhaseSet(), plan.getOperands(), context, sub.newChild(EngineWork));
		} finally {
			sub.done();
		}
	}

	public IStatus provision(ProfileChangeRequest request, ProvisioningContext context, IProgressMonitor monitor) {
		String taskName = NLS.bind(Messages.Director_Task_Installing, request.getProfile().getProperty(IProfile.PROP_INSTALL_FOLDER));
		SubMonitor sub = SubMonitor.convert(monitor, taskName, PlanWork + EngineWork);
		try {
			IInstallableUnit[] installRoots = request.getAddedInstallableUnits();
			// mark the roots as such
			for (int i = 0; i < installRoots.length; i++) {
				request.setInstallableUnitProfileProperty(installRoots[i], IInstallableUnit.PROP_PROFILE_ROOT_IU, Boolean.toString(true));
			}
			ProvisioningPlan plan = planner.getProvisioningPlan(request, context, sub.newChild(PlanWork));
			if (!plan.getStatus().isOK())
				return plan.getStatus();

			IStatus engineResult = engine.perform(request.getProfile(), new DefaultPhaseSet(), plan.getOperands(), context, sub.newChild(EngineWork));
			return engineResult;
		} finally {
			sub.done();
		}
	}
}
