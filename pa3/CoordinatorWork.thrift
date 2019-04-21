service CoordinatorWork {
  void forwardReq(1: REQ r),
  void join(1: Node S)
}

struct REQ {
	1: string OP,
	2: string Filename,
	3: string Content,
	4: string ClientIP,
	5: string ClientPort
}

struct Node {
    // 1: i32 ID,
    1: string IP,
    2: string Port
}
