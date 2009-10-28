package gov.nih.nci.evs.browser.utils;

/**
  * <!-- LICENSE_TEXT_START -->
* Copyright 2008,2009 NGIT. This software was developed in conjunction with the National Cancer Institute,
* and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
* Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
* 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions
* in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other
* materials provided with the distribution.
* 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
* "This product includes software developed by NGIT and the National Cancer Institute."
* If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself,
* wherever such third-party acknowledgments normally appear.
* 3. The names "The National Cancer Institute", "NCI" and "NGIT" must not be used to endorse or promote products derived from this software.
* 4. This license does not authorize the incorporation of this software into any third party proprietary programs. This license does not authorize
* the recipient to use any trademarks owned by either NCI or NGIT
* 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
* NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */

/**
  * @author EVS Team
  * @version 1.0
  *
  * Modification history
  *     Initial implementation kim.ong@ngc.com
  *
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

import org.LexGrid.LexBIG.DataModel.Collections.MetadataPropertyList;
import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
import org.LexGrid.LexBIG.DataModel.Core.MetadataProperty;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;
import org.LexGrid.LexBIG.LexBIGService.LexBIGServiceMetadata;
import org.LexGrid.LexBIG.Utility.Constructors;


import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;

public class MetadataUtils {

	private static final String SOURCE_ABBREVIATION = "rsab";
	private static final String SOURCE_DESCRIPTION = "son";


	public Vector getMetadataForCodingSchemes() {
		LexBIGService lbs = RemoteServerUtil.createLexBIGService();
		LexBIGServiceMetadata lbsm = null;
		MetadataPropertyList mdpl = null;
		try {
			lbsm = lbs.getServiceMetadata();
			lbsm = lbsm.restrictToProperties(new String[]{SOURCE_ABBREVIATION});
			mdpl = lbsm.resolve();
		} catch (Exception e) {
			return null;
		}

		Vector v = getMetadataCodingSchemeNames(mdpl);

		try {
			lbsm = lbs.getServiceMetadata();
			lbsm = lbsm.restrictToProperties(new String[]{SOURCE_DESCRIPTION});
			mdpl = lbsm.resolve();

		} catch (Exception e) {
			return null;
		}

		Vector v2 = getMetadataCodingSchemeNames(mdpl);
		Vector w = new Vector();
		for(int i=0; i<v.size(); i++){
			String name = (String) v.get(i);
			String value = (String) v2.get(i);
			w.add(name + "|" + value);
		}
		w = SortUtils.quickSort(w);
		return w;
	}



	private static Vector getMetadataCodingSchemeNames(MetadataPropertyList mdpl){
        Vector v = new Vector();
		Iterator<MetadataProperty> metaItr = mdpl.iterateMetadataProperty();
		while(metaItr.hasNext()){
			MetadataProperty property = metaItr.next();
            v.add(property.getValue());
		}
		return v;
	}



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static Vector getSourceMetaData(String uri, String version) {
		//Map<String,String> map = test.getTtyExpandedForm("urn:oid:2.16.840.1.113883.3.26.1.2", "200904");
		Map<String,String> map = null;
		try {
			map = getTtyExpandedForm(uri, version);
			if (map == null) return null;
			Vector v = new Vector();
			for(String key : map.keySet()){
				String value = (String) map.get(key);
				v.add(key + "|" + value);
			}
			v = SortUtils.quickSort(v);
			return v;
	    } catch (Exception ex) {
			return null;
		}
	}

	private static Map<String,String> getTtyExpandedForm(String uri, String version) throws Exception {
		Map<String,String> ttys = new HashMap<String,String>();
		LexBIGService lbs = RemoteServerUtil.createLexBIGService();
		LexBIGServiceMetadata lbsm = null;
		MetadataPropertyList mdpl = null;
		try {
			lbsm = lbs.getServiceMetadata();
		    lbsm = lbsm.restrictToCodingScheme(Constructors.createAbsoluteCodingSchemeVersionReference(uri, version));

			mdpl = lbsm.resolve();
		} catch (Exception e) {
			return null;
		}

		for(int i=0;i<mdpl.getMetadataPropertyCount();i++){
			MetadataProperty prop = mdpl.getMetadataProperty(i);
			if(prop.getName().equals("dockey") &&
					prop.getValue().equals("TTY")){
				if(mdpl.getMetadataProperty(i + 2).getValue().equals("expanded_form")){
					ttys.put(mdpl.getMetadataProperty(i + 1).getValue(),
							mdpl.getMetadataProperty(i + 3).getValue());
				}
			}
		}
		return ttys;
	}


	/**
	 * Simple example to demonstrate extracting a specific Coding Scheme's Metadata.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		MetadataUtils test = new MetadataUtils();
		String serviceUrl = "http://ncias-d177-v.nci.nih.gov:19480/lexevsapi51";

		LexBIGService lbSvc = RemoteServerUtil.createLexBIGService(serviceUrl);

		if (lbSvc == null) {
			System.out.println("Unable to connect to " + serviceUrl);
			System.exit(1);
		} else {
			System.out.println("Connected to " + serviceUrl);
		}

        Vector v = test.getMetadataForCodingSchemes();
        for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			System.out.println(t);
		}

    }
}







