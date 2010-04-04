package org.eclipse.equinox.p2.tests.planner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.*;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.planner.IPlanner;
import org.eclipse.equinox.p2.planner.IProfileChangeRequest;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;

public class Bug306279b extends AbstractProvisioningTest {

	public void testGreedy() throws ProvisionException, OperationCanceledException, URISyntaxException {
		IProvisioningAgent agent = getAgentProvider().createAgent(getTestData("test data bug306279", "testData/bug306279/p2").toURI());

		IMetadataRepository repo1 = ((IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME)).loadRepository(new URI("http://download.eclipse.org/releases/helios"), null);
		assertFalse(repo1.query(QueryUtil.createIUQuery("org.eclipse.rap.jface.databinding"), new NullProgressMonitor()).isEmpty());

		URI rienaRepo = getTestData("repo for bug306279", "testData/bug306279/repo/riena").toURI();
		IMetadataRepository repo2 = ((IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME)).loadRepository(rienaRepo, null);

		IPlanner planner = getPlanner(agent);
		IProfile profile = ((IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME)).getProfile("SDKProfile");
		IProfileChangeRequest request = planner.createChangeRequest(profile);
		Set<IInstallableUnit> ius = repo2.query(QueryUtil.createIUQuery("org.eclipse.riena.toolbox.feature.feature.group", Version.create("2.0.0.201003181312")), new NullProgressMonitor()).toUnmodifiableSet();
		request.addAll(ius);
		ProvisioningContext ctx = new ProvisioningContext(getAgent());
		ctx.setMetadataRepositories(new URI[] {new URI("http://download.eclipse.org/releases/helios"), rienaRepo});
		IProvisioningPlan plan = planner.getProvisioningPlan(request, ctx, new NullProgressMonitor());

		assertOK("resolution failed", plan.getStatus());
		assertEquals(0, plan.getAdditions().query(QueryUtil.createIUQuery("org.eclipse.rap.jface.databinding"), new NullProgressMonitor()).toUnmodifiableSet().size());
		System.out.println(plan);
	}
}
