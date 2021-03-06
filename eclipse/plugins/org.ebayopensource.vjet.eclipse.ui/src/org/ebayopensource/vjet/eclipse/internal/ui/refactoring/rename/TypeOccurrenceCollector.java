/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.internal.ui.refactoring.rename;

import org.ebayopensource.vjet.eclipse.internal.ui.refactoring.core.SourceModuleCollectingSearchRequestor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.mod.core.ISourceModule;
import org.eclipse.dltk.mod.core.IType;
import org.eclipse.dltk.mod.core.search.SearchMatch;

public class TypeOccurrenceCollector extends
		SourceModuleCollectingSearchRequestor {

	final String fOldName;
	final String fOldQualifiedName;

	public TypeOccurrenceCollector(IType type) {
		fOldName = type.getElementName();
		fOldQualifiedName = type.getFullyQualifiedName(".");
	}

	public void acceptSearchMatch(ISourceModule unit, SearchMatch match)
			throws CoreException {
		collectMatch(acceptSearchMatch2(unit, match));
	}

	public SearchMatch acceptSearchMatch2(ISourceModule unit, SearchMatch match)
			throws CoreException {
		int start = match.getOffset();
		int length = match.getLength();

		// unqualified:
		String matchText = unit.getBuffer().getText(start, length);
		if (fOldName.equals(matchText)) {
			return match;
		}

		// (partially) qualified:
		if (fOldQualifiedName.endsWith(matchText)) {
			// e.g. rename B and p.A.B ends with match A.B
			int simpleNameLenght = fOldName.length();
			match.setOffset(start + length - simpleNameLenght);
			match.setLength(simpleNameLenght);
			return match;
		}

		// Not a standard reference -- use scanner to find last identifier
		// token:

		// TODO why need a scanner in java refactoring

		// IScanner scanner= getScanner(unit);
		// scanner.setSource(matchText.toCharArray());
		// int simpleNameStart= -1;
		// int simpleNameEnd= -1;
		// try {
		// int token = scanner.getNextToken();
		// while (token != ITerminalSymbols.TokenNameEOF) {
		// if (token == ITerminalSymbols.TokenNameIdentifier) {
		// simpleNameStart= scanner.getCurrentTokenStartPosition();
		// simpleNameEnd= scanner.getCurrentTokenEndPosition();
		// }
		// token = scanner.getNextToken();
		// }
		// } catch (InvalidInputException e){
		// //ignore
		// }
		// if (simpleNameStart != -1) {
		// match.setOffset(start + simpleNameStart);
		// match.setLength(simpleNameEnd + 1 - simpleNameStart);
		// }
		return match;
	}
}