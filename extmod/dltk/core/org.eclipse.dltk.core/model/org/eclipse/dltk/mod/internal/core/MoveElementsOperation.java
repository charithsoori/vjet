/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.mod.internal.core;

import org.eclipse.dltk.mod.core.IModelElement;
import org.eclipse.dltk.mod.internal.core.util.Messages;

/**
 * This operation moves elements from their current container to a specified
 * destination container, optionally renaming the elements. A move operation is
 * equivalent to a copy operation, where the source elements are deleted after
 * the copy.
 * <p>
 * This operation can be used for reorganizing elements within the same
 * container.
 * 
 * @see CopyElementsOperation
 */
public class MoveElementsOperation extends CopyElementsOperation {
	/**
	 * When executed, this operation will move the given elements to the given
	 * containers.
	 */
	public MoveElementsOperation(IModelElement[] elementsToMove, IModelElement[] destContainers, boolean force) {
		super(elementsToMove, destContainers, force);
	}

	/**
	 * Returns the <code>String</code> to use as the main task name for
	 * progress monitoring.
	 */
	protected String getMainTaskName() {
		return Messages.operation_moveElementProgress;
	}

	/**
	 * @see CopyElementsOperation#isMove()
	 */
	protected boolean isMove() {
		return true;
	}
}
