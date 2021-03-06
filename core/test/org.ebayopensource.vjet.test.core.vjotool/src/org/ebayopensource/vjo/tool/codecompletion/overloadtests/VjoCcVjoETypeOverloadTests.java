/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjo.tool.codecompletion.overloadtests;
import static com.ebay.junitnexgen.category.Category.Groups.FAST;
import static com.ebay.junitnexgen.category.Category.Groups.P1;
import static com.ebay.junitnexgen.category.Category.Groups.UNIT;

import java.util.Arrays;
import java.util.List;

import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.vjo.tool.codecompletion.VjoCcBaseTest;
import org.ebayopensource.vjo.tool.codecompletion.jsresource.CodeCompletionUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ebay.junitnexgen.category.Category;
import com.ebay.junitnexgen.category.ModuleInfo;

@Category({P1,FAST,UNIT})
@ModuleInfo(value="DsfPrebuild",subModuleId="VJET")
public class VjoCcVjoETypeOverloadTests extends VjoCcBaseTest{
	
	private VjoCcOverloadUtil overloadUtil;
	
	@BeforeClass
	protected void setUp() throws Exception {
		overloadUtil = new VjoCcOverloadUtil();
	}
	@Test
	public void testBaseNonStatOverloadProposal(){
		// JS Type name
		String js = "engine.overload.EBase";
		
		// Function to test for overloading
		String funcName = "pubCompute";
		
		IJstType jstType = getJstType(CodeCompletionUtil.GROUP_NAME, js);
		
		// Position where proposal to be displayed
		int position = lastPositionInFile("ebase.", jstType);
		
		// list of expected argument list. include null in case of no argument expected
		String[] expectArgs = {null,"int i","int i, String s",
				"int i, String s, Date d"};
		
		VjoCcOverloadUtil.Proposals prop = overloadUtil.new Proposals(jstType,position,funcName);
		
		List<String> actualList = overloadUtil.getActMethParamList(prop);
		List<String> expectedList = Arrays.asList(expectArgs);
		
		overloadUtil.checkProposals(expectedList,actualList);
	}
	
	@Test
	public void testBaseStatOverloadProposal(){
		
		String js = "engine.overload.EBase";
		String funcName = "pubStaticCompute";
		IJstType jstType = getJstType(CodeCompletionUtil.GROUP_NAME, js);
		int position = lastPositionInFile("this.", jstType);
		
		// null to be included in case of no argument methods
		String[] expectArgs = {null,"int i","int i, String s",
				"int i, String s, Date d"};
		
		VjoCcOverloadUtil.Proposals prop = overloadUtil.new Proposals(jstType,position,funcName);
		
		List<String> actualList = overloadUtil.getActMethParamList(prop);
		List<String> expectedList = Arrays.asList(expectArgs);
		
		overloadUtil.checkProposals(expectedList,actualList);
	}
	
	@Test
	public void testChildNeedsCTypeOverloadProposal(){
		// JS Type name
		String js = "engine.overload.EChild";
		
		// Function to test for overloading
		String funcName = "pubCompute";
		
		IJstType jstType = getJstType(CodeCompletionUtil.GROUP_NAME, js);
		
		// Position where proposal to be displayed
		int position = lastPositionInFile("var pubVar = cbase.", jstType);
		
		// list of expected argument list. include null in case of no argument expected
		String[] expectArgs = {null,"int i","int i, String s",
				"int i, String s, Date d"};
		
		VjoCcOverloadUtil.Proposals prop = overloadUtil.new Proposals(jstType,position,funcName);
		
		List<String> actualList = overloadUtil.getActMethParamList(prop);
		List<String> expectedList = Arrays.asList(expectArgs);
		
		overloadUtil.checkProposals(expectedList,actualList);
	}
	
	@Test
	public void testChildNeedsETypeOverloadProposal(){
		// JS Type name
		String js = "engine.overload.EChild";
		
		// Function to test for overloading
		String funcName = "pubCompute";
		
		IJstType jstType = getJstType(CodeCompletionUtil.GROUP_NAME, js);
		
		// Position where proposal to be displayed
		int position = lastPositionInFile("ebase.", jstType);
		
		// list of expected argument list. include null in case of no argument expected
		String[] expectArgs = {null,"int i","int i, String s",
				"int i, String s, Date d"};
		
		VjoCcOverloadUtil.Proposals prop = overloadUtil.new Proposals(jstType,position,funcName);
		
		List<String> actualList = overloadUtil.getActMethParamList(prop);
		List<String> expectedList = Arrays.asList(expectArgs);
		
		overloadUtil.checkProposals(expectedList,actualList);
	}

}
