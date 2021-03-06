/*L
 *  Copyright SAIC
 *
 *  Distributed under the OSI-approved BSD 3-Clause License.
 *  See http://ncip.github.com/stats-analysis/LICENSE.txt for details.
 */

import gov.nih.nci.cagrid.caintegrator.common.RembrandtGridServiceI;
import gov.nih.nci.cagrid.caintegrator.stubs.RembrandtGridServicePortType;
import gov.nih.nci.cagrid.caintegrator.stubs.service.RembrandtGridServiceAddressingLocator;
import gov.nih.nci.cagrid.introduce.security.client.ServiceSecurityClient;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI.MalformedURIException;
import org.globus.gsi.GlobusCredential;

/**
 * This class is autogenerated, DO NOT EDIT GENERATED GRID SERVICE ACCESS METHODS.
 *
 * This client is generated automatically by Introduce to provide a clean unwrapped API to the
 * service.
 *
 * On construction the class instance will contact the remote service and retrieve it's security
 * metadata description which it will use to configure the Stub specifically for each method call.
 * 
 * @created by Introduce Toolkit version 1.1
 */
public class RembrandtGridServiceClient extends ServiceSecurityClient implements RembrandtGridServiceI {	
	protected RembrandtGridServicePortType portType;
	private Object portTypeMutex;

	public RembrandtGridServiceClient(String url) throws MalformedURIException, RemoteException {
		this(url,null);	
	}

	public RembrandtGridServiceClient(String url, GlobusCredential proxy) throws MalformedURIException, RemoteException {
	   	super(url,proxy);
	   	initialize();
	}
	
	public RembrandtGridServiceClient(EndpointReferenceType epr) throws MalformedURIException, RemoteException {
	   	this(epr,null);
	}
	
	public RembrandtGridServiceClient(EndpointReferenceType epr, GlobusCredential proxy) throws MalformedURIException, RemoteException {
	   	super(epr,proxy);
		initialize();
	}
	
	private void initialize() throws RemoteException {
	    this.portTypeMutex = new Object();
		this.portType = createPortType();
	}

	private RembrandtGridServicePortType createPortType() throws RemoteException {

		RembrandtGridServiceAddressingLocator locator = new RembrandtGridServiceAddressingLocator();
		// attempt to load our context sensitive wsdd file
		InputStream resourceAsStream = getClass().getResourceAsStream("client-config.wsdd");
		if (resourceAsStream != null) {
			// we found it, so tell axis to configure an engine to use it
			EngineConfiguration engineConfig = new FileProvider(resourceAsStream);
			// set the engine of the locator
			locator.setEngine(new AxisClient(engineConfig));
		}
		RembrandtGridServicePortType port = null;
		try {
			port = locator.getRembrandtGridServicePortTypePort(getEndpointReference());
		} catch (Exception e) {
			throw new RemoteException("Unable to locate portType:" + e.getMessage(), e);
		}

		return port;
	}
	

	public static void usage(){
		System.out.println(RembrandtGridServiceClient.class.getName() + " -url <service url>");
	}
	
	public static void main(String [] args){
	    System.out.println("Running the Rembrandt Grid Service Client");
		try{
		if(!(args.length < 2)){
			if(args[0].equals("-url")){
			  RembrandtGridServiceClient client = new RembrandtGridServiceClient(args[1]);
			  client.run();
			} else {
				usage();
				System.exit(1);
			}
		} else {
			usage();
			System.exit(1);
		}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

  public gov.nih.nci.cagrid.cqlresultset.CQLQueryResults query(gov.nih.nci.cagrid.cqlquery.CQLQuery cqlQuery) throws RemoteException, gov.nih.nci.cagrid.data.faults.QueryProcessingExceptionType, gov.nih.nci.cagrid.data.faults.MalformedQueryExceptionType {
	    synchronized(portTypeMutex){
	      configureStubSecurity((Stub)portType,"query");
	    gov.nih.nci.cagrid.data.QueryRequest params = new gov.nih.nci.cagrid.data.QueryRequest();
	    gov.nih.nci.cagrid.data.QueryRequestCqlQuery cqlQueryContainer = new gov.nih.nci.cagrid.data.QueryRequestCqlQuery();
	    cqlQueryContainer.setCQLQuery(cqlQuery);
	    params.setCqlQuery(cqlQueryContainer);
	    gov.nih.nci.cagrid.data.QueryResponse boxedResult = portType.query(params);
	    return boxedResult.getCQLQueryResultCollection();
	    }
	  }
  public org.oasis.wsrf.properties.GetMultipleResourcePropertiesResponse getMultipleResourceProperties(org.oasis.wsrf.properties.GetMultipleResourceProperties_Element params) throws RemoteException {
    synchronized(portTypeMutex){
      configureStubSecurity((Stub)portType,"getMultipleResourceProperties");
    return portType.getMultipleResourceProperties(params);
    }
  }

  public org.oasis.wsrf.properties.GetResourcePropertyResponse getResourceProperty(javax.xml.namespace.QName params) throws RemoteException {
    synchronized(portTypeMutex){
      configureStubSecurity((Stub)portType,"getResourceProperty");
    return portType.getResourceProperty(params);
    }
  }

  public org.oasis.wsrf.properties.QueryResourcePropertiesResponse queryResourceProperties(org.oasis.wsrf.properties.QueryResourceProperties_Element params) throws RemoteException {
    synchronized(portTypeMutex){
      configureStubSecurity((Stub)portType,"queryResourceProperties");
    return portType.queryResourceProperties(params);
    }
  }
  public void testAllMethods(Object object) {
  	try {
			Class domainObject = object.getClass();
			Method[] allMethods = domainObject.getMethods();
			for(int i=0; i<allMethods.length;i++) {
		    	try {
		    		String methodString = allMethods[i].getName();			    		
		    		if (methodString.substring(0,3).equals("get")) {
		    			Class returntype = allMethods[i].getReturnType();
		    			if (methodString.equals("getBigid")) {
		    				System.out.println("=====result of big id ===== for class" + object.getClass().getName());
		    			}
		    			if(returntype.getName().substring(0,10).equals("java.lang.")){
		    				System.out.println("testing method:" + methodString);
		    				System.out.println("[result of method: " + methodString + "] " + allMethods[i].invoke(object));
		    			}
		    			
		    		}
		    	}catch(Exception e) {
		    		e.printStackTrace();
		    		continue;
				}
			}
		}catch(Exception e) {
  		e.printStackTrace();
		}
  }
  public void run() throws Exception{
		Properties testcases;

  	System.out.println("Test XML");
  	
  	testcases = new Properties();
  	testcases.load(new FileInputStream("conf/testcases"));
  	Iterator testnumbers = testcases.keySet().iterator();
  	while (testnumbers.hasNext()){
			long startTime = System.currentTimeMillis(),endTime =0;
  		try {
  			String className = null;
  			String id = null;
  			String testname = (String)testnumbers.next();
  			if(testname.indexOf("|")> 0){
  					className = testname.substring(0,testname.indexOf("|"));
	    				id = testname.substring(testname.indexOf("|")+1);
  			}else{
  				className = testname;
  			}
  			
  			  System.out.println("-----------------------------------" + className + "------------------------------------");
			  gov.nih.nci.cagrid.cqlquery.CQLQuery query = new gov.nih.nci.cagrid.cqlquery.CQLQuery();
              gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
              target.setName(className);
              gov.nih.nci.cagrid.cqlquery.Attribute att = new gov.nih.nci.cagrid.cqlquery.Attribute();
              att.setName("id");
              att.setValue(id);
              att.setPredicate(gov.nih.nci.cagrid.cqlquery.Predicate.EQUAL_TO);
              target.setAttribute(att);
              query.setTarget(target);
              query.setTarget(target);
              gov.nih.nci.cagrid.cqlresultset.CQLQueryResults results = query(query);

              java.util.Iterator iter = new gov.nih.nci.cagrid.data.utilities.CQLQueryResultsIterator(results,
            		  RembrandtGridServiceClient.class.getResourceAsStream("client-config.wsdd"));
              if (iter.hasNext()) {
                  Object o = iter.next();
                  testAllMethods(o);
              }
              System.out.println("Query Complete.");	    			
              endTime = System.currentTimeMillis();
      		System.out.println("time = "+(endTime-startTime));
  		}catch(Exception e) {
  			e.printStackTrace();
  			endTime = System.currentTimeMillis();
      		System.out.println("time = "+(endTime-startTime));
  			continue;
  		}
  		
      }
  }
}
