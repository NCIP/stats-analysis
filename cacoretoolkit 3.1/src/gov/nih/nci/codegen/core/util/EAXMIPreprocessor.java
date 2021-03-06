/*L
 *  Copyright SAIC
 *
 *  Distributed under the OSI-approved BSD 3-Clause License.
 *  See http://ncip.github.com/stats-analysis/LICENSE.txt for details.
 */

package gov.nih.nci.codegen.core.util;

import gov.nih.nci.codegen.framework.XMIPreprocessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jaxen.JaxenException;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.apache.log4j.*;




/**
 * FixEAXMI provides methods to convert the XMI produced by <I>Enterprise Architect</I> to <I>NetBean MDR XMI Reader </I>compatible format
 * @author caBIO Team
 * @version 1.0
 */
public class EAXMIPreprocessor implements XMIPreprocessor {

	private static Logger log = Logger.getLogger(EAXMIPreprocessor.class);

	/**
	 * Modifies a copy of the XMI file named by xmiIn and writes this copy to the file named by xmiOut.
	 *
	 * @param xmiIn the EA XMI file
	 * @param xmiOut the fixed EA XMI file
	 * @throws JDOMException
	 * @throws JaxenException
	 * @throws IOException
	 */
	public void fix(String xmiIn, String xmiOut) throws JDOMException,
			JaxenException, IOException {

		Element root = (new SAXBuilder(false)).build(new File(xmiIn))
				.getRootElement();


		Element model = (Element) (new JDOMXPath(
				"*[local-name()='XMI.content']/*[local-name()='Model']"))
				.selectSingleNode(root);
		fixIDs(model);


		fixOperations(root);


		removeDiagramsElements(root);


		fixTaggedValues(root);


		fixSterotypes(root);


		removeMETaggedValues(root);


		removeMEStereotypes(root);


		removeDifference(root);


		collapseAttributes(root);


		Writer writer = new OutputStreamWriter(new FileOutputStream(new File(xmiOut)), "UTF-8");
		XMLOutputter xmlout = new XMLOutputter();
		xmlout.setFormat(Format.getPrettyFormat());
		writer.write(xmlout.outputString(root));
		writer.flush();
		writer.close();
	}

	/**
	 * @param root
	 * @throws JaxenException
	 */
	private void removeDifference(Element root) throws JaxenException {
		String exp = "//*[local-name()='XMI.difference']";
		Collection nodes = (new JDOMXPath(exp)).selectNodes(root);
		List toDetach = new ArrayList();
		for (Iterator i = nodes.iterator(); i.hasNext();) {
			Element n = (Element) i.next();
			toDetach.add(n);
		}
		detachElements(toDetach);
	}

	/**
	 * @param root
	 * @throws JaxenException
	 */
	private void removeMEStereotypes(Element root) throws JaxenException {
		String exp = "//*[local-name()='ModelElement.stereotype']";
		Collection nodes = (new JDOMXPath(exp)).selectNodes(root);
		List toDetach = new ArrayList();
		for (Iterator i = nodes.iterator(); i.hasNext();) {
			Element n = (Element) i.next();
			toDetach.add(n);
		}
		detachElements(toDetach);
	}

	/**
	 * @param root
	 * @throws JaxenException
	 */
	private void removeMETaggedValues(Element root) throws JaxenException {
		String exp = "//*[local-name()='ModelElement.taggedValue']";
		Collection mtvs = (new JDOMXPath(exp)).selectNodes(root);
		List toDetach = new ArrayList();
		for (Iterator i = mtvs.iterator(); i.hasNext();) {
			Element mtv = (Element) i.next();

			toDetach.add(mtv);
		}
		detachElements(toDetach);
	}

	/**
	 * @param toDetach
	 */
	private void detachElements(List toDetach) {
		for (Iterator i = toDetach.iterator(); i.hasNext();) {
			Element e = (Element) i.next();
			e.detach();
		}
	}

	/**
	 * @param root
	 * @throws JaxenException
	 */
	private void removeDiagramsElements(Element root) throws JaxenException {
		List toDetach = new ArrayList();
		String exp = "//*[local-name()='DiagramElement']";
		Collection nodes = (new JDOMXPath(exp)).selectNodes(root);
		for (Iterator i = nodes.iterator(); i.hasNext();) {
			Element n = (Element) i.next();
			toDetach.add(n);
		}
		for (Iterator i = toDetach.iterator(); i.hasNext();) {
			Element e = (Element) i.next();
			e.detach();
		}
	}

	/**
	 * @param root
	 * @throws JaxenException
	 */
	private void fixSterotypes(Element root) throws JaxenException {
		String exp = "//*[local-name()='ModelElement.stereotype']";
		Collection nodes = (new JDOMXPath(exp)).selectNodes(root);
		Element contentEl = root.getChild("XMI.content");
		HashMap sHash = new HashMap();

		for (Iterator i = contentEl.getChildren().iterator(); i.hasNext();) {
			Element sterEl = (Element) i.next();
			if (sterEl.getName().endsWith("Stereotype")) {

				Collection els = (new JDOMXPath(
						"*[local-name()='Stereotype.extendedElement']/*[local-name()='Foundation.Core.ModelElement']"))
						.selectNodes(sterEl);

				for (Iterator j = els.iterator(); j.hasNext();) {
					Element e = (Element) j.next();
					String v = sterEl.getAttributeValue("extendedElement");
					if (v == null) {
						v = "";
					}
					sterEl.setAttribute("extendedElement", v + " "
							+ e.getAttributeValue("xmi.idref") + " ");
				}
				Element child = (Element) (new JDOMXPath(
						"//*[local-name()='Stereotype.extendedElement']"))
						.selectSingleNode(sterEl);
				child.detach();
				sHash.put(sterEl.getAttributeValue("name"), sterEl);
			}
		}

		for (Iterator i = nodes.iterator(); i.hasNext();) {
			Element meSEl = (Element) i.next();
			Element meParentEl = meSEl.getParentElement();
			String parentId = meParentEl.getAttributeValue("xmi.id");
			if (parentId == null) {
				log.error("no xmi.id for "
						+ meParentEl.getName());
				throw new RuntimeException("no xmi.id for "
						+ meParentEl.getName());
			}
			int idSeq = 0;
			for (Iterator j = meSEl.getChildren().iterator(); j.hasNext(); idSeq++) {
				Element sEl = (Element) j.next();
				String sName = sEl.getAttributeValue("name");
				Element newSEl = (Element) sHash.get(sName);
				if (newSEl == null) {
					newSEl = new Element("Stereotype");
					contentEl.addContent(newSEl);
					newSEl.setNamespace(Namespace.getNamespace("UML",
							"href://org.omg/UML"));
					newSEl.setAttribute("xmi.id", parentId + "_fix_ster_"
							+ idSeq);
					newSEl.setAttribute("name", sName);
					newSEl.setAttribute("visibility", "public");
					newSEl.setAttribute("isSpecification", "false");
					newSEl.setAttribute("isRoot", "false");
					newSEl.setAttribute("isLeaf", "false");
					newSEl.setAttribute("isAbstract", "false");
					newSEl.setAttribute("baseClass", meParentEl.getName());
				}
				String ext = newSEl.getAttributeValue("extendedElement");
				if (ext == null) {
					ext = "";
				}
				if (ext.indexOf(" " + parentId + " ") == -1) {

					newSEl.setAttribute("extendedElement", ext + " " + parentId
							+ " ");
				}

			}
		}

	}

	/**
	 * @param root
	 * @throws JaxenException
	 */
	private void fixTaggedValues(Element root) throws JaxenException {
		String exp = "//*[local-name()='ModelElement.taggedValue']";
		Collection nodes = (new JDOMXPath(exp)).selectNodes(root);
		Element contentEl = root.getChild("XMI.content");
		for (Iterator i = nodes.iterator(); i.hasNext();) {
			Element meTVEl = (Element) i.next();
			Element meParentEl = meTVEl.getParentElement();
			String parentId = meParentEl.getAttributeValue("xmi.id");
			if (parentId == null) {
				log.error("no xmi.id for "
						+ meParentEl.getName());
				throw new RuntimeException("no xmi.id for "
						+ meParentEl.getName());
			}
			int idSeq = 0;
			for (Iterator j = meTVEl.getChildren().iterator(); j.hasNext(); idSeq++) {
				Element tvEl = (Element) j.next();
				Element newTVEl = new Element("TaggedValue");
				contentEl.addContent(newTVEl);
				newTVEl.setNamespace(Namespace.getNamespace("UML",
						"href://org.omg/UML"));
				newTVEl.setAttribute("xmi.id", parentId + "_fix_" + idSeq);
				String tagName = tvEl.getAttributeValue("tag");
				if(tagName == null){
					log.error("'tag' attribute is null");
				    throw new RuntimeException("'tag' attribute is null");
				}
				newTVEl.setAttribute("tag", tagName);
				newTVEl.setAttribute("modelElement", parentId);
				String tagValue = tvEl.getAttributeValue("value");
				if(tagValue == null){
				    log.debug("No value for TaggedValue:");
				    for(Iterator attIt = tvEl.getAttributes().iterator(); attIt.hasNext();){
				        Attribute anAtt = (Attribute)attIt.next();
				        System.err.println("\tAtt: " + anAtt);
				    }
				    log.error("'value' is null for " + tvEl);
				    throw new RuntimeException("'value' is null for " + tvEl);
				}
				newTVEl.setAttribute("value", tvEl.getAttributeValue("value"));
			}
		}
	}

	/**
	 * @param root
	 */
	private void fixIDs(Element parent) {
		int idSeq = 0;
		String parentId = parent.getAttributeValue("xmi.id");
		if (parentId == null) {
			log.error("xmi.id is null for " + parent.getName());
			throw new RuntimeException("xmi.id is null for " + parent.getName());
		}
		for (Iterator i = parent.getChildren().iterator(); i.hasNext(); idSeq++) {
			Element child = (Element) i.next();
			String childName = child.getName();
			if (!childName.endsWith(".taggedValue")
					&& !childName.endsWith(".stereotype")) {


				String childId = child.getAttributeValue("xmi.id");
				if (childId == null) {
					childId = parentId + "_fix_" + idSeq;
					child.setAttribute("xmi.id", childId);
				}

				fixIDs(child);
			}
		}
	}

	/**
	 * @param root
	 * @throws JaxenException
	 */
	private void fixOperations(Element root) throws JaxenException {
		String exp = "//*[local-name()='Operation']";
		Collection ops = (new JDOMXPath(exp)).selectNodes(root);

		for (Iterator i = ops.iterator(); i.hasNext();) {
			Element opEl = (Element) i.next();
			exp = "*[local-name()='ModelElement.taggedValue']/*[@tag='behaviour']";
			Collection specs = (new JDOMXPath(exp)).selectNodes(opEl);
			if (specs.size() > 1) {
				log.error("more than one spec for op "
						+ opEl.getAttributeValue("name"));
				throw new RuntimeException("more than one spec for op "
						+ opEl.getAttributeValue("name"));
			}
			if (specs.size() == 1) {
				Element oldSpecEl = (Element) specs.iterator().next();
				Element newSpecEl = new Element("Operation.specification");
				newSpecEl.setNamespace(Namespace.getNamespace("UML",
						"href://org.omg.UML/1.3"));
				opEl.addContent(newSpecEl);
				newSpecEl.addContent(oldSpecEl.getAttributeValue("value"));
			}
		}
	}

	/**
	 * @param root
	 * @throws JaxenException
	 */
	private void collapseAttributes(Element el) throws JaxenException {
		List toRemove = new ArrayList();
		for (Iterator i = el.getAttributes().iterator(); i.hasNext();) {
			Attribute att = (Attribute) i.next();
			String exp = "*[ends-with(local-name(), '" + att.getName() + "')]";
			Collection nodes = (new JDOMXPath(exp)).selectNodes(el);
			if (nodes.size() > 0) {
				toRemove.add(att);
			}
		}
		for (Iterator i = toRemove.iterator(); i.hasNext();) {
			el.removeAttribute((Attribute) i.next());
		}
		for (Iterator i = el.getChildren().iterator(); i.hasNext();) {
			collapseAttributes((Element) i.next());
		}
	}


}
