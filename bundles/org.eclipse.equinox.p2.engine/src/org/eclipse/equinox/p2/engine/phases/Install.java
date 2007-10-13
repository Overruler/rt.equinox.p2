/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.engine.phases;

import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.engine.EngineActivator;
import org.eclipse.equinox.p2.core.eventbus.ProvisioningEventBus;
import org.eclipse.equinox.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.p2.engine.*;
import org.eclipse.equinox.p2.metadata.*;

public class Install extends Phase {

	final static class BeforeInstallEventAction extends ProvisioningAction {
		public IStatus execute(Map parameters) {
			Profile profile = (Profile) parameters.get("profile");
			String phaseId = (String) parameters.get("phaseId");
			Touchpoint touchpoint = (Touchpoint) parameters.get("touchpoint");
			Operand operand = (Operand) parameters.get("operand");
			((ProvisioningEventBus) ServiceHelper.getService(EngineActivator.getContext(), ProvisioningEventBus.class.getName())).publishEvent(new InstallableUnitEvent(phaseId, true, profile, operand, InstallableUnitEvent.INSTALL, touchpoint));
			return null;
		}

		public IStatus undo(Map parameters) {
			Profile profile = (Profile) parameters.get("profile");
			String phaseId = (String) parameters.get("phaseId");
			Touchpoint touchpoint = (Touchpoint) parameters.get("touchpoint");
			Operand operand = (Operand) parameters.get("operand");
			((ProvisioningEventBus) ServiceHelper.getService(EngineActivator.getContext(), ProvisioningEventBus.class.getName())).publishEvent(new InstallableUnitEvent(phaseId, false, profile, operand, InstallableUnitEvent.UNINSTALL, touchpoint));
			return null;
		}
	}

	final static class AfterInstallEventAction extends ProvisioningAction {
		public IStatus execute(Map parameters) {
			Profile profile = (Profile) parameters.get("profile");
			String phaseId = (String) parameters.get("phaseId");
			Touchpoint touchpoint = (Touchpoint) parameters.get("touchpoint");
			Operand operand = (Operand) parameters.get("operand");
			((ProvisioningEventBus) ServiceHelper.getService(EngineActivator.getContext(), ProvisioningEventBus.class.getName())).publishEvent(new InstallableUnitEvent(phaseId, false, profile, operand, InstallableUnitEvent.INSTALL, touchpoint));
			return null;
		}

		public IStatus undo(Map parameters) {
			Profile profile = (Profile) parameters.get("profile");
			String phaseId = (String) parameters.get("phaseId");
			Touchpoint touchpoint = (Touchpoint) parameters.get("touchpoint");
			Operand operand = (Operand) parameters.get("operand");
			((ProvisioningEventBus) ServiceHelper.getService(EngineActivator.getContext(), ProvisioningEventBus.class.getName())).publishEvent(new InstallableUnitEvent(phaseId, true, profile, operand, InstallableUnitEvent.UNINSTALL, touchpoint));
			return null;
		}
	}

	private static final String PHASE_ID = "install"; //$NON-NLS-1$

	public Install(int weight) {
		super(PHASE_ID, weight, Messages.Engine_Install_Phase);
	}

	//	protected IStatus performOperand(EngineSession session, Profile profile, Operand operand, IProgressMonitor monitor) {
	//		IInstallableUnit unit = operand.second();
	//
	//		monitor.subTask(NLS.bind(Messages.Engine_Installing_IU, unit.getId()));
	//
	//		Touchpoint touchpoint = TouchpointManager.getInstance().getTouchpoint(unit.getTouchpointType());
	//		if (!touchpoint.supports(PHASE_ID))
	//			return Status.OK_STATUS;
	//
	//		((ProvisioningEventBus) ServiceHelper.getService(EngineActivator.getContext(), ProvisioningEventBus.class.getName())).publishEvent(new InstallableUnitEvent(PHASE_ID, true, profile, operand, InstallableUnitEvent.INSTALL, touchpoint));
	//
	//		ProvisioningAction[] actions = getActions();
	//		MultiStatus result = new MultiStatus();
	//		for (int i = 0; i < actions.length; i++) {
	//			IStatus actionStatus = (IStatus) actions[i].execute();
	//			result.add(actionStatus);
	//			if (!actionStatus.isOK())
	//				return result;
	//
	//			session.record(actions[i]);
	//		}
	//		((ProvisioningEventBus) ServiceHelper.getService(EngineActivator.getContext(), ProvisioningEventBus.class.getName())).publishEvent(new InstallableUnitEvent(PHASE_ID, false, profile, operand, InstallableUnitEvent.INSTALL, touchpoint, result));
	//		return result;
	//	}

	protected boolean isApplicable(Operand op) {
		if (op.second() != null)
			return true;
		return false;
	}

	protected ProvisioningAction[] getActions(Touchpoint touchpoint, Operand currentOperand) {
		//TODO: monitor.subTask(NLS.bind(Messages.Engine_Installing_IU, unit.getId()));

		IInstallableUnit unit = currentOperand.second();
		if (unit.isFragment())
			return new ProvisioningAction[0];
		TouchpointData[] data = unit.getTouchpointData();

		if (data == null)
			return new ProvisioningAction[0];
		String[] instructions = getInstructionsFor("configurationData", data);
		if (instructions.length == 0)
			return new ProvisioningAction[0];
		InstructionParser parser = new InstructionParser(this, touchpoint);
		ProvisioningAction[] parsedActions = parser.parseActions(instructions[0]);
		ProvisioningAction[] actions = new ProvisioningAction[parsedActions.length + 2];
		actions[0] = new BeforeInstallEventAction();
		System.arraycopy(parsedActions, 0, actions, 1, parsedActions.length);
		actions[actions.length - 1] = new AfterInstallEventAction();
		return actions;
	}

	// We could put this in a utility class or perhaps refactor touchpoint data
	static private String[] getInstructionsFor(String key, TouchpointData[] data) {
		String[] matches = new String[data.length];
		int count = 0;
		for (int i = 0; i < data.length; i++) {
			matches[count] = data[i].getInstructions(key);
			if (matches[count] != null)
				count++;
		}
		if (count == data.length)
			return matches;
		String[] result = new String[count];
		System.arraycopy(matches, 0, result, 0, count);
		return result;
	}

	protected IStatus initializeOperand(Operand operand, Map parameters) {
		IResolvedInstallableUnit iu = operand.second();
		parameters.put("iu", iu);

		IArtifactKey[] artifacts = iu.getArtifacts();
		if (artifacts != null && artifacts.length > 0)
			parameters.put("artifactId", artifacts[0].getId());

		return Status.OK_STATUS;
	}
}