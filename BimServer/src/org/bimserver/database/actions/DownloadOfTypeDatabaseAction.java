package org.bimserver.database.actions;

import org.bimserver.SettingsManager;
import org.bimserver.database.BimDatabaseException;
import org.bimserver.database.BimDatabaseSession;
import org.bimserver.database.BimDeadlockException;
import org.bimserver.ifc.IfcModel;
import org.bimserver.ifc.IfcModelSet;
import org.bimserver.merging.Merger;
import org.bimserver.models.log.AccessMethod;
import org.bimserver.models.store.ConcreteRevision;
import org.bimserver.models.store.Project;
import org.bimserver.models.store.Revision;
import org.bimserver.models.store.User;
import org.bimserver.rights.RightsManager;
import org.bimserver.shared.UserException;

public class DownloadOfTypeDatabaseAction extends BimDatabaseAction<IfcModel> {

	private final String className;
	private final long actingUoid;
	private final long roid;
	private final SettingsManager settingsManager;

	public DownloadOfTypeDatabaseAction(BimDatabaseSession bimDatabaseSession, AccessMethod accessMethod, SettingsManager settingsManager, long roid, String className, long actingUoid) {
		super(bimDatabaseSession, accessMethod);
		this.settingsManager = settingsManager;
		this.roid = roid;
		this.actingUoid = actingUoid;
		this.className = className;
	}

	@Override
	public IfcModel execute() throws UserException, BimDeadlockException, BimDatabaseException {
		Revision virtualRevision = getVirtualRevision(roid);
		Project project = virtualRevision.getProject();
		User user = getUserByUoid(actingUoid);
		if (!RightsManager.hasRightsOnProjectOrSuperProjectsOrSubProjects(user, project)) {
			throw new UserException("User has insufficient rights to download revisions from this project");
		}
		IfcModelSet ifcModelSet = new IfcModelSet();
		for (ConcreteRevision concreteRevision : virtualRevision.getConcreteRevisions()) {
			IfcModel subModel = getDatabaseSession().getAllOfType(className, concreteRevision.getProject().getId(), concreteRevision.getId(), true);
			subModel.setDate(concreteRevision.getDate());
			ifcModelSet.add(subModel);
		}
		IfcModel IfcModel = new Merger().merge(project, ifcModelSet, settingsManager.getSettings().isIntelligentMerging());
		IfcModel.setRevisionNr(project.getRevisions().indexOf(virtualRevision) + 1);
		IfcModel.setAuthorizedUser(getUserByUoid(actingUoid).getName());
		IfcModel.setDate(virtualRevision.getDate());
		return IfcModel;
	}
}