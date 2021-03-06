/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.phenotips.messaging.internal;

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.data.similarity.PatientSimilarityViewFactory;
import org.phenotips.messaging.Connection;
import org.phenotips.messaging.ConnectionManager;

import org.xwiki.component.annotation.Component;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Example;

import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;

/**
 * Default implementation for the {@code ConnectionManager} role, based on Hibernate for storage.
 * 
 * @version $Id$
 */
@Component
@Singleton
public class DefaultConnectionManager implements ConnectionManager
{
    /** Handles persistence. */
    @Inject
    private HibernateSessionFactory sessionFactory;

    /** Converts possibly restricted patient pairs into open patient pairs, to be able to read all their data. */
    @Inject
    private PatientSimilarityViewFactory publicPatientSimilarityViewFactory;

    @Override
    public Connection getConnection(PatientSimilarityView patientPair)
    {
        Session session = this.sessionFactory.getSessionFactory().openSession();
        Criteria c = session.createCriteria(Connection.class);
        Connection connection = new Connection(this.publicPatientSimilarityViewFactory.convert(patientPair));
        c.add(Example.create(connection).excludeProperty("id"));
        @SuppressWarnings("unchecked")
        List<Connection> foundEntries = c.list();
        if (foundEntries.isEmpty()) {
            Transaction t = session.beginTransaction();
            t.begin();
            session.save(connection);
            t.commit();
            return connection;
        }
        return foundEntries.get(0);
    }

    @Override
    public Connection getConnectionById(Long id)
    {
        Session session = this.sessionFactory.getSessionFactory().openSession();
        return (Connection) session.load(Connection.class, id);
    }
}
