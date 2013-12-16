/**
 * 
 * Copyright 2013 Paul Scherrer Institute. All rights reserved.
 * 
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This code is distributed in the hope that it will be useful, but without any
 * warranty; without even the implied warranty of merchantability or fitness for
 * a particular purpose. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package ch.psi.streamer;

import java.util.LinkedList;
import java.util.List;

import ch.psi.streamer.model.StreamAccounting;

public class Accountant {
	
	private final int size = 10;
	private final LinkedList<StreamAccounting> queue = new LinkedList<>();
	
	public void add(StreamAccounting info){
		queue.addFirst(info);
		if(queue.size()>size){
			queue.removeLast(); // remove head
		}
	}
	
	public List<StreamAccounting> getInformation(){
		return (List<StreamAccounting>) queue;
	}

}
