package org.bimserver.notifications;

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

import org.bimserver.database.BimserverDatabaseException;
import org.bimserver.endpoints.EndPoint;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;

public class NewExtendedDataOnRevisionTopic extends Topic {

	private long roid;

	public NewExtendedDataOnRevisionTopic(long roid) {
		this.roid = roid;
	}
	
	public void register(EndPoint endPoint) throws TopicRegisterException {
		// TODO check rights here too
		super.register(endPoint);
	}

	public void process(final NewExtendedDataOnRevisionNotification newExtendedDataOnRevisionNotification) throws BimserverDatabaseException, UserException, ServerException {
		map(new Mapper(){
			@Override
			public void map(EndPoint endPoint) throws UserException, ServerException, BimserverDatabaseException {
				endPoint.getNotificationInterface().newExtendedData(roid, newExtendedDataOnRevisionNotification.getEdid());
			}});
	}
}
