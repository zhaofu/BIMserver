package org.bimserver.unittests;

/******************************************************************************
 * Copyright (C) 2009-2013  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.bimserver.BimServer;
import org.bimserver.LocalDevBimServerStarter;
import org.bimserver.client.BimServerClient;
import org.bimserver.client.BimServerClientFactory;
import org.bimserver.client.ChannelConnectionException;
import org.bimserver.client.ClientIfcModel;
import org.bimserver.client.protocolbuffers.ProtocolBuffersBimServerClientFactory;
import org.bimserver.ifc.step.serializer.IfcStepSerializer;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.interfaces.objects.SRevisionSummary;
import org.bimserver.interfaces.objects.SRevisionSummaryContainer;
import org.bimserver.interfaces.objects.SRevisionSummaryType;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.serializers.SerializerException;
import org.bimserver.plugins.services.BimServerClientException;
import org.bimserver.shared.PublicInterfaceNotFoundException;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.ServiceException;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.shared.pb.ProtocolBuffersMetaData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestClientEmfModelLocal {
	private BimServerClient bimServerClient;
	private static BimServer bimServer;

	@BeforeClass
	public static void setup() {
		try {
			File home = new File("home");
			if (home.isDirectory()) {
				FileUtils.deleteDirectory(home);
			}

			LocalDevBimServerStarter localDevBimWebServerStarter = new LocalDevBimServerStarter();
			localDevBimWebServerStarter.start("localhost", 8082);
			bimServer = localDevBimWebServerStarter.getBimServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void shutdown() {
		// bimServer.stop();
	}

	private SProject createProject() throws PublicInterfaceNotFoundException {
		try {
			SProject project = bimServerClient.getBimsie1ServiceInterface().addProject("Project " + new Random().nextInt());
			return project;
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Test
	public void test() {
		ProtocolBuffersMetaData protocolBuffersMetaData = new ProtocolBuffersMetaData();
		protocolBuffersMetaData.load(bimServer.getServicesMap(), ProtocolBuffersBimServerClientFactory.class);
		BimServerClientFactory factory = new ProtocolBuffersBimServerClientFactory("localhost", 8020, 8080, protocolBuffersMetaData);
		UsernamePasswordAuthenticationInfo usernamePasswordAuthenticationInfo = new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin");
		try {
			bimServerClient = factory.create(usernamePasswordAuthenticationInfo);
		} catch (ChannelConnectionException e1) {
			e1.printStackTrace();
		} catch (ServerException e) {
			e.printStackTrace();
		} catch (UserException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		try {
			ClientIfcModel model = bimServerClient.newModel(createProject());
			SProject project = createProject();
			CreateFromScratch createFromScratch = new CreateFromScratch();
			createFromScratch.createIfcProject(model);
			long roid = model.commit("tralala");

			dumpToFile(project.getOid(), roid);
			
			createFromScratch.createWall(model);
			roid = model.commit("test");
			
			dumpToFile(project.getOid(), roid);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void dumpToFile(long poid, long roid) throws SerializerException {
		try {
			ClientIfcModel model = bimServerClient.getModel(poid, roid, false);
			IfcStepSerializer serializer = new IfcStepSerializer(new PluginConfiguration());
			serializer.init(model, null, bimServer.getPluginManager(), null, false);
			File output = new File("output");
			if (!output.exists()) {
				output.mkdir();
			}
			serializer.writeToFile(new File(output, roid + ".ifc"));
		} catch (BimServerClientException e) {
			e.printStackTrace();
		} catch (UserException e) {
			e.printStackTrace();
		} catch (ServerException e) {
			e.printStackTrace();
		} catch (PublicInterfaceNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void dumpSummary(long roid) throws ServiceException, PublicInterfaceNotFoundException {
		SRevisionSummary revisionSummary = bimServerClient.getServiceInterface().getRevisionSummary(roid);
		List<SRevisionSummaryContainer> list = revisionSummary.getList();
		for (SRevisionSummaryContainer container : list) {
			System.out.println(container.getName());
			for (SRevisionSummaryType type : container.getTypes()) {
				System.out.println(type.getName() + ": " + type.getCount());
			}
		}
	}
}
