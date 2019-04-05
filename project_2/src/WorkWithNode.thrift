struct Node {
	1: i32 ID,
	2: string IP,
	3: string Port
}

struct FingerTable {
	1: i32 Start,
	2: i32 IntervalBegin,
	3: i32 IntervalEnd,
	4: Node Successor
}

service WorkWithNode {
	Node getSuccessor(),
	Node getPredecessor(),
	void setPredecessor(1: Node n),
	Node find_predecessor(1: i32 id),
	Node closet_preceding_finger(1: i32 id),
	Node find_successor(1:i32 id),
	void update_finger_table(1:Node s, 2:i32 i)
}
