/*L
 *  Copyright SAIC
 *
 *  Distributed under the OSI-approved BSD 3-Clause License.
 *  See http://ncip.github.com/stats-analysis/LICENSE.txt for details.
 */

package gov.nih.nci.common.util;

import java.util.List;

public class HibernateQueryWrapper
{
	private String hql;
	private List parameters;
	public String getHql()
	{
		return hql;
	}
	public void setHql(String hql)
	{
		this.hql = hql;
	}
	public List getParameters()
	{
		return parameters;
	}
	public void setParameters(List parameters)
	{
		this.parameters = parameters;
	}
	public HibernateQueryWrapper(String hql, List parameters)
	{
		super();
		this.hql = hql;
		this.parameters = parameters;
	}
}