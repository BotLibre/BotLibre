/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
package org.botlibre.api.knowledge;

import java.util.Map;

/**
 * Allow iteration over all related vertices and all descendants.
 */

public interface VertexIterator {	
	/**
	 * Do not iterate on or over primitives.
	 */
	boolean getIgnorePrimitives();
		
	/**
	 * Maximum number of vertices to iterator over, -1 means no maximum.
	 */
	int getMaxIterations();

	/**
	 * Return the max depth of relationships to traverse, -1 means all.
	 */
	int getMaxDepth();

	/**
	 * Return if the iterator is at the max depth.
	 */
	boolean isMaxDepth();

	/**
	 * Return if the iterator has iterated the max iterations.
	 */
	boolean isMaxIterations();

	/**
	 * Increment the current depth of traversal.
	 */
	int incrementDepth();

	/**
	 * Increment the current depth of traversal.
	 */
	int decrementDepth();
	
	/**
	 * Return the current depth of traversal.
	 */
	int getDepth();

	/**
	 * Set the current depth of traversal.
	 */
	void setDepth(int depth);
	
	/**
	 * Traversal path.
	 */
	Path getPath();
	
	/**
	 * Iterate on the vertex, and return true if children should be traversed.
	 */
	boolean iterate(Vertex vertex);

	/**
	 * Return the set of already traversed vertices.
	 */
	Map<Vertex, Vertex> getTraversed();

	/**
	 * Return the set of vertices for the next breadth first traversal level.
	 */
	Map<Vertex, Vertex> getBreadthSet();

	/**
	 * Set of vertices for the next breadth first traversal level.
	 */
	void setBreadthSet(Map<Vertex, Vertex> breadthSet);
	
	/**
	 * Add the vertex to the breadth set for traversal.
	 */
	void addBreadth(Vertex vertex);
}