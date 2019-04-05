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

service WorkWithSuperNode {
	string Join(1:string nodeIP, 2:string nodePort),
	void PostJoin(1:i32 id)
}
