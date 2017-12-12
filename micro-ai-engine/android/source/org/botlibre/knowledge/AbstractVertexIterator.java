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
package org.botlibre.knowledge;

import java.util.IdentityHashMap;
import java.util.Map;

import org.botlibre.api.knowledge.Path;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.knowledge.VertexIterator;

/**
 * Allow iteration over all related vertices and all descendants.
 */

public abstract class AbstractVertexIterator implements VertexIterator {
	protected boolean ignorePrimitives;
	protected int maxIterations;
	protected int depth;
	protected int maxDepth;
	protected Path path;
	protected Map<Vertex, Vertex> traversed;
	protected Map<Vertex, Vertex> breadthSet;
	
	public AbstractVertexIterator() {
		this.path = Path.DepthFirst;
		this.depth = 0;
		this.maxDepth = -1;
		this.ignorePrimitives = true;
		this.maxIterations = -1;
		this.traversed = new IdentityHashMap<Vertex, Vertex>();
	}
	
	/**
	 * Return if should iterate on or over primitives.
	 */
	public boolean getIgnorePrimitives() {
		return ignorePrimitives;
	}
		
	/**
	 * Maximum number of vertices to iterator over.
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * Return if the iterator is at the max depth.
	 */
	public boolean isMaxDepth() {
		return (this.maxDepth >= 0) && (this.depth >= this.maxDepth);
	}

	/**
	 * Return if the iterator has iterated the max iterations.
	 */
	public boolean isMaxIterations() {
		return (this.maxIterations >= 0) && (this.traversed.size() >= this.maxIterations);		
	}

	/**
	 * Increment the current depth of traversal.
	 */
	public int incrementDepth() {
		return this.depth++;
	}

	/**
	 * Decrement the current depth of traversal.
	 */
	public int decrementDepth() {
		return this.depth++;
	}

	/**
	 * Return the current depth of traversal.
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Return the max depth of relationships to traverse, -1 means all.
	 */
	public int getMaxDepth() {
		return maxDepth;
	}
	
	/**
	 * Set if should iterate on or over primitives.
	 */
	public void setIgnorePrimitives(boolean ignorePrimitives) {
		this.ignorePrimitives = ignorePrimitives;
	}
		
	/**
	 * Set the maximum number of vertices to iterator over.
	 */
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	/**
	 * Set the current depth of traversal.
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * Set the max depth of relationships to traverse, -1 means all.
	 */
	public void setMxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}
	
	/**
	 * Traversal path.
	 */
	public Path getPath() {
		return path;
	}
	
	/**
	 * Set the traversal path.
	 */
	public void getPath(Path path) {
		this.path = path;
	}

	public Map<Vertex, Vertex> getTraversed() {
		return traversed;
	}

	public void setTraversed(Map<Vertex, Vertex> traversed) {
		this.traversed = traversed;
	}

	public Map<Vertex, Vertex> getBreadthSet() {
		return breadthSet;
	}

	public void setBreadthSet(Map<Vertex, Vertex> breadthSet) {
		this.breadthSet = breadthSet;
	}
	
	/**
	 * Add the vertex to the breadth set for traversal.
	 */
	public void addBreadth(Vertex vertex) {
		this.breadthSet.put(vertex, vertex);
	}
}