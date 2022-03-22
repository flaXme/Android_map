package com.example.server.Routerplaner;

import java.util.ArrayList;
import java.util.HashSet;

public class MinHeap {
	private int size;
	private int[] nodeIdAt;//indicate the nodeId at correspond index, equals -1 if node not in heap
	private int[] cost; // indicate the cost of the node/given index
	public int[] posInHeap;//indicate the heap position/index of a nodeid

	/**
	 * Constructor of heap with given size
	 * @param capacity : int number, the given size
	 */
	MinHeap(int capacity){
		this.size = 0;//current number of element in heap
		this.nodeIdAt = new int[capacity];//mapping: heap position -> nodeId at that position
		this.cost = new int[capacity];// mapping: heap position -> cost at that position
		this.posInHeap = new int[capacity];//mapping: nodeId -> position in heap


		for (int i = 0; i < capacity; i++) {
			nodeIdAt[i] = -1;//heap is empty
			cost[i] = Integer.MAX_VALUE;//initialize cost as max value
			posInHeap[i] = -1;// no element is in heap
		}
	}
	/**
	 * Add a unseen node in the heap 
	 * @param nodeId: int number, id of the node
	 * @param cost: double number, cost from start point to this node
	 */
	void add(int nodeId, int cost) {
		this.nodeIdAt[size] = nodeId;
		this.cost[size] = cost;
		this.posInHeap[nodeId] = size;
		heapifyUP(size);
		size++;
	}
	
	public int getPositionInHeap(int nodeId){
		return posInHeap[nodeId];
	}

	int[] remove() {
		int[] min = {nodeIdAt[0], cost[0]};
		
		if (size != 1) {// more than 1 element in heap
			swap(0, size - 1);
			this.posInHeap[nodeIdAt[size - 1]] = -1;
			this.nodeIdAt[size - 1] = -1;
			this.cost[size - 1] = Integer.MAX_VALUE;
			size--;
			heapifyDown(0);
		} else {//last element in heap is removed
			
			this.posInHeap[nodeIdAt[0]] = -1;
			this.nodeIdAt[0] = -1;
			this.cost[0] = Integer.MAX_VALUE;
			size--;
		}
		return min;
	}

	int[] peek(){
		return new int[] {nodeIdAt[0],cost[0]};
	}

	private void heapifyUP(int from) {
		int current = from;
		while(cost[current] < cost[parent(current)]) {
			swap(current, parent(current));
			current = parent(current);
		}
		
	}

	private int parent(int pos) {
		return ( pos - 1 ) / 2;
	}
	
	private void swap(int fpos, int spos) {
		if(fpos == -1 || spos == -1){
			System.out.println("not in heap");
		}
		int tmp0;
		int tmp1;
		// position or index of node
		posInHeap[nodeIdAt[fpos]] = spos;
		posInHeap[nodeIdAt[spos]] = fpos;
		// swap Id
		tmp1 = nodeIdAt[fpos];
		nodeIdAt[fpos] = nodeIdAt[spos];
		nodeIdAt[spos] = tmp1;
		// swap cost
		tmp0 = cost[fpos];
		cost[fpos] = cost[spos];
		cost[spos] = tmp0;
	}
	
	private boolean isLeaf(int pos) {
		if (pos >= (size / 2) + 1 && pos <= size) {
			return true;
		}
		return false;
	}

	private int rightChild(int pos) {
		return (2 * pos) + 2;
	}

	private int leftChild(int pos) {
		return (2 * pos) + 1;
	}

	private void heapifyDown(int pos) {
		if (!isLeaf(pos)) {
			if (cost[pos] > cost[leftChild(pos)] || cost[pos] > cost[rightChild(pos)]) {
				if (cost[leftChild(pos)] < cost[rightChild(pos)]) {
					swap(pos, leftChild(pos));
					heapifyDown(leftChild(pos));
				}
				else {
					swap(pos, rightChild(pos));
					heapifyDown(rightChild(pos));
				}
			}
		}
	}
	
	public void decreaseKey(int nodeId, int newcost) {
		int pos = this.posInHeap[nodeId];
		if(pos == -1){
			add(nodeId, newcost);
		}
		if(pos != -1){
			cost[pos] = newcost;
			heapifyUP(pos);
		}
	}
	
	int getSize() {
		return size;
	}

	public void reset(){
		for (int i = 0; i < size; i++) {
			int nodeId = nodeIdAt[i];
			nodeIdAt[i] = -1;
			cost[i] = Integer.MAX_VALUE;
			posInHeap[nodeId] = -1;
		}
		size = 0;
	}

}
